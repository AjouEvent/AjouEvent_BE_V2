package com.example.ajouevent_be_v2.controller.docs;

import com.example.ajouevent_be_v2.common.auth.AuthUser;
import com.example.ajouevent_be_v2.common.exception.ErrorResponse;
import com.example.ajouevent_be_v2.domain.member.Member;
import com.example.ajouevent_be_v2.dto.topic.TopicDetailResponse;
import com.example.ajouevent_be_v2.dto.topic.TopicNotificationUpdateRequest;
import com.example.ajouevent_be_v2.dto.topic.TopicResponse;
import com.example.ajouevent_be_v2.dto.topic.TopicStatusResponse;
import com.example.ajouevent_be_v2.dto.topic.TopicSubscribeRequest;
import com.example.ajouevent_be_v2.dto.topic.TopicUnsubscribeRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "Topic", description = """
    토픽 구독 관련 API

    [응답 형식 변경]
    - 뮤테이션 응답: 200 OK + ResponseDto body → 204 No Content
    - 인증 방식: @PreAuthorize + SecurityContext → Bearer JWT (Authorization 헤더)
    """)
public interface TopicControllerDocs {

    @Operation(
        summary = "전체 토픽 목록 조회",
        description = """
            구독 가능한 전체 토픽 목록을 반환합니다. 인증 불필요.
            """)
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = TopicDetailResponse.class))))
    })
    ResponseEntity<List<TopicDetailResponse>> getAllTopics();

    @Operation(
        summary = "내 구독 토픽 목록 조회",
        description = """
            현재 로그인한 사용자가 구독 중인 토픽 목록을 반환합니다.
            """,
        security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = TopicResponse.class)))),
        @ApiResponse(responseCode = "401", description = "인증 필요",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    ResponseEntity<List<TopicResponse>> getSubscribedTopics(@AuthUser Member member);

    @Operation(
        summary = "토픽 구독",
        description = """
            특정 토픽을 구독합니다. 이미 구독 중인 경우 409를 반환합니다.
            """,
        security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "구독 성공"),
        @ApiResponse(responseCode = "401", description = "인증 필요",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "존재하지 않는 토픽",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "409", description = "이미 구독 중인 토픽",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    ResponseEntity<Void> subscribeToTopic(@RequestBody TopicSubscribeRequest request, @AuthUser Member member);

    @Operation(
        summary = "토픽 구독 취소",
        description = """
            특정 토픽의 구독을 취소합니다.
            """,
        security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "구독 취소 성공"),
        @ApiResponse(responseCode = "401", description = "인증 필요",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "존재하지 않는 토픽",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    ResponseEntity<Void> unsubscribeFromTopic(@RequestBody TopicUnsubscribeRequest request, @AuthUser Member member);

    @Operation(
        summary = "전체 구독 초기화",
        description = """
            현재 로그인한 사용자의 모든 토픽 구독을 초기화합니다.
            """,
        security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "초기화 성공"),
        @ApiResponse(responseCode = "401", description = "인증 필요",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    ResponseEntity<Void> resetAllTopicSubscriptions(@AuthUser Member member);

    @Operation(
        summary = "구독 상태 포함 전체 토픽 조회",
        description = """
            전체 토픽 목록과 현재 사용자의 구독 여부 및 알림 수신 설정을 함께 반환합니다.
            """,
        security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = TopicStatusResponse.class)))),
        @ApiResponse(responseCode = "401", description = "인증 필요",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    ResponseEntity<List<TopicStatusResponse>> getTopicsWithSubscriptionStatus(@AuthUser Member member);

    @Operation(
        summary = "알림 수신 설정 변경",
        description = """
            특정 토픽의 알림 수신 여부를 변경합니다. 구독 중이 아닌 토픽에 대해 요청 시 404를 반환합니다.
            """,
        security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "설정 변경 성공"),
        @ApiResponse(responseCode = "401", description = "인증 필요",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "존재하지 않는 토픽 또는 구독 정보 없음",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    ResponseEntity<Void> updateNotificationPreference(
        @RequestBody TopicNotificationUpdateRequest request, @AuthUser Member member);
}
