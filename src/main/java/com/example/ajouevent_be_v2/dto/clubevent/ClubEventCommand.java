package com.example.ajouevent_be_v2.dto.clubevent;

import java.time.LocalDateTime;
import java.util.List;

public record ClubEventCommand(
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
}
