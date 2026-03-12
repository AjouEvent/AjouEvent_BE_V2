package com.example.ajouevent_be_v2.service.keyword;

import com.example.ajouevent_be_v2.domain.keyword.KeywordMember;
import com.example.ajouevent_be_v2.domain.member.Member;
import com.example.ajouevent_be_v2.repository.port.keyword.KeywordMemberRepositoryPort;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

// V1 참고: service/KeywordService.java — getUserKeyword() 포팅
@Service
@RequiredArgsConstructor
public class KeywordQueryService {

    private final KeywordMemberRepositoryPort keywordMemberRepositoryPort;

    public List<KeywordMember> getUserKeywords(Member member) {
        return keywordMemberRepositoryPort.findByMemberWithKeywordAndTopic(member);
    }
}
