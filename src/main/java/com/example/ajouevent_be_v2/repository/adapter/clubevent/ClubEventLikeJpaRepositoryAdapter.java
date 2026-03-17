package com.example.ajouevent_be_v2.repository.adapter.clubevent;

import com.example.ajouevent_be_v2.domain.clubevent.ClubEvent;
import com.example.ajouevent_be_v2.domain.clubevent.ClubEventLike;
import com.example.ajouevent_be_v2.domain.member.Member;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ClubEventLikeJpaRepositoryAdapter extends JpaRepository<ClubEventLike, Long> {

    boolean existsByMemberAndClubEvent(Member member, ClubEvent clubEvent);

    Optional<ClubEventLike> findByClubEventAndMember(ClubEvent clubEvent, Member member);

    @EntityGraph(attributePaths = {"clubEvent"})
    @Query("SELECT el FROM ClubEventLike el INNER JOIN el.clubEvent ce WHERE el.member = :member")
    List<ClubEventLike> findByMemberWithClubEvent(@Param("member") Member member);
}
