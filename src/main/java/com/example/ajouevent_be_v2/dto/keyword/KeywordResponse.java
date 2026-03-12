package com.example.ajouevent_be_v2.dto.keyword;

import com.example.ajouevent_be_v2.domain.keyword.KeywordMember;
import java.time.LocalDateTime;

public record KeywordResponse(
    String encodedKeyword,
    String koreanKeyword,
    String searchKeyword,
    String topicName,
    boolean isRead,
    LocalDateTime lastReadAt
) {
    public static KeywordResponse from(KeywordMember keywordMember) {
        return new KeywordResponse(
            keywordMember.getKeyword().getEncodedKeyword(),
            keywordMember.getKeyword().getKoreanKeyword(),
            keywordMember.getKeyword().getSearchKeyword(),
            keywordMember.getKeyword().getTopic().getKoreanTopic(),
            keywordMember.isRead(),
            keywordMember.getLastReadAt()
        );
    }
}
