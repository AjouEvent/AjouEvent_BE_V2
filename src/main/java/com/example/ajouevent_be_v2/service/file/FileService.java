package com.example.ajouevent_be_v2.service.file;

import com.amazonaws.HttpMethod;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import com.example.ajouevent_be_v2.config.properties.S3Properties;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileService {

    private static final long PRESIGNED_URL_EXPIRATION_MILLIS = 1000L * 60 * 2;

    private final AmazonS3 amazonS3;
    private final S3Properties s3Properties;

    public List<Map<String, String>> generateMultiplePresignedUrls(String prefix, int fileCount) {
        List<Map<String, String>> urls = new ArrayList<>();
        for (int i = 0; i < fileCount; i++) {
            String fileName = createPath(prefix);
            GeneratePresignedUrlRequest request = buildPresignedUrlRequest(fileName);
            URL url = amazonS3.generatePresignedUrl(request);
            urls.add(Map.of("url", url.toString()));
        }
        return urls;
    }

    private GeneratePresignedUrlRequest buildPresignedUrlRequest(String fileName) {
        Date expiration = new Date(System.currentTimeMillis() + PRESIGNED_URL_EXPIRATION_MILLIS);
        return new GeneratePresignedUrlRequest(s3Properties.getS3().getBucket(), fileName)
            .withMethod(HttpMethod.PUT)
            .withExpiration(expiration);
    }

    private String createPath(String prefix) {
        return String.format("%s/%s", prefix, UUID.randomUUID());
    }
}
