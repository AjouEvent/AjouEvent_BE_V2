package com.example.ajouevent_be_v2.service.clubevent;

import com.example.ajouevent_be_v2.repository.port.clubevent.ClubEventCachePort;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
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
 *
 * <p>DB 커밋 이후의 Redis 실패는 DB 실패와 분리해 처리한다.
 * DB 커밋 후에는 어떤 실패 경로에서도 committed 키를 지우지 않는다. 지우면 다음 실행에서 같은 delta가 DB에 다시 반영된다.
 * committed 키 저장 자체가 실패하는 경우(Redis 장애, maxmemory 도달)에 대비해
 * 같은 delta를 인스턴스 메모리({@code unsettledDeltas})에도 보관하고, Redis 키가 없을 때 이를 대신 사용한다.
 * 메모리 보관분은 앱이 재시작되면 사라진다.
 *
 * <p>이 보장은 flush가 한 인스턴스에서만 실행된다는 전제에 기대고 있다.
 * 멀티 인스턴스나 블루그린 배포로 flush가 동시에 실행되면 깨진다(스케줄러 락 필요).
 * 같은 cron 작업은 자기 자신과 겹쳐 실행되지 않지만 실행 스레드는 바뀔 수 있으므로 ConcurrentHashMap을 쓴다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ClubEventViewCountService {

    private static final int CHUNK_SIZE = 100;

    private final ClubEventCachePort clubEventCachePort;
    private final ClubEventViewCountWriteService clubEventViewCountWriteService;

    // DB에는 반영됐지만 Redis 차감이 끝나지 않은 delta (committed 키 저장 실패 대비)
    private final Map<Long, Long> unsettledDeltas = new ConcurrentHashMap<>();

    public void flushViewCountsToDatabase() {
        // 1. 더티 셋에서 반영 대상 조회
        Set<Long> dirtyIds = clubEventCachePort.getDirtyEventIds();

        // 2. SCAN으로 더티 셋 미등록 누락 키 탐지
        Set<Long> scannedIds = clubEventCachePort.scanViewCountIds();

        Set<Long> allIds = new HashSet<>(dirtyIds);
        allIds.addAll(scannedIds);
        allIds.addAll(unsettledDeltas.keySet());

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
            Long committedDelta = findCommittedDelta(eventId);
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
            settle(pendingMap);
        }

        if (freshMap.isEmpty()) {
            return;
        }

        try {
            // [Write] JdbcTemplate Batch Update
            clubEventViewCountWriteService.batchIncrementViews(freshMap);
        } catch (RuntimeException e) {
            // DB에 반영되지 않았으므로 Redis는 손대지 않고 다음 실행에서 그대로 재시도
            log.error("조회수 DB 반영 실패 — 다음 실행에서 재시도", e);
            return;
        }

        // [Write 이후] DB 반영이 끝난 delta를 committed로 기록 (메모리 → Redis 순)
        unsettledDeltas.putAll(freshMap);
        try {
            clubEventCachePort.saveCommittedDeltas(freshMap);
        } catch (RuntimeException e) {
            // DB와 Redis가 어긋난 상태이므로 error로 남긴다
            log.error("committed 키 저장 실패 — 메모리 보관분으로 다음 실행에서 차감만 재시도", e);
            return;
        }

        // [Subtract] DB 커밋 성공 후 Redis 조회수 차감 (Lua Script, 청크 단위 원자적 처리)
        settle(freshMap);
    }

    // Redis committed 키를 우선하고, 없으면 메모리 보관분을 사용한다
    private Long findCommittedDelta(Long eventId) {
        Long committedDelta = clubEventCachePort.getCommittedDelta(eventId);
        return committedDelta != null ? committedDelta : unsettledDeltas.get(eventId);
    }

    // DB에 반영된 delta를 Redis에서 차감하고 committed 표식을 정리한다. 실패하면 표식을 남겨 다음 실행에서 재시도한다
    private void settle(Map<Long, Long> deltaMap) {
        try {
            clubEventCachePort.subtractViewCountChunk(deltaMap);
        } catch (RuntimeException e) {
            log.warn("조회수 차감 실패 — committed 표식 유지, 다음 실행에서 차감만 재시도", e);
            return;
        }
        deltaMap.keySet().forEach(unsettledDeltas::remove);
        try {
            clubEventCachePort.deleteCommittedDeltas(deltaMap.keySet());
        } catch (RuntimeException e) {
            log.warn("committed 키 삭제 실패 — TTL 만료까지 남음", e);
        }
    }
}
