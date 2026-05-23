package com.example.ajouevent_be_v2.orchestrator;

import com.example.ajouevent_be_v2.common.discord.DiscordMessageService;
import com.example.ajouevent_be_v2.domain.keyword.Keyword;
import com.example.ajouevent_be_v2.domain.member.Member;
import com.example.ajouevent_be_v2.domain.member.Token;
import com.example.ajouevent_be_v2.domain.topic.Topic;
import com.example.ajouevent_be_v2.dto.auth.AuthTokenResult;
import com.example.ajouevent_be_v2.dto.auth.GoogleUserInfoResult;
import com.example.ajouevent_be_v2.dto.auth.LoginResponse;
import com.example.ajouevent_be_v2.dto.auth.OauthRequest;
import com.example.ajouevent_be_v2.dto.auth.TestLoginResponse;
import com.example.ajouevent_be_v2.dto.member.MemberLoginResult;
import com.example.ajouevent_be_v2.service.auth.AuthService;
import com.example.ajouevent_be_v2.service.auth.OauthService;
import com.example.ajouevent_be_v2.service.keyword.KeywordQueryService;
import com.example.ajouevent_be_v2.service.member.MemberService;
import com.example.ajouevent_be_v2.service.token.FcmTokenCommandService;
import com.example.ajouevent_be_v2.service.token.TokenService;
import com.example.ajouevent_be_v2.service.topic.TopicQueryService;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuthOrchestrator {

    private final OauthService oauthService;
    private final AuthService authService;
    private final MemberService memberService;
    private final FcmTokenCommandService fcmTokenCommandService;
    private final TopicQueryService topicQueryService;
    private final KeywordQueryService keywordQueryService;
    private final TokenService tokenService;
    private final DiscordMessageService discordMessageService;

    public AuthTokenResult socialLogin(OauthRequest request) {
        GoogleUserInfoResult userInfo = oauthService.getUserInfo(request);
        MemberLoginResult memberResult = memberService.findOrCreateMember(userInfo);
        if (memberResult.isNewMember()) {
            sendNewMemberDiscordNotification(memberResult.member());
        }
        if (request.fcmToken() != null) {
            registerFcmToken(memberResult.member(), request.fcmToken());
        }
        return authService.issueTokens(memberResult.member(), memberResult.isNewMember());
    }

    public AuthTokenResult reissueToken(String refreshToken) {
        Long memberId = authService.extractMemberId(refreshToken);
        Member member = memberService.findById(memberId);
        return authService.issueTokens(member, null);
    }

    public LoginResponse testLogin(String email) {
        Member member = memberService.findByEmail(email);
        AuthTokenResult authResult = authService.issueTokens(member, false);
        return new LoginResponse(
            authResult.id(), "Authorization", authResult.accessToken(),
            authResult.name(), authResult.major(), authResult.email(), false);
    }

    public TestLoginResponse testLoginWithFcm(String email, String fcmToken) {
        String name = email.contains("@") ? email.split("@")[0] : email;
        Member member = memberService.findOrCreateByInfo(email, name, "소프트웨어학과");
        AuthTokenResult authResult = authService.issueTokens(member, false);
        registerFcmToken(member, fcmToken);
        return new TestLoginResponse(
            authResult.accessToken(), authResult.name(), authResult.major(),
            authResult.email(), false, fcmToken);
    }

    private void sendNewMemberDiscordNotification(Member member) {
        try {
            String message = String.format(
                "%d번째 유저 %s(%s) 님이 회원가입했습니다!",
                member.getId(),
                member.getName(),
                member.getMajor() != null ? member.getMajor() : "미입력"
            );
            discordMessageService.sendMessage(message);
        } catch (Exception e) {
            log.warn("Discord 신규 회원 알림 전송 실패 - email: {}", member.getEmail(), e);
        }
    }

    private void registerFcmToken(Member member, String fcmToken) {
        Optional<Token> newToken = fcmTokenCommandService.registerToken(member, fcmToken);
        if (newToken.isPresent()) {
            String tokenValue = newToken.get().getTokenValue();
            List<Topic> subscribedTopics = topicQueryService.getSubscribedTopics(member).stream()
                .map(topicMember -> topicMember.getTopic())
                .toList();
            List<Keyword> subscribedKeywords = keywordQueryService.getSubscribedKeywords(member).stream()
                .map(keywordMember -> keywordMember.getKeyword())
                .toList();
            tokenService.linkNewTokenToTopics(tokenValue, subscribedTopics);
            tokenService.linkNewTokenToKeywords(tokenValue, subscribedKeywords);
        }
    }
}
