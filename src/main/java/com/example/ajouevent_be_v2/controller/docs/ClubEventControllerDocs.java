package com.example.ajouevent_be_v2.controller.docs;

import com.example.ajouevent_be_v2.common.auth.AuthOptional;
import com.example.ajouevent_be_v2.common.auth.AuthUser;
import com.example.ajouevent_be_v2.common.dto.SliceResponse;
import com.example.ajouevent_be_v2.common.exception.ErrorResponse;
import com.example.ajouevent_be_v2.domain.member.Member;
import com.example.ajouevent_be_v2.dto.clubevent.ClubEventDetailResponse;
import com.example.ajouevent_be_v2.dto.clubevent.ClubEventResponse;
import com.example.ajouevent_be_v2.dto.clubevent.ClubEventWithKeywordResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;

@Tag(name = "ClubEvent", description = """
    공지사항 조회 관련 API

    [V1 → V2 변경사항]

    [변경된 endpoint]
    - GET  /api/event/detail/{eventId}              → GET  /api/v2/event/detail/{eventId}
    - GET  /api/event/{type}                        → GET  /api/v2/event/{type}
    - GET  /api/event/popular                       → GET  /api/v2/event/popular
    - GET  /api/event/subscribed                    → GET  /api/v2/event/subscribed
    - GET  /api/event/getSubscribedPostsByKeyword   → GET  /api/v2/event/getSubscribedPostsByKeyword
    - GET  /api/event/getSubscribedPostsByKeyword/{keyword} → GET /api/v2/event/getSubscribedPostsByKeyword/{keyword}
    - POST /api/event/like/{eventId}                → POST /api/v2/event/like/{eventId}
    - DELETE /api/event/like/{eventId}              → DELETE /api/v2/event/like/{eventId}
    - GET  /api/event/liked                         → GET  /api/v2/event/liked

    [이번 이슈 12에서 제외 API]
    - GET  /api/event/all                           — 전체 글 조회 (scope 제외)
    - POST /api/event/new, /api/event/post          — 이벤트 생성
    - PATCH /api/event/{eventId}                    — 이벤트 수정
    - DELETE /api/event/{eventId}                   — 이벤트 삭제
    - POST /api/event/addBanner                     — 배너 추가
    - DELETE /api/event/deleteBanner/{id}           — 배너 삭제
    - GET /api/event/banner                         — 배너 조회
    - POST /api/event/calendar                      — Google Calendar 연동 (별도 이슈)

    [공통 변경]
    - 뮤테이션 응답: 200 OK + ResponseDto body → 204 No Content
    """)
public interface ClubEventControllerDocs {

