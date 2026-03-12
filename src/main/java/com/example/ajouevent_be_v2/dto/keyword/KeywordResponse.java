package com.example.ajouevent_be_v2.dto.keyword;

import com.example.ajouevent_be_v2.domain.keyword.KeywordMember;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

@Schema(description = "구독 중인 키워드 응답")
public record KeywordResponse(
    @Schema(description = "키워드 URL 인코딩 식별자 (URLEncode(koreanKeyword) + '_' + topicName(영문 department 식별자))", example = "%EC%9E%A5%ED%95%99%EA%B8%88_Software")
    String encodedKeyword,

    @Schema(description = "키워드 한글명", example = "장학금")
    String koreanKeyword,

    @Schema(description = "알림 검색에 사용되는 복합 식별자 (koreanKeyword + '_' + topicName(영문 department 식별자))", example = "장학금_Software")
    String searchKeyword,

    @Schema(description = "키워드가 속한 토픽 한국어 이름", example = "소프트웨어학과")
    String topicName,

    @Schema(description = "해당 키워드의 최신 공지를 읽었는지 여부", example = "false")
    boolean isRead,

    @Schema(description = "마지막으로 공지를 읽은 시각", example = "2025-03-01T12:00:00")
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
