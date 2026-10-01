package com.example.ajouevent_be_v2.config;

import com.example.ajouevent_be_v2.common.trace.MdcTaskDecorator;
import com.example.ajouevent_be_v2.config.properties.FcmExecutorProperties;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.binder.MeterBinder;
import io.micrometer.core.instrument.binder.jvm.ExecutorServiceMetrics;
import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.util.Assert;

@Configuration
@RequiredArgsConstructor
public class FcmConfig {

    // Firebase SDK 가 sendEach 1회에 허용하는 최대 메시지 수
    private static final int MAX_MESSAGES_PER_SEND_CALL = 500;

    private final FcmExecutorProperties fcmExecutorProperties;
    private final MdcTaskDecorator mdcTaskDecorator;

    @Bean(name = "fcmCallbackExecutor")
    public ThreadPoolTaskExecutor fcmCallbackExecutor() {
        FcmExecutorProperties.Pool pool = fcmExecutorProperties.getCallback();
        // 콜백 실행이 거절되면 세마포어 허가가 반납되지 않으므로, 동시 콜백 상한(= 처리 중 배치 수)을 항상 수용해야 한다
        Assert.state(pool.getMaxPoolSize() + pool.getQueueCapacity() >= fcmExecutorProperties.getMaxInFlightBatches(),
            "fcm-callback 용량(max + queue)은 max-in-flight-batches 이상이어야 합니다");
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(pool.getCorePoolSize());
        executor.setMaxPoolSize(pool.getMaxPoolSize());
        executor.setQueueCapacity(pool.getQueueCapacity());
        executor.setThreadNamePrefix("fcm-callback-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.AbortPolicy());
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(pool.getAwaitTerminationSeconds());
        executor.setTaskDecorator(mdcTaskDecorator);
        executor.initialize();
        return executor;
    }

    @Bean
    public MeterBinder fcmCallbackExecutorMetrics(
            @Qualifier("fcmCallbackExecutor") ThreadPoolTaskExecutor executor) {
        return registry -> new ExecutorServiceMetrics(
            executor.getThreadPoolExecutor(),
            "fcm_callback_executor",
            List.of(Tag.of("pool", "fcm-callback"))
        ).bindTo(registry);
    }

    // 종료 순서 고정: dispatch → default → callback. 진행 중인 배치의 콜백이 먼저 종료된 callback 풀에 거절되지 않게 한다.
    @Bean(name = "fcmDefaultExecutor")
    @DependsOn("fcmCallbackExecutor")
    public ThreadPoolTaskExecutor fcmDefaultExecutor() {
        FcmExecutorProperties.Pool pool = fcmExecutorProperties.getDefaultPool();
        // 세마포어를 거친 발송 배치 + 세마포어 밖의 토큰 검증 배치(동기, 한 번에 1개)를 모두 수용해야 거절이 발생하지 않는다.
        // 세마포어를 거치지 않는 Firebase 비동기 호출(예: 토픽 구독 해제)을 추가하면 이 계산식을 다시 맞춰야 한다.
        int maxOutstandingMessages = (fcmExecutorProperties.getMaxInFlightBatches() + 1) * MAX_MESSAGES_PER_SEND_CALL;
        Assert.state(pool.getMaxPoolSize() + pool.getQueueCapacity() >= maxOutstandingMessages,
            "fcm-default 용량(max + queue)은 (max-in-flight-batches + 1) x 500 이상이어야 합니다");
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(pool.getCorePoolSize());
        executor.setMaxPoolSize(pool.getMaxPoolSize());
        executor.setQueueCapacity(pool.getQueueCapacity());
        executor.setThreadNamePrefix("fcm-default-");
        // 발송은 평일 낮에 몰리므로 유휴 시간에는 스레드를 반납한다
        executor.setAllowCoreThreadTimeOut(true);
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.AbortPolicy());
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(pool.getAwaitTerminationSeconds());
        executor.setTaskDecorator(mdcTaskDecorator);
        executor.initialize();
        return executor;
    }

    @Bean
    public MeterBinder fcmDefaultExecutorMetrics(
            @Qualifier("fcmDefaultExecutor") ThreadPoolTaskExecutor executor) {
        return registry -> new ExecutorServiceMetrics(
            executor.getThreadPoolExecutor(),
            "fcm_default_executor",
            List.of(Tag.of("pool", "fcm-default"))
        ).bindTo(registry);
    }

    @Bean(name = "fcmDispatchExecutor")
    @DependsOn("fcmDefaultExecutor")
    public ThreadPoolTaskExecutor fcmDispatchExecutor() {
        FcmExecutorProperties.Pool pool = fcmExecutorProperties.getDispatch();
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(pool.getCorePoolSize());
        executor.setMaxPoolSize(pool.getMaxPoolSize());
        executor.setQueueCapacity(pool.getQueueCapacity());
        executor.setThreadNamePrefix("fcm-dispatch-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.AbortPolicy());
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(pool.getAwaitTerminationSeconds());
        executor.setTaskDecorator(mdcTaskDecorator);
        executor.initialize();
        return executor;
    }

    @Bean
    public MeterBinder fcmDispatchExecutorMetrics(
            @Qualifier("fcmDispatchExecutor") ThreadPoolTaskExecutor executor) {
        return registry -> new ExecutorServiceMetrics(
            executor.getThreadPoolExecutor(),
            "fcm_dispatch_executor",
            List.of(Tag.of("pool", "fcm-dispatch"))
        ).bindTo(registry);
    }
}
