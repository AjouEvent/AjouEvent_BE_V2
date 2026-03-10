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

@Tag(name = "Auth", description = "인증 관련 API")
public interface AuthControllerDocs {

    @Operation(summary = "Google 소셜 로그인",
        description = "Google OAuth 인가 코드로 로그인하거나 신규 회원을 등록하고 JWT 토큰을 발급합니다. "
            + "액세스 토큰은 응답 바디, 리프레시 토큰은 HttpOnly 쿠키(refreshToken)로 반환됩니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "소셜 로그인 성공",
            headers = @Header(name = "Set-Cookie",
                description = "HttpOnly 쿠키로 리프레시 토큰 설정 (refreshToken=...; HttpOnly; Secure; Path=/; SameSite=Lax)",
                schema = @Schema(type = "string")),
            content = @Content(schema = @Schema(implementation = LoginResponse.class))),
        @ApiResponse(responseCode = "400", description = "잘못된 인가 코드",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    ResponseEntity<LoginResponse> login(@RequestBody OauthRequest request);

    @Operation(summary = "토큰 재발급",
        description = "HttpOnly 쿠키(refreshToken)의 리프레시 토큰으로 액세스 토큰과 리프레시 토큰을 모두 재발급합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "토큰 재발급 성공",
            headers = @Header(name = "Set-Cookie",
                description = "새 리프레시 토큰으로 쿠키 갱신",
                schema = @Schema(type = "string")),
            content = @Content(schema = @Schema(implementation = LoginResponse.class))),
        @ApiResponse(responseCode = "401", description = "유효하지 않은 토큰",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    ResponseEntity<LoginResponse> reissueToken(@CookieValue(name = "refreshToken") String refreshToken);
}
