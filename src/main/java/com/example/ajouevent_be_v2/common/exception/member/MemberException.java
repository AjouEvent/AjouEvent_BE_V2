package com.example.ajouevent_be_v2.common.exception.member;

import com.example.ajouevent_be_v2.common.exception.AjouBaseException;

public class MemberException extends AjouBaseException {

    public MemberException(MemberErrorCode errorCode) {
        super(errorCode);
    }

    public MemberException(MemberErrorCode errorCode, String customMessage) {
        super(errorCode, customMessage);
    }
}
