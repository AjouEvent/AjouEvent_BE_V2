package com.example.ajouevent_be_v2.service.notice;

import com.example.ajouevent_be_v2.repository.port.event.ClubEventRepositoryPort;
import com.example.ajouevent_be_v2.repository.port.notice.NoticeCachePort;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NoticeCommandService {

    private final NoticeCachePort noticeCachePort;
    // ⚠️ cross-domain: 조회수 Redis → DB 동기화를 위해 ClubEvent 도메인 Repository 직접 참조
    private final ClubEventRepositoryPort clubEventRepositoryPort;

    @Transactional
    public void flushViewCountsToDatabase() {
        Set<String> keys = noticeCachePort.scanViewCountKeys();
        for (String key : keys) {
            Long eventId = noticeCachePort.extractEventIdFromViewKey(key);
            String view = noticeCachePort.getViewCount(key);
            if (view != null) {
                clubEventRepositoryPort.updateViews(Long.valueOf(view), eventId);
                noticeCachePort.deleteKey(key);
            }
        }
    }
}
