package com.example.ajouevent_be_v2.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Value("${ajou.swagger.server-url}")
    private String serverUrl;

    @Bean
    public OpenAPI openAPI() {
        Server server = new Server();
        server.setUrl(serverUrl);
        return new OpenAPI().components(jwtToken()).servers(List.of(server)).info(apiInfo());
    }

    private Info apiInfo() {
        return new Info().title("Ajou Event").description("Ajou Event의 API 명세서").version("2.0.0");
    }

    private Components jwtToken() {
        return new Components()
                .addSecuritySchemes(
                        "authorization",
                        new SecurityScheme()
                                .name("authorization")
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT"));
    }
}
