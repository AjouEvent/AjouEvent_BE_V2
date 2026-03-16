package com.example.ajouevent_be_v2.service.push;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.example.ajouevent_be_v2.common.exception.push.PushErrorCode;
import com.example.ajouevent_be_v2.common.exception.push.PushException;
import com.example.ajouevent_be_v2.domain.member.Token;
import com.example.ajouevent_be_v2.domain.push.PushCluster;
import com.example.ajouevent_be_v2.domain.push.PushClusterToken;
import com.example.ajouevent_be_v2.repository.port.push.PushClusterRepositoryPort;
import com.example.ajouevent_be_v2.repository.port.push.PushClusterTokenRepositoryPort;
import com.example.ajouevent_be_v2.service.token.TokenService;
import com.google.firebase.messaging.BatchResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PushResultService {

    private final PushClusterRepositoryPort pushClusterRepositoryPort;
    private final PushClusterTokenRepositoryPort pushClusterTokenRepositoryPort;
    private final TokenService tokenService;

    @Transactional
    public void markAsInProgressAndSave(PushCluster pushCluster) {
        pushCluster.markAsInProgress();
        pushClusterRepositoryPort.save(pushCluster);
    }

    @Transactional
    public void skipWithNoTargets(PushCluster pushCluster) {
        pushCluster.updateCountsAndStatus(0, 0);
        pushClusterRepositoryPort.save(pushCluster);
    }

    @Transactional
    public void processPushResult(Long pushClusterId, List<PushClusterToken> clusterTokens, BatchResponse response) {
        PushCluster pushCluster = pushClusterRepositoryPort.findById(pushClusterId)
            .orElseThrow(() -> new PushException(PushErrorCode.PUSH_CLUSTER_NOT_FOUND));

        int successCount = 0;
        int failCount = 0;
        List<Token> tokensToSoftDelete = new ArrayList<>();

        for (int i = 0; i < clusterTokens.size(); i++) {
            PushClusterToken pushClusterToken = clusterTokens.get(i);
            if (response.getResponses().get(i).isSuccessful()) {
                pushClusterToken.markAsSuccess();
                successCount++;
            } else {
                pushClusterToken.markAsFail();
                failCount++;
                Optional<Token> token = tokenService.findByTokenValueAndMember(
                    pushClusterToken.getTokenValue(), pushClusterToken.getMember());
                token.ifPresent(tokensToSoftDelete::add);
            }
        }

        updatePushClusterTokens(clusterTokens);

        if (!tokensToSoftDelete.isEmpty()) {
            tokenService.softDeleteInvalidTokens(tokensToSoftDelete);
        }

        pushCluster.updateCountsAndStatus(successCount, failCount);
        pushClusterRepositoryPort.save(pushCluster);
        log.info("푸시 완료 - PushClusterID: {} 성공: {} 실패: {}", pushClusterId, successCount, failCount);
    }

    @Transactional
    public void markBatchAsSendingAndSave(List<PushClusterToken> batch) {
        batch.forEach(PushClusterToken::markAsSending);
        pushClusterTokenRepositoryPort.bulkSaveAll(batch);
    }

    @Transactional
    public void markBatchAsFailAndSave(List<PushClusterToken> batch) {
        batch.forEach(PushClusterToken::markAsFail);
        updatePushClusterTokens(batch);
    }

    private void updatePushClusterTokens(List<PushClusterToken> clusterTokens) {
        pushClusterTokenRepositoryPort.saveAll(clusterTokens);
        pushClusterTokenRepositoryPort.bulkUpdateAll(clusterTokens);
    }
}
