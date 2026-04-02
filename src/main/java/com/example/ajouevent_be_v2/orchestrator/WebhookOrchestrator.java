package com.example.ajouevent_be_v2.orchestrator;

import com.example.ajouevent_be_v2.domain.clubevent.ClubEvent;
import com.example.ajouevent_be_v2.dto.clubevent.ClubEventCommand;
import com.example.ajouevent_be_v2.dto.push.PushClusterSendRequest;
import com.example.ajouevent_be_v2.dto.webhook.WebhookRequest;
import com.example.ajouevent_be_v2.dto.webhook.WebhookResponse;
import com.example.ajouevent_be_v2.service.webhook.WebhookService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class WebhookOrchestrator {

    private final WebhookService webhookService;
    private final ClubEventOrchestrator clubEventOrchestrator;
    private final PushOrchestrator pushOrchestrator;
    private final FcmOrchestrator fcmOrchestrator;

    public WebhookResponse processWebhook(String crawlingToken, WebhookRequest request) {

        webhookService.validateToken(crawlingToken);

        ClubEventCommand command = request.toClubEventCommand();

        ClubEvent clubEvent = clubEventOrchestrator.createClubEvent(command);

        // PENDING 상태의 PushCluster / PushClusterToken을 DB에 원자적으로 저장한 뒤 즉시 발송한다.
        // TX 커밋 후 서버 장애 시(구간 A/B)에는 PushPollingPublisherScheduler가 10분 후 복구한다.
        List<PushClusterSendRequest> sendRequests = pushOrchestrator.createClusters(clubEvent, command);
        if (!sendRequests.isEmpty()) {
            fcmOrchestrator.dispatchClusters(sendRequests);
        }

        return new WebhookResponse(
                "Webhook processed successfully.",
                command.englishTopic(),
                command.title(),
                clubEvent.getEventId());
    }
}
