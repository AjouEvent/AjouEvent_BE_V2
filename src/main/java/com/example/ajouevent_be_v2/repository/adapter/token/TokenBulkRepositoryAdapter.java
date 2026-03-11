package com.example.ajouevent_be_v2.repository.adapter.token;

import com.example.ajouevent_be_v2.domain.member.Token;
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
public class TokenBulkRepositoryAdapter {

    private final JdbcTemplate jdbcTemplate;
    private final EntityManager entityManager;

    public void batchSoftDeleteTokens(List<Token> tokens) {
        String sql = "UPDATE tokens SET is_deleted = ? WHERE id = ?";

        jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                Token token = tokens.get(i);
                ps.setBoolean(1, true);
                ps.setLong(2, token.getId());
            }

            @Override
            public int getBatchSize() {
                return tokens.size();
            }
        });

        tokens.forEach(entityManager::detach);
    }
}
