package com.example.ajouevent_be_v2.service.webhook;

import java.util.ArrayList;
import java.util.List;

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
                pushClusterToken.markAsFail();
                failCount++;
                if (isInvalidTokenError(sendResponse)) {
                    invalidTokenValues.add(pushClusterToken.getTokenValue());
                }
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

    private boolean isInvalidTokenError(SendResponse sendResponse) {
        if (sendResponse.getException() == null) {
            return false;
        }
        MessagingErrorCode errorCode = sendResponse.getException().getMessagingErrorCode();
        return errorCode == MessagingErrorCode.UNREGISTERED
            || errorCode == MessagingErrorCode.INVALID_ARGUMENT;
    }

    private void updatePushClusterTokens(List<PushClusterToken> clusterTokens) {
        pushClusterTokenRepositoryPort.bulkUpdateAll(clusterTokens);
    }
}
