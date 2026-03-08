package com.example.ajouevent_be_v2.common.discord;

import static com.example.ajouevent_be_v2.common.discord.DiscordMessage.createDiscordMessage;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class DiscordMessageProvider {

    private final DiscordFeignClient discordFeignClient;

    public void sendMessage(String eventMessage) {
        DiscordMessage discordMessage = createDiscordMessage(eventMessage);
        sendMessageToDiscord(discordMessage);
    }

    private void sendMessageToDiscord(DiscordMessage discordMessage) {
        try {
            discordFeignClient.sendMessage(discordMessage);
        } catch (FeignException e) {
            log.error("Discord webhook 전송 실패: {}", e.getMessage(), e);
            throw new IllegalArgumentException("Discord webhook call failed", e);
        }
    }
}
