package com.example.ajouevent_be_v2.config.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "ajou.fcm.executor")
public class FcmExecutorProperties {

    private Pool callback = new Pool();
    private Pool defaultPool = new Pool();

    @Getter
    @Setter
    public static class Pool {
        private int corePoolSize = 4;
        private int maxPoolSize = 16;
        private int queueCapacity = 100;
        private int awaitTerminationSeconds = 30;
    }
}
