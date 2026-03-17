package com.example.ajouevent_be_v2.service.webhook;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;

import com.example.ajouevent_be_v2.domain.push.PushClusterToken;
import com.example.ajouevent_be_v2.dto.push.FcmMessageCommand;
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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class FcmService {

    private final Executor fcmCallbackExecutor;

    public void sendBatchAsync(List<Message> messages, ApiFutureCallback<BatchResponse> callback) {
        ApiFutures.addCallback(
            FirebaseMessaging.getInstance().sendEachAsync(messages),
            callback,
            fcmCallbackExecutor
        );
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
