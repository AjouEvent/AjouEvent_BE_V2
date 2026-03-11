package com.example.ajouevent_be_v2.dto.topic;

import com.example.ajouevent_be_v2.domain.topic.Topic;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "전체 토픽 목록 응답")
public record TopicDetailResponse(
    @Schema(description = "토픽 분류 (예: 학과, 장학, 기숙사 등)", example = "학과")
    String classification,

    @Schema(description = "한국어 정렬 순서", example = "1")
    Long koreanOrder,

    @Schema(description = "토픽 한국어 이름", example = "소프트웨어학과")
    String koreanTopic
) {

    public static TopicDetailResponse from(Topic topic) {
        return new TopicDetailResponse(topic.getClassification(), topic.getKoreanOrder(), topic.getKoreanTopic());
    }
}
