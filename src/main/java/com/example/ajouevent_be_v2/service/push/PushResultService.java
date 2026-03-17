package com.example.ajouevent_be_v2.service.push;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.example.ajouevent_be_v2.domain.member.Token;
import com.example.ajouevent_be_v2.domain.push.PushCluster;
import com.example.ajouevent_be_v2.domain.push.PushClusterToken;
import com.example.ajouevent_be_v2.repository.port.push.PushClusterRepositoryPort;
import com.example.ajouevent_be_v2.repository.port.push.PushClusterTokenRepositoryPort;
import com.example.ajouevent_be_v2.repository.port.token.TokenRepositoryPort;
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
        List<Token> tokensToSoftDelete = new ArrayList<>();

        for (int i = 0; i < clusterTokens.size(); i++) {
            PushClusterToken pushClusterToken = clusterTokens.get(i);
            if (response.getResponses().get(i).isSuccessful()) {
                pushClusterToken.markAsSuccess();
                successCount++;
            } else {
                pushClusterToken.markAsFail();
                failCount++;
                Optional<Token> token = tokenRepositoryPort.findByTokenValueAndMember(
                    pushClusterToken.getTokenValue(), pushClusterToken.getMember());
                token.ifPresent(tokensToSoftDelete::add);
            }
        }

        updatePushClusterTokens(clusterTokens);

        if (!tokensToSoftDelete.isEmpty()) {
            tokensToSoftDelete.forEach(Token::markAsDeleted);
            tokenRepositoryPort.batchSoftDeleteTokens(tokensToSoftDelete);
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
    public void markBatchAsFailAndSave(List<PushClusterToken> batch) {
        batch.forEach(PushClusterToken::markAsFail);
        updatePushClusterTokens(batch);
    }

    private void updatePushClusterTokens(List<PushClusterToken> clusterTokens) {
        pushClusterTokenRepositoryPort.bulkUpdateAll(clusterTokens);
    }
}
