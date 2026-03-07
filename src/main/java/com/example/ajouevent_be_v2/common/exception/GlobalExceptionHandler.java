package com.example.ajouevent_be_v2.common.exception;

import com.example.ajouevent_be_v2.common.exception.common.CommonErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AjouBaseException.class)
    public ResponseEntity<ErrorResponse> handleAjouBaseException(AjouBaseException e) {
        log.error("[AjouBaseException] code={} message={}", e.getErrorCode().getCode(), e.getMessage(), e);
        ErrorCode errorCode = e.getErrorCode();
        return ResponseEntity.status(errorCode.getStatus()).body(ErrorResponse.of(errorCode));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValid(Exception e) {
        log.error("[Exception] Unhandled Server Error - {}", e.getMessage(), e);
        return ResponseEntity.status(CommonErrorCode.INTERNAL_FAILURE.getStatus())
                .body(ErrorResponse.of(CommonErrorCode.INTERNAL_FAILURE));
    }
}
