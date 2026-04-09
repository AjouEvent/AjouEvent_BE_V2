package com.example.ajouevent_be_v2.config;

import com.example.ajouevent_be_v2.config.properties.SchedulerProperties;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.binder.MeterBinder;
import io.micrometer.core.instrument.binder.jvm.ExecutorServiceMetrics;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

@Configuration
@RequiredArgsConstructor
public class SchedulerConfig {

    private final SchedulerProperties schedulerProperties;

    // @EnableScheduling이 TaskScheduler 빈을 감지하면 기본 단일 스레드 스케줄러 대신 이 빈을 사용함.
    // 스케줄러가 4개(ViewCount, CrawlingToken, TokenValidation, PushPollingPublisher)이므로
    // pool-size=4로 설정해 스케줄러 간 블로킹 없이 병렬 실행을 보장함.
    @Bean
    public TaskScheduler taskScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(schedulerProperties.getPoolSize());
        scheduler.setThreadNamePrefix("app-scheduler-");
        scheduler.setWaitForTasksToCompleteOnShutdown(true);
        scheduler.setAwaitTerminationSeconds(schedulerProperties.getAwaitTerminationSeconds());
        // initialize()를 여기서 직접 호출하지 않음.
        // Spring이 빈 반환 후 afterPropertiesSet() → initialize()를 자동 호출하므로,
        // 여기서 호출하면 내부 ScheduledExecutorService가 두 번 생성되어 이전 인스턴스가 GC 대상이 됨.
        // Micrometer Gauge의 WeakReference가 GC된 구버전 executor를 참조하면 NaN 반환.
        return scheduler;
    }

    // MeterBinder 빈은 MeterRegistryPostProcessor가 MeterRegistry 초기화 이후에 bindTo()를 호출함.
    // 이 시점에는 taskScheduler의 afterPropertiesSet()가 이미 완료되어
    // getScheduledExecutor()가 최종 인스턴스를 반환하는 것이 보장됨.
    // 노출 메트릭 (태그: name="app_scheduler", pool="app-scheduler"):
    //   executor_pool_size_threads     — getPoolSize()                 현재 생성된 총 스레드 수
    //   executor_active_threads        — getActiveCount()              현재 작업 중인 스레드 수
    //   executor_queued_tasks          — getQueue().size()             대기 중인 예약 작업 수
    //   executor_completed_tasks_total — getCompletedTaskCount()       누적 완료 작업 수 (Counter)
    //   executor_queue_remaining_tasks — getQueue().remainingCapacity() 항상 Integer.MAX_VALUE
    //                                   (ScheduledThreadPoolExecutor 내부 DelayedWorkQueue는 무제한 큐)
    @Bean
    public MeterBinder schedulerMetrics(TaskScheduler taskScheduler) {
        return registry -> new ExecutorServiceMetrics(
            ((ThreadPoolTaskScheduler) taskScheduler).getScheduledExecutor(),
            "app_scheduler",
            List.of(Tag.of("pool", "app-scheduler"))
        ).bindTo(registry);
    }
}
