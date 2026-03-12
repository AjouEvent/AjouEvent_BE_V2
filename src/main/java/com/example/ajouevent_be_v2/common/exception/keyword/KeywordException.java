package com.example.ajouevent_be_v2.common.exception.keyword;

import com.example.ajouevent_be_v2.common.exception.AjouBaseException;

public class KeywordException extends AjouBaseException {

    public KeywordException(KeywordErrorCode errorCode) {
        super(errorCode);
    }
}
