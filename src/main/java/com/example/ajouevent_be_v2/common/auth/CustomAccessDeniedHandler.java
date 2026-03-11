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
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

@Component
@Slf4j(topic = "FORBIDDEN_EXCEPTION_HANDLER")
@RequiredArgsConstructor
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper;

    @Override
    public void handle(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull AccessDeniedException accessDeniedException) throws IOException {
        log.error("No Authorities", accessDeniedException);

        ErrorResponse errorResponse = ErrorResponse.of(AuthErrorCode.FORBIDDEN);
        String responseBody = objectMapper.writeValueAsString(errorResponse);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(AuthErrorCode.FORBIDDEN.getStatus());
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(responseBody);
    }
}
