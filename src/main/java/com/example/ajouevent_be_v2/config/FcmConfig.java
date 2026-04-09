package com.example.ajouevent_be_v2.config;

import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.binder.MeterBinder;
import io.micrometer.core.instrument.binder.jvm.ExecutorServiceMetrics;
import java.util.List;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
public class FcmConfig {

    @Bean(name = "fcmCallbackExecutor")
    public ThreadPoolTaskExecutor fcmCallbackExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(4);
        executor.setMaxPoolSize(16);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("fcm-callback-");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(30);
        // initialize()를 여기서 직접 호출하지 않음.
        // Spring이 빈 반환 후 afterPropertiesSet() → initialize()를 자동 호출하므로,
        // 여기서 호출하면 내부 ThreadPoolExecutor가 두 번 생성되어 이전 인스턴스가 GC 대상이 됨.
        // Micrometer Gauge의 WeakReference가 GC된 구버전 executor를 참조하면 NaN 반환.
        return executor;
    }

    // MeterBinder 빈은 MeterRegistryPostProcessor가 MeterRegistry 초기화 이후에 bindTo()를 호출함.
    // 이 시점에는 fcmCallbackExecutor의 afterPropertiesSet()가 이미 완료되어
    // getThreadPoolExecutor()가 최종 인스턴스를 반환하는 것이 보장됨.
    // 노출 메트릭 (태그: name="fcm_callback_executor", pool="fcm-callback"):
    //   executor_pool_size_threads     — getPoolSize()                 현재 생성된 총 스레드 수
    //   executor_active_threads        — getActiveCount()              현재 작업 중인 스레드 수
    //   executor_queued_tasks          — getQueue().size()             큐에서 대기 중인 작업 수
    //   executor_completed_tasks_total — getCompletedTaskCount()       누적 완료 작업 수 (Counter)
    //   executor_queue_remaining_tasks — getQueue().remainingCapacity() 큐의 남은 용량
    @Bean
    public MeterBinder fcmCallbackExecutorMetrics(
            @Qualifier("fcmCallbackExecutor") ThreadPoolTaskExecutor executor) {
        return registry -> new ExecutorServiceMetrics(
            executor.getThreadPoolExecutor(),
            "fcm_callback_executor",
            List.of(Tag.of("pool", "fcm-callback"))
        ).bindTo(registry);
    }
}
