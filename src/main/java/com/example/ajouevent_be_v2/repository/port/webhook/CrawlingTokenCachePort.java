package com.example.ajouevent_be_v2.repository.port.webhook;

public interface CrawlingTokenCachePort {

    String generateAndStoreToken(String key);

    boolean isTokenValid(String key, String token);
}
