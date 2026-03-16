package com.example.ajouevent_be_v2.repository.port.notice;

import java.util.Set;

public interface NoticeCachePort {

    // View count deduplication for authenticated users (email-based)
    boolean isFirstUserRequest(String userEmail, Long eventId);

    void recordUserRequest(String userEmail, Long eventId);

    // View count deduplication for anonymous users (IP + User-Agent based)
    boolean isFirstAnonymousRequest(String ip, String userAgent, Long eventId);

    void recordAnonymousRequest(String ip, String userAgent, Long eventId);

    // View count management (Redis key: ClubEvent:views:{eventId})
    void incrementViewCount(Long eventId, Long currentViewCount);

    // For ViewCountScheduler: scan, read, delete, parse
    Set<String> scanViewCountKeys();

    String getViewCount(String key);

    void deleteKey(String key);

    Long extractEventIdFromViewKey(String key);
}
