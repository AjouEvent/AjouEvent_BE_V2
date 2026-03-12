package com.example.ajouevent_be_v2.repository.adapter.keyword;

import com.example.ajouevent_be_v2.domain.keyword.Keyword;
import com.example.ajouevent_be_v2.domain.keyword.KeywordMember;
import com.example.ajouevent_be_v2.domain.member.Member;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface KeywordMemberJpaRepositoryAdapter extends JpaRepository<KeywordMember, Long> {

    boolean existsByKeywordAndMember(Keyword keyword, Member member);

    long countByMember(Member member);

    void deleteByKeywordAndMember(Keyword keyword, Member member);

    @Query("SELECT km FROM KeywordMember km JOIN FETCH km.keyword k JOIN FETCH k.topic WHERE km.member = :member")
    List<KeywordMember> findByMemberWithKeywordAndTopic(@Param("member") Member member);

    @Query("SELECT km FROM KeywordMember km JOIN FETCH km.keyword WHERE km.member = :member")
    List<KeywordMember> findByMemberWithKeyword(@Param("member") Member member);

    @Modifying
    @Query("DELETE FROM KeywordMember km WHERE km.id IN :ids")
    void deleteAllByIds(@Param("ids") List<Long> ids);

    boolean existsByMemberAndIsReadFalse(Member member);
}
