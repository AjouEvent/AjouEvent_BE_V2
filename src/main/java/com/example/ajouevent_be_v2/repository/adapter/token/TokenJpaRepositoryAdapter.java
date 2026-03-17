package com.example.ajouevent_be_v2.repository.adapter.token;

import com.example.ajouevent_be_v2.domain.member.Member;
import com.example.ajouevent_be_v2.domain.member.Token;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TokenJpaRepositoryAdapter extends JpaRepository<Token, Long> {

    List<Token> findByMember(Member member);

    List<Token> findByMemberAndIsDeletedFalse(Member member);

    List<Token> findByIsDeletedFalse();

    @Query("SELECT t FROM Token t WHERE t.member IN :members AND t.isDeleted = false")
    List<Token> findByMemberInAndIsDeletedFalse(@Param("members") List<Member> members);

    Optional<Token> findByTokenValueAndMember(String tokenValue, Member member);

    @Query("SELECT t FROM Token t WHERE t.tokenValue IN :tokenValues AND t.isDeleted = false")
    List<Token> findByTokenValueInAndIsDeletedFalse(@Param("tokenValues") List<String> tokenValues);

    @Modifying
    @Query("DELETE FROM Token t WHERE t.id IN :tokenIds")
    void deleteAllByTokenIds(@Param("tokenIds") List<Long> tokenIds);
}
