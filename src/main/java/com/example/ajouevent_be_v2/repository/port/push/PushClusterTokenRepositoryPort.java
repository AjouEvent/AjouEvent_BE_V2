package com.example.ajouevent_be_v2.repository.port.push;

import java.time.LocalDateTime;
import java.util.List;

import com.example.ajouevent_be_v2.domain.clubevent.JobStatus;
import com.example.ajouevent_be_v2.domain.push.PushCluster;
import com.example.ajouevent_be_v2.domain.push.PushClusterToken;
import com.example.ajouevent_be_v2.repository.adapter.push.PushClusterTokenBulkRepositoryAdapter;
import com.example.ajouevent_be_v2.repository.adapter.push.PushClusterTokenJpaRepositoryAdapter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class PushClusterTokenRepositoryPort {

    private final PushClusterTokenJpaRepositoryAdapter pushClusterTokenJpaRepositoryAdapter;
    private final PushClusterTokenBulkRepositoryAdapter pushClusterTokenBulkRepositoryAdapter;

    public List<PushClusterToken> findAllByPushClusterWithMember(PushCluster pushCluster) {
        return pushClusterTokenJpaRepositoryAdapter.findAllByPushClusterWithMember(pushCluster);
    }

    public List<PushClusterToken> saveAll(List<PushClusterToken> clusterTokens) {
        return pushClusterTokenJpaRepositoryAdapter.saveAll(clusterTokens);
    }

    public void bulkSaveAll(List<PushClusterToken> clusterTokens) {
        pushClusterTokenBulkRepositoryAdapter.saveAll(clusterTokens);
    }

    public void bulkUpdateAll(List<PushClusterToken> clusterTokens) {
        pushClusterTokenBulkRepositoryAdapter.updateAll(clusterTokens);
    }

    public List<PushClusterToken> findRetryPendingTokensReady(LocalDateTime now, int maxRetryCount) {
        return pushClusterTokenJpaRepositoryAdapter.findRetryPendingTokensReady(
            JobStatus.RETRY_PENDING, now, maxRetryCount);
    }

    public List<PushClusterToken> findStaleInProgressTokens(LocalDateTime threshold) {
        return pushClusterTokenJpaRepositoryAdapter.findStaleInProgressTokens(
            JobStatus.IN_PROGRESS, threshold);
    }
}
