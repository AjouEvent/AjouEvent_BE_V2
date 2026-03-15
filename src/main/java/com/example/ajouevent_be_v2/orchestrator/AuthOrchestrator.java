package com.example.ajouevent_be_v2.orchestrator;

import com.example.ajouevent_be_v2.domain.member.Member;
import com.example.ajouevent_be_v2.dto.auth.AuthTokenResult;
import com.example.ajouevent_be_v2.dto.auth.GoogleUserInfoResult;
import com.example.ajouevent_be_v2.dto.auth.OauthRequest;
import com.example.ajouevent_be_v2.dto.member.MemberLoginResult;
import com.example.ajouevent_be_v2.service.auth.AuthService;
import com.example.ajouevent_be_v2.service.auth.OauthService;
import com.example.ajouevent_be_v2.service.member.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AuthOrchestrator {

    private final OauthService oauthService;
    private final AuthService authService;
    private final MemberService memberService;

    public AuthTokenResult socialLogin(OauthRequest request) {
        // TODO : [#10 subscription 완료 후 연결] topicService.saveFCMToken(request.fcmToken())
        GoogleUserInfoResult userInfo = oauthService.getUserInfo(request);
        MemberLoginResult memberResult = memberService.findOrCreateMember(userInfo);
        return authService.issueTokens(memberResult.member(), memberResult.isNewMember());
    }

    public AuthTokenResult reissueToken(String refreshToken) {
        String email = authService.extractEmail(refreshToken);
        Member member = memberService.findByEmail(email);
        return authService.issueTokens(member, null);
    }

    public AuthTokenResult testLogin(String email) {
        Member member = memberService.findByEmail(email);
        return authService.issueTokens(member, false);
    }
}
