package com.example.ajouevent_be_v2.common.util;

import com.example.ajouevent_be_v2.common.dto.SliceResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Slf4j
@RequiredArgsConstructor
@Component
public class JsonParsingUtil {

    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;

    public String saveData(String key, Object object, long timeout, TimeUnit unit) {
        try {
            String jsonData = objectMapper.writeValueAsString(object);
            redisTemplate.opsForValue().set(key, jsonData, timeout, unit);
            return "Data saved successfully";
        } catch (JsonProcessingException e) {
            log.error("Error serializing object: ", e);
            throw new RuntimeException(e);
        }
    }

    public <T> Optional<SliceResponse<T>> getSliceData(String key, Class<T> typeClass) {
        String jsonData = redisTemplate.opsForValue().get(key);

        try {
            if (StringUtils.hasText(jsonData)) {
                JavaType type = objectMapper.getTypeFactory()
                        .constructParametricType(SliceResponse.class, typeClass);
                return Optional.ofNullable(objectMapper.readValue(jsonData, type));
            }
            return Optional.empty();
        } catch (JsonProcessingException e) {
            log.error("Error deserializing object: ", e);
            throw new RuntimeException(e);
        }
    }

    public void deleteData(String key) {
        redisTemplate.delete(key);
    }

    public <T> Optional<T> getData(String key, TypeReference<T> typeReference) {
        String json = redisTemplate.opsForValue().get(key);
        if (json == null) {
            return Optional.empty();
        }
        try {
            return Optional.of(objectMapper.readValue(json, typeReference));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    public void clearCacheForType(String type) {
        List<String> keys = scanKeys(type + ":*");
        if (!keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
    }

    public void clearCache(String pattern) {
        List<String> keys = scanKeys(pattern);
        if (!keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
    }

    private List<String> scanKeys(String pattern) {
        ScanOptions options = ScanOptions.scanOptions().match(pattern).count(100).build();
        List<String> keys = new ArrayList<>();
        redisTemplate.execute((RedisCallback<Void>) connection -> {
            try (Cursor<byte[]> cursor = connection.scan(options)) {
                while (cursor.hasNext()) {
                    keys.add(new String(cursor.next(), StandardCharsets.UTF_8));
                }
            }
            return null;
        });
        return keys;
    }
}
