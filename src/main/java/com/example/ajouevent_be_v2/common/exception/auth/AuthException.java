package com.example.ajouevent_be_v2.common.exception.auth;

import com.example.ajouevent_be_v2.common.exception.AjouBaseException;

public class AuthException extends AjouBaseException {

    public AuthException(AuthErrorCode errorCode) {
        super(errorCode);
    }
}
