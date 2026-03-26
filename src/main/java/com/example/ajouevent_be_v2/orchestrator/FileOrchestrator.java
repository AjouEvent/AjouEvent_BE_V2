package com.example.ajouevent_be_v2.orchestrator;

import com.example.ajouevent_be_v2.service.file.FileService;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class FileOrchestrator {

    private final FileService fileService;

    /**
     * S3 Presigned URL 다중 발급
     *
     * 1. fileCount만큼 UUID 기반 S3 키 생성 ({prefix}/{uuid})
     * 2. 각 키에 대한 PUT Presigned URL 발급 (유효시간 10분)
     * 3. [{fileName, presignedUrl}, ...] 형태로 반환 — 클라이언트가 직접 S3에 PUT 업로드
     */
    public List<Map<String, String>> generateMultiplePresignedUrls(String prefix, int fileCount) {
        return fileService.generateMultiplePresignedUrls(prefix, fileCount);
    }
}
