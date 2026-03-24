package com.example.ajouevent_be_v2.controller.docs;

import com.example.ajouevent_be_v2.common.auth.AuthUser;
import com.example.ajouevent_be_v2.common.exception.ErrorResponse;
import com.example.ajouevent_be_v2.domain.member.Member;
import com.example.ajouevent_be_v2.dto.token.FcmTokenRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "FcmToken", description = """
    FCM 디바이스 토큰 등록 API
    """)
public interface FcmTokenControllerDocs {

    @Operation(
        summary = "FCM 토큰 등록",
        description = """
            클라이언트의 FCM 디바이스 토큰을 서버에 등록합니다.
            - 이미 존재하는 토큰이면 만료일만 갱신 (10주)
            - 신규 토큰이면 저장 후 기존 구독 중인 모든 Topic/Keyword에 매핑
            """,
        security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "토큰 등록 성공"),
        @ApiResponse(responseCode = "401", description = "인증 필요",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    ResponseEntity<Void> saveToken(@AuthUser Member member, @RequestBody FcmTokenRequest request);
}
