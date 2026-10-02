package com.example.ajouevent_be_v2.orchestrator;

import com.example.ajouevent_be_v2.common.discord.DiscordMessageService;
import com.example.ajouevent_be_v2.domain.clubevent.JobStatus;
import com.example.ajouevent_be_v2.domain.push.PushCluster;
import com.example.ajouevent_be_v2.domain.push.PushClusterToken;
import com.example.ajouevent_be_v2.dto.push.FcmMessageCommand;
import com.example.ajouevent_be_v2.dto.push.PushClusterSendRequest;
import com.example.ajouevent_be_v2.service.notification.NotificationPushService;
import com.example.ajouevent_be_v2.service.push.PushClusterQueryService;
import com.example.ajouevent_be_v2.service.webhook.FcmPushResultService;
import com.example.ajouevent_be_v2.service.webhook.FcmPushService;
import com.example.ajouevent_be_v2.service.webhook.FcmPushService.PreparedBatch;
import com.google.api.core.ApiFutureCallback;
import com.google.firebase.messaging.BatchResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.RejectedExecutionException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class FcmOrchestrator {

    // 배치 크기는 FCM 처리량과 무관하다 (메시지마다 HTTP 요청 1건). DB 트랜잭션·콜백 단위를 정하는 값이다.
    private static final int BATCH_SIZE = 400;

    private final FcmPushService fcmPushService;
    private final PushClusterQueryService pushClusterQueryService;
    private final FcmPushResultService fcmPushResultService;
    private final NotificationPushService notificationPushService;
    private final DiscordMessageService discordMessageService;
    private final Executor fcmDispatchExecutor;

    public FcmOrchestrator(
        FcmPushService fcmPushService,
        PushClusterQueryService pushClusterQueryService,
        FcmPushResultService fcmPushResultService,
        NotificationPushService notificationPushService,
        DiscordMessageService discordMessageService,
        @Qualifier("fcmDispatchExecutor") Executor fcmDispatchExecutor
    ) {
        this.fcmPushService = fcmPushService;
        this.pushClusterQueryService = pushClusterQueryService;
        this.fcmPushResultService = fcmPushResultService;
        this.notificationPushService = notificationPushService;
        this.discordMessageService = discordMessageService;
        this.fcmDispatchExecutor = fcmDispatchExecutor;
    }

    /**
     * 즉시 전송용 — 클러스터 발송을 fcm-dispatch 실행기에 넘기고 바로 반환한다.
     * 세마포어 대기는 dispatch 스레드가 맡으므로 웹훅 응답이 발송량만큼 늦어지지 않는다.
     * 클러스터와 토큰은 호출 전에 PENDING 으로 커밋되어 있으므로, 제출이 거절되거나 서버가 내려가도
     * PushPollingPublisherScheduler 가 stale PENDING 토큰을 복구한다.
     */
    public void dispatchClusters(List<PushClusterSendRequest> sendRequests) {
        for (PushClusterSendRequest req : sendRequests) {
            try {
                fcmDispatchExecutor.execute(() -> sendToClusterSafely(req.fcmMessageCommand(), req.pushClusterId()));
            } catch (RejectedExecutionException e) {
                log.warn("푸시 발송 대기열 초과 - PushClusterID: {} 는 PENDING 으로 남아 폴링 릴레이가 복구합니다.",
                    req.pushClusterId(), e);
                discordMessageService.sendErrorMessage(String.format(
                    "[FCM 발송 대기열 초과] pushClusterId=%d — PENDING 으로 남아 폴링 릴레이 운영 시간에 복구됩니다.",
                    req.pushClusterId()));
            }
        }
    }

    // 클러스터 하나의 실패가 같은 dispatch 스레드의 다음 클러스터 발송을 막지 않도록 격리한다.
    private void sendToClusterSafely(FcmMessageCommand command, Long pushClusterId) {
        try {
            sendToCluster(command, pushClusterId);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("푸시 발송 중단(인터럽트) - PushClusterID: {} 남은 토큰은 폴링 릴레이가 복구합니다.", pushClusterId);
        } catch (Exception e) {
            log.error("푸시 발송 중 오류 - PushClusterID: {}", pushClusterId, e);
            discordMessageService.sendErrorMessage(String.format(
                "[FCM 발송 오류] pushClusterId=%d — 선점하지 못한 토큰은 폴링 릴레이가 복구합니다. (%s)",
                pushClusterId, e.getMessage()));
        }
    }

    private void sendToCluster(FcmMessageCommand command, Long pushClusterId) throws InterruptedException {
        PushCluster cluster = pushClusterQueryService.findById(pushClusterId);
        List<PushClusterToken> clusterTokens = pushClusterQueryService.findTokensByCluster(cluster);

        if (clusterTokens.isEmpty()) {
            log.info("푸시 전송 스킵 - PushClusterID: {} 알림 대상 토큰이 없습니다.", cluster.getId());
            fcmPushResultService.skipWithNoTargets(cluster);
            return;
        }

        fcmPushResultService.markAsInProgressAndSave(cluster);

        Map<Long, Long> unreadCountMap = notificationPushService.countUnreadByCommand(command);
        sendBatches(cluster, clusterTokens, command, unreadCountMap, JobStatus.PENDING, false);
    }

    /**
     * Polling Publisher 전용 — RETRY_PENDING 토큰들에 대해 재발송한다.
     * 클러스터 상태는 변경하지 않고, 전달받은 토큰만 배치 처리한다.
     * 세마포어 대기는 스케줄러 스레드가 맡는다 (ShedLock 을 잡은 동안 처리를 끝내는 기존 방식 유지).
     *
     * @return 허가 대기 초과 또는 인터럽트로 중단되면 false. 호출자는 남은 클러스터 처리를 멈추고 다음 주기에 맡긴다.
     */
    public boolean dispatchRetryTokens(PushCluster cluster, List<PushClusterToken> retryTokens) {
        if (retryTokens.isEmpty()) {
            return true;
        }
        try {
            FcmMessageCommand command = notificationPushService.buildCommandFromCluster(cluster);
            Map<Long, Long> unreadCountMap = notificationPushService.countUnreadByCommand(command);
            return sendBatches(cluster, retryTokens, command, unreadCountMap, JobStatus.RETRY_PENDING, true);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("재시도 발송 중단(인터럽트) - pushClusterId={} 남은 토큰은 다음 주기에 복구합니다.", cluster.getId());
            return false;
        }
    }

    /**
     * 배치마다 발송 허가 → 토큰 선점(expected 상태인 것만 IN_PROGRESS) → 선점한 토큰으로 메시지 생성 → 전송 순서로 처리한다.
     * IN_PROGRESS 표시가 허가를 얻은 뒤에 일어나므로 processedTime 이 실제 전송 시작 시각과 같아지고,
     * 선점 덕분에 대기 중 폴링 릴레이가 먼저 가져간 토큰은 다시 보내지 않는다.
     *
     * @return 허가 대기 시간을 넘겨 중단했으면 false
     */
    private boolean sendBatches(
        PushCluster cluster,
        List<PushClusterToken> tokens,
        FcmMessageCommand command,
        Map<Long, Long> unreadCountMap,
        JobStatus expected,
        boolean retry
    ) throws InterruptedException {
        for (List<PushClusterToken> batch : splitIntoBatches(tokens, BATCH_SIZE)) {
            boolean acquired = fcmPushService.sendBatchAsync(() -> {
                List<PushClusterToken> claimed = fcmPushResultService.claimForSending(batch, expected);
                if (claimed.isEmpty()) {
                    return null;
                }
                return new PreparedBatch(
                    fcmPushService.buildMessages(cluster.getId(), claimed, command, unreadCountMap),
                    retry ? retryCallback(cluster, claimed) : initialCallback(cluster, claimed));
            });
            if (!acquired) {
                // 처리 중 배치가 허가 대기 시간 안에 하나도 끝나지 않았다. 허가 누수 또는 FCM 장애를 의심해야 한다.
                // 남은 배치는 선점하지 않았으므로 상태가 그대로이며, 폴링 릴레이가 복구한다.
                log.error("FCM 발송 허가 대기 시간 초과 - pushClusterId={} 남은 배치 발송을 중단합니다.", cluster.getId());
                discordMessageService.sendErrorMessage(String.format(
                    "[FCM 발송 허가 대기 초과] pushClusterId=%d — 처리 중 배치가 끝나지 않습니다. 허가 누수 또는 FCM 장애를 확인하세요.",
                    cluster.getId()));
                return false;
            }
        }
        return true;
    }

    // 초기 발송 콜백 — 결과를 successCount/failCount에 반영한다.
    private ApiFutureCallback<BatchResponse> initialCallback(PushCluster cluster, List<PushClusterToken> batch) {
        return new ApiFutureCallback<>() {
            @Override
            public void onSuccess(BatchResponse response) {
                fcmPushResultService.processPushResult(cluster.getId(), batch, response);
            }

            @Override
            public void onFailure(Throwable t) {
                log.error("FCM 발송 실패 - pushClusterId={}", cluster.getId(), t);
                fcmPushResultService.markBatchAsRetryPendingAndSave(batch);
            }
        };
    }

    // 재시도 콜백 — 결과를 retrySuccessCount/retryFailCount에 반영한다.
    private ApiFutureCallback<BatchResponse> retryCallback(PushCluster cluster, List<PushClusterToken> batch) {
        return new ApiFutureCallback<>() {
            @Override
            public void onSuccess(BatchResponse response) {
                fcmPushResultService.processRetryPushResult(cluster.getId(), batch, response);
            }

            @Override
            public void onFailure(Throwable t) {
                log.error("FCM 재시도 발송 실패 - pushClusterId={}", cluster.getId(), t);
                fcmPushResultService.markBatchAsRetryPendingAndSave(batch);
            }
        };
    }

    private <T> List<List<T>> splitIntoBatches(List<T> items, int batchSize) {
        List<List<T>> batches = new ArrayList<>();
        for (int i = 0; i < items.size(); i += batchSize) {
            batches.add(items.subList(i, Math.min(i + batchSize, items.size())));
        }
        return batches;
    }
}
