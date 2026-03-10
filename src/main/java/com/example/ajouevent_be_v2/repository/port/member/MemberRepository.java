package com.example.ajouevent_be_v2.repository.port.member;

import com.example.ajouevent_be_v2.domain.member.Member;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {

    Optional<Member> findByEmail(String email);

    @Query("SELECT m FROM Member m LEFT JOIN FETCH m.topicMembers WHERE m.email = :email")
    Optional<Member> findByEmailWithSubscriptions(@Param("email") String email);

    @Query("SELECT m FROM Member m LEFT JOIN FETCH m.tokens WHERE m.email = :email")
    Optional<Member> findByEmailWithTokens(@Param("email") String email);

    @Query("SELECT m FROM Member m LEFT JOIN FETCH m.tokens t WHERE m.email = :email AND t.isDeleted = false")
    Optional<Member> findByEmailWithValidTokens(@Param("email") String email);
}
