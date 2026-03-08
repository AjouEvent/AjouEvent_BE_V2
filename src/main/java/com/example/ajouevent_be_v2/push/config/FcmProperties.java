package com.example.ajouevent_be_v2.push.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "ajou.fcm")
public class FcmProperties {

    private String defaultImageUrl;
    private String redirectionUrlPrefix;
    private String defaultClickActionUrl;
}
