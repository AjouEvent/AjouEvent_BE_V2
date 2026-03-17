package com.example.ajouevent_be_v2.repository.port.clubevent;

import com.example.ajouevent_be_v2.domain.clubevent.ClubEvent;
import com.example.ajouevent_be_v2.domain.clubevent.ClubEventLike;
import com.example.ajouevent_be_v2.domain.member.Member;
import com.example.ajouevent_be_v2.repository.adapter.clubevent.ClubEventLikeJpaRepositoryAdapter;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ClubEventLikeRepositoryPort {

    private final ClubEventLikeJpaRepositoryAdapter clubEventLikeJpaRepositoryAdapter;

    public ClubEventLike save(ClubEventLike clubEventLike) {
        return clubEventLikeJpaRepositoryAdapter.save(clubEventLike);
    }

    public boolean existsByMemberAndClubEvent(Member member, ClubEvent clubEvent) {
        return clubEventLikeJpaRepositoryAdapter.existsByMemberAndClubEvent(member, clubEvent);
    }

    public Optional<ClubEventLike> findByClubEventAndMember(ClubEvent clubEvent, Member member) {
        return clubEventLikeJpaRepositoryAdapter.findByClubEventAndMember(clubEvent, member);
    }

    public List<ClubEventLike> findByMemberWithClubEvent(Member member) {
        return clubEventLikeJpaRepositoryAdapter.findByMemberWithClubEvent(member);
    }

    public void delete(ClubEventLike clubEventLike) {
        clubEventLikeJpaRepositoryAdapter.delete(clubEventLike);
    }
}
