package com.example.ajouevent_be_v2.dto.clubevent;

public record CalendarRequest(
    String summary,
    String description,
    String startDate,
    String endDate
) {
}
