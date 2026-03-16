package com.example.ajouevent_be_v2.service.clubevent;

import com.example.ajouevent_be_v2.common.exception.clubevent.ClubEventErrorCode;
import com.example.ajouevent_be_v2.common.exception.clubevent.ClubEventException;
import com.example.ajouevent_be_v2.domain.event.ClubEvent;
import com.example.ajouevent_be_v2.domain.event.EventLike;
import com.example.ajouevent_be_v2.domain.member.Member;
import com.example.ajouevent_be_v2.repository.port.event.EventLikeRepositoryPort;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EventLikeQueryService {

    private final EventLikeRepositoryPort eventLikeRepositoryPort;

    public Set<Long> getLikedEventIds(Member member) {
        if (member == null) return Set.of();
        return eventLikeRepositoryPort.findByMemberWithClubEvent(member).stream()
            .map(el -> el.getClubEvent().getEventId())
            .collect(Collectors.toSet());
    }

    public boolean isEventLiked(Member member, ClubEvent event) {
        if (member == null) return false;
        return eventLikeRepositoryPort.existsByMemberAndClubEvent(member, event);
    }

    public List<EventLike> getLikedEventsWithDetails(Member member) {
        return eventLikeRepositoryPort.findByMemberWithClubEvent(member);
    }

    public EventLike getEventLike(ClubEvent clubEvent, Member member) {
        return eventLikeRepositoryPort.findByClubEventAndMember(clubEvent, member)
            .orElseThrow(() -> new ClubEventException(ClubEventErrorCode.EVENT_NOT_LIKED));
    }
}
