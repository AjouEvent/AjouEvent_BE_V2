package com.example.ajouevent_be_v2.repository.port.webhook;

// TODO: #15 webhook 도메인 완료 후 구현체(CrawlingTokenCacheAdapter) 연결
public interface CrawlingTokenCachePort {

    String generateAndStoreToken(String key);
}
