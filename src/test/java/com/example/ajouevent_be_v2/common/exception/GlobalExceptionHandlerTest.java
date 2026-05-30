package com.example.ajouevent_be_v2.common.exception;

import com.example.ajouevent_be_v2.common.exception.auth.AuthErrorCode;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingRequestCookieException;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler globalExceptionHandler = new GlobalExceptionHandler();

    @Test
    void handleMissingRequestCookieReturnsUnauthorized() {
        MissingRequestCookieException exception = new MissingRequestCookieException("refreshToken", null);

        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleMissingRequestCookie(exception);

        assertThat(response.getStatusCode().value()).isEqualTo(AuthErrorCode.UNAUTHORIZED.getStatus());
        assertThat(response.getBody()).isEqualTo(ErrorResponse.of(AuthErrorCode.UNAUTHORIZED));
    }
}
