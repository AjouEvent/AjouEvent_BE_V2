package com.example.ajouevent_be_v2.scheduler;

import com.example.ajouevent_be_v2.service.clubevent.ClubEventQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PopularEventCacheScheduler {

    private final ClubEventQueryService clubEventQueryService;

    @Scheduled(cron = "0 0 0/1 * * *")
    public void refreshPopularEvents() {
        clubEventQueryService.refreshPopularEventsCache();
    }
}
