package com.example.ajouevent_be_v2.common.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Component;

/**
 * FCM 알림 전송 결과 및 소요 시간을 추적하는 커스텀 메트릭.
 * 전송 서비스에서 주입받아 recordSuccess() / recordFailure() / getSendTimer() 를 호출한다.
 */
@Component
public class NotificationMetrics {

    private final Counter successCounter;
    private final Counter failureCounter;
    private final Timer sendTimer;

    public NotificationMetrics(MeterRegistry registry) {
        this.successCounter = Counter.builder("notification.send")
            .tag("result", "success")
            .description("FCM 알림 전송 성공 횟수")
            .register(registry);
        this.failureCounter = Counter.builder("notification.send")
            .tag("result", "failure")
            .description("FCM 알림 전송 실패 횟수")
            .register(registry);
        this.sendTimer = Timer.builder("notification.send.duration")
            .description("FCM 알림 전송 소요 시간")
            .publishPercentiles(0.5, 0.95, 0.99)
            .publishPercentileHistogram()
            .register(registry);
    }

    public void recordSuccess() {
        successCounter.increment();
    }

    public void recordFailure() {
        failureCounter.increment();
    }

    public Timer getSendTimer() {
        return sendTimer;
    }
}
