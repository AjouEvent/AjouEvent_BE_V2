package com.example.ajouevent_be_v2.common.auth;

import com.example.ajouevent_be_v2.common.exception.ErrorResponse;
import com.example.ajouevent_be_v2.common.exception.auth.AuthErrorCode;
import tools.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

@Component
@Slf4j(topic = "UNAUTHORIZED_EXCEPTION_HANDLER")
@RequiredArgsConstructor
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    @Override
    public void commence(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull AuthenticationException authenticationException) throws IOException {
        log.error("Not Authenticated Request", authenticationException);

        ErrorResponse errorResponse = ErrorResponse.of(AuthErrorCode.UNAUTHORIZED);
        String responseBody = objectMapper.writeValueAsString(errorResponse);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(AuthErrorCode.UNAUTHORIZED.getStatus());
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(responseBody);
    }
}
