package com.example.ajouevent_be_v2.controller.docs;

import com.example.ajouevent_be_v2.common.exception.ErrorResponse;
import com.example.ajouevent_be_v2.dto.auth.LoginResponse;
import com.example.ajouevent_be_v2.dto.auth.OauthRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "Auth", description = """
    인증 관련 API

    [응답 형식 변경]
    - 이메일/비밀번호 로그인 제거 → Google OAuth2 전용
    - 회원가입, 이메일 인증, 비밀번호 관련 엔드포인트 전체 제거
    - refreshToken 전달 방식 변경: 응답 바디 → HttpOnly 쿠키 (Set-Cookie: refreshToken)
    - 토큰 재발급 요청 방식 변경: RequestBody → Cookie
    - 토큰 재발급 시 accessToken + refreshToken 모두 재발급 (V1은 accessToken만)
    """)
public interface AuthControllerDocs {

    @Operation(
        summary = "Google 소셜 로그인",
        description = """
            Google OAuth2 인가 코드로 로그인합니다.
            - 신규 회원이면 자동 가입 후 로그인 처리 (isNewMember: true)
            - Google Workspace(@ajou.ac.kr) 프로필에서 학과 정보를 자동으로 가져옵니다
            - accessToken은 응답 바디, refreshToken은 HttpOnly 쿠키(Set-Cookie: refreshToken)로 반환됩니다

            [응답 형식 변경]
            - refreshToken이 바디에서 제거되고 HttpOnly 쿠키로만 전달
            """
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "로그인 성공",
            headers = @Header(name = "Set-Cookie",
                description = "HttpOnly 쿠키로 리프레시 토큰 설정 (refreshToken=...; HttpOnly; Path=/; SameSite=Lax)",
                schema = @Schema(type = "string")),
            content = @Content(schema = @Schema(implementation = LoginResponse.class))),
        @ApiResponse(responseCode = "400", description = "잘못된 인가 코드",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    ResponseEntity<LoginResponse> login(@RequestBody OauthRequest request);

    @Operation(
        summary = "토큰 재발급",
        description = """
            쿠키의 refreshToken으로 accessToken과 refreshToken을 모두 재발급합니다.
            - 요청: Cookie에 refreshToken 포함 필요
            - 응답: 새 accessToken은 바디, 새 refreshToken은 HttpOnly 쿠키로 반환

            [응답 형식 변경]
            - refreshToken 전달 방식: RequestBody → Cookie
            - refreshToken도 함께 재발급 (V1은 accessToken만 재발급)
            """
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "재발급 성공",
            headers = @Header(name = "Set-Cookie",
                description = "새 refreshToken으로 쿠키 갱신",
                schema = @Schema(type = "string")),
            content = @Content(schema = @Schema(implementation = LoginResponse.class))),
        @ApiResponse(responseCode = "401", description = "유효하지 않거나 만료된 토큰",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    ResponseEntity<LoginResponse> reissueToken(@CookieValue(name = "refreshToken") String refreshToken);
}
