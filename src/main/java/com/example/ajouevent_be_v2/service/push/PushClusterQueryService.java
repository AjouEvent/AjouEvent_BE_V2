package com.example.ajouevent_be_v2.service.push;

import java.time.LocalDateTime;
import java.util.List;

import com.example.ajouevent_be_v2.common.exception.push.PushErrorCode;
import com.example.ajouevent_be_v2.common.exception.push.PushException;
import com.example.ajouevent_be_v2.config.properties.PushProperties;
import com.example.ajouevent_be_v2.domain.push.PushCluster;
import com.example.ajouevent_be_v2.domain.push.PushClusterToken;
import com.example.ajouevent_be_v2.repository.port.push.PushClusterRepositoryPort;
import com.example.ajouevent_be_v2.repository.port.push.PushClusterTokenRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PushClusterQueryService {

    private final PushClusterRepositoryPort pushClusterRepositoryPort;
    private final PushClusterTokenRepositoryPort pushClusterTokenRepositoryPort;
    private final PushProperties pushProperties;

    public PushCluster findById(Long pushClusterId) {
        return pushClusterRepositoryPort.findById(pushClusterId)
            .orElseThrow(() -> new PushException(PushErrorCode.PUSH_CLUSTER_NOT_FOUND));
    }

    public List<PushClusterToken> findTokensByCluster(PushCluster cluster) {
        return pushClusterTokenRepositoryPort.findAllByPushClusterWithMember(cluster);
    }

    public List<PushClusterToken> findRecoverableTokens() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime staleThreshold = now.minusMinutes(pushProperties.getStaleThresholdMinutes());
        return pushClusterTokenRepositoryPort.findRecoverableTokens(staleThreshold, now, pushProperties.getMaxRetryCount());
    }
}
