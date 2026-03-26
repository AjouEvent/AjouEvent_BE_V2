package com.example.ajouevent_be_v2.controller.docs;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "File", description = "파일 업로드 관련 API")
public interface FileControllerDocs {

    @Operation(
        summary = "S3 Presigned URL 다중 발급",
        description = """
            S3에 파일을 직접 업로드하기 위한 Presigned URL을 여러 개 발급합니다.
            - 유효 시간: 2분
            - HTTP 메서드: PUT
            """)
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "URL 발급 성공")
    })
    ResponseEntity<List<Map<String, String>>> getMultiplePresignedUrls(
        @RequestParam String prefix,
        @RequestParam int fileCount);
}
