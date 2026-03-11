package com.example.ajouevent_be_v2.dto.member;

import com.example.ajouevent_be_v2.domain.member.Member;

public record MemberLoginResult(
        Member member,
        boolean isNewMember
) {
}
