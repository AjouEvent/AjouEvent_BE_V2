package com.example.ajouevent_be_v2.service.keyword;

import com.example.ajouevent_be_v2.common.exception.keyword.KeywordErrorCode;
import com.example.ajouevent_be_v2.common.exception.keyword.KeywordException;
import com.example.ajouevent_be_v2.domain.keyword.Keyword;
import com.example.ajouevent_be_v2.domain.keyword.KeywordMember;
import com.example.ajouevent_be_v2.domain.member.Member;
import com.example.ajouevent_be_v2.domain.topic.Topic;
import com.example.ajouevent_be_v2.dto.clubevent.ClubEventCommand;
import com.example.ajouevent_be_v2.repository.port.keyword.KeywordMemberRepositoryPort;
import com.example.ajouevent_be_v2.repository.port.keyword.KeywordRepositoryPort;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

// V1 참고: service/KeywordService.java — getUserKeyword() 포팅
@Service
@RequiredArgsConstructor
public class KeywordQueryService {

    private final KeywordMemberRepositoryPort keywordMemberRepositoryPort;
    private final KeywordRepositoryPort keywordRepositoryPort;

    public List<Keyword> findMatchingByClubEvent(Topic topic, ClubEventCommand command) {
        return keywordRepositoryPort.findByTopic(topic).stream()
            .filter(keyword -> {
                String kw = keyword.getKoreanKeyword().toLowerCase();
                return (command.title() != null && command.title().toLowerCase().contains(kw))
                    || (command.content() != null && command.content().toLowerCase().contains(kw));
            })
            .toList();
    }

    public List<KeywordMember> getUserKeywords(Member member) {
        return keywordMemberRepositoryPort.findByMemberWithKeywordAndTopic(member);
    }

    public boolean hasUnreadKeywords(Member member) {
        return keywordMemberRepositoryPort.existsByMemberAndIsReadFalse(member);
    }

    // FCM 토큰 등록 시 KeywordToken 매핑용 — Topic join 불필요
    public List<KeywordMember> getSubscribedKeywords(Member member) {
        return keywordMemberRepositoryPort.findByMemberWithKeyword(member);
    }

    public Keyword getSubscribedKeywordByName(Member member, String keyword) {
        return keywordMemberRepositoryPort.findByMemberAndKeywordName(member, keyword)
            .map(KeywordMember::getKeyword)
            .orElseThrow(() -> new KeywordException(KeywordErrorCode.KEYWORD_NOT_FOUND));
    }
}
