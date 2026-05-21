package com.example.ajouevent_be_v2.config;

import com.example.ajouevent_be_v2.config.properties.FcmExecutorProperties;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.binder.MeterBinder;
import io.micrometer.core.instrument.binder.jvm.ExecutorServiceMetrics;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
@RequiredArgsConstructor
public class FcmConfig {

    private final FcmExecutorProperties fcmExecutorProperties;

    @Bean(name = "fcmCallbackExecutor")
    public ThreadPoolTaskExecutor fcmCallbackExecutor() {
        FcmExecutorProperties.Pool pool = fcmExecutorProperties.getCallback();
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(pool.getCorePoolSize());
        executor.setMaxPoolSize(pool.getMaxPoolSize());
        executor.setQueueCapacity(pool.getQueueCapacity());
        executor.setThreadNamePrefix("fcm-callback-");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(pool.getAwaitTerminationSeconds());
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

    @Bean(name = "fcmDefaultExecutor")
    public ThreadPoolTaskExecutor fcmDefaultExecutor() {
        FcmExecutorProperties.Pool pool = fcmExecutorProperties.getDefaultPool();
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(pool.getCorePoolSize());
        executor.setMaxPoolSize(pool.getMaxPoolSize());
        executor.setQueueCapacity(pool.getQueueCapacity());
        executor.setThreadNamePrefix("fcm-default-");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(pool.getAwaitTerminationSeconds());
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
}
