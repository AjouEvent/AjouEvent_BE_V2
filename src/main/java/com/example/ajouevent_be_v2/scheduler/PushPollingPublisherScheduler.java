package com.example.ajouevent_be_v2.scheduler;

import com.example.ajouevent_be_v2.domain.push.PushCluster;
import com.example.ajouevent_be_v2.domain.push.PushClusterToken;

import com.example.ajouevent_be_v2.orchestrator.FcmOrchestrator;
import com.example.ajouevent_be_v2.service.push.PushClusterQueryService;
import com.example.ajouevent_be_v2.service.webhook.FcmPushResultService;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PushPollingPublisherScheduler {

    private final FcmOrchestrator fcmOrchestrator;
    private final PushClusterQueryService pushClusterQueryService;
    private final FcmPushResultService fcmPushResultService;

    @Scheduled(cron = "0 */5 9-21 * * MON-FRI")
    @SchedulerLock(name = "pushPollingPublisher")
    public void run() {
        log.info("PushPollingPublisher 실행 시작");
        recoverAndRetry();
        log.info("PushPollingPublisher 실행 완료");
    }

    // PENDING(stale) / IN_PROGRESS(stale) / RETRY_PENDING(ready) 토큰을 단일 쿼리로 조회한 뒤
    // stale 토큰은 RETRY_PENDING으로 전환하고, 전체를 클러스터별로 즉시 재발송한다.
    private void recoverAndRetry() {
        List<PushClusterToken> recoverableTokens = pushClusterQueryService.findRecoverableTokens();
        List<PushClusterToken> tokens = fcmPushResultService.recoverStaleTokens(recoverableTokens);

        if (tokens.isEmpty()) {
            return;
        }

        Map<PushCluster, List<PushClusterToken>> tokensByCluster = tokens.stream()
            .collect(Collectors.groupingBy(PushClusterToken::getPushCluster));

        log.info("복구 및 재발송 시작 - {}건 ({}개 클러스터)", tokens.size(), tokensByCluster.size());

        tokensByCluster.forEach((cluster, clusterTokens) -> {
            try {
                fcmOrchestrator.dispatchRetryTokens(cluster, clusterTokens);
            } catch (Exception e) {
                log.error("복구 발송 중 오류 - clusterId={}", cluster.getId(), e);
            }
        });
    }
}
