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

    @Scheduled(cron = "0 * * * * *")
    @SchedulerLock(name = "pushPollingPublisher")
    public void run() {
        log.info("PushPollingPublisher 실행 시작");
        recoverStaleTokens();
        processPendingClusters();
        processRetryTokens();
        log.info("PushPollingPublisher 실행 완료");
    }

    // 서버 크래시로 IN_PROGRESS 상태에서 멈춘 토큰을 RETRY_PENDING으로 되돌린다.
    private void recoverStaleTokens() {
        List<PushClusterToken> staleTokens = pushClusterQueryService.findStaleInProgressTokens();
        fcmPushResultService.recoverStaleTokens(staleTokens);
    }

    // PENDING 클러스터를 찾아 FCM 발송을 시작한다. (최초 발송 경로)
    private void processPendingClusters() {
        List<PushCluster> pendingClusters = pushClusterQueryService.findAllPendingClusters();

        if (pendingClusters.isEmpty()) {
            return;
        }

        log.info("PENDING 클러스터 발송 시작 - {}건", pendingClusters.size());
        pendingClusters.forEach(cluster -> {
            try {
                fcmOrchestrator.dispatchCluster(cluster);
            } catch (Exception e) {
                log.error("클러스터 발송 중 오류 - clusterId={}", cluster.getId(), e);
            }
        });
    }

    // retryAfter가 지난 RETRY_PENDING 토큰을 클러스터별로 그룹핑해 재발송한다.
    private void processRetryTokens() {
        List<PushClusterToken> retryTokens = pushClusterQueryService.findRetryPendingTokensReady();

        if (retryTokens.isEmpty()) {
            return;
        }

        Map<PushCluster, List<PushClusterToken>> tokensByCluster = retryTokens.stream()
            .collect(Collectors.groupingBy(PushClusterToken::getPushCluster));

        log.info("RETRY_PENDING 토큰 재발송 시작 - {}건 ({}개 클러스터)",
            retryTokens.size(), tokensByCluster.size());

        tokensByCluster.forEach((cluster, tokens) -> {
            try {
                fcmOrchestrator.dispatchRetryTokens(cluster, tokens);
            } catch (Exception e) {
                log.error("재시도 발송 중 오류 - clusterId={}", cluster.getId(), e);
            }
        });
    }
}
