package com.example.ajouevent_be_v2.orchestrator;

import com.example.ajouevent_be_v2.domain.event.ClubEvent;
import com.example.ajouevent_be_v2.dto.clubevent.ClubEventCommand;
import com.example.ajouevent_be_v2.dto.push.PushClusterSendRequest;
import com.example.ajouevent_be_v2.dto.webhook.WebhookRequest;
import com.example.ajouevent_be_v2.service.clubevent.ClubEventService;
import com.example.ajouevent_be_v2.service.push.FcmService;
import com.example.ajouevent_be_v2.service.push.PushService;
import com.example.ajouevent_be_v2.service.redis.RedisService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class WebhookOrchestrator {

    private final RedisService redisService;
    private final ClubEventService clubEventService;
    private final PushService pushService;
    private final FcmService fcmService;

    public void processWebhook(String crawlingToken, WebhookRequest request) {

        // 크롤링 토큰 검증 — TODO #15 CrawlingTokenCachePort 연결
        redisService.validateCrawlingToken(crawlingToken);

        ClubEventCommand command = request.toClubEventCommand();

        // 크롤링한 공지사항을 DB에 저장 (중복 검증 포함) — TODO ClubEventService 구현
        ClubEvent clubEvent = clubEventService.createClubEvent(command);

        // Topic + Keyword 구독자 대상 PushCluster/PushClusterToken/PushNotification 생성
        List<PushClusterSendRequest> sendRequests = pushService.createPushClusters(clubEvent, command);

        // 각 PushCluster에 대해 FCM 메시지 전송
        sendRequests.forEach(req ->
            fcmService.sendFcmToPushCluster(req.fcmMessageCommand(), req.pushClusterId()));
    }
}
