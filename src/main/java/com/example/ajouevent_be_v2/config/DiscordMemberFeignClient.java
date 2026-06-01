package com.example.ajouevent_be_v2.config;

import com.example.ajouevent_be_v2.common.discord.DiscordMessage;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "${ajou.discord.member.name}", url = "${ajou.discord.member.webhook-url}")
public interface DiscordMemberFeignClient {

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    void sendMessage(@RequestBody DiscordMessage discordMessage);
}
