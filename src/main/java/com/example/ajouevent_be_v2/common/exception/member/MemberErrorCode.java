package com.example.ajouevent_be_v2.common.exception.member;

import com.example.ajouevent_be_v2.common.exception.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum MemberErrorCode implements ErrorCode {

    MEMBER_NOT_FOUND(404, "AE-MEMBER-NOT-FOUND", "존재하지 않는 사용자입니다.");

    private final int status;
    private final String code;
    private final String message;
}
