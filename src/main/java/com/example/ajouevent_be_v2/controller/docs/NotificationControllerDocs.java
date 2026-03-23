package com.example.ajouevent_be_v2.controller.docs;

import com.example.ajouevent_be_v2.common.auth.AuthUser;
import com.example.ajouevent_be_v2.common.dto.SliceResponse;
import com.example.ajouevent_be_v2.common.exception.ErrorResponse;
import com.example.ajouevent_be_v2.domain.member.Member;
import com.example.ajouevent_be_v2.dto.notification.KeywordNotificationResponse;
import com.example.ajouevent_be_v2.dto.notification.NotificationClickRequest;
import com.example.ajouevent_be_v2.dto.notification.TopicNotificationResponse;
import com.example.ajouevent_be_v2.dto.notification.UnreadNotificationCountResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "Notification", description = """
    알림 조회 및 읽음 처리 관련 API
    """)
public interface NotificationControllerDocs {

    @Operation(
        summary = "토픽 알림 목록 조회",
        description = """
            현재 로그인한 사용자의 토픽 알림 목록을 반환합니다.
            - 조회 시 해당 페이지의 미읽음 알림을 즉시 읽음 처리합니다.
            - 기본 정렬: notifiedAt DESC, 기본 페이지 크기: 10
            - 응답은 SliceResponse<TopicNotificationResponse> 형태로 반환됩니다.
            """,
        security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공 (SliceResponse<TopicNotificationResponse> 형태로 반환)"),
        @ApiResponse(responseCode = "401", description = "인증 필요",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    ResponseEntity<SliceResponse<TopicNotificationResponse>> getTopicNotifications(
        @AuthUser Member member, Pageable pageable);

    @Operation(
        summary = "키워드 알림 목록 조회",
        description = """
            현재 로그인한 사용자의 키워드 알림 목록을 반환합니다.
            - 조회 시 해당 페이지의 미읽음 알림을 즉시 읽음 처리합니다.
            - 기본 정렬: notifiedAt DESC, 기본 페이지 크기: 10
            - 응답은 SliceResponse<KeywordNotificationResponse> 형태로 반환됩니다.
            """,
        security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공 (SliceResponse<KeywordNotificationResponse> 형태로 반환)"),
        @ApiResponse(responseCode = "401", description = "인증 필요",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    ResponseEntity<SliceResponse<KeywordNotificationResponse>> getKeywordNotifications(
        @AuthUser Member member, Pageable pageable);

    @Operation(
        summary = "단건 알림 읽음 처리",
        description = """
            특정 알림을 읽음 처리합니다.
            """,
        security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "읽음 처리 성공"),
        @ApiResponse(responseCode = "401", description = "인증 필요",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "존재하지 않는 알림",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    ResponseEntity<Void> markNotificationAsRead(
        @RequestBody NotificationClickRequest request, @AuthUser Member member);

    @Operation(
        summary = "미읽음 알림 수 조회",
        description = """
            현재 로그인한 사용자의 미읽음 알림 수를 반환합니다.
            - 토픽 알림과 키워드 알림을 합산하여 반환합니다.
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
            - 토픽 알림과 키워드 알림 모두 일괄 처리됩니다.
            """,
        security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "전체 읽음 처리 성공"),
        @ApiResponse(responseCode = "401", description = "인증 필요",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    ResponseEntity<Void> markAllNotificationsAsRead(@AuthUser Member member);
}
