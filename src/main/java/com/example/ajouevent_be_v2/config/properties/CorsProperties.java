package com.example.ajouevent_be_v2.config.properties;

import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "ajou.cors")
public class CorsProperties {

    private List<String> allowedOrigins;
}
