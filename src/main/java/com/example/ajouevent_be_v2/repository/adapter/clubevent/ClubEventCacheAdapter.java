package com.example.ajouevent_be_v2.repository.adapter.clubevent;

import com.example.ajouevent_be_v2.repository.port.clubevent.ClubEventCachePort;
import jakarta.annotation.PostConstruct;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.scripting.support.ResourceScriptSource;
import org.springframework.stereotype.Component;

/**
 * Redis 키 네이밍 컨벤션: {도메인}:{목적}:{eventId}:{식별자}
 *
 * <p>ClubEvent:dedup:{eventId}:u:{email}  — 회원 dedup (TTL 24시간)
 * <p>ClubEvent:dedup:{eventId}:a:{hash}   — 비회원 dedup, IP/UA SHA-256 앞 16자 (TTL 24시간)
 * <p>ClubEvent:views:{eventId}            — 조회수 누적 카운터 (안전장치 TTL 7일)
 * <p>ClubEvent:dirty                      — 변경 대상 더티 셋 (TTL 없음, SREM으로 관리)
 * <p>ClubEvent:committed:{eventId}        — DB 커밋 완료 대기 delta (TTL 10분, idempotent용)
 */
@Component
@RequiredArgsConstructor
public class ClubEventCacheAdapter implements ClubEventCachePort {

    private static final String VIEW_KEY_PREFIX = "ClubEvent:views:";
    private static final String DEDUP_PREFIX = "ClubEvent:dedup:";
    private static final String DIRTY_SET_KEY = "ClubEvent:dirty";
    private static final String COMMITTED_PREFIX = "ClubEvent:committed:";

    private static final long DEDUP_TTL_SECONDS = 86400L;
    private static final long VIEW_KEY_TTL_SECONDS = 604800L;
    private static final long COMMITTED_TTL_SECONDS = 600L;

    private final StringRedisTemplate stringRedisTemplate;

    private DefaultRedisScript<Long> incrementViewScript;
    private DefaultRedisScript<Long> subtractViewChunkScript;

    @PostConstruct
    private void loadScripts() {
        incrementViewScript = new DefaultRedisScript<>();
        incrementViewScript.setScriptSource(
            new ResourceScriptSource(new ClassPathResource("scripts/increment_view.lua")));
        incrementViewScript.setResultType(Long.class);

        subtractViewChunkScript = new DefaultRedisScript<>();
        subtractViewChunkScript.setScriptSource(
            new ResourceScriptSource(new ClassPathResource("scripts/subtract_view_chunk.lua")));
        subtractViewChunkScript.setResultType(Long.class);
    }

    @Override
    public void incrementViewForUser(String userEmail, Long eventId) {
        String dedupKey = DEDUP_PREFIX + eventId + ":u:" + userEmail;
        executeIncrementView(dedupKey, eventId);
    }

    @Override
    public void incrementViewForAnonymous(String ip, String userAgent, Long eventId) {
        String hash = hashIdentifier(ip, userAgent);
        String dedupKey = DEDUP_PREFIX + eventId + ":a:" + hash;
        executeIncrementView(dedupKey, eventId);
    }

    @Override
    public Set<Long> getDirtyEventIds() {
        Set<String> members = stringRedisTemplate.opsForSet().members(DIRTY_SET_KEY);
        if (members == null || members.isEmpty()) {
            return Collections.emptySet();
        }
        return members.stream().map(Long::parseLong).collect(Collectors.toSet());
    }

    @Override
    public Set<Long> scanViewCountIds() {
        ScanOptions options = ScanOptions.scanOptions()
            .match(VIEW_KEY_PREFIX + "*")
            .count(100)
            .build();
        Set<Long> ids = new HashSet<>();
        try (Cursor<byte[]> cursor = stringRedisTemplate.executeWithStickyConnection(
            connection -> connection.scan(options))) {
            if (cursor != null) {
                while (cursor.hasNext()) {
                    String key = new String(cursor.next(), StandardCharsets.UTF_8);
                    ids.add(Long.parseLong(key.substring(VIEW_KEY_PREFIX.length())));
                }
            }
        }
        return ids;
    }

    @Override
    public Long getViewCountDelta(Long eventId) {
        String value = stringRedisTemplate.opsForValue().get(VIEW_KEY_PREFIX + eventId);
        if (value == null) {
            return null;
        }
        long delta = Long.parseLong(value);
        return delta > 0 ? delta : null;
    }

    @Override
    public void subtractViewCountChunk(Map<Long, Long> deltaMap) {
        List<String> keys = new ArrayList<>();
        List<String> args = new ArrayList<>();
        for (Map.Entry<Long, Long> entry : deltaMap.entrySet()) {
            keys.add(VIEW_KEY_PREFIX + entry.getKey());
            args.add(String.valueOf(entry.getValue()));
            args.add(String.valueOf(entry.getKey()));
        }
        keys.add(DIRTY_SET_KEY);
        stringRedisTemplate.execute(subtractViewChunkScript, keys, (Object[]) args.toArray(String[]::new));
    }

    @Override
    public void saveCommittedDeltas(Map<Long, Long> deltaMap) {
        for (Map.Entry<Long, Long> entry : deltaMap.entrySet()) {
            stringRedisTemplate.opsForValue().set(
                COMMITTED_PREFIX + entry.getKey(),
                String.valueOf(entry.getValue()),
                COMMITTED_TTL_SECONDS,
                TimeUnit.SECONDS);
        }
    }

    @Override
    public Long getCommittedDelta(Long eventId) {
        String value = stringRedisTemplate.opsForValue().get(COMMITTED_PREFIX + eventId);
        return value != null ? Long.parseLong(value) : null;
    }

    @Override
    public void deleteCommittedDeltas(Set<Long> eventIds) {
        if (eventIds.isEmpty()) {
            return;
        }
        List<String> keys = eventIds.stream()
            .map(id -> COMMITTED_PREFIX + id)
            .toList();
        stringRedisTemplate.delete(keys);
    }

    private void executeIncrementView(String dedupKey, Long eventId) {
        List<String> keys = List.of(dedupKey, VIEW_KEY_PREFIX + eventId, DIRTY_SET_KEY);
        stringRedisTemplate.execute(
            incrementViewScript,
            keys,
            String.valueOf(DEDUP_TTL_SECONDS),
            String.valueOf(eventId),
            String.valueOf(VIEW_KEY_TTL_SECONDS));
    }

    private static String hashIdentifier(String ip, String userAgent) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest((ip + "|" + userAgent).getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash).substring(0, 16);
        } catch (NoSuchAlgorithmException e) {
            return Integer.toHexString((ip + "|" + userAgent).hashCode());
        }
    }
}
