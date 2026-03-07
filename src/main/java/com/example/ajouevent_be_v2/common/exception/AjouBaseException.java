package com.example.ajouevent_be_v2.common.exception;

import lombok.Getter;

@Getter
public abstract class AjouBaseException extends RuntimeException {

    private final ErrorCode errorCode;
    private final String customMessage;

    protected AjouBaseException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
        this.customMessage = errorCode.getMessage();
    }

    protected AjouBaseException(ErrorCode errorCode, String customMessage) {
        super(customMessage);
        this.errorCode = errorCode;
        this.customMessage = customMessage;
    }

    protected AjouBaseException(ErrorCode errorCode, Throwable cause) {
        super(errorCode.getMessage(), cause);
        this.errorCode = errorCode;
        this.customMessage = errorCode.getMessage();
    }

    protected AjouBaseException(ErrorCode errorCode, String customMessage, Throwable cause) {
        super(customMessage, cause);
        this.errorCode = errorCode;
        this.customMessage = customMessage;
    }
}
