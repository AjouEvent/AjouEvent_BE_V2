package com.example.ajouevent_be_v2.common.exception.discord;

import com.example.ajouevent_be_v2.common.exception.AjouBaseException;

public class DiscordException extends AjouBaseException {

    public DiscordException(DiscordErrorCode errorCode) {
        super(errorCode);
    }

    public DiscordException(DiscordErrorCode errorCode, Throwable cause) {
        super(errorCode, cause);
    }
}
