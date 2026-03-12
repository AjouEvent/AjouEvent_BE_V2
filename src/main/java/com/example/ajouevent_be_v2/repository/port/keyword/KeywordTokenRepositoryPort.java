package com.example.ajouevent_be_v2.repository.port.keyword;

import com.example.ajouevent_be_v2.domain.keyword.Keyword;
import com.example.ajouevent_be_v2.domain.keyword.KeywordToken;
import com.example.ajouevent_be_v2.domain.member.Token;
import com.example.ajouevent_be_v2.repository.adapter.keyword.KeywordTokenBulkAdapter;
import com.example.ajouevent_be_v2.repository.adapter.keyword.KeywordTokenJpaRepositoryAdapter;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class KeywordTokenRepositoryPort {

    private final KeywordTokenJpaRepositoryAdapter keywordTokenJpaRepositoryAdapter;
    private final KeywordTokenBulkAdapter keywordTokenBulkAdapter;

    public void saveAll(List<KeywordToken> keywordTokens) {
        keywordTokenBulkAdapter.saveAll(keywordTokens);
    }

    public void deleteByKeywordAndTokens(Keyword keyword, List<Token> tokens) {
        List<String> tokenValues = tokens.stream()
            .map(Token::getTokenValue)
            .toList();
        keywordTokenJpaRepositoryAdapter.deleteByKeywordAndTokenValues(keyword, tokenValues);
    }

    public void deleteAllByTokenIds(List<Long> tokenIds) {
        keywordTokenJpaRepositoryAdapter.deleteAllByTokenIds(tokenIds);
    }

    public List<KeywordToken> findKeywordTokensWithKeyword(List<Token> tokens) {
        List<String> tokenValues = tokens.stream()
            .map(Token::getTokenValue)
            .toList();
        return keywordTokenJpaRepositoryAdapter.findKeywordTokensWithKeyword(tokenValues);
    }
}
