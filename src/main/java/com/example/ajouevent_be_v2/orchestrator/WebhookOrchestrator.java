package com.example.ajouevent_be_v2.orchestrator;

import com.example.ajouevent_be_v2.domain.clubevent.ClubEvent;
import com.example.ajouevent_be_v2.dto.clubevent.ClubEventCommand;
import com.example.ajouevent_be_v2.dto.push.PushClusterSendRequest;
import com.example.ajouevent_be_v2.dto.webhook.WebhookRequest;
import com.example.ajouevent_be_v2.service.clubevent.ClubEventCommandService;
import com.example.ajouevent_be_v2.service.redis.RedisService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class WebhookOrchestrator {

    private final RedisService redisService;
    private final ClubEventCommandService clubEventCommandService;
    private final PushOrchestrator pushOrchestrator;
    private final FcmOrchestrator fcmOrchestrator;

    public void processWebhook(String crawlingToken, WebhookRequest request) {

        // 크롤링 토큰 검증 — TODO #15 CrawlingTokenCachePort 연결
        redisService.validateCrawlingToken(crawlingToken);

        ClubEventCommand command = request.toClubEventCommand();

        // 크롤링한 공지사항을 DB에 저장 (중복 검증 포함) — TODO ClubEventCommandService 구현
        ClubEvent clubEvent = clubEventCommandService.createClubEvent(command);

        // PushCluster 생성 후 FCM 전송
        List<PushClusterSendRequest> sendRequests = pushOrchestrator.createClusters(clubEvent, command);
        fcmOrchestrator.dispatch(sendRequests);
    }
}
