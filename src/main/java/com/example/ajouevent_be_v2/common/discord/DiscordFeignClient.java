package com.example.ajouevent_be_v2.common.discord;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "${ajou.discord.name}", url = "${ajou.discord.webhook-url}")
public interface DiscordFeignClient {

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    void sendMessage(@RequestBody DiscordMessage discordMessage);
}
