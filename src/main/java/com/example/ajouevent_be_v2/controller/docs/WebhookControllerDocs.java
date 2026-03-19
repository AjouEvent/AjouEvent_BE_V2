package com.example.ajouevent_be_v2.controller.docs;

import com.example.ajouevent_be_v2.common.exception.ErrorResponse;
import com.example.ajouevent_be_v2.dto.webhook.WebhookRequest;
import com.example.ajouevent_be_v2.dto.webhook.WebhookResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

@Tag(name = "Webhook", description = """
    외부 크롤러로부터 공지사항을 수신하는 내부 API

    [인증 방식]
    - JWT Bearer 토큰 미사용
    - 요청 헤더 `crawling-token`에 Redis에 저장된 크롤링 전용 토큰을 포함해야 합니다.
    - 토큰은 매일 05:30에 자동 갱신됩니다 (CrawlingTokenScheduler).
    """)
public interface WebhookControllerDocs {

    @Operation(
        summary = "크롤링 공지사항 수신",
        description = """
            외부 크롤러가 수집한 공지사항을 수신하여 저장하고 FCM 푸시 알림을 발송합니다.

            [처리 순서]
            1. `crawling-token` 헤더 검증 — 실패 시 401 반환
            2. 공지사항 중복 여부 확인 — 중복 시 409 반환
            3. 공지사항 DB 저장
            4. 구독자별 PushCluster 생성 및 FCM 알림 발송
            5. 키워드 구독자 대상 추가 알림 처리
            """)
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "처리 성공",
            content = @Content(schema = @Schema(implementation = WebhookResponse.class))),
        @ApiResponse(responseCode = "401", description = "유효하지 않은 크롤링 토큰",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "409", description = "이미 처리된 중복 공지사항",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    ResponseEntity<WebhookResponse> handleWebhook(
        @Parameter(in = ParameterIn.HEADER, name = "crawling-token",
            description = "크롤링 전용 인증 토큰 (LOCAL: POST /api/v2/auth/test-crawling-token 으로 발급)",
            required = true, example = "dGVzdHRva2Vu") String token,
        WebhookRequest webhookRequest);
}
