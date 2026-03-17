package com.example.ajouevent_be_v2.controller.docs;

import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;

import com.example.ajouevent_be_v2.common.auth.AuthUser;
import com.example.ajouevent_be_v2.common.dto.SliceResponse;
import com.example.ajouevent_be_v2.common.exception.ErrorResponse;
import com.example.ajouevent_be_v2.domain.member.Member;
import com.example.ajouevent_be_v2.dto.notification.KeywordNotificationResponse;
import com.example.ajouevent_be_v2.dto.notification.TopicNotificationResponse;
import com.example.ajouevent_be_v2.dto.notification.UnreadNotificationCountResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Notification", description = """
    알림 조회 및 읽음 처리 관련 API

    [V1 → V2 변경사항]

    [변경된 endpoint]
    - GET  /api/notification/topic          → GET  /api/v2/notification/topic     (path 변경)
    - GET  /api/notification/keyword        → GET  /api/v2/notification/keyword   (path 변경)
    - POST /api/notification/click          → POST /api/v2/notification/{id}      (path 변경, id를 RequestBody → PathVariable로 변경)
    - GET  /api/notification/unread-count   → GET /api/v2/notification/unread-count  (path 변경)
    - POST /api/notification/readAll        → PUT /api/v2/notification/readAll       (POST → PUT)

    [공통 변경]
    - 뮤테이션 응답: 200 OK + ResponseDto body → 204 No Content
    """)
public interface NotificationControllerDocs {

    @Operation(
        summary = "토픽 알림 목록 조회",
        description = """
            현재 로그인한 사용자의 토픽 알림 목록을 반환합니다.
            조회 시 해당 페이지의 미읽음 알림을 즉시 읽음 처리합니다.

            [V1 대비 변경]
            - V1: GET /api/notification/topic
            - V2: GET /api/v2/notification/topic
            """,
        security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공",
            content = @Content(schema = @Schema(implementation = TopicNotificationResponse.class))),
        @ApiResponse(responseCode = "401", description = "인증 필요",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    ResponseEntity<SliceResponse<TopicNotificationResponse>> getTopicNotifications(
        @AuthUser Member member, Pageable pageable);

    @Operation(
        summary = "키워드 알림 목록 조회",
        description = """
            현재 로그인한 사용자의 키워드 알림 목록을 반환합니다.
            조회 시 해당 페이지의 미읽음 알림을 즉시 읽음 처리합니다.

            [V1 대비 변경]
            - V1: GET /api/notification/keyword
            - V2: GET /api/v2/notification/keyword
            """,
        security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공",
            content = @Content(schema = @Schema(implementation = KeywordNotificationResponse.class))),
        @ApiResponse(responseCode = "401", description = "인증 필요",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    ResponseEntity<SliceResponse<KeywordNotificationResponse>> getKeywordNotifications(
        @AuthUser Member member, Pageable pageable);

    @Operation(
        summary = "단건 알림 읽음 처리",
        description = """
            특정 알림을 읽음 처리합니다.

            [V1 대비 변경]
            - V1: POST /api/notification/click — RequestBody로 pushNotificationId 전달
            - V2: POST /api/v2/notification/{id} — PathVariable로 id 전달, 204 No Content 응답
            """,
        security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "읽음 처리 성공"),
        @ApiResponse(responseCode = "401", description = "인증 필요",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "존재하지 않는 알림",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    ResponseEntity<Void> markNotificationAsRead(Long id, @AuthUser Member member);

    @Operation(
        summary = "미읽음 알림 수 조회",
        description = """
            현재 로그인한 사용자의 미읽음 알림 수를 반환합니다.

            [V1 대비 변경]
            - V1: GET /api/notification/unread-count
            - V2: GET /api/v2/notification/unread-count
            """,
        security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공",
            content = @Content(schema = @Schema(implementation = UnreadNotificationCountResponse.class))),
        @ApiResponse(responseCode = "401", description = "인증 필요",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    ResponseEntity<UnreadNotificationCountResponse> getUnreadCount(@AuthUser Member member);

    @Operation(
        summary = "전체 알림 읽음 처리",
        description = """
            현재 로그인한 사용자의 모든 미읽음 알림을 읽음 처리합니다.

            [V1 대비 변경]
            - V1: POST /api/notification/readAll — 200 OK + ResponseDto
            - V2: PUT /api/v2/notification/readAll — 204 No Content
            """,
        security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "전체 읽음 처리 성공"),
        @ApiResponse(responseCode = "401", description = "인증 필요",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    ResponseEntity<Void> markAllNotificationsAsRead(@AuthUser Member member);
}
