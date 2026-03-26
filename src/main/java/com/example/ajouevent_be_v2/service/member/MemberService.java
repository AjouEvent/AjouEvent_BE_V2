package com.example.ajouevent_be_v2.service.member;

import com.example.ajouevent_be_v2.common.discord.DiscordMessageService;
import com.example.ajouevent_be_v2.common.exception.member.MemberErrorCode;
import com.example.ajouevent_be_v2.common.exception.member.MemberException;
import com.example.ajouevent_be_v2.domain.member.Member;
import com.example.ajouevent_be_v2.dto.auth.GoogleUserInfoResult;
import com.example.ajouevent_be_v2.dto.member.MemberLoginResult;
import com.example.ajouevent_be_v2.dto.member.MemberUpdateRequest;
import com.example.ajouevent_be_v2.dto.member.RegisterMemberInfoRequest;
import com.example.ajouevent_be_v2.repository.port.member.MemberRepositoryPort;
import java.time.LocalDateTime;
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
    private final DiscordMessageService discordMessageService;

    @Transactional
    public MemberLoginResult findOrCreateMember(GoogleUserInfoResult userInfo) {
        Optional<Member> memberOptional = memberRepositoryPort.findByEmail(userInfo.email());
        if (memberOptional.isPresent()) {
            return new MemberLoginResult(memberOptional.get(), false);
        }

        Member member = new Member(userInfo.email(), userInfo.name(), userInfo.department());
        memberRepositoryPort.save(member);
        log.info("신규 회원 가입 - email: {}", userInfo.email());
        sendNewMemberDiscordNotification(member);
        return new MemberLoginResult(member, true);
    }

    public Member findById(Long id) {
        return memberRepositoryPort.findById(id)
            .orElseThrow(() -> new MemberException(MemberErrorCode.MEMBER_NOT_FOUND));
    }

    public Member findByEmail(String email) {
        return memberRepositoryPort.findByEmail(email)
            .orElseThrow(() -> new MemberException(MemberErrorCode.MEMBER_NOT_FOUND));
    }

    @Transactional
    public void updateMemberInfo(MemberUpdateRequest request, Member member) {
        member.updateInfo(request.name(), request.major());
        memberRepositoryPort.save(member);
    }

    @Transactional
    public Member registerMemberInfo(RegisterMemberInfoRequest request, Member member) {
        if (request.major() != null) {
            member.updateInfo(null, request.major());
            memberRepositoryPort.save(member);
        }
        return member;
    }

    @Transactional
    public Member findOrCreateByInfo(String email, String name, String major) {
        return memberRepositoryPort.findByEmail(email)
            .orElseGet(() -> {
                Member member = new Member(email, name, major);
                return memberRepositoryPort.save(member);
            });
    }

    @Transactional
    public void deleteMember(Member member) {
        // Token 은 (CascadeType.REMOVE + orphanRemoval = true 설정으로) 자동 삭제
        memberRepositoryPort.delete(member);
    }

    @Transactional
    public void saveGoogleCalendarRefreshToken(Member member, String refreshToken) {
        member.updateGoogleCalendarRefreshToken(refreshToken);
        memberRepositoryPort.save(member);
    }

    private void sendNewMemberDiscordNotification(Member member) {
        try {
            String message = String.format(
                "🎉 신규 회원 가입\n가입 일시: %s\n이름: %s\n이메일: %s\n학과: %s",
                LocalDateTime.now(),
                member.getName(),
                member.getEmail(),
                member.getMajor() != null ? member.getMajor() : "미입력"
            );
            discordMessageService.sendMessage(message);
        } catch (Exception e) {
            log.warn("Discord 신규 회원 알림 전송 실패 - email: {}", member.getEmail(), e);
        }
    }
}
