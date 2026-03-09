package com.example.ajouevent_be_v2.common.exception.discord;

import com.example.ajouevent_be_v2.common.exception.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum DiscordErrorCode implements ErrorCode {

    WEBHOOK_FAILED(502, "AE-DISCORD-WEBHOOK-FAILED", "Discord 웹훅 전송에 실패했습니다.");

    private final int status;
    private final String code;
    private final String message;
}
