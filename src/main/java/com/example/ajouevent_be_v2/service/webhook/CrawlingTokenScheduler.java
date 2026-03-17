package com.example.ajouevent_be_v2.service.webhook;

import com.example.ajouevent_be_v2.config.properties.WebhookProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CrawlingTokenScheduler {

    // TODO: #15 webhook 도메인 완료 후 CrawlingTokenCacheAdapter 연결 (cross-domain 주입)
//    private final CrawlingTokenCachePort crawlingTokenCachePort;
    private final WebhookProperties webhookProperties;

    @Scheduled(cron = "0 30 5 * * ?")
    public void scheduleTokenGeneration() {
//        String token = crawlingTokenCachePort.generateAndStoreToken(webhookProperties.getCrawlingTokenKey());
//        log.info("크롤링 토큰 갱신 완료: {}", token);
    }
}
