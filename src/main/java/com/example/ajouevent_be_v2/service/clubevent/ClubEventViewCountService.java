package com.example.ajouevent_be_v2.service.clubevent;

import com.example.ajouevent_be_v2.repository.port.clubevent.ClubEventCachePort;
import com.example.ajouevent_be_v2.repository.port.clubevent.ClubEventRepositoryPort;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 조회수 flush 전용 Service — Redis → DB 동기화
 *
 * <p>Read-Write-Subtract 패턴:
 * <ol>
 *   <li>[Read]     더티 셋에서 반영 대상 조회 + SCAN으로 누락 탐지
 *   <li>[Write]    JdbcTemplate Batch Update (view_count = view_count + delta)
 *   <li>[Subtract] Lua Script 청크 단위 DECRBY + 조건부 더티 셋 정리
 * </ol>
 *
 * <p>Idempotent 보장: DB 커밋 성공 후 committed 키를 기록하여,
 * subtract 실패 시 다음 스케줄러 실행에서 DB 재기록 없이 subtract만 재시도.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ClubEventViewCountService {

    private static final int CHUNK_SIZE = 100;

    private final ClubEventCachePort clubEventCachePort;
    // ⚠️ cross-domain: 조회수 Redis → DB 동기화를 위해 ClubEvent 도메인 Repository 직접 참조
    private final ClubEventRepositoryPort clubEventRepositoryPort;

    public void flushViewCountsToDatabase() {
        // 1. 더티 셋에서 반영 대상 조회
        Set<Long> dirtyIds = clubEventCachePort.getDirtyEventIds();

        // 2. SCAN으로 더티 셋 미등록 누락 키 탐지
        Set<Long> scannedIds = clubEventCachePort.scanViewCountIds();

        Set<Long> allIds = new HashSet<>(dirtyIds);
        allIds.addAll(scannedIds);

        if (allIds.isEmpty()) {
            return;
        }

        // 3. 청크 단위 처리 (Redis 블로킹 방지)
        List<Long> idList = new ArrayList<>(allIds);
        for (int i = 0; i < idList.size(); i += CHUNK_SIZE) {
            List<Long> chunk = idList.subList(i, Math.min(i + CHUNK_SIZE, idList.size()));
            processChunk(chunk);
        }
    }

    private void processChunk(List<Long> eventIds) {
        Map<Long, Long> pendingMap = new HashMap<>();
        Map<Long, Long> freshMap = new HashMap<>();

        for (Long eventId : eventIds) {
            Long committedDelta = clubEventCachePort.getCommittedDelta(eventId);
            if (committedDelta != null) {
                // 이전 실행에서 DB 커밋 완료됐으나 subtract 실패 → subtract만 재시도
                pendingMap.put(eventId, committedDelta);
            } else {
                Long delta = clubEventCachePort.getViewCountDelta(eventId);
                if (delta != null) {
                    freshMap.put(eventId, delta);
                }
            }
        }

        // subtract 재시도 (DB 이미 반영됨)
        if (!pendingMap.isEmpty()) {
            clubEventCachePort.subtractViewCountChunk(pendingMap);
            clubEventCachePort.deleteCommittedDeltas(pendingMap.keySet());
        }

        if (freshMap.isEmpty()) {
            return;
        }

        // [Read] DB 커밋 전 delta 기록 (subtract 실패 시 재시도용)
        clubEventCachePort.saveCommittedDeltas(freshMap);

        try {
            // [Write] JdbcTemplate Batch Update
            clubEventRepositoryPort.batchIncrementViews(freshMap);
        } catch (Exception e) {
            log.error("조회수 DB 반영 실패 — committed 키 정리 후 재시도 대기: {}", e.getMessage());
            clubEventCachePort.deleteCommittedDeltas(freshMap.keySet());
            return;
        }

        // [Subtract] DB 커밋 성공 후 Redis 조회수 차감 (Lua Script, 청크 단위 원자적 처리)
        clubEventCachePort.subtractViewCountChunk(freshMap);
        clubEventCachePort.deleteCommittedDeltas(freshMap.keySet());
    }
}
