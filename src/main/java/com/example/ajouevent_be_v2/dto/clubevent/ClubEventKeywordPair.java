package com.example.ajouevent_be_v2.dto.clubevent;

import com.example.ajouevent_be_v2.domain.clubevent.ClubEvent;

/**
 * clubEvent 오케스트레이터에서 keyword와 공지 매핑하는 객체로 사용되는 DTO
 * @param event
 * @param keyword
 */
public record ClubEventKeywordPair(ClubEvent event, String keyword) {}
