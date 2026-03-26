package com.example.ajouevent_be_v2.orchestrator;

import com.example.ajouevent_be_v2.domain.member.Member;
import com.example.ajouevent_be_v2.dto.auth.OauthRequest;
import com.example.ajouevent_be_v2.dto.member.MemberInfoResponse;
import com.example.ajouevent_be_v2.dto.member.MemberUpdateRequest;
import com.example.ajouevent_be_v2.dto.member.RegisterMemberInfoRequest;
import com.example.ajouevent_be_v2.dto.member.RegisterMemberInfoResponse;
import com.example.ajouevent_be_v2.service.calendar.CalendarCommandService;
import com.example.ajouevent_be_v2.service.member.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MemberOrchestrator {

    private final MemberService memberService;
    private final CalendarCommandService calendarCommandService;

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

    /**
     * Google 캘린더 연동
     *
     * 1. Google OAuth 인가 코드를 access token / refresh token으로 교환
     * 2. refresh token을 서버 로컬 파일(tokens/{email})에 저장
     *
     * 이후 일정 추가 요청 시 파일에서 refresh token을 읽어 Calendar API를 호출한다.
     * refresh token이 없으면(prompt=consent&access_type=offline 미포함) 400 반환.
     */
    public void connectCalendar(OauthRequest request, Member member) {
        calendarCommandService.connect(request, member.getEmail());
    }
}
