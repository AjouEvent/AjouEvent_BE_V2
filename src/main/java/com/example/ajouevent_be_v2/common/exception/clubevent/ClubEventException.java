package com.example.ajouevent_be_v2.common.exception.clubevent;

import com.example.ajouevent_be_v2.common.exception.AjouBaseException;

public class ClubEventException extends AjouBaseException {

    public ClubEventException(ClubEventErrorCode errorCode) {
        super(errorCode);
    }
}
