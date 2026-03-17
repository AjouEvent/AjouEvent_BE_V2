package com.example.ajouevent_be_v2.service.webhook;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.example.ajouevent_be_v2.domain.member.Token;
import com.example.ajouevent_be_v2.service.token.TokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class TokenValidationScheduler {

    private static final int BATCH_SIZE = 400;

    private final FcmService fcmService;
    private final TokenService tokenService;

    @Scheduled(cron = "0 0 5 ? * SUN")
    public void validateAndRemoveInvalidTokens() {
        log.info("[START] FCM 토큰 유효성 검사 및 삭제 프로세스 시작");

        // TODO: #10 subscription 완료 후 TopicService.unsubscribeExpiredTokens() 연결
        // TODO: #7 FcmTokenValidationLogger 연결

        Map<String, Token> activeTokenMap = tokenService.findAllActiveTokens().stream()
            .collect(Collectors.toMap(Token::getTokenValue, Function.identity()));
        log.info("현재 저장된 활성 토큰 개수: {}", activeTokenMap.size());

        Set<String> invalidTokenValues = new HashSet<>(batchValidateTokens(new ArrayList<>(activeTokenMap.keySet())));

        if (invalidTokenValues.isEmpty()) {
            log.info("[END] FCM 토큰 유효성 검사 종료");
            return;
        }

        log.info("유효하지 않은 토큰 개수: {}", invalidTokenValues.size());

        List<Token> invalidTokenEntities = invalidTokenValues.stream()
            .map(activeTokenMap::get)
            .filter(Objects::nonNull)
            .toList();

        tokenService.softDeleteInvalidTokens(invalidTokenEntities);

        log.info("[END] FCM 토큰 유효성 검사 및 삭제 완료. 삭제된 토큰 수: {}", invalidTokenEntities.size());
    }

    private List<String> batchValidateTokens(List<String> tokenValues) {
        List<String> invalidTokens = new ArrayList<>();
        int totalTokens = tokenValues.size();
        int batchCount = (int) Math.ceil((double) totalTokens / BATCH_SIZE);

        log.info("총 {}개의 토큰을 {}개씩 배치 처리 (총 {}개 배치)", totalTokens, BATCH_SIZE, batchCount);

        for (int i = 0; i < batchCount; i++) {
            int fromIndex = i * BATCH_SIZE;
            int toIndex = Math.min(fromIndex + BATCH_SIZE, totalTokens);
            List<String> batch = tokenValues.subList(fromIndex, toIndex);

            List<String> batchInvalidTokens = fcmService.validateTokens(batch);
            invalidTokens.addAll(batchInvalidTokens);

            log.info("배치 {} 완료 | {} ~ {} 번 토큰 검증 | 무효 토큰 수: {}",
                i + 1, fromIndex, toIndex - 1, batchInvalidTokens.size());
        }

        return invalidTokens;
    }
}
