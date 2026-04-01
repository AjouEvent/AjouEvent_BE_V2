package com.example.ajouevent_be_v2.config.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "ajou.push")
public class PushProperties {

    private int staleThresholdMinutes = 10;
    private int maxRetryCount = 3;
}
