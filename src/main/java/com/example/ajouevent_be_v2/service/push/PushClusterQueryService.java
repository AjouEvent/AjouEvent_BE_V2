package com.example.ajouevent_be_v2.service.push;

import com.example.ajouevent_be_v2.common.exception.push.PushErrorCode;
import com.example.ajouevent_be_v2.common.exception.push.PushException;
import com.example.ajouevent_be_v2.domain.push.PushCluster;
import com.example.ajouevent_be_v2.domain.push.PushClusterToken;
import com.example.ajouevent_be_v2.repository.port.push.PushClusterRepositoryPort;
import com.example.ajouevent_be_v2.repository.port.push.PushClusterTokenRepositoryPort;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PushClusterQueryService {

    private final PushClusterRepositoryPort pushClusterRepositoryPort;
    private final PushClusterTokenRepositoryPort pushClusterTokenRepositoryPort;

    public PushCluster findById(Long pushClusterId) {
        return pushClusterRepositoryPort.findById(pushClusterId)
            .orElseThrow(() -> new PushException(PushErrorCode.PUSH_CLUSTER_NOT_FOUND));
    }

    public List<PushClusterToken> findTokensByCluster(PushCluster cluster) {
        return pushClusterTokenRepositoryPort.findAllByPushClusterWithMember(cluster);
    }
}
