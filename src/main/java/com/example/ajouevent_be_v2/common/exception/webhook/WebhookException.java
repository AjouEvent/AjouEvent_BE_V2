package com.example.ajouevent_be_v2.common.exception.webhook;

import com.example.ajouevent_be_v2.common.exception.AjouBaseException;

public class WebhookException extends AjouBaseException {

    public WebhookException(WebhookErrorCode errorCode) {
        super(errorCode);
    }
}
