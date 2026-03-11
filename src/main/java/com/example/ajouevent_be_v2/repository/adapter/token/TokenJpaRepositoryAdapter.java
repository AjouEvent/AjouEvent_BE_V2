package com.example.ajouevent_be_v2.repository.adapter.token;

import com.example.ajouevent_be_v2.domain.member.Member;
import com.example.ajouevent_be_v2.domain.member.Token;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TokenJpaRepositoryAdapter extends JpaRepository<Token, Long> {

    List<Token> findByMember(Member member);

    @Modifying
    @Query("DELETE FROM Token t WHERE t.id IN :tokenIds")
    void deleteAllByTokenIds(@Param("tokenIds") List<Long> tokenIds);
}
