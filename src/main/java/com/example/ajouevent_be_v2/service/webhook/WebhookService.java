package com.example.ajouevent_be_v2.service.webhook;

import com.example.ajouevent_be_v2.common.exception.webhook.WebhookErrorCode;
import com.example.ajouevent_be_v2.common.exception.webhook.WebhookException;
import com.example.ajouevent_be_v2.config.properties.WebhookProperties;
import com.example.ajouevent_be_v2.repository.port.webhook.CrawlingTokenCachePort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class WebhookService {

    private final CrawlingTokenCachePort crawlingTokenCachePort;
    private final WebhookProperties webhookProperties;

    public void validateToken(String token) {
        if (!crawlingTokenCachePort.isTokenValid(webhookProperties.getCrawlingTokenKey(), token)) {
            throw new WebhookException(WebhookErrorCode.INVALID_CRAWLING_TOKEN);
        }
    }

    public String generateToken() {
        return crawlingTokenCachePort.generateAndStoreToken(webhookProperties.getCrawlingTokenKey());
    }
}
