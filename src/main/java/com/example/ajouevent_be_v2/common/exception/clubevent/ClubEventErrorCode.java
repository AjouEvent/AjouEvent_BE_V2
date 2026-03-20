package com.example.ajouevent_be_v2.common.exception.clubevent;

import com.example.ajouevent_be_v2.common.exception.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ClubEventErrorCode implements ErrorCode {

    EVENT_NOT_FOUND(404, "AE-EVENT-NOT-FOUND", "존재하지 않는 공지사항입니다."),
    EVENT_NOT_LIKED(404, "AE-EVENT-NOT-LIKED", "찜하지 않은 공지사항입니다."),
    ALREADY_LIKED(409, "AE-EVENT-ALREADY-LIKED", "이미 찜한 공지사항입니다."),
    DUPLICATE_NOTICE(409, "AE-EVENT-DUPLICATE-NOTICE", "이미 처리된 공지사항입니다."),
    INVALID_TYPE(400, "AE-EVENT-INVALID-TYPE", "유효하지 않은 공지사항 타입입니다.");

    private final int status;
    private final String code;
    private final String message;
}