    @Operation(
        summary = "공지사항 상세 조회",
        description = """
            공지사항 상세 정보를 반환합니다.
            - 비인증 사용자도 조회 가능하며 IP 기반으로 조회수가 증가합니다.
            - 인증 사용자의 경우 이메일 기반으로 중복 조회를 방지합니다.
            - JWT 토큰이 있으면 Authorization 헤더에 포함하면 찜 여부(star)가 정확히 반환됩니다.

            [V1 대비 변경]
            - V1: GET /api/event/detail/{eventId}
            - V2: GET /api/v2/event/detail/{eventId}
            """)
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공",
            content = @Content(schema = @Schema(implementation = ClubEventDetailResponse.class))),
        @ApiResponse(responseCode = "404", description = "존재하지 않는 공지사항",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    ResponseEntity<ClubEventDetailResponse> getEventDetail(
        @Parameter(description = "공지사항 ID", required = true, example = "1") Long eventId,
        @AuthOptional Member member,
        HttpServletRequest request);

    @Operation(
        summary = "타입별 공지사항 조회",
        description = """
            특정 타입의 공지사항 목록을 페이징하여 반환합니다.
            - keyword 파라미터로 제목 검색 가능 (기본값: 전체 조회)
            - 인증 사용자의 경우 찜 여부(star)가 정확히 반환됩니다.
            - type 값은 Topic의 영문명과 동일합니다 (예: Software, AjouNormal, AjouScholarship)

            [V1 대비 변경]
            - V1: GET /api/event/{type}
            - V2: GET /api/v2/event/{type}
            """)
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공 (SliceResponse<ClubEventResponse> 형태로 반환)"),
        @ApiResponse(responseCode = "400", description = "유효하지 않은 타입",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    ResponseEntity<SliceResponse<ClubEventResponse>> getEventTypeList(
        @Parameter(description = "토픽 타입 (예: Software, AjouNormal, AjouScholarship)", required = true, example = "Software") String type,
        @Parameter(description = "제목 검색 키워드 (미입력 시 전체 조회)", example = "장학금") String keyword,
        Pageable pageable,
        @AuthOptional Member member);

    @Operation(
        summary = "주간 인기 공지사항 TOP 10",
        description = """
            이번 주 생성된 공지사항 중 조회수 상위 10개를 반환합니다.
            - 인증 사용자의 경우 찜 여부(star)가 정확히 반환됩니다.

            [V1 대비 변경]
            - V1: GET /api/event/popular
            - V2: GET /api/v2/event/popular
            """)
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = ClubEventResponse.class))))
    })
    ResponseEntity<List<ClubEventResponse>> getTopPopularEvents(@AuthOptional Member member);

    @Operation(
        summary = "구독 토픽 기반 공지사항 조회",
        description = """
            로그인한 사용자가 구독한 토픽의 공지사항을 반환합니다.
            - 비로그인 시 기본 타입(AjouNormal)의 공지사항을 반환합니다.
            - keyword 파라미터로 제목 검색 가능합니다.

            [V1 대비 변경]
            - V1: GET /api/event/subscribed
            - V2: GET /api/v2/event/subscribed
            """)
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공 (SliceResponse<ClubEventResponse> 형태로 반환)")
    })
    ResponseEntity<SliceResponse<ClubEventResponse>> getSubscribedEvents(
        Pageable pageable,
        @AuthOptional Member member,
        @Parameter(description = "제목 검색 키워드 (미입력 시 전체 조회)", example = "장학금") String keyword);

    @Operation(
        summary = "구독 키워드 전체 공지사항 조회",
        description = """
            로그인한 사용자가 구독한 모든 키워드에 해당하는 공지사항을 반환합니다.
            - 각 게시글에 매칭된 키워드(keyword 필드)가 함께 반환됩니다.

            [V1 대비 변경]
            - V1: GET /api/event/getSubscribedPostsByKeyword
            - V2: GET /api/v2/event/getSubscribedPostsByKeyword
            """,
        security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공 (SliceResponse<ClubEventWithKeywordResponse> 형태로 반환)"),
        @ApiResponse(responseCode = "401", description = "인증 필요",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    ResponseEntity<SliceResponse<ClubEventWithKeywordResponse>> getAllBySubscribedKeywords(
        @AuthUser Member member, Pageable pageable);

    @Operation(
        summary = "특정 키워드 공지사항 조회",
        description = """
            특정 키워드에 해당하는 공지사항을 반환합니다.
            - keyword 경로 변수는 encodedKeyword 값입니다 (URLEncode(한글키워드) + "_" + topicName 형식).

            [V1 대비 변경]
            - V1: GET /api/event/getSubscribedPostsByKeyword/{keyword}
            - V2: GET /api/v2/event/getSubscribedPostsByKeyword/{keyword}
            """,
        security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공 (SliceResponse<ClubEventWithKeywordResponse> 형태로 반환)"),
        @ApiResponse(responseCode = "401", description = "인증 필요",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "해당 키워드 구독 정보 없음",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    ResponseEntity<SliceResponse<ClubEventWithKeywordResponse>> getByKeyword(
        @Parameter(description = "encodedKeyword (URLEncode(한글키워드)_topicName 형식)", required = true, example = "%EC%9E%A5%ED%95%99%EA%B8%88_Software") String keyword,
        @AuthUser Member member,
        Pageable pageable);

    @Operation(
        summary = "공지사항 찜하기",
        description = """
            공지사항을 찜 목록에 추가합니다. 이미 찜한 경우 409를 반환합니다.

            [V1 대비 변경]
            - V1: POST /api/event/like/{eventId} — 성공 시 200 OK + ResponseDto
            - V2: POST /api/v2/event/like/{eventId} — 성공 시 204 No Content
            """,
        security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "찜하기 성공"),
        @ApiResponse(responseCode = "401", description = "인증 필요",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "존재하지 않는 공지사항",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "409", description = "이미 찜한 공지사항",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    ResponseEntity<Void> likeEvent(
        @Parameter(description = "공지사항 ID", required = true, example = "1") Long eventId,
        @AuthUser Member member);

    @Operation(
        summary = "공지사항 찜 취소",
        description = """
            찜한 공지사항을 목록에서 제거합니다.

            [V1 대비 변경]
            - V1: DELETE /api/event/like/{eventId} — 성공 시 200 OK + ResponseDto
            - V2: DELETE /api/v2/event/like/{eventId} — 성공 시 204 No Content
            """,
        security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "찜 취소 성공"),
        @ApiResponse(responseCode = "401", description = "인증 필요",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "찜하지 않은 공지사항",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    ResponseEntity<Void> cancelLikeEvent(
        @Parameter(description = "공지사항 ID", required = true, example = "1") Long eventId,
        @AuthUser Member member);

    @Operation(
        summary = "찜한 공지사항 목록 조회",
        description = """
            로그인한 사용자가 찜한 공지사항 목록을 반환합니다.
            - type, keyword로 필터링 가능합니다.
            - type 미입력 시 전체 타입 조회, keyword 미입력 시 전체 제목 조회합니다.

            [V1 대비 변경]
            - V1: GET /api/event/liked
            - V2: GET /api/v2/event/liked
            """,
        security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공 (SliceResponse<ClubEventResponse> 형태로 반환)"),
        @ApiResponse(responseCode = "401", description = "인증 필요",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    ResponseEntity<SliceResponse<ClubEventResponse>> getLikedEvents(
        @Parameter(description = "토픽 타입 필터 (미입력 시 전체)", example = "Software") String type,
        @Parameter(description = "제목 검색 키워드 (미입력 시 전체 조회)", example = "장학금") String keyword,
        Pageable pageable,
        @AuthUser Member member);
}
