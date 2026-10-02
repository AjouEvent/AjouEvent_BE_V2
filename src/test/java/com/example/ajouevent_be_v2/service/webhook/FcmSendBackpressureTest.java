package com.example.ajouevent_be_v2.service.webhook;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.example.ajouevent_be_v2.common.discord.DiscordMessageService;
import com.example.ajouevent_be_v2.common.trace.MdcTaskDecorator;
import com.example.ajouevent_be_v2.config.FcmConfig;
import com.example.ajouevent_be_v2.config.properties.FcmExecutorProperties;
import com.example.ajouevent_be_v2.dto.push.PushClusterSendRequest;
import com.example.ajouevent_be_v2.orchestrator.FcmOrchestrator;
import com.example.ajouevent_be_v2.service.notification.NotificationPushService;
import com.example.ajouevent_be_v2.service.push.PushClusterQueryService;
import com.example.ajouevent_be_v2.service.webhook.FcmPushService.PreparedBatch;
import com.google.api.core.ApiFutureCallback;
import com.google.auth.oauth2.AccessToken;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.ThreadManager;
import com.google.firebase.messaging.BatchResponse;
import com.google.firebase.messaging.Message;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

/**
 * FCM 발송 백프레셔(세마포어)와 실행기 구성 검증.
 * FCM 서버에는 가짜 액세스 토큰으로 요청하므로 실제 발송은 일어나지 않고, 응답(실패 포함)이 콜백으로 전달되는지만 사용한다.
 */
class FcmSendBackpressureTest {

    private final ApplicationContextRunner runner = new ApplicationContextRunner()
        .withUserConfiguration(PropertiesConfig.class, FcmConfig.class, MdcTaskDecorator.class, FcmPushService.class)
        .withBean(SimpleMeterRegistry.class)
        .withPropertyValues(
            "ajou.fcm.executor.max-in-flight-batches=3",
            "ajou.fcm.executor.callback.core-pool-size=4",
            "ajou.fcm.executor.callback.max-pool-size=4",
            "ajou.fcm.executor.callback.queue-capacity=100",
            "ajou.fcm.executor.default-pool.core-pool-size=200",
            "ajou.fcm.executor.default-pool.max-pool-size=200",
            "ajou.fcm.executor.default-pool.queue-capacity=2000",
            "ajou.fcm.executor.dispatch.core-pool-size=2",
            "ajou.fcm.executor.dispatch.max-pool-size=2",
            "ajou.fcm.executor.dispatch.queue-capacity=100");

    @EnableConfigurationProperties(FcmExecutorProperties.class)
    static class PropertiesConfig {
    }

    @AfterEach
    void tearDown() {
        FirebaseApp.getApps().forEach(FirebaseApp::delete);
    }

    @Test
    void configuresExecutorsAndInFlightGauge() {
        runner.run(ctx -> {
            assertThat(ctx).hasNotFailed();
            ThreadPoolTaskExecutor defaultExecutor = ctx.getBean("fcmDefaultExecutor", ThreadPoolTaskExecutor.class);
            assertThat(defaultExecutor.getCorePoolSize()).isEqualTo(200);
            assertThat(defaultExecutor.getMaxPoolSize()).isEqualTo(200);
            assertThat(defaultExecutor.getThreadPoolExecutor().allowsCoreThreadTimeOut()).isTrue();
            assertThat(ctx.getBean("fcmDispatchExecutor", ThreadPoolTaskExecutor.class).getMaxPoolSize()).isEqualTo(2);
            assertThat(ctx.getBean(SimpleMeterRegistry.class).find("fcm.batches.in_flight").gauge()).isNotNull();
        });
    }

    @Test
    void failsStartupWhenPoolCapacityCannotHoldInFlightBatches() {
        runner.withPropertyValues("ajou.fcm.executor.default-pool.queue-capacity=100").run(ctx -> {
            assertThat(ctx).hasFailed();
            assertThat(ctx.getStartupFailure()).rootCause().hasMessageContaining("fcm-default 용량");
        });
        runner.withPropertyValues(
            "ajou.fcm.executor.callback.core-pool-size=1",
            "ajou.fcm.executor.callback.max-pool-size=1",
            "ajou.fcm.executor.callback.queue-capacity=1").run(ctx -> {
            assertThat(ctx).hasFailed();
            assertThat(ctx.getStartupFailure()).rootCause().hasMessageContaining("fcm-callback 용량");
        });
    }

