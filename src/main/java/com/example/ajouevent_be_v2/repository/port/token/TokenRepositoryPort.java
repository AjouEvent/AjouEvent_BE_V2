package com.example.ajouevent_be_v2.repository.port.token;

import com.example.ajouevent_be_v2.domain.member.Member;
import com.example.ajouevent_be_v2.domain.member.Token;
import com.example.ajouevent_be_v2.repository.adapter.token.TokenBulkRepositoryAdapter;
import com.example.ajouevent_be_v2.repository.adapter.token.TokenJpaRepositoryAdapter;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class TokenRepositoryPort {

    private final TokenJpaRepositoryAdapter tokenJpaRepositoryAdapter;
    private final TokenBulkRepositoryAdapter tokenBulkRepositoryAdapter;

    public List<Token> findByMember(Member member) {
        return tokenJpaRepositoryAdapter.findByMember(member);
    }

    public List<Token> findActiveTokensByMember(Member member) {
        return tokenJpaRepositoryAdapter.findByMemberAndIsDeletedFalse(member);
    }

    public void deleteAllByTokenIds(List<Long> tokenIds) {
        tokenJpaRepositoryAdapter.deleteAllByTokenIds(tokenIds);
    }

    public void batchSoftDeleteTokens(List<Token> tokens) {
        tokenBulkRepositoryAdapter.batchSoftDeleteTokens(tokens);
    }

    public Token save(Token token) {
        return tokenJpaRepositoryAdapter.save(token);
    }

    public Optional<Token> findByTokenValueAndMember(String tokenValue, Member member) {
        return tokenJpaRepositoryAdapter.findByTokenValueAndMember(tokenValue, member);
    }

    public List<Token> findAllActiveTokens() {
        return tokenJpaRepositoryAdapter.findByIsDeletedFalse();
    }

    public List<Token> findActiveTokensByMembers(List<Member> members) {
        return tokenJpaRepositoryAdapter.findByMemberInAndIsDeletedFalse(members);
    }
}
