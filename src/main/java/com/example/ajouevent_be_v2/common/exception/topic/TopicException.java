package com.example.ajouevent_be_v2.common.exception.topic;

import com.example.ajouevent_be_v2.common.exception.AjouBaseException;

public class TopicException extends AjouBaseException {

    public TopicException(TopicErrorCode errorCode) {
        super(errorCode);
    }
}
