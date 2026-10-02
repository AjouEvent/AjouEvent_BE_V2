package com.example.ajouevent_be_v2.config.properties;

import java.time.Duration;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "spring.data.redis")
public class RedisProperties {

    private String host;
    private int port;
    private Duration timeout = Duration.ofSeconds(2);
    private Duration connectTimeout = Duration.ofSeconds(1);
}
