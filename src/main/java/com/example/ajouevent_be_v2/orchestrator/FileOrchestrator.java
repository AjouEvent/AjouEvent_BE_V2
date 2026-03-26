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

    public List<Map<String, String>> generateMultiplePresignedUrls(String prefix, int fileCount) {
        return fileService.generateMultiplePresignedUrls(prefix, fileCount);
    }
}
