package com.example.ajouevent_be_v2.service.clubevent;

import com.example.ajouevent_be_v2.common.exception.clubevent.ClubEventErrorCode;
import com.example.ajouevent_be_v2.common.exception.clubevent.ClubEventException;
import com.example.ajouevent_be_v2.domain.event.ClubEvent;
import com.example.ajouevent_be_v2.domain.event.EventLike;
import com.example.ajouevent_be_v2.domain.member.Member;
import com.example.ajouevent_be_v2.repository.port.event.ClubEventRepositoryPort;
import com.example.ajouevent_be_v2.repository.port.event.EventLikeRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class EventLikeCommandService {

    private final ClubEventRepositoryPort clubEventRepositoryPort;
    private final EventLikeRepositoryPort eventLikeRepositoryPort;

    @Transactional
    public void likeEvent(ClubEvent clubEvent, Member member) {
        if (eventLikeRepositoryPort.existsByMemberAndClubEvent(member, clubEvent)) {
            throw new ClubEventException(ClubEventErrorCode.ALREADY_LIKED);
        }
        clubEvent.incrementLikes();
        clubEventRepositoryPort.save(clubEvent);
        eventLikeRepositoryPort.save(EventLike.builder()
            .clubEvent(clubEvent)
            .member(member)
            .build());
    }

    @Transactional
    public void cancelLike(ClubEvent clubEvent, EventLike eventLike) {
        clubEvent.decreaseLikes();
        clubEventRepositoryPort.save(clubEvent);
        eventLikeRepositoryPort.delete(eventLike);
    }
}
