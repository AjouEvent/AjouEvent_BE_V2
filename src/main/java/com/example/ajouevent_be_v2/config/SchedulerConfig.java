package com.example.ajouevent_be_v2.config;

import com.example.ajouevent_be_v2.config.properties.SchedulerProperties;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.binder.MeterBinder;
import io.micrometer.core.instrument.binder.jvm.ExecutorServiceMetrics;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

@Configuration
@RequiredArgsConstructor
public class SchedulerConfig {

    private final SchedulerProperties schedulerProperties;

    @Bean
    public ThreadPoolTaskScheduler taskScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(schedulerProperties.getPoolSize());
        scheduler.setThreadNamePrefix("app-scheduler-");
        scheduler.setWaitForTasksToCompleteOnShutdown(true);
        scheduler.setAwaitTerminationSeconds(schedulerProperties.getAwaitTerminationSeconds());
        return scheduler;
    }

    @Bean
    public MeterBinder schedulerMetrics(ThreadPoolTaskScheduler taskScheduler) {
        return registry -> new ExecutorServiceMetrics(
            taskScheduler.getScheduledExecutor(),
            "app_scheduler",
            List.of(Tag.of("pool", "app-scheduler"))
        ).bindTo(registry);
    }
}
