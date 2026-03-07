package com.example.ajouevent_be_v2.domain.member;

import jakarta.persistence.Id;
import lombok.Getter;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.index.Indexed;

@Getter
@RedisHash(value = "EmailCheck", timeToLive = 300)
public class EmailCheck {

    @Id
    private String id;

    @Indexed
    private final String email;

    private String code;

    public EmailCheck(String email, String code) {
        this.email = email;
        this.code = code;
    }
}
