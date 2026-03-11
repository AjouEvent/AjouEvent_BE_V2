package com.example.ajouevent_be_v2.common.exception.subscription;

import com.example.ajouevent_be_v2.common.exception.AjouBaseException;

public class SubscriptionException extends AjouBaseException {

    public SubscriptionException(SubscriptionErrorCode errorCode) {
        super(errorCode);
    }
}
