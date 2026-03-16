package com.example.ajouevent_be_v2.repository.port.event;

import com.example.ajouevent_be_v2.domain.event.ClubEvent;
import com.example.ajouevent_be_v2.domain.event.EventLike;
import com.example.ajouevent_be_v2.domain.member.Member;
import com.example.ajouevent_be_v2.repository.adapter.event.EventLikeJpaRepositoryAdapter;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class EventLikeRepositoryPort {

    private final EventLikeJpaRepositoryAdapter eventLikeJpaRepositoryAdapter;

    public EventLike save(EventLike eventLike) {
        return eventLikeJpaRepositoryAdapter.save(eventLike);
    }

    public boolean existsByMemberAndClubEvent(Member member, ClubEvent clubEvent) {
        return eventLikeJpaRepositoryAdapter.existsByMemberAndClubEvent(member, clubEvent);
    }

    public Optional<EventLike> findByClubEventAndMember(ClubEvent clubEvent, Member member) {
        return eventLikeJpaRepositoryAdapter.findByClubEventAndMember(clubEvent, member);
    }

    public List<EventLike> findByMemberWithClubEvent(Member member) {
        return eventLikeJpaRepositoryAdapter.findByMemberWithClubEvent(member);
    }

    public void delete(EventLike eventLike) {
        eventLikeJpaRepositoryAdapter.delete(eventLike);
    }
}
