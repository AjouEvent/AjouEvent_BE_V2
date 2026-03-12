package com.example.ajouevent_be_v2.dto.keyword;

import com.example.ajouevent_be_v2.domain.member.Member;

public record KeywordUnsubscribeCommand(
    Member member,
    String encodedKeyword
) {}
