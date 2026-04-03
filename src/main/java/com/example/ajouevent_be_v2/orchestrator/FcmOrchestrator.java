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

    /**
     * 즉시 전송용 — 컨트롤러에서 전달받은 FcmMessageCommand와 PushClusterId로 바로 발송한다.
     */
    public void dispatchClusters(List<PushClusterSendRequest> sendRequests) {
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
            sendFcmBatch(cluster, batch, messages);
        }
    }

    /**
     * Polling Publisher 전용 — RETRY_PENDING 토큰들에 대해 재발송한다.
     * 클러스터 상태는 변경하지 않고, 전달받은 토큰만 배치 처리한다.
     */
    public void dispatchRetryTokens(PushCluster cluster, List<PushClusterToken> retryTokens) {
        if (retryTokens.isEmpty()) {
            return;
        }

        FcmMessageCommand command = notificationPushService.buildCommandFromCluster(cluster);
        Map<Long, Long> unreadCountMap = notificationPushService.countUnreadByCommand(command);

        List<List<PushClusterToken>> batches = splitIntoBatches(retryTokens, 400);
        for (List<PushClusterToken> batch : batches) {
            fcmPushResultService.markBatchAsSendingAndSave(batch);
            List<Message> messages = fcmPushService.buildMessages(cluster.getId(), batch, command, unreadCountMap);
            sendRetryFcmBatch(cluster, batch, messages);
        }
    }

    // 초기 발송 콜백 — 결과를 successCount/failCount에 반영한다.
    private void sendFcmBatch(PushCluster cluster, List<PushClusterToken> batch, List<Message> messages) {
        fcmPushService.sendBatchAsync(messages, new ApiFutureCallback<>() {
            @Override
            public void onSuccess(BatchResponse response) {
                fcmPushResultService.processPushResult(cluster.getId(), batch, response);
            }

            @Override
            public void onFailure(Throwable t) {
                log.error("FCM 발송 실패 - pushClusterId={}", cluster.getId(), t);
                fcmPushResultService.markBatchAsRetryPendingAndSave(batch);
            }
        });
    }

    // 재시도 콜백 — 결과를 retrySuccessCount/retryFailCount에 반영한다.
    private void sendRetryFcmBatch(PushCluster cluster, List<PushClusterToken> batch, List<Message> messages) {
        fcmPushService.sendBatchAsync(messages, new ApiFutureCallback<>() {
            @Override
            public void onSuccess(BatchResponse response) {
                fcmPushResultService.processRetryPushResult(cluster.getId(), batch, response);
            }

            @Override
            public void onFailure(Throwable t) {
                log.error("FCM 재시도 발송 실패 - pushClusterId={}", cluster.getId(), t);
                fcmPushResultService.markBatchAsRetryPendingAndSave(batch);
            }
        });
    }

    private <T> List<List<T>> splitIntoBatches(List<T> items, int batchSize) {
        List<List<T>> batches = new ArrayList<>();
        for (int i = 0; i < items.size(); i += batchSize) {
            batches.add(items.subList(i, Math.min(i + batchSize, items.size())));
        }
        return batches;
    }
}
