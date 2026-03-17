package com.example.ajouevent_be_v2.common.exception.notification;

import com.example.ajouevent_be_v2.common.exception.AjouBaseException;

public class NotificationException extends AjouBaseException {

    public NotificationException(NotificationErrorCode errorCode) {
        super(errorCode);
    }

    public NotificationException(NotificationErrorCode errorCode, String customMessage) {
        super(errorCode, customMessage);
    }
}
