package com.example.ajouevent_be_v2.repository.adapter.event;

import com.example.ajouevent_be_v2.domain.event.ClubEvent;
import com.example.ajouevent_be_v2.domain.event.EventLike;
import com.example.ajouevent_be_v2.domain.member.Member;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EventLikeJpaRepositoryAdapter extends JpaRepository<EventLike, Long> {

    boolean existsByMemberAndClubEvent(Member member, ClubEvent clubEvent);

    Optional<EventLike> findByClubEventAndMember(ClubEvent clubEvent, Member member);

    @EntityGraph(attributePaths = {"clubEvent"})
    @Query("SELECT el FROM EventLike el INNER JOIN el.clubEvent ce WHERE el.member = :member")
    List<EventLike> findByMemberWithClubEvent(@Param("member") Member member);
}
