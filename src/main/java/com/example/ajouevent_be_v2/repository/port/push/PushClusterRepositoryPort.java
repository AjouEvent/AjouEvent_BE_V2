package com.example.ajouevent_be_v2.repository.port.push;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import com.example.ajouevent_be_v2.domain.clubevent.JobStatus;
import com.example.ajouevent_be_v2.domain.push.PushCluster;
import com.example.ajouevent_be_v2.repository.adapter.push.PushClusterJpaRepositoryAdapter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class PushClusterRepositoryPort {

    private final PushClusterJpaRepositoryAdapter pushClusterJpaRepositoryAdapter;

    public Optional<PushCluster> findById(Long id) {
        return pushClusterJpaRepositoryAdapter.findById(id);
    }

    public PushCluster save(PushCluster pushCluster) {
        return pushClusterJpaRepositoryAdapter.save(pushCluster);
    }

    public void incrementCountsAndUpdateStatus(Long id, int successDelta, int failDelta) {
        pushClusterJpaRepositoryAdapter.incrementCountsAndUpdateStatus(id, successDelta, failDelta);
    }

    public List<PushCluster> findAllByJobStatus(JobStatus jobStatus) {
        return pushClusterJpaRepositoryAdapter.findAllByJobStatus(jobStatus);
    }

    public List<PushCluster> findAllPendingOlderThan(LocalDateTime threshold) {
        return pushClusterJpaRepositoryAdapter.findAllByJobStatusAndRegisteredAtBefore(
            JobStatus.PENDING, threshold);
    }
}
