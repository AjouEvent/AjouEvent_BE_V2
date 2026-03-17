package com.example.ajouevent_be_v2.service.clubevent;

import com.example.ajouevent_be_v2.repository.port.clubevent.ClubEventRepositoryPort;
import com.example.ajouevent_be_v2.repository.port.clubevent.ClubEventCachePort;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 *  조회수 flush 전용 Service 클래스
 */
@Service
@RequiredArgsConstructor
public class ClubEventViewCountService {

    private final ClubEventCachePort clubEventCachePort;
    // ⚠️ cross-domain: 조회수 Redis → DB 동기화를 위해 ClubEvent 도메인 Repository 직접 참조
    private final ClubEventRepositoryPort clubEventRepositoryPort;

    @Transactional
    public void flushViewCountsToDatabase() {
        Set<String> keys = clubEventCachePort.scanViewCountKeys();
        for (String key : keys) {
            Long eventId = clubEventCachePort.extractEventIdFromViewKey(key);
            String view = clubEventCachePort.getViewCount(key);
            if (view != null) {
                clubEventRepositoryPort.updateViews(Long.valueOf(view), eventId);
                clubEventCachePort.deleteKey(key);
            }
        }
    }
}
