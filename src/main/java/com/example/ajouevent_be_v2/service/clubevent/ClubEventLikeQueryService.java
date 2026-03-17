package com.example.ajouevent_be_v2.service.clubevent;

import com.example.ajouevent_be_v2.common.exception.clubevent.ClubEventErrorCode;
import com.example.ajouevent_be_v2.common.exception.clubevent.ClubEventException;
import com.example.ajouevent_be_v2.domain.clubevent.ClubEvent;
import com.example.ajouevent_be_v2.domain.clubevent.ClubEventLike;
import com.example.ajouevent_be_v2.domain.member.Member;
import com.example.ajouevent_be_v2.repository.port.clubevent.ClubEventLikeRepositoryPort;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ClubEventLikeQueryService {

    private final ClubEventLikeRepositoryPort clubEventLikeRepositoryPort;

    public Set<Long> getLikedEventIds(Member member) {
        if (member == null) return Set.of();
        return clubEventLikeRepositoryPort.findByMemberWithClubEvent(member).stream()
            .map(el -> el.getClubEvent().getEventId())
            .collect(Collectors.toSet());
    }

    public boolean isEventLiked(Member member, ClubEvent event) {
        if (member == null) return false;
        return clubEventLikeRepositoryPort.existsByMemberAndClubEvent(member, event);
    }

    public List<ClubEventLike> getLikedEventsWithDetails(Member member) {
        return clubEventLikeRepositoryPort.findByMemberWithClubEvent(member);
    }

    public ClubEventLike getEventLike(ClubEvent clubEvent, Member member) {
        return clubEventLikeRepositoryPort.findByClubEventAndMember(clubEvent, member)
            .orElseThrow(() -> new ClubEventException(ClubEventErrorCode.EVENT_NOT_LIKED));
    }
}
