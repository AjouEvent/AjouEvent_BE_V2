package com.example.ajouevent_be_v2.common.exception.notification;

import com.example.ajouevent_be_v2.common.exception.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum NotificationErrorCode implements ErrorCode {

    NOTIFICATION_NOT_FOUND(404, "AE-NOTIFICATION-NOT-FOUND", "존재하지 않는 알림입니다.");

    private final int status;
    private final String code;
    private final String message;
}
