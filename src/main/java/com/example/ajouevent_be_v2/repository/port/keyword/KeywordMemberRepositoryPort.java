package com.example.ajouevent_be_v2.repository.port.keyword;

import com.example.ajouevent_be_v2.domain.keyword.Keyword;
import com.example.ajouevent_be_v2.domain.keyword.KeywordMember;
import com.example.ajouevent_be_v2.domain.member.Member;
import com.example.ajouevent_be_v2.repository.adapter.keyword.KeywordMemberJpaRepositoryAdapter;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class KeywordMemberRepositoryPort {

    private final KeywordMemberJpaRepositoryAdapter keywordMemberJpaRepositoryAdapter;

    public KeywordMember save(KeywordMember keywordMember) {
        return keywordMemberJpaRepositoryAdapter.save(keywordMember);
    }

    public boolean existsByKeywordAndMember(Keyword keyword, Member member) {
        return keywordMemberJpaRepositoryAdapter.existsByKeywordAndMember(keyword, member);
    }

    public long countByMember(Member member) {
        return keywordMemberJpaRepositoryAdapter.countByMember(member);
    }

    public void deleteByKeywordAndMember(Keyword keyword, Member member) {
        keywordMemberJpaRepositoryAdapter.deleteByKeywordAndMember(keyword, member);
    }

    public List<KeywordMember> findByMemberWithKeywordAndTopic(Member member) {
        return keywordMemberJpaRepositoryAdapter.findByMemberWithKeywordAndTopic(member);
    }

    public List<KeywordMember> findByMemberWithKeyword(Member member) {
        return keywordMemberJpaRepositoryAdapter.findByMemberWithKeyword(member);
    }

    public void deleteAllByIds(List<Long> ids) {
        keywordMemberJpaRepositoryAdapter.deleteAllByIds(ids);
    }

    public boolean existsByMemberAndIsReadFalse(Member member) {
        return keywordMemberJpaRepositoryAdapter.existsByMemberAndIsReadFalse(member);
    }

    public Optional<KeywordMember> findByMemberAndKeywordName(Member member, String keyword) {
        return keywordMemberJpaRepositoryAdapter.findByMemberAndKeywordName(member, keyword);
    }

    public Optional<KeywordMember> findByKeywordAndMemberAndIsReadFalse(Keyword keyword, Member member) {
        return keywordMemberJpaRepositoryAdapter.findByKeywordAndMemberAndIsReadFalse(keyword, member);
    }
}
