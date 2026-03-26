package com.example.ajouevent_be_v2.controller;

import com.example.ajouevent_be_v2.controller.docs.FileControllerDocs;
import com.example.ajouevent_be_v2.orchestrator.FileOrchestrator;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class FileController implements FileControllerDocs {

    private final FileOrchestrator fileOrchestrator;

    @Override
    @PostMapping("/api/file/multiple-presigned-urls")
    public ResponseEntity<List<Map<String, String>>> getMultiplePresignedUrls(
        @RequestParam String prefix,
        @RequestParam int fileCount) {
        return ResponseEntity.ok(fileOrchestrator.generateMultiplePresignedUrls(prefix, fileCount));
    }
}
