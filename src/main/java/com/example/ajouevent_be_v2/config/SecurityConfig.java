package com.example.ajouevent_be_v2.config;

import com.example.ajouevent_be_v2.common.auth.CustomAccessDeniedHandler;
import com.example.ajouevent_be_v2.common.auth.CustomAuthenticationEntryPoint;
import com.example.ajouevent_be_v2.common.auth.JwtAuthFilter;
import com.example.ajouevent_be_v2.common.auth.AuthArgumentResolver;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import static org.springframework.security.config.Customizer.withDefaults;

@Slf4j
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig implements WebMvcConfigurer {

    private final JwtAuthFilter jwtAuthFilter;
    private final CustomAccessDeniedHandler customAccessDeniedHandler;
    private final CustomAuthenticationEntryPoint customAuthenticationEntryPoint;
    private final AuthArgumentResolver authArgumentResolver;
    private final Environment environment;

    private static final String[] AUTH_WHITELIST = {
            "/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html",
            "/api/v2/auth/login", "/api/v2/auth/reissue",
            "/api/v2/topics",
            "/api/webhook/crawling"
    };

    private static final String[] LOCAL_AUTH_WHITELIST = {
            "/api/v2/auth/test-login", "/api/v2/auth/test-login/fcm",
            "/api/v2/auth/test-crawling-token"
    };

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        List<String> whitelist = new ArrayList<>(Arrays.asList(AUTH_WHITELIST));
        if (Arrays.asList(environment.getActiveProfiles()).contains("local")) {
            whitelist.addAll(Arrays.asList(LOCAL_AUTH_WHITELIST));
        }

        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(withDefaults())
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .oauth2Login(AbstractHttpConfigurer::disable)
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(customAuthenticationEntryPoint)
                        .accessDeniedHandler(customAccessDeniedHandler)
                )
                .authorizeHttpRequests(authorize -> authorize
                                .requestMatchers(whitelist.toArray(String[]::new)).permitAll()
                                .anyRequest().authenticated()
                );

        return http.build();
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(authArgumentResolver);
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
            .allowedOriginPatterns(
                "http://localhost:3000"
            )
            .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH")
            .allowedHeaders("Authorization", "Content-Type")
            .exposedHeaders("Custom-Header")
            .allowCredentials(true)
            .maxAge(3600);
    }
}
