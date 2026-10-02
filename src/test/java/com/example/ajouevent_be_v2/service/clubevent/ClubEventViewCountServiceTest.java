package com.example.ajouevent_be_v2.service.clubevent;

import com.example.ajouevent_be_v2.repository.port.clubevent.ClubEventCachePort;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.dao.QueryTimeoutException;
import org.springframework.data.redis.RedisSystemException;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ClubEventViewCountServiceTest {

    private static final Long EVENT_ID = 1L;
    private static final Long DELTA = 5L;

    private ClubEventCachePort cachePort;
    private ClubEventViewCountWriteService writeService;
    private ClubEventViewCountService service;

    @BeforeEach
    void setUp() {
        cachePort = mock(ClubEventCachePort.class);
        writeService = mock(ClubEventViewCountWriteService.class);
        service = new ClubEventViewCountService(cachePort, writeService);

        when(cachePort.getDirtyEventIds()).thenReturn(Set.of(EVENT_ID));
        when(cachePort.scanViewCountIds()).thenReturn(Set.of());
        // Mockito는 Long 반환값의 기본값으로 0L을 돌려주므로 "표식 없음"을 명시한다
        when(cachePort.getCommittedDelta(EVENT_ID)).thenReturn(null);
        when(cachePort.getViewCountDelta(EVENT_ID)).thenReturn(DELTA);
    }

    @Test
    void flushWritesDbThenRecordsCommittedThenSubtracts() {
        service.flushViewCountsToDatabase();

        InOrder order = inOrder(writeService, cachePort);
        order.verify(writeService).batchIncrementViews(Map.of(EVENT_ID, DELTA));
        order.verify(cachePort).saveCommittedDeltas(Map.of(EVENT_ID, DELTA));
        order.verify(cachePort).subtractViewCountChunk(Map.of(EVENT_ID, DELTA));
        order.verify(cachePort).deleteCommittedDeltas(Set.of(EVENT_ID));
    }

    @Test
    void dbFailureLeavesRedisUntouchedAndRetriesDbOnNextFlush() {
        doThrow(new DataAccessResourceFailureException("db down"))
            .doNothing()
            .when(writeService).batchIncrementViews(anyMap());

        service.flushViewCountsToDatabase();

        verify(cachePort, never()).saveCommittedDeltas(anyMap());
        verify(cachePort, never()).subtractViewCountChunk(anyMap());
        verify(cachePort, never()).deleteCommittedDeltas(anySet());

        // 다음 실행: DB에 반영된 적이 없으므로 DB 반영부터 다시 시도한다
        service.flushViewCountsToDatabase();

        verify(writeService, times(2)).batchIncrementViews(Map.of(EVENT_ID, DELTA));
        verify(cachePort).subtractViewCountChunk(Map.of(EVENT_ID, DELTA));
    }

    @Test
    void committedKeySaveFailureDoesNotReapplyDeltaToDbOnNextFlush() {
        // maxmemory 도달(noeviction) 또는 Redis 장애로 committed 키 저장이 거부되는 상황
        doThrow(new RedisSystemException("OOM command not allowed", null))
            .when(cachePort).saveCommittedDeltas(anyMap());

        service.flushViewCountsToDatabase();
        verify(cachePort, never()).deleteCommittedDeltas(anySet());

        // 다음 실행: Redis에는 표식이 없고 카운터는 그대로 남아 있다
        service.flushViewCountsToDatabase();

        verify(writeService, times(1)).batchIncrementViews(any());
        verify(cachePort).subtractViewCountChunk(Map.of(EVENT_ID, DELTA));
    }

    @Test
    void committedKeySaveTimeoutKeepsKeyThatMayHaveBeenWritten() {
        // 클라이언트는 타임아웃을 받았지만 서버에서는 SET이 실행됐을 수 있다 → 키를 지우면 안 된다
        doThrow(new QueryTimeoutException("Redis command timed out"))
            .when(cachePort).saveCommittedDeltas(anyMap());

        service.flushViewCountsToDatabase();
        verify(cachePort, never()).deleteCommittedDeltas(anySet());

        // 다음 실행: 서버에 저장돼 있던 Redis 표식으로 차감만 재시도한다
        when(cachePort.getCommittedDelta(EVENT_ID)).thenReturn(DELTA);
        service.flushViewCountsToDatabase();

        verify(writeService, times(1)).batchIncrementViews(any());
        verify(cachePort).subtractViewCountChunk(Map.of(EVENT_ID, DELTA));
        verify(cachePort).deleteCommittedDeltas(Set.of(EVENT_ID));
    }

    @Test
    void sustainedOomDoesNotReapplyDeltaUntilRecovered() {
        // maxmemory 도달이 이어지는 동안 SET과 차감 스크립트가 모두 거부된다
        RedisSystemException oom = new RedisSystemException("OOM command not allowed", null);
        doThrow(oom).when(cachePort).saveCommittedDeltas(anyMap());
        doThrow(oom).doThrow(oom).doNothing().when(cachePort).subtractViewCountChunk(anyMap());

        service.flushViewCountsToDatabase();  // DB 반영 후 표식 저장 실패
        service.flushViewCountsToDatabase();  // 차감 실패
        service.flushViewCountsToDatabase();  // 차감 실패
        service.flushViewCountsToDatabase();  // 복구: 차감 성공

        verify(writeService, times(1)).batchIncrementViews(any());
        verify(cachePort, times(3)).subtractViewCountChunk(Map.of(EVENT_ID, DELTA));
        verify(cachePort).deleteCommittedDeltas(Set.of(EVENT_ID));
    }

    @Test
    void unsettledDeltaIsSettledEvenWhenEventLeavesDirtySet() {
        doThrow(new RedisSystemException("OOM command not allowed", null))
            .when(cachePort).saveCommittedDeltas(anyMap());

        service.flushViewCountsToDatabase();

        // 다음 실행: dirty set과 SCAN 결과에서 빠져도 메모리 보관분만으로 차감 대상이 된다
        when(cachePort.getDirtyEventIds()).thenReturn(Set.of());
        service.flushViewCountsToDatabase();

        verify(writeService, times(1)).batchIncrementViews(any());
        verify(cachePort).subtractViewCountChunk(Map.of(EVENT_ID, DELTA));
    }

    @Test
    void committedKeyDeleteFailureDoesNotPropagate() {
        doThrow(new QueryTimeoutException("Redis command timed out"))
            .when(cachePort).deleteCommittedDeltas(anySet());

        service.flushViewCountsToDatabase();

        verify(cachePort).subtractViewCountChunk(Map.of(EVENT_ID, DELTA));
    }

    @Test
    void committedKeyReadFailureAbortsFlushBeforeDbWrite() {
        // 표식 조회 실패를 표식 없음으로 오인하면 이중 반영되므로, 예외가 전파돼 flush가 중단돼야 한다
        when(cachePort.getCommittedDelta(EVENT_ID))
            .thenThrow(new QueryTimeoutException("Redis command timed out"));

        assertThatThrownBy(() -> service.flushViewCountsToDatabase())
            .isInstanceOf(QueryTimeoutException.class);
        verify(writeService, never()).batchIncrementViews(any());
    }

    @Test
    void subtractFailureKeepsCommittedKeyAndRetriesSubtractOnly() {
        doThrow(new QueryTimeoutException("Redis command timed out"))
            .doNothing()
            .when(cachePort).subtractViewCountChunk(anyMap());

        service.flushViewCountsToDatabase();
        verify(cachePort, never()).deleteCommittedDeltas(anySet());

        // 다음 실행: Redis committed 키가 남아 있다
        when(cachePort.getCommittedDelta(EVENT_ID)).thenReturn(DELTA);
        service.flushViewCountsToDatabase();

        verify(writeService, times(1)).batchIncrementViews(any());
        verify(cachePort, times(2)).subtractViewCountChunk(Map.of(EVENT_ID, DELTA));
        verify(cachePort).deleteCommittedDeltas(Set.of(EVENT_ID));
    }

    @Test
    void settledDeltaIsNotSubtractedAgain() {
        service.flushViewCountsToDatabase();

        // 다음 실행: 차감이 끝나 카운터와 표식이 모두 사라졌다
        when(cachePort.getDirtyEventIds()).thenReturn(Set.of());
        when(cachePort.getViewCountDelta(EVENT_ID)).thenReturn(null);
        service.flushViewCountsToDatabase();

        verify(cachePort, times(1)).subtractViewCountChunk(anyMap());
    }
}
