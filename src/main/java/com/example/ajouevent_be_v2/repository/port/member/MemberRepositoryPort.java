package com.example.ajouevent_be_v2.repository.port.member;

import com.example.ajouevent_be_v2.domain.member.Member;
import com.example.ajouevent_be_v2.repository.adapter.member.MemberJpaRepositoryAdapter;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class MemberRepositoryPort {

    private final MemberJpaRepositoryAdapter memberJpaRepositoryAdapter;

    public Member save(Member member) {
        return memberJpaRepositoryAdapter.save(member);
    }

    public Optional<Member> findByEmail(String email) {
        return memberJpaRepositoryAdapter.findByEmail(email);
    }

    public void delete(Member member) {
        memberJpaRepositoryAdapter.delete(member);
    }
}
