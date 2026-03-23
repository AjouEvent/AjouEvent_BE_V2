package com.example.ajouevent_be_v2.controller.docs;

import com.example.ajouevent_be_v2.common.auth.AuthUser;
import com.example.ajouevent_be_v2.common.exception.ErrorResponse;
import com.example.ajouevent_be_v2.domain.member.Member;
import com.example.ajouevent_be_v2.dto.keyword.KeywordResponse;
import com.example.ajouevent_be_v2.dto.keyword.KeywordSubscribeRequest;
import com.example.ajouevent_be_v2.dto.keyword.KeywordUnsubscribeRequest;
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

@Tag(name = "Keyword", description = """
    키워드 구독 관련 API
    """)
public interface KeywordControllerDocs {

    @Operation(
        summary = "키워드 구독",
        description = """
            특정 토픽 내 키워드를 구독합니다.
            - 최대 10개 구독 가능. 초과 시 400 반환
            - 구독 시 회원의 모든 FCM 토큰 × Keyword → KeywordToken 생성 (BulkInsert)
            - encodedKeyword 생성 규칙: URLEncode(koreanKeyword).replace("+", "%20") + "_" + topicName
            """,
        security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "구독 성공"),
        @ApiResponse(responseCode = "400", description = "키워드 구독 한도 초과 (최대 10개) — AE-KEYWORD-MAX-SUBSCRIPTION-LIMIT",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "401", description = "인증 필요",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "존재하지 않는 토픽 — AE-TOPIC-NOT-FOUND",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "409", description = "이미 구독 중인 키워드 — AE-KEYWORD-ALREADY-SUBSCRIBED",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    ResponseEntity<Void> subscribeToKeyword(@AuthUser Member member, @RequestBody KeywordSubscribeRequest request);

    @Operation(
        summary = "키워드 구독 취소",
        description = """
            구독 중인 키워드를 취소합니다.
            - KeywordMember 삭제 후 해당 KeywordToken 삭제
            """,
        security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "구독 취소 성공"),
        @ApiResponse(responseCode = "401", description = "인증 필요",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "존재하지 않는 키워드 — AE-KEYWORD-NOT-FOUND",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    ResponseEntity<Void> unsubscribeFromKeyword(@AuthUser Member member, @RequestBody KeywordUnsubscribeRequest request);

    @Operation(
        summary = "내 구독 키워드 목록 조회",
        description = """
            로그인된 사용자의 구독 키워드 목록을 반환합니다.
            - KeywordMember JOIN FETCH Keyword JOIN FETCH Topic 으로 조회
            """,
        security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = KeywordResponse.class)))),
        @ApiResponse(responseCode = "401", description = "인증 필요",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    ResponseEntity<List<KeywordResponse>> getUserKeywords(@AuthUser Member member);

    @Operation(
        summary = "키워드 구독 전체 초기화",
        description = """
            로그인된 사용자의 모든 키워드 구독을 초기화합니다.
            - 회원의 TokenId 목록으로 KeywordToken 전체 삭제 후 KeywordMember 전체 삭제
            """,
        security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "초기화 성공"),
        @ApiResponse(responseCode = "401", description = "인증 필요",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    ResponseEntity<Void> resetKeywordSubscriptions(@AuthUser Member member);
}
