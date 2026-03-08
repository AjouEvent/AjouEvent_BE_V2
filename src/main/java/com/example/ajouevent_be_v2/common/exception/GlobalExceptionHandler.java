package com.example.ajouevent_be_v2.common.exception;

import com.example.ajouevent_be_v2.common.exception.common.CommonErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AjouBaseException.class)
    public ResponseEntity<ErrorResponse> handleAjouBaseException(AjouBaseException e) {
        log.error("[AjouBaseException] code={} message={}", e.getErrorCode().getCode(), e.getMessage(), e);
        ErrorCode errorCode = e.getErrorCode();
        return ResponseEntity.status(errorCode.getStatus())
                .body(new ErrorResponse(errorCode.getCode(), e.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValid(MethodArgumentNotValidException e) {
        log.error("[MethodArgumentNotValidException] {}", e.getMessage(), e);
        String message = e.getBindingResult().getAllErrors().get(0).getDefaultMessage();
        return ResponseEntity.status(CommonErrorCode.VALIDATION_ERROR.getStatus())
                .body(new ErrorResponse(CommonErrorCode.VALIDATION_ERROR.getCode(), message));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException e) {
        log.error("[IllegalArgumentException] {}", e.getMessage(), e);
        return ResponseEntity.status(CommonErrorCode.VALIDATION_ERROR.getStatus())
                .body(new ErrorResponse(CommonErrorCode.VALIDATION_ERROR.getCode(), e.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnhandledException(Exception e) {
        log.error("[Exception] Unhandled Server Error - {}", e.getMessage(), e);
        return ResponseEntity.status(CommonErrorCode.INTERNAL_FAILURE.getStatus())
                .body(ErrorResponse.of(CommonErrorCode.INTERNAL_FAILURE));
    }
}
