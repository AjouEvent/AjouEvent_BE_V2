package com.example.ajouevent_be_v2.config;

import com.example.ajouevent_be_v2.common.auth.AuthAdmin;
import com.example.ajouevent_be_v2.common.auth.AuthOptional;
import com.example.ajouevent_be_v2.common.auth.AuthUser;
import com.example.ajouevent_be_v2.config.properties.SwaggerProperties;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.utils.SpringDocUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class SwaggerConfig {

    static {
        SpringDocUtils.getConfig()
            .addAnnotationsToIgnore(AuthUser.class, AuthAdmin.class, AuthOptional.class);
    }

    private final SwaggerProperties swaggerProperties;

    @Bean
    public OpenAPI openAPI() {
        Server server = new Server();
        server.setUrl(swaggerProperties.getServerUrl());
        return new OpenAPI().components(jwtToken()).servers(List.of(server)).info(apiInfo());
    }

    private Info apiInfo() {
        return new Info()
            .title("아주이벤트 V2 API 명세서")
            .description("아주이벤트(AjouEvent) V2 REST API 명세서입니다.")
            .version("2.0.0");
    }

    private Components jwtToken() {
        return new Components()
            .addSecuritySchemes(
                "bearerAuth",
                new SecurityScheme()
                    .name("bearerAuth")
                    .type(SecurityScheme.Type.HTTP)
                    .scheme("bearer")
                    .bearerFormat("JWT"));
    }
}
