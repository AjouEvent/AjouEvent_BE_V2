package com.example.ajouevent_be_v2.common.discord;

import com.example.ajouevent_be_v2.common.exception.discord.DiscordErrorCode;
import com.example.ajouevent_be_v2.common.exception.discord.DiscordException;
import com.example.ajouevent_be_v2.config.DiscordFeignClient;
import com.example.ajouevent_be_v2.config.DiscordMemberFeignClient;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class DiscordMessageService {

    private final DiscordFeignClient discordFeignClient;
    private final DiscordMemberFeignClient discordMemberFeignClient;

    public void sendErrorMessage(String message) {
        try {
            discordFeignClient.sendMessage(new DiscordMessage(message));
        } catch (FeignException e) {
            log.error("Discord error webhook 전송 실패: {}", e.getMessage(), e);
            throw new DiscordException(DiscordErrorCode.WEBHOOK_FAILED, e);
        }
    }

    public void sendMemberMessage(String message) {
        try {
            discordMemberFeignClient.sendMessage(new DiscordMessage(message));
        } catch (FeignException e) {
            log.error("Discord member webhook 전송 실패: {}", e.getMessage(), e);
            throw new DiscordException(DiscordErrorCode.WEBHOOK_FAILED, e);
        }
    }
}
