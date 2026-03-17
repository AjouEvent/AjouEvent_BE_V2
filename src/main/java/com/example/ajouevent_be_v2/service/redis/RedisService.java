package com.example.ajouevent_be_v2.service.redis;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class RedisService {

    private final StringRedisTemplate stringRedisTemplate;

    public void validateCrawlingToken(String crawlingToken) {
        // TODO: 크롤링 토큰 검증 로직 작성
    }
}
