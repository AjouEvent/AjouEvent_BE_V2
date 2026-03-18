package com.example.ajouevent_be_v2.service.member;

import com.example.ajouevent_be_v2.common.exception.member.MemberErrorCode;
import com.example.ajouevent_be_v2.common.exception.member.MemberException;
import com.example.ajouevent_be_v2.domain.member.Member;
import com.example.ajouevent_be_v2.dto.auth.GoogleUserInfoResult;
import com.example.ajouevent_be_v2.dto.member.MemberLoginResult;
import com.example.ajouevent_be_v2.dto.member.MemberUpdateRequest;
import com.example.ajouevent_be_v2.repository.port.member.MemberRepositoryPort;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class MemberService {

    private final MemberRepositoryPort memberRepositoryPort;

    @Transactional
    public MemberLoginResult findOrCreateMember(GoogleUserInfoResult userInfo) {
        Optional<Member> memberOptional = memberRepositoryPort.findByEmail(userInfo.email());
        if (memberOptional.isPresent()) {
            return new MemberLoginResult(memberOptional.get(), false);
        }

        Member member = new Member(userInfo.email(), userInfo.name(), userInfo.department());
        memberRepositoryPort.save(member);
        log.info("신규 회원 가입 - email: {}", userInfo.email()); // TODO : discord 웹훅 전송
        return new MemberLoginResult(member, true);
    }

    public Member findByEmail(String email) {
        return memberRepositoryPort.findByEmail(email)
            .orElseThrow(() -> new MemberException(MemberErrorCode.MEMBER_NOT_FOUND));
    }

    @Transactional
    public Member findOrCreateByInfo(String email, String name, String major) {
        return memberRepositoryPort.findByEmail(email)
            .orElseGet(() -> {
                Member member = Member.builder().email(email).name(name).major(major).build();
                log.info("테스트 계정 생성 - email: {}", email);
                return memberRepositoryPort.save(member);
            });
    }

    @Transactional
    public void updateMemberInfo(MemberUpdateRequest request, Member member) {
        member.updateInfo(request.name(), request.major());
        memberRepositoryPort.save(member);
    }

    @Transactional
    public void deleteMember(Member member) {
        // Token 은 (CascadeType.REMOVE + orphanRemoval = true 설정으로) 자동 삭제
        memberRepositoryPort.delete(member);
    }
}
