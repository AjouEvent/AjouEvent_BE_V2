package com.example.ajouevent_be_v2.service.webhook;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.example.ajouevent_be_v2.common.discord.DiscordMessageService;
import com.example.ajouevent_be_v2.config.properties.PushProperties;
import com.example.ajouevent_be_v2.domain.push.PushCluster;
import com.example.ajouevent_be_v2.domain.push.PushClusterToken;
import com.example.ajouevent_be_v2.repository.port.push.PushClusterRepositoryPort;
import com.example.ajouevent_be_v2.repository.port.push.PushClusterTokenRepositoryPort;
import com.example.ajouevent_be_v2.repository.port.token.TokenRepositoryPort;
import com.google.firebase.messaging.BatchResponse;
import com.google.firebase.messaging.MessagingErrorCode;
import com.google.firebase.messaging.SendResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class FcmPushResultService {

    private final PushClusterRepositoryPort pushClusterRepositoryPort;
    private final PushClusterTokenRepositoryPort pushClusterTokenRepositoryPort;
    private final TokenRepositoryPort tokenRepositoryPort;
    private final DiscordMessageService discordMessageService;
    private final PushProperties pushProperties;

    @Transactional
    public void markAsInProgressAndSave(PushCluster pushCluster) {
        pushCluster.markAsInProgress();
        pushClusterRepositoryPort.save(pushCluster);
    }

    @Transactional
    public void skipWithNoTargets(PushCluster pushCluster) {
        pushClusterRepositoryPort.incrementCountsAndUpdateStatus(pushCluster.getId(), 0, 0);
    }

    @Transactional
    public void processPushResult(Long pushClusterId, List<PushClusterToken> clusterTokens, BatchResponse response) {
        int successCount = 0;
        int failCount = 0;
        List<String> invalidTokenValues = new ArrayList<>();

        for (int i = 0; i < clusterTokens.size(); i++) {
            PushClusterToken pushClusterToken = clusterTokens.get(i);
            SendResponse sendResponse = response.getResponses().get(i);

            if (sendResponse.isSuccessful()) {
                pushClusterToken.markAsSuccess();
                successCount++;
            } else {
                MessagingErrorCode errorCode = sendResponse.getException().getMessagingErrorCode();
                failCount += handleFailedToken(pushClusterToken, errorCode, invalidTokenValues);
            }
        }

        updatePushClusterTokens(clusterTokens);

        if (!invalidTokenValues.isEmpty()) {
            tokenRepositoryPort.batchSoftDeleteByTokenValues(invalidTokenValues);
        }

        pushClusterRepositoryPort.incrementCountsAndUpdateStatus(pushClusterId, successCount, failCount);
        log.info("푸시 완료 - PushClusterID: {} 성공: {} 실패: {}", pushClusterId, successCount, failCount);
    }

    @Transactional
    public void markBatchAsSendingAndSave(List<PushClusterToken> batch) {
        batch.forEach(PushClusterToken::markAsSending);
        pushClusterTokenRepositoryPort.bulkUpdateAll(batch);
    }

    @Transactional
    public void markBatchAsFailAndSave(Long pushClusterId, List<PushClusterToken> batch) {
        batch.forEach(PushClusterToken::markAsFail);
        updatePushClusterTokens(batch);
        pushClusterRepositoryPort.incrementCountsAndUpdateStatus(pushClusterId, 0, batch.size());
    }

    @Transactional
    public void recoverStaleTokens(List<PushClusterToken> staleTokens) {
        if (staleTokens.isEmpty()) {
            return;
        }
        staleTokens.forEach(PushClusterToken::markAsStaleRecovered);
        pushClusterTokenRepositoryPort.bulkUpdateAll(staleTokens);
        log.info("Stale IN_PROGRESS 토큰 RETRY_PENDING 복구 - {}건", staleTokens.size());
    }

    /**
     * 에러 코드에 따라 토큰 상태를 결정한다.
     * @return failCount에 더할 값 (최종 실패=1, 재시도 예정=0)
     */
    private int handleFailedToken(
        PushClusterToken token,
        MessagingErrorCode errorCode,
        List<String> invalidTokenValues
    ) {
        if (errorCode == null) {
            token.markAsFail();
            return 1;
        }

        switch (errorCode) {
            case INTERNAL:
            case UNAVAILABLE:
                markAsRetryPendingOrPermanentFail(token, false);
                return 0;

            case QUOTA_EXCEEDED:
                markAsRetryPendingOrPermanentFail(token, true);
                return 0;

            case UNREGISTERED:
            case INVALID_ARGUMENT:
                token.markAsFail();
                invalidTokenValues.add(token.getTokenValue());
                return 1;

            case SENDER_ID_MISMATCH:
                token.markAsPermanentFail();
                discordMessageService.sendMessage(
                    String.format("[FCM SENDER_ID_MISMATCH] pushClusterId=%d, token=%s",
                        token.getPushCluster().getId(), token.getTokenValue()));
                return 1;

            default:
                token.markAsFail();
                return 1;
        }
    }

    private void markAsRetryPendingOrPermanentFail(PushClusterToken token, boolean quotaExceeded) {
        if (token.getRetryCount() >= pushProperties.getMaxRetryCount()) {
            token.markAsPermanentFail();
            log.warn("최대 재시도 초과 PERMANENT_FAIL - tokenId={}, retryCount={}",
                token.getId(), token.getRetryCount());
            return;
        }
        long delayMinutes = (long) Math.pow(2, token.getRetryCount());
        if (quotaExceeded) {
            delayMinutes *= 2;
        }
        token.markAsRetryPending(LocalDateTime.now().plusMinutes(delayMinutes));
        log.debug("RETRY_PENDING - tokenId={}, retryCount={}, retryAfter={}분 후",
            token.getId(), token.getRetryCount(), delayMinutes);
    }

    private void updatePushClusterTokens(List<PushClusterToken> clusterTokens) {
        pushClusterTokenRepositoryPort.bulkUpdateAll(clusterTokens);
    }
}
