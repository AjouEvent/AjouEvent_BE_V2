package com.example.ajouevent_be_v2.service.webhook;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.example.ajouevent_be_v2.common.discord.DiscordMessageService;
import com.example.ajouevent_be_v2.config.properties.PushProperties;
import com.example.ajouevent_be_v2.domain.clubevent.JobStatus;
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
    private final DiscordMessageService discordMessageService;
    private final PushProperties pushProperties;

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
                MessagingErrorCode errorCode = sendResponse.getException().getMessagingErrorCode();
                failCount += handleFailedToken(pushClusterToken, errorCode, invalidTokenValues);
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

    // onFailure는 FCM 에러 코드가 아닌 Java 레벨 예외 (네트워크 단절, SDK 타임아웃 등)다.
    // 영구 실패가 아니라 인프라 일시 문제이므로 Exponential Backoff 재시도 흐름에 합류시킨다.
    @Transactional
    public void markBatchAsRetryPendingAndSave(List<PushClusterToken> batch) {
        batch.forEach(token -> markAsRetryPendingOrPermanentFail(token, false));
        updatePushClusterTokens(batch);
    }

    // PENDING / IN_PROGRESS(stale) / RETRY_PENDING(ready) 토큰을 retryCount 기준으로 분류한다.
    // 상태(PENDING/IN_PROGRESS/RETRY_PENDING)는 "왜 여기 왔는가"만 다를 뿐,
    // 앞으로의 처리는 retryCount 하나로 결정된다.
    //   - retryCount < maxRetryCount  → RETRY_PENDING (dispatch 대상으로 반환)
    //   - retryCount >= maxRetryCount → PERMANENT_FAIL (dispatch 제외)
    @Transactional
    public List<PushClusterToken> recoverStaleTokens(List<PushClusterToken> recoverableTokens) {
        if (recoverableTokens.isEmpty()) {
            return recoverableTokens;
        }

        List<PushClusterToken> toRetry = new ArrayList<>();
        List<PushClusterToken> staleConverted = new ArrayList<>();
        List<PushClusterToken> toPermanentFail = new ArrayList<>();

        for (PushClusterToken token : recoverableTokens) {
            if (token.getRetryCount() >= pushProperties.getMaxRetryCount()) {
                token.markAsPermanentFail();
                toPermanentFail.add(token);
            } else {
                if (token.getJobStatus() != JobStatus.RETRY_PENDING) {
                    token.markAsStaleRecovered();
                    staleConverted.add(token);
                }
                toRetry.add(token);
            }
        }

        if (!staleConverted.isEmpty()) {
            pushClusterTokenRepositoryPort.bulkUpdateAll(staleConverted);
            log.info("Stale 토큰 RETRY_PENDING 복구 - {}건", staleConverted.size());
        }
        if (!toPermanentFail.isEmpty()) {
            pushClusterTokenRepositoryPort.bulkUpdateAll(toPermanentFail);
            log.warn("최대 재시도 초과 PERMANENT_FAIL - {}건", toPermanentFail.size());
        }

        return toRetry;
    }

    /**
     * 에러 코드에 따라 토큰 상태를 결정한다.
     * @return failCount에 더할 값 (최종 실패=1, 재시도 예정=0)
     */
    private int handleFailedToken(
        PushClusterToken token,
        MessagingErrorCode errorCode,
        List<String> invalidTokenValues
    ) {
        if (errorCode == null) {
            token.markAsFail();
            return 1;
        }

        switch (errorCode) {
            case INTERNAL:
            case UNAVAILABLE:
                // FCM 서버 일시 오류 — 클라이언트 요청 자체에는 문제 없음. 재시도 대기
                markAsRetryPendingOrPermanentFail(token, false);
                return 0;

            case QUOTA_EXCEEDED:
                // 프로젝트 전송 속도 제한 초과 — 쿼터 회복 여유를 위해 대기 시간 2배 적용
                markAsRetryPendingOrPermanentFail(token, true);
                return 0;

            case UNREGISTERED:
                // 앱 삭제 또는 토큰 만료 — 재시도해도 동일 실패. 즉시 영구 실패 + 토큰 삭제
                token.markAsFail();
                invalidTokenValues.add(token.getTokenValue());
                return 1;

            case INVALID_ARGUMENT:
                // 잘못된 토큰 형식 또는 페이로드 오류 — 재시도해도 동일 실패. 즉시 영구 실패 + 토큰 삭제
                token.markAsFail();
                invalidTokenValues.add(token.getTokenValue());
                return 1;

            case SENDER_ID_MISMATCH:
                // Firebase 프로젝트 Sender ID 불일치 — 서버 설정 오류. 운영자 확인 필요
                token.markAsPermanentFail();
                discordMessageService.sendMessage(
                    String.format("[FCM SENDER_ID_MISMATCH] pushClusterId=%d, token=%s",
                        token.getPushCluster().getId(), token.getTokenValue()));
                return 1;

            default:
                // 알 수 없는 에러 — 즉시 영구 실패
                token.markAsFail();
                return 1;
        }
    }

    private void markAsRetryPendingOrPermanentFail(PushClusterToken token, boolean quotaExceeded) {
        if (token.getRetryCount() >= pushProperties.getMaxRetryCount()) {
            token.markAsPermanentFail();
            log.warn("최대 재시도 초과 PERMANENT_FAIL - tokenId={}, retryCount={}",
                token.getId(), token.getRetryCount());
            return;
        }
        long delayMinutes = (long) Math.pow(2, token.getRetryCount());
        if (quotaExceeded) {
            delayMinutes *= 2;
        }
        token.markAsRetryPending(LocalDateTime.now().plusMinutes(delayMinutes));
        log.debug("RETRY_PENDING - tokenId={}, retryCount={}, retryAfter={}분 후",
            token.getId(), token.getRetryCount(), delayMinutes);
    }

    private void updatePushClusterTokens(List<PushClusterToken> clusterTokens) {
        pushClusterTokenRepositoryPort.bulkUpdateAll(clusterTokens);
    }
}
