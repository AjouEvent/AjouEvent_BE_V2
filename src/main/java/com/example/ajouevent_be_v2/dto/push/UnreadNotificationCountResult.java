package com.example.ajouevent_be_v2.dto.push;

public record UnreadNotificationCountResult(Long memberId, Long unreadNotificationCount) {

    public UnreadNotificationCountResult(Number memberId, Number unreadNotificationCount) {
        this(memberId.longValue(), unreadNotificationCount.longValue());
    }
}
