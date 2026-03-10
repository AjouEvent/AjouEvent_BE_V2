package com.example.ajouevent_be_v2.service.member;

import com.example.ajouevent_be_v2.common.exception.member.MemberErrorCode;
import com.example.ajouevent_be_v2.common.exception.member.MemberException;
import com.example.ajouevent_be_v2.domain.member.Member;
import com.example.ajouevent_be_v2.domain.member.Token;
import com.example.ajouevent_be_v2.dto.auth.GoogleUserInfoResult;
import com.example.ajouevent_be_v2.dto.member.MemberInfoResponse;
import com.example.ajouevent_be_v2.dto.member.MemberLoginResult;
import com.example.ajouevent_be_v2.dto.member.MemberUpdateRequest;
import com.example.ajouevent_be_v2.repository.port.member.MemberRepository;
import com.example.ajouevent_be_v2.repository.port.member.TokenRepository;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class MemberService {

    private final MemberRepository memberRepository;
    private final TokenRepository tokenRepository;

    @Transactional
    public MemberLoginResult findOrCreateMember(GoogleUserInfoResult userInfo) {
        Optional<Member> memberOptional = memberRepository.findByEmail(userInfo.email());
        if (memberOptional.isPresent()) {
            return new MemberLoginResult(memberOptional.get(), false);
        }

        Member member = new Member(userInfo.email(), userInfo.name(), userInfo.department());
        memberRepository.save(member);
        log.info("신규 회원 가입 - email: {}", userInfo.email()); // TODO : discord 웹훅 전송
        return new MemberLoginResult(member, true);
    }

    public Member findByEmail(String email) {
        return memberRepository.findByEmail(email)
            .orElseThrow(() -> new MemberException(MemberErrorCode.MEMBER_NOT_FOUND));
    }

    public MemberInfoResponse getMemberInfo(Member member) {
        return new MemberInfoResponse(member.getName(), member.getEmail(), member.getMajor());
    }

    @Transactional
    public void updateMemberInfo(MemberUpdateRequest request, Member member) {
        member.updateInfo(request.name(), request.major());
    }

    @Transactional
    public void deleteMember(Member member) {
        List<Token> memberTokens = tokenRepository.findByMember(member);
        List<Long> tokenIds = memberTokens.stream().map(Token::getId).toList();
        if (!tokenIds.isEmpty()) {
            tokenRepository.deleteAllByTokenIds(tokenIds);
        }
        memberRepository.delete(member);
    }
}
