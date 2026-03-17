package com.example.ajouevent_be_v2.dto.webhook;

import com.example.ajouevent_be_v2.dto.clubevent.ClubEventCommand;
import java.time.LocalDateTime;
import java.util.List;

public record WebhookRequest(
        String title,
        String content,
        String category,
        String department,
        String englishTopic,
        String koreanTopic,
        String url,
        List<String> images,
        LocalDateTime date
) {

    public ClubEventCommand toClubEventCommand() {
        return new ClubEventCommand(title, content, category, department, englishTopic, koreanTopic, url, images, date);
    }
}
