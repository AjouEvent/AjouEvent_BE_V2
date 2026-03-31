package com.example.ajouevent_be_v2.orchestrator;

import com.example.ajouevent_be_v2.domain.clubevent.ClubEvent;
import com.example.ajouevent_be_v2.dto.clubevent.ClubEventCommand;
import com.example.ajouevent_be_v2.dto.webhook.WebhookRequest;
import com.example.ajouevent_be_v2.dto.webhook.WebhookResponse;
import com.example.ajouevent_be_v2.service.webhook.WebhookService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class WebhookOrchestrator {

    private final WebhookService webhookService;
    private final ClubEventOrchestrator clubEventOrchestrator;
    private final PushOrchestrator pushOrchestrator;

    public WebhookResponse processWebhook(String crawlingToken, WebhookRequest request) {

        webhookService.validateToken(crawlingToken);

        ClubEventCommand command = request.toClubEventCommand();

        ClubEvent clubEvent = clubEventOrchestrator.createClubEvent(command);

        // PENDING 상태의 PushCluster / PushClusterToken을 DB에 저장한다.
        // FCM 발송은 PushPollingPublisherScheduler가 1분마다 폴링해서 처리한다.
        pushOrchestrator.createClusters(clubEvent, command);

        return new WebhookResponse(
                "Webhook processed successfully.",
                command.englishTopic(),
                command.title(),
                clubEvent.getEventId());
    }
}
