package com.example.ajouevent_be_v2.repository.adapter.keyword;

import com.example.ajouevent_be_v2.domain.keyword.KeywordToken;
import jakarta.persistence.EntityManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class KeywordTokenBulkAdapter {

    private final JdbcTemplate jdbcTemplate;
    private final EntityManager entityManager;

    public void saveAll(List<KeywordToken> keywordTokens) {
        String sql = "INSERT INTO keyword_tokens (keyword_id, token_value) VALUES (?, ?)";

        jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                KeywordToken keywordToken = keywordTokens.get(i);
                ps.setLong(1, keywordToken.getKeyword().getId());
                ps.setString(2, keywordToken.getTokenValue());
            }

            @Override
            public int getBatchSize() {
                return keywordTokens.size();
            }
        });
    }
}
