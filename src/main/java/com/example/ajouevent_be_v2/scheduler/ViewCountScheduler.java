package com.example.ajouevent_be_v2.scheduler;

import com.example.ajouevent_be_v2.service.notice.NoticeCommandService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ViewCountScheduler {

    private final NoticeCommandService noticeCommandService;

    /**
     * 3분마다 Redis에 쌓인 조회수를 DB에 flush하는 스케쥴러
     */
    @Scheduled(cron = "0 0/3 * * * *")
    public void updateDBFromRedis() {
        noticeCommandService.flushViewCountsToDatabase();
    }
}
