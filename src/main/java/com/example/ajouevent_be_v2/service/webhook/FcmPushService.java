package com.example.ajouevent_be_v2.service.webhook;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import com.example.ajouevent_be_v2.config.properties.FcmExecutorProperties;
import com.example.ajouevent_be_v2.domain.push.PushClusterToken;
import com.example.ajouevent_be_v2.dto.push.FcmMessageCommand;
import com.google.api.core.ApiFuture;
import com.google.api.core.ApiFutureCallback;
import com.google.api.core.ApiFutures;
import com.google.firebase.messaging.BatchResponse;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.MessagingErrorCode;
import com.google.firebase.messaging.MulticastMessage;
import com.google.firebase.messaging.Notification;
import com.google.firebase.messaging.SendResponse;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class FcmPushService {

    private final Executor fcmCallbackExecutor;
    // 전송 제출부터 콜백의 DB 반영 종료까지를 하나의 처리 단위로 보고, 동시에 처리 중인 배치 수를 제한한다.
    // 상한에 닿으면 제출 스레드가 대기하므로 전송 스레드 풀의 큐가 넘치지 않는다 (백프레셔).
    private final Semaphore inFlightBatches;
    private final long acquireTimeoutSeconds;

    /**
     * 허가를 얻은 뒤 만들어지는 발송 단위. 메시지 순서와 콜백이 참조하는 토큰 순서가 같아야 한다.
     */
    public record PreparedBatch(List<Message> messages, ApiFutureCallback<BatchResponse> callback) {
    }

    public FcmPushService(
        @Qualifier("fcmCallbackExecutor") Executor fcmCallbackExecutor,
        FcmExecutorProperties fcmExecutorProperties,
        MeterRegistry meterRegistry
    ) {
        this.fcmCallbackExecutor = fcmCallbackExecutor;
        int maxInFlightBatches = fcmExecutorProperties.getMaxInFlightBatches();
        this.inFlightBatches = new Semaphore(maxInFlightBatches);
        this.acquireTimeoutSeconds = fcmExecutorProperties.getAcquireTimeoutSeconds();
        Gauge.builder("fcm.batches.in_flight", inFlightBatches, s -> maxInFlightBatches - s.availablePermits())
            .description("전송 제출 후 콜백 DB 반영이 끝나지 않은 FCM 배치 수")
            .register(meterRegistry);
    }

    /**
     * 처리 중 배치 수가 상한에 닿아 있으면 허가가 날 때까지 호출 스레드를 대기시킨 뒤 발송한다.
     * prepare 는 허가를 얻은 직후 실행되어 토큰 선점(IN_PROGRESS)과 메시지 생성을 수행한다.
     * prepare 가 null 을 반환하면(선점된 토큰 없음) 발송하지 않는다.
     * 허가는 콜백이 끝난 뒤 반납되며, 전송을 제출하기 전에 예외가 나도 반납된다.
     *
     * @return 허가 대기 시간(acquire-timeout-seconds)을 넘기면 false. 이때는 아무것도 선점·발송하지 않았다.
     */
    public boolean sendBatchAsync(Supplier<PreparedBatch> prepare) throws InterruptedException {
        if (!inFlightBatches.tryAcquire(acquireTimeoutSeconds, TimeUnit.SECONDS)) {
            return false;
        }
        // 콜백 등록까지 마치면 허가 반납 책임이 콜백으로 넘어간다. 그 전에 어떤 이유로든 빠져나가면 여기서 반납한다.
        boolean permitHandedOver = false;
        PreparedBatch batch = null;
        try {
            batch = prepare.get();
            if (batch == null || batch.messages().isEmpty()) {
                return true;
            }
            ApiFuture<BatchResponse> future = FirebaseMessaging.getInstance().sendEachAsync(batch.messages());
            ApiFutures.addCallback(future, releasingPermit(batch.callback()), fcmCallbackExecutor);
            permitHandedOver = true;
            return true;
        } catch (RejectedExecutionException e) {
            // 풀 용량은 세마포어 상한을 수용하도록 기동 시 검증되므로 정상 상황에서는 도달하지 않는다.
            // 제출 도중 거절되면 콜백이 등록되지 않으므로 직접 실패 처리해 재시도 흐름에 합류시킨다.
            log.error("FCM 전송 스레드 풀 거절 - 배치를 재시도 대기로 전환합니다", e);
            if (batch != null) {
                batch.callback().onFailure(e);
            }
            return true;
        } finally {
            if (!permitHandedOver) {
                inFlightBatches.release();
            }
        }
    }

    private ApiFutureCallback<BatchResponse> releasingPermit(ApiFutureCallback<BatchResponse> callback) {
        return new ApiFutureCallback<>() {
            @Override
            public void onSuccess(BatchResponse response) {
                try {
                    callback.onSuccess(response);
                } finally {
                    inFlightBatches.release();
                }
            }

            @Override
            public void onFailure(Throwable t) {
                try {
                    callback.onFailure(t);
                } finally {
                    inFlightBatches.release();
                }
            }
        };
    }

    public List<Message> buildMessages(
        Long clusterId,
        List<PushClusterToken> tokens,
        FcmMessageCommand command,
        Map<Long, Long> unreadCountMap
    ) {
        return tokens.stream()
            .map(token -> buildMessage(clusterId, token,
                command.title(), command.body(), command.imageUrl(), command.clickUrl(),
                unreadCountMap))
            .toList();
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
                SendResponse sendResponse = response.getResponses().get(i);
                if (!sendResponse.isSuccessful() && isInvalidTokenError(sendResponse)) {
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

    private List<String> retryValidation(List<String> tokenValues) {
        List<String> invalidTokens = new ArrayList<>();
        for (String tokenValue : tokenValues) {
            try {
                Message singleMessage = Message.builder().setToken(tokenValue).build();
                FirebaseMessaging.getInstance().send(singleMessage, true);
            } catch (FirebaseMessagingException ex) {
                if (isInvalidTokenError(ex.getMessagingErrorCode())) {
                    log.warn("재시도 중 유효하지 않은 토큰: {}", tokenValue);
                    invalidTokens.add(tokenValue);
                }
            }
        }
        return invalidTokens;
    }

    private boolean isInvalidTokenError(SendResponse sendResponse) {
        if (sendResponse.getException() == null) {
            return false;
        }
        return isInvalidTokenError(sendResponse.getException().getMessagingErrorCode());
    }

    private boolean isInvalidTokenError(MessagingErrorCode errorCode) {
        return errorCode == MessagingErrorCode.UNREGISTERED
            || errorCode == MessagingErrorCode.INVALID_ARGUMENT;
    }
}
