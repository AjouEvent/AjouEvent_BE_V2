package com.example.ajouevent_be_v2.repository.port.member;

import com.example.ajouevent_be_v2.domain.member.Member;
import com.example.ajouevent_be_v2.domain.member.Token;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface TokenRepository extends JpaRepository<Token, Long> {

    Optional<Token> findByTokenValueAndMember(String tokenValue, Member member);

    List<Token> findByExpirationDate(LocalDate date);

    List<Token> findByMember(Member member);

    @Modifying
    @Query("DELETE FROM Token t WHERE t.id IN :tokenIds")
    void deleteAllByTokenIds(@Param("tokenIds") List<Long> tokenIds);

    List<Token> findByIsDeletedFalse();
}
