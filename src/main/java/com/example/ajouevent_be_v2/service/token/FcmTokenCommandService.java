package com.example.ajouevent_be_v2.service.token;

import com.example.ajouevent_be_v2.domain.member.Member;
import com.example.ajouevent_be_v2.domain.member.Token;
import com.example.ajouevent_be_v2.repository.port.token.TokenRepositoryPort;
import java.time.LocalDate;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// V1 참고: service/TopicService.java — saveFCMToken() 포팅
@Slf4j
@Service
@RequiredArgsConstructor
public class FcmTokenCommandService {

    private static final long TOKEN_EXPIRATION_WEEKS = 10;

    private final TokenRepositoryPort tokenRepositoryPort;

    /**
     * FCM 토큰 등록 또는 만료일 갱신
     * - 이미 존재하는 토큰이면 만료일만 갱신하고 Optional.empty() 반환
     * - 신규 토큰이면 저장 후 Optional.of(newToken) 반환
     */
    @Transactional
    public Optional<Token> registerToken(Member member, String fcmToken) {
        Optional<Token> existing = tokenRepositoryPort.findByTokenValueAndMember(fcmToken, member);

        if (existing.isPresent()) {
            existing.get().renewExpiration(TOKEN_EXPIRATION_WEEKS);
            log.info("FCM 토큰 만료일 갱신 - member: {}, token: {}", member.getEmail(), fcmToken);
            return Optional.empty();
        }

        Token newToken = Token.builder()
            .tokenValue(fcmToken)
            .member(member)
            .expirationDate(LocalDate.now().plusWeeks(TOKEN_EXPIRATION_WEEKS))
            .isDeleted(false)
            .build();

        Token saved = tokenRepositoryPort.save(newToken);
        log.info("FCM 토큰 신규 등록 - member: {}, token: {}", member.getEmail(), fcmToken);
        return Optional.of(saved);
    }

    /**
     * [LOCAL 전용] 테스트용 FCM 토큰 등록 — 만료일 당일 (LocalDate 최소 단위)
     * - 이미 존재하는 토큰이면 만료일을 당일로 갱신
     * - 신규 토큰이면 저장
     */
    @Transactional
    public Token registerTestToken(Member member, String fcmToken) {
        Optional<Token> existing = tokenRepositoryPort.findByTokenValueAndMember(fcmToken, member);

        if (existing.isPresent()) {
            existing.get().renewExpiration(0);
            log.info("테스트 FCM 토큰 만료일 갱신 (당일) - member: {}, token: {}", member.getEmail(), fcmToken);
            return existing.get();
        }

        Token testToken = Token.builder()
            .tokenValue(fcmToken)
            .member(member)
            .expirationDate(LocalDate.now())
            .isDeleted(false)
            .build();

        Token saved = tokenRepositoryPort.save(testToken);
        log.info("테스트 FCM 토큰 신규 등록 (당일 만료) - member: {}, token: {}", member.getEmail(), fcmToken);
        return saved;
    }
}
