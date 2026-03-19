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

        List<PushClusterSendRequest> sendRequests = pushOrchestrator.createClusters(clubEvent, command);
        fcmOrchestrator.dispatch(sendRequests);

        return new WebhookResponse(
                "Webhook processed successfully.",
                command.englishTopic(),
                command.title(),
                clubEvent.getEventId());
    }
}
