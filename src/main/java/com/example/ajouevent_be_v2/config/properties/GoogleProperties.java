package com.example.ajouevent_be_v2.config.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "ajou.google")
public class GoogleProperties {

    private String registrationId;
    private String tokenEndpoint;
    private String calendarApiUrl;
    private String peopleApiUrl;
}
