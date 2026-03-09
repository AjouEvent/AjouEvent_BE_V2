package com.example.ajouevent_be_v2.service.discord;

import com.example.ajouevent_be_v2.service.discord.exception.DiscordErrorCode;
import com.example.ajouevent_be_v2.service.discord.exception.DiscordException;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class DiscordMessageService {

    private final DiscordFeignClient discordFeignClient;

    public void sendMessage(String message) {
        try {
            discordFeignClient.sendMessage(DiscordMessage.of(message));
        } catch (FeignException e) {
            log.error("Discord webhook 전송 실패: {}", e.getMessage(), e);
            throw new DiscordException(DiscordErrorCode.WEBHOOK_FAILED, e);
        }
    }
}
