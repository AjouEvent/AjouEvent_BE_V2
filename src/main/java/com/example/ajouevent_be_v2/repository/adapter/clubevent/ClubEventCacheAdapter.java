package com.example.ajouevent_be_v2.repository.adapter.clubevent;

import com.example.ajouevent_be_v2.repository.port.clubevent.ClubEventCachePort;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

/**
 * Redis 키 네이밍 컨벤션: {도메인}:{목적}:{eventId}:{식별자...}
 *
 * <p>ClubEvent:views:{eventId}             — 조회수 버퍼 (TTL 4분)
 * <p>ClubEvent:user:{eventId}:{email}      — 인증 사용자 중복 조회 방지 (TTL 24시간)
 * <p>ClubEvent:anon:{eventId}:{ip}:{ua}    — 익명 사용자 중복 조회 방지 (TTL 24시간)
 */
@Component
@RequiredArgsConstructor
public class ClubEventCacheAdapter implements ClubEventCachePort {

    private static final String VIEW_KEY_PREFIX = "ClubEvent:views:";
    private static final String USER_REQUEST_PREFIX = "ClubEvent:user:";
    private static final String ANON_REQUEST_PREFIX = "ClubEvent:anon:";
    private static final long VIEW_COUNT_TTL_MINUTES = 4L;
    private static final long VIEW_DEDUP_TTL_SECONDS = 86400L;

    private final StringRedisTemplate stringRedisTemplate;

    @Override
    public boolean isFirstUserRequest(String userEmail, Long eventId) {
        String key = USER_REQUEST_PREFIX + eventId + ":" + userEmail;
        return Boolean.FALSE.equals(stringRedisTemplate.hasKey(key));
    }

    @Override
    public void recordUserRequest(String userEmail, Long eventId) {
        String key = USER_REQUEST_PREFIX + eventId + ":" + userEmail;
        stringRedisTemplate.opsForValue().set(key, "1", VIEW_DEDUP_TTL_SECONDS, TimeUnit.SECONDS);
    }

    @Override
    public boolean isFirstAnonymousRequest(String ip, String userAgent, Long eventId) {
        String key = ANON_REQUEST_PREFIX + eventId + ":" + ip + ":" + userAgent;
        return Boolean.FALSE.equals(stringRedisTemplate.hasKey(key));
    }

    @Override
    public void recordAnonymousRequest(String ip, String userAgent, Long eventId) {
        String key = ANON_REQUEST_PREFIX + eventId + ":" + ip + ":" + userAgent;
        stringRedisTemplate.opsForValue().set(key, "1", VIEW_DEDUP_TTL_SECONDS, TimeUnit.SECONDS);
    }

    @Override
    public void incrementViewCount(Long eventId, Long currentViewCount) {
        String key = VIEW_KEY_PREFIX + eventId;
        Boolean isNew = stringRedisTemplate.opsForValue()
            .setIfAbsent(key, String.valueOf(currentViewCount + 1), VIEW_COUNT_TTL_MINUTES, TimeUnit.MINUTES);
        if (Boolean.FALSE.equals(isNew)) {
            stringRedisTemplate.opsForValue().increment(key);
            stringRedisTemplate.expire(key, VIEW_COUNT_TTL_MINUTES, TimeUnit.MINUTES);
        }
    }

    @Override
    public Set<String> scanViewCountKeys() {
        ScanOptions options = ScanOptions.scanOptions()
            .match(VIEW_KEY_PREFIX + "*")
            .count(10)
            .build();
        Set<String> keys = new HashSet<>();
        try (Cursor<byte[]> cursor = stringRedisTemplate.executeWithStickyConnection(
            connection -> connection.scan(options))) {
            if (cursor != null) {
                while (cursor.hasNext()) {
                    keys.add(new String(cursor.next()));
                }
            }
        }
        return keys;
    }

    @Override
    public String getViewCount(String key) {
        return stringRedisTemplate.opsForValue().get(key);
    }

    @Override
    public void deleteKey(String key) {
        stringRedisTemplate.delete(key);
    }

    @Override
    public Long extractEventIdFromViewKey(String key) {
        return Long.parseLong(key.substring(VIEW_KEY_PREFIX.length()));
    }
}
