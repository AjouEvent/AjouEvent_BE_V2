package com.example.ajouevent_be_v2.controller.docs;

import com.example.ajouevent_be_v2.common.auth.AuthUser;
import com.example.ajouevent_be_v2.common.exception.ErrorResponse;
import com.example.ajouevent_be_v2.domain.member.Member;
import com.example.ajouevent_be_v2.dto.subscription.TabReadStatusResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

@Tag(name = "Subscription", description = """
    구독 탭 통합 관련 API (토픽 + 키워드 구독 공통)

    [V1 → V2 변경사항]
    - API 경로 변경: GET /api/subscriptions/isSubscribedTabRead → GET /api/v2/subscriptions/read-status
    - 인증 방식 변경: SecurityContextHolder → @AuthUser (AuthArgumentResolver 기반)
    - 응답 형식 변경: 바디 직접 반환 → ResponseEntity<TabReadStatusResponse>
    """)
public interface SubscriptionControllerDocs {

    @Operation(
        summary = "구독 탭 읽음 상태 조회",
        description = """
            토픽/키워드 구독 탭에 읽지 않은 알림이 있는지 여부를 반환합니다.
            - TopicMember와 KeywordMember 중 isRead = false 인 항목이 하나라도 있으면 false 반환
            - 모두 읽은 상태이면 true 반환

            [V1 대비 변경]
            - V1: GET /api/subscriptions/isSubscribedTabRead → TabReadStatusResponse 바디 직접 반환
            - V2: GET /api/v2/subscriptions/read-status → ResponseEntity<TabReadStatusResponse>
            - 응답 필드 변경 없음: { isSubscribedTabRead: boolean } 동일
            """,
        security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공",
            content = @Content(schema = @Schema(implementation = TabReadStatusResponse.class))),
        @ApiResponse(responseCode = "401", description = "인증 필요",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    ResponseEntity<TabReadStatusResponse> isSubscribedTabRead(@AuthUser Member member);
}
