package com.example.ajouevent_be_v2.orchestrator.member;

import com.example.ajouevent_be_v2.domain.member.Member;
import com.example.ajouevent_be_v2.dto.auth.OauthRequest;
import com.example.ajouevent_be_v2.dto.member.MemberInfoResponse;
import com.example.ajouevent_be_v2.dto.member.MemberUpdateRequest;
import com.example.ajouevent_be_v2.service.member.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MemberOrchestrator {

    private final MemberService memberService;

    public MemberInfoResponse getMemberInfo(Member member) {
        return memberService.getMemberInfo(member);
    }

    public void updateMemberInfo(MemberUpdateRequest request, Member member) {
        memberService.updateMemberInfo(request, member);
    }

    public void deleteMember(Member member) {
        memberService.deleteMember(member);
    }

    public void connectCalendar(OauthRequest request) {
        // TODO : 캘린더 연동
    }
}
