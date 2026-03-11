package com.example.ajouevent_be_v2.repository.adapter.member;

import com.example.ajouevent_be_v2.domain.member.Member;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberJpaRepositoryAdapter extends JpaRepository<Member, Long> {

    Optional<Member> findByEmail(String email);
}
