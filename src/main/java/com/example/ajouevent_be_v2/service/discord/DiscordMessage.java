package com.example.ajouevent_be_v2.service.discord;

public record DiscordMessage(String content) {

    public static DiscordMessage of(String message) {
        return new DiscordMessage(message);
    }
}
