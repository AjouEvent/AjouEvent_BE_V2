package com.example.ajouevent_be_v2.orchestrator;

import com.example.ajouevent_be_v2.domain.push.PushCluster;
import com.example.ajouevent_be_v2.domain.push.PushClusterToken;
import com.example.ajouevent_be_v2.dto.push.FcmMessageCommand;
import com.example.ajouevent_be_v2.dto.push.PushClusterSendRequest;
import com.example.ajouevent_be_v2.service.notification.NotificationPushService;
import com.example.ajouevent_be_v2.service.push.PushClusterQueryService;
import com.example.ajouevent_be_v2.service.webhook.FcmPushResultService;
import com.example.ajouevent_be_v2.service.webhook.FcmPushService;
import com.google.api.core.ApiFutureCallback;
import com.google.firebase.messaging.BatchResponse;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class FcmOrchestrator {

    private final FcmPushService fcmPushService;
    private final PushClusterQueryService pushClusterQueryService;
    private final FcmPushResultService fcmPushResultService;
    private final NotificationPushService notificationPushService;

    public void dispatch(List<PushClusterSendRequest> sendRequests) {
        sendRequests.forEach(req -> sendToCluster(req.fcmMessageCommand(), req.pushClusterId()));
    }

    private void sendToCluster(FcmMessageCommand command, Long pushClusterId) {
        PushCluster cluster = pushClusterQueryService.findById(pushClusterId);
        List<PushClusterToken> clusterTokens = pushClusterQueryService.findTokensByCluster(cluster);

        if (clusterTokens.isEmpty()) {
            log.info("푸시 전송 스킵 - PushClusterID: {} 알림 대상 토큰이 없습니다.", cluster.getId());
            fcmPushResultService.skipWithNoTargets(cluster);
            return;
        }

        fcmPushResultService.markAsInProgressAndSave(cluster);

        Map<Long, Long> unreadCountMap = notificationPushService.countUnreadByCommand(command);

        List<List<PushClusterToken>> batches = splitIntoBatches(clusterTokens, 400);
        for (List<PushClusterToken> batch : batches) {
            fcmPushResultService.markBatchAsSendingAndSave(batch);

            List<Message> messages = fcmPushService.buildMessages(cluster.getId(), batch, command, unreadCountMap);
            fcmPushService.sendBatchAsync(messages, new ApiFutureCallback<>() {
                @Override
                public void onSuccess(BatchResponse response) {
                    fcmPushResultService.processPushResult(cluster.getId(), batch, response);
                }

                @Override
                public void onFailure(Throwable t) {
                    log.error("FCM 알림 전송 실패 - pushClusterId={}", cluster.getId(), t);
                    fcmPushResultService.markBatchAsFailAndSave(cluster.getId(), batch);
                }
            });
        }
    }

    // <------- TEST --------->
    public void dispatchSync(List<PushClusterSendRequest> sendRequests) {
        sendRequests.forEach(req -> sendToClusterSync(req.fcmMessageCommand(), req.pushClusterId()));
    }

    private void sendToClusterSync(FcmMessageCommand command, Long pushClusterId) {
        PushCluster cluster = pushClusterQueryService.findById(pushClusterId);
        List<PushClusterToken> clusterTokens = pushClusterQueryService.findTokensByCluster(cluster);

        if (clusterTokens.isEmpty()) {
            log.info("푸시 전송 스킵 - PushClusterID: {}", cluster.getId());
            fcmPushResultService.skipWithNoTargets(cluster);
            return;
        }

        fcmPushResultService.markAsInProgressAndSave(cluster);

        Map<Long, Long> unreadCountMap = notificationPushService.countUnreadByCommand(command);

        List<List<PushClusterToken>> batches = splitIntoBatches(clusterTokens, 400);
        for (List<PushClusterToken> batch : batches) {
            fcmPushResultService.markBatchAsSendingAndSave(batch);

            List<Message> messages = fcmPushService.buildMessages(cluster.getId(), batch, command, unreadCountMap);
            try {
                BatchResponse response = FirebaseMessaging.getInstance().sendEach(messages);
                fcmPushResultService.processPushResult(cluster.getId(), batch, response);
            } catch (FirebaseMessagingException e) {
                log.error("FCM 동기 전송 실패 - pushClusterId={}", cluster.getId(), e);
                fcmPushResultService.markBatchAsFailAndSave(cluster.getId(), batch);
            }
        }
    }

    private <T> List<List<T>> splitIntoBatches(List<T> items, int batchSize) {
        List<List<T>> batches = new ArrayList<>();
        for (int i = 0; i < items.size(); i += batchSize) {
            batches.add(items.subList(i, Math.min(i + batchSize, items.size())));
        }
        return batches;
    }
}
