package com.example.ajouevent_be_v2.repository.port.push;

import java.util.Optional;

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
}
