package com.example.ajouevent_be_v2.orchestrator;

import com.example.ajouevent_be_v2.domain.member.Member;
import com.example.ajouevent_be_v2.dto.auth.OauthRequest;
import com.example.ajouevent_be_v2.dto.member.MemberInfoResponse;
import com.example.ajouevent_be_v2.dto.member.MemberUpdateRequest;
import com.example.ajouevent_be_v2.dto.member.RegisterMemberInfoRequest;
import com.example.ajouevent_be_v2.dto.member.RegisterMemberInfoResponse;
import com.example.ajouevent_be_v2.service.auth.OauthService;
import com.example.ajouevent_be_v2.service.member.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MemberOrchestrator {

    private final MemberService memberService;
    private final OauthService oauthService;

    public MemberInfoResponse getMemberInfo(Member member) {
        return new MemberInfoResponse(member.getName(), member.getEmail(), member.getMajor());
    }

    public void updateMemberInfo(MemberUpdateRequest request, Member member) {
        memberService.updateMemberInfo(request, member);
    }

    public RegisterMemberInfoResponse registerMemberInfo(RegisterMemberInfoRequest request, Member member) {
        Member updated = memberService.registerMemberInfo(request, member);
        return new RegisterMemberInfoResponse(updated.getEmail(), updated.getName());
    }

    public void deleteMember(Member member) {
        memberService.deleteMember(member);
    }

    public void connectCalendar(OauthRequest request, Member member) {
        String refreshToken = oauthService.exchangeForCalendarRefreshToken(request);
        memberService.saveGoogleCalendarRefreshToken(member, refreshToken);
    }
}
