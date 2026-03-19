package com.example.ajouevent_be_v2.repository.adapter.webhook;

import com.example.ajouevent_be_v2.repository.port.webhook.CrawlingTokenCachePort;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

@Slf4j
@Repository
@RequiredArgsConstructor
public class CrawlingTokenCacheAdapter implements CrawlingTokenCachePort {

    private static final long CRAWLING_TOKEN_EXPIRE_SECONDS = 90000L;
    private static final int TOKEN_BYTE_LENGTH = 16;

    private final StringRedisTemplate stringRedisTemplate;

    @Override
    public String generateAndStoreToken(String key) {
        byte[] randomBytes = new byte[TOKEN_BYTE_LENGTH];
        new SecureRandom().nextBytes(randomBytes);
        String token = Base64.getEncoder().encodeToString(randomBytes);
        stringRedisTemplate.opsForValue().set(key, token, CRAWLING_TOKEN_EXPIRE_SECONDS, TimeUnit.SECONDS);
        log.info("크롤링 토큰 갱신 완료: key={}", key);
        return token;
    }

    @Override
    public boolean isTokenValid(String key, String token) {
        String storedToken = stringRedisTemplate.opsForValue().get(key);
        return token.equals(storedToken);
    }
}