    @Test
    void waitsForPermitUntilPreviousBatchCallbackCompletes() throws Exception {
        initFirebase(Executors.newFixedThreadPool(8));
        runner.withPropertyValues("ajou.fcm.executor.max-in-flight-batches=1").run(ctx -> {
            FcmPushService service = ctx.getBean(FcmPushService.class);
            CountDownLatch firstCallbackEntered = new CountDownLatch(1);
            CountDownLatch releaseFirstCallback = new CountDownLatch(1);
            AtomicInteger preparedCount = new AtomicInteger();

            service.sendBatchAsync(() -> {
                preparedCount.incrementAndGet();
                return new PreparedBatch(List.of(message()), callback(() -> {
                    firstCallbackEntered.countDown();
                    awaitQuietly(releaseFirstCallback);
                }));
            });
            assertThat(firstCallbackEntered.await(30, TimeUnit.SECONDS)).isTrue();
            assertThat(inFlight(ctx.getBean(SimpleMeterRegistry.class))).isEqualTo(1.0);

            CountDownLatch secondSubmitted = new CountDownLatch(1);
            Thread second = new Thread(() -> {
                try {
                    service.sendBatchAsync(() -> {
                        preparedCount.incrementAndGet();
                        return new PreparedBatch(List.of(message()), callback(() -> { }));
                    });
                    secondSubmitted.countDown();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
            second.start();

            // 첫 배치 콜백이 끝나지 않았으므로 두 번째 배치는 허가를 기다리고, 선점(prepare)도 실행되지 않는다
            assertThat(secondSubmitted.await(500, TimeUnit.MILLISECONDS)).isFalse();
            assertThat(preparedCount.get()).isEqualTo(1);

            releaseFirstCallback.countDown();
            assertThat(secondSubmitted.await(30, TimeUnit.SECONDS)).isTrue();
            assertThat(preparedCount.get()).isEqualTo(2);
            second.join();
        });
    }

    @Test
    void returnsFalseWithoutPreparingWhenPermitWaitTimesOut() throws Exception {
        initFirebase(Executors.newFixedThreadPool(2));
        runner.withPropertyValues(
            "ajou.fcm.executor.max-in-flight-batches=1",
            "ajou.fcm.executor.acquire-timeout-seconds=1").run(ctx -> {
            FcmPushService service = ctx.getBean(FcmPushService.class);
            CountDownLatch holdFirstCallback = new CountDownLatch(1);
            service.sendBatchAsync(() -> new PreparedBatch(List.of(message()), callback(() -> awaitQuietly(holdFirstCallback))));

            AtomicInteger preparedCount = new AtomicInteger();
            boolean acquired = service.sendBatchAsync(() -> {
                preparedCount.incrementAndGet();
                return null;
            });

            assertThat(acquired).isFalse();
            assertThat(preparedCount.get()).isZero();
            holdFirstCallback.countDown();
        });
    }

    @Test
    void releasesPermitWhenNothingIsClaimed() throws Exception {
        initFirebase(Executors.newFixedThreadPool(2));
        runner.withPropertyValues("ajou.fcm.executor.max-in-flight-batches=1").run(ctx -> {
            FcmPushService service = ctx.getBean(FcmPushService.class);

            assertThat(service.sendBatchAsync(() -> null)).isTrue();
            assertThat(service.sendBatchAsync(() -> null)).isTrue();
            assertThat(inFlight(ctx.getBean(SimpleMeterRegistry.class))).isZero();
        });
    }

    @Test
    void callsOnFailureAndReleasesPermitWhenSendExecutorRejects() throws Exception {
        // 스레드 1개 + 큐 적재 불가 → 3건 배치의 두 번째 메시지 제출에서 거절된다
        ThreadPoolExecutor rejecting = new ThreadPoolExecutor(1, 1, 0, TimeUnit.SECONDS, new LinkedBlockingQueue<>() {
            @Override
            public boolean offer(Runnable runnable) {
                return false;
            }
        });
        initFirebase(rejecting);
        runner.withPropertyValues("ajou.fcm.executor.max-in-flight-batches=1").run(ctx -> {
            FcmPushService service = ctx.getBean(FcmPushService.class);
            AtomicInteger failures = new AtomicInteger();
            ApiFutureCallback<BatchResponse> countingFailures = new ApiFutureCallback<>() {
                @Override
                public void onSuccess(BatchResponse result) {
                }

                @Override
                public void onFailure(Throwable t) {
                    failures.incrementAndGet();
                }
            };

            service.sendBatchAsync(() -> new PreparedBatch(List.of(message(), message(), message()), countingFailures));

            assertThat(failures.get()).isEqualTo(1);
            assertThat(inFlight(ctx.getBean(SimpleMeterRegistry.class))).isZero();
        });
        rejecting.shutdownNow();
    }

    @Test
    void orchestratorDispatchesOnDispatchThreadAndIsolatesClusterFailure() {
        AtomicReference<String> threadName = new AtomicReference<>();
        CountDownLatch dispatched = new CountDownLatch(1);
        PushClusterQueryService queryService = mock(PushClusterQueryService.class);
        when(queryService.findById(1L)).thenAnswer(invocation -> {
            threadName.set(Thread.currentThread().getName());
            dispatched.countDown();
            throw new IllegalStateException("클러스터 조회 실패");
        });

        runner
            // 같은 Executor 타입 후보를 추가해 @Qualifier 주입을 검증한다
            .withBean(ThreadPoolTaskScheduler.class)
            .withBean(PushClusterQueryService.class, () -> queryService)
            .withBean(FcmPushResultService.class, () -> mock(FcmPushResultService.class))
            .withBean(NotificationPushService.class, () -> mock(NotificationPushService.class))
            .withBean(DiscordMessageService.class, () -> mock(DiscordMessageService.class))
            .withUserConfiguration(FcmOrchestrator.class)
            .run(ctx -> {
                assertThat(ctx).hasNotFailed();

                ctx.getBean(FcmOrchestrator.class).dispatchClusters(List.of(new PushClusterSendRequest(1L, null)));

                assertThat(dispatched.await(10, TimeUnit.SECONDS)).isTrue();
                assertThat(threadName.get()).startsWith("fcm-dispatch-");
            });
    }

    private static double inFlight(SimpleMeterRegistry registry) {
        return registry.find("fcm.batches.in_flight").gauge().value();
    }

    private static void initFirebase(ExecutorService executor) {
        FirebaseOptions options = FirebaseOptions.builder()
            .setCredentials(GoogleCredentials.create(
                new AccessToken("fake", new Date(System.currentTimeMillis() + 3_600_000))))
            .setProjectId("backpressure-test")
            .setConnectTimeout(2000)
            .setReadTimeout(2000)
            .setThreadManager(new ThreadManager() {
                @Override
                protected ExecutorService getExecutor(FirebaseApp app) {
                    return executor;
                }

                @Override
                protected void releaseExecutor(FirebaseApp app, ExecutorService executorService) {
                }

                @Override
                protected ThreadFactory getThreadFactory() {
                    return Executors.defaultThreadFactory();
                }
            })
            .build();
        FirebaseApp.initializeApp(options);
    }

    private static Message message() {
        return Message.builder().setToken("test-token").build();
    }

    private static ApiFutureCallback<BatchResponse> callback(Runnable onComplete) {
        return new ApiFutureCallback<>() {
            @Override
            public void onSuccess(BatchResponse result) {
                onComplete.run();
            }

            @Override
            public void onFailure(Throwable t) {
                onComplete.run();
            }
        };
    }

    private static void awaitQuietly(CountDownLatch latch) {
        try {
            latch.await(30, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
