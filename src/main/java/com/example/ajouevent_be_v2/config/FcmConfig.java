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
}
