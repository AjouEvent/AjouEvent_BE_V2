package com.example.ajouevent_be_v2.controller.docs;

import com.example.ajouevent_be_v2.common.exception.ErrorResponse;
import com.example.ajouevent_be_v2.common.auth.AuthUser;
import com.example.ajouevent_be_v2.domain.member.Member;
import com.example.ajouevent_be_v2.dto.auth.OauthRequest;
import com.example.ajouevent_be_v2.dto.member.MemberInfoResponse;
import com.example.ajouevent_be_v2.dto.member.MemberUpdateRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "Member", description = """
    회원 관련 API

    [V1 → V2 변경사항]
    - 회원가입 정보 입력 엔드포인트 제거: POST /api/users/register-info → 삭제 (Google OAuth 자동 처리)
    - API 경로 변경: /api/users → /api/v2/members
    - phone(전화번호) 필드 전체 제거: 회원 정보 조회/수정 응답·요청에서 삭제
    - 회원 정보 수정 응답: 기존 문자열 응답 → 204 No Content
    - 회원 탈퇴 응답: 기존 문자열 응답 → 204 No Content
    - Google 캘린더 연동 응답: 기존 이메일 문자열 응답 → 204 No Content
    """)
public interface MemberControllerDocs {

    @Operation(
        summary = "회원 정보 조회",
        description = """
            로그인된 사용자의 이름·이메일·전공을 반환합니다.

            [V1 대비 변경]
            - V1: GET /api/users/info — 응답에 phone 필드 포함
            - V2: GET /api/v2/members/info — phone 필드 제거
            """,
        security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공",
            content = @Content(schema = @Schema(implementation = MemberInfoResponse.class))),
        @ApiResponse(responseCode = "401", description = "인증 필요",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "존재하지 않는 사용자",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    ResponseEntity<MemberInfoResponse> getMember(@AuthUser Member member);

    @Operation(
        summary = "회원 정보 수정",
        description = """
            이름·전공을 수정합니다. null 필드는 변경하지 않습니다.

            [V1 대비 변경]
            - V1: PATCH /api/users/update-info — 요청에 phone 필드 포함, 성공 시 문자열 응답
            - V2: PATCH /api/v2/members/info — phone 필드 제거, 성공 시 204 No Content
            """,
        security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "수정 성공"),
        @ApiResponse(responseCode = "401", description = "인증 필요",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "존재하지 않는 사용자",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    ResponseEntity<Void> updateMember(@RequestBody MemberUpdateRequest request, @AuthUser Member member);

    @Operation(
        summary = "회원 탈퇴",
        description = """
            로그인된 사용자의 계정과 관련 데이터를 삭제합니다.

            [V1 대비 변경]
            - V1: DELETE /api/users — 성공 시 문자열 응답
            - V2: DELETE /api/v2/members — 성공 시 204 No Content
            """,
        security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "탈퇴 성공"),
        @ApiResponse(responseCode = "401", description = "인증 필요",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "존재하지 않는 사용자",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    ResponseEntity<Void> deleteMember(@AuthUser Member member);

    @Operation(
        summary = "Google 캘린더 연동",
        description = """
            Google OAuth 인가 코드로 캘린더를 연동합니다.

            [V1 대비 변경]
            - V1: POST /api/users/connect-calendar — 성공 시 이메일 문자열 응답
            - V2: POST /api/v2/members/calendar — 성공 시 204 No Content
            """,
        security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "연동 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 인가 코드",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    ResponseEntity<Void> connectCalendar(@RequestBody OauthRequest request, @AuthUser Member member);
}
