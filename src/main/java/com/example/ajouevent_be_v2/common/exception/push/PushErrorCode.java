package com.example.ajouevent_be_v2.common.exception.push;

import com.example.ajouevent_be_v2.common.exception.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum PushErrorCode implements ErrorCode {

    PUSH_CLUSTER_NOT_FOUND(404, "AE-PUSH-CLUSTER-NOT-FOUND", "PushCluster를 찾을 수 없습니다.");

    private final int status;
    private final String code;
    private final String message;
}
