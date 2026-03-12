package com.example.ajouevent_be_v2.repository.adapter.keyword;

import com.example.ajouevent_be_v2.domain.keyword.Keyword;
import com.example.ajouevent_be_v2.domain.keyword.KeywordToken;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface KeywordTokenJpaRepositoryAdapter extends JpaRepository<KeywordToken, Long> {

    // V2: KeywordToken은 Token FK 없이 tokenValue(String)만 보유 — V1의 kt.token IN :tokens 대신 tokenValue 기준 삭제
    @Modifying
    @Query("DELETE FROM KeywordToken kt WHERE kt.keyword = :keyword AND kt.tokenValue IN :tokenValues")
    void deleteByKeywordAndTokenValues(
        @Param("keyword") Keyword keyword,
        @Param("tokenValues") List<String> tokenValues
    );

    // V2: 서브쿼리로 Token 테이블에서 tokenValue를 조회 후 삭제 (Token FK 미보유)
    @Modifying
    @Query("DELETE FROM KeywordToken kt WHERE kt.tokenValue IN (SELECT t.tokenValue FROM Token t WHERE t.id IN :tokenIds)")
    void deleteAllByTokenIds(@Param("tokenIds") List<Long> tokenIds);

    @Query("SELECT kt FROM KeywordToken kt JOIN FETCH kt.keyword WHERE kt.tokenValue IN :tokenValues")
    List<KeywordToken> findKeywordTokensWithKeyword(@Param("tokenValues") List<String> tokenValues);
}
