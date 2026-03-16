package com.example.ajouevent_be_v2.service.push;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

import com.example.ajouevent_be_v2.common.exception.push.PushErrorCode;
import com.example.ajouevent_be_v2.common.exception.push.PushException;
import com.example.ajouevent_be_v2.domain.push.PushCluster;
import com.example.ajouevent_be_v2.domain.push.PushClusterToken;
import com.example.ajouevent_be_v2.dto.push.FcmMessageCommand;
import com.example.ajouevent_be_v2.dto.push.UnreadNotificationCountResult;
import com.example.ajouevent_be_v2.repository.port.notification.PushNotificationRepositoryPort;
import com.example.ajouevent_be_v2.repository.port.push.PushClusterRepositoryPort;
import com.example.ajouevent_be_v2.repository.port.push.PushClusterTokenRepositoryPort;
import com.google.api.core.ApiFutureCallback;
import com.google.api.core.ApiFutures;
import com.google.firebase.messaging.BatchResponse;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.MulticastMessage;
import com.google.firebase.messaging.Notification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class FcmService {

    private final PushClusterRepositoryPort pushClusterRepositoryPort;
    private final PushClusterTokenRepositoryPort pushClusterTokenRepositoryPort;
    private final PushNotificationRepositoryPort pushNotificationRepositoryPort;
    private final PushResultService pushResultService;
    private final Executor fcmCallbackExecutor;

    /**
     * PushCluster에 속한 모든 토큰에 FCM 메시지를 전송한다.
     * koreanTopic 또는 encodedKeyword를 기준으로 각 멤버의 읽지 않은 알림 수를 조회한다.
     */
    public void sendFcmToPushCluster(FcmMessageCommand command, Long pushClusterId) {
        PushCluster cluster = pushClusterRepositoryPort.findById(pushClusterId)
            .orElseThrow(() -> new PushException(PushErrorCode.PUSH_CLUSTER_NOT_FOUND));

        List<PushClusterToken> clusterTokens =
            pushClusterTokenRepositoryPort.findAllByPushClusterWithMember(cluster);

        sendFcmPush(
            clusterTokens, cluster,
            command.title(), command.body(), command.imageUrl(), command.clickUrl(),
            resolveUnreadCountMap(command)
        );
    }

    /**
     * FCM 푸시알림 전송
     */
    private void sendFcmPush(
        List<PushClusterToken> clusterTokens,
        PushCluster pushCluster,
        String title,
        String body,
        String imageUrl,
        String clickUrl,
        Map<Long, Long> unreadCountMap
    ) {
        if (clusterTokens.isEmpty()) {
            log.info("푸시 전송 스킵 - PushClusterID: {} 알림 대상 토큰이 없습니다.", pushCluster.getId());
            pushResultService.skipWithNoTargets(pushCluster);
            return;
        }

        pushResultService.markAsInProgressAndSave(pushCluster);

        List<List<PushClusterToken>> batches = splitIntoBatches(clusterTokens, 400);

        for (List<PushClusterToken> batch : batches) {
            pushResultService.markBatchAsSendingAndSave(batch);

            List<Message> messages = batch.stream()
                .map(token -> buildMessage(pushCluster.getId(), token, title, body, imageUrl, clickUrl, unreadCountMap))
                .toList();

            ApiFutures.addCallback(
                FirebaseMessaging.getInstance().sendEachAsync(messages),
                new ApiFutureCallback<>() {
                    @Override
                    public void onSuccess(BatchResponse response) {
                        pushResultService.processPushResult(pushCluster.getId(), batch, response);
                    }

                    @Override
                    public void onFailure(Throwable t) {
                        log.error("FCM 알림 전송 실패 - pushClusterId={}", pushCluster.getId(), t);
                        pushResultService.markBatchAsFailAndSave(batch);
                    }
                },
                fcmCallbackExecutor
            );
        }
    }

    public List<String> validateTokens(List<String> tokenValues) {
        if (tokenValues.isEmpty()) {
            return List.of();
        }

        MulticastMessage message = MulticastMessage.builder()
            .addAllTokens(tokenValues)
            .build();

        try {
            BatchResponse response = FirebaseMessaging.getInstance().sendEachForMulticast(message, true);

            List<String> invalidTokens = new ArrayList<>();
            for (int i = 0; i < response.getResponses().size(); i++) {
                if (!response.getResponses().get(i).isSuccessful()) {
                    invalidTokens.add(tokenValues.get(i));
                }
            }
            return invalidTokens;
        } catch (FirebaseMessagingException e) {
            log.error("FCM 토큰 유효성 검사 중 오류 발생", e);
            List<String> invalidTokens = retryValidation(tokenValues);
            log.info("재시도 후 유효하지 않은 토큰 수: {}", invalidTokens.size());
            return invalidTokens;
        }
    }

    private Map<Long, Long> resolveUnreadCountMap(FcmMessageCommand command) {
        List<UnreadNotificationCountResult> counts = command.koreanTopic() != null
            ? pushNotificationRepositoryPort.countUnreadNotificationsForTopic(command.koreanTopic())
            : pushNotificationRepositoryPort.countUnreadNotificationsForKeyword(command.encodedKeyword());

        return counts.stream()
            .collect(Collectors.toMap(
                UnreadNotificationCountResult::memberId,
                UnreadNotificationCountResult::unreadNotificationCount));
    }

    private List<String> retryValidation(List<String> tokenValues) {
        List<String> invalidTokens = new ArrayList<>();
        for (String tokenValue : tokenValues) {
            try {
                Message singleMessage = Message.builder().setToken(tokenValue).build();
                FirebaseMessaging.getInstance().send(singleMessage, true);
            } catch (FirebaseMessagingException ex) {
                log.warn("재시도 중 실패한 토큰: {}", tokenValue);
                invalidTokens.add(tokenValue);
            }
        }
        return invalidTokens;
    }

    private Message buildMessage(
        Long pushClusterId,
        PushClusterToken token,
        String title,
        String body,
        String imageUrl,
        String clickUrl,
        Map<Long, Long> unreadCountMap
    ) {
        Long unreadCount = unreadCountMap.getOrDefault(token.getMember().getId(), 0L);

        return Message.builder()
            .setToken(token.getTokenValue())
            .setNotification(Notification.builder()
                .setTitle(title)
                .setBody(body)
                .setImage(imageUrl)
                .build())
            .putData("click_action", clickUrl)
            .putData("push_cluster_id", String.valueOf(pushClusterId))
            .putData("unread_count", String.valueOf(unreadCount))
            .build();
    }

    private <T> List<List<T>> splitIntoBatches(List<T> items, int batchSize) {
        List<List<T>> batches = new ArrayList<>();
        for (int i = 0; i < items.size(); i += batchSize) {
            int end = Math.min(i + batchSize, items.size());
            batches.add(items.subList(i, end));
        }
        return batches;
    }
}
