package com.example.ajouevent_be_v2.common.discord;

public record DiscordMessage(String content) {

    public static DiscordMessage createDiscordMessage(String message) {
        return new DiscordMessage(message);
    }
}
