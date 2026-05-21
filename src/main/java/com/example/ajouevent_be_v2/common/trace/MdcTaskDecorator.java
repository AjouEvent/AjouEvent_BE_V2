package com.example.ajouevent_be_v2.common.trace;

import java.util.Collections;
import java.util.Map;
import org.slf4j.MDC;
import org.springframework.core.task.TaskDecorator;
import org.springframework.stereotype.Component;

/**
 * 비동기 스레드로 MDC 컨텍스트(requestId 등)를 전파하는 TaskDecorator.
 * FCM 콜백 Executor, 기본 Executor 등 ThreadPoolTaskExecutor에 적용한다.
 */
@Component
public class MdcTaskDecorator implements TaskDecorator {

    @Override
    public Runnable decorate(Runnable runnable) {
        Map<String, String> contextMap = MDC.getCopyOfContextMap();
        return () -> {
            try {
                MDC.setContextMap(contextMap != null ? contextMap : Collections.emptyMap());
                runnable.run();
            } finally {
                MDC.clear();
            }
        };
    }
}
