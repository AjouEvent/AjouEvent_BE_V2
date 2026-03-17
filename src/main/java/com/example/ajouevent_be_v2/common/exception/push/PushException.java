package com.example.ajouevent_be_v2.common.exception.push;

import com.example.ajouevent_be_v2.common.exception.AjouBaseException;

public class PushException extends AjouBaseException {

    public PushException(PushErrorCode errorCode) {
        super(errorCode);
    }
}
