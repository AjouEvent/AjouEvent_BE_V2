package com.example.ajouevent_be_v2.repository.adapter.member;

import com.example.ajouevent_be_v2.domain.member.Token;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class TokenBulkRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @PersistenceContext
    private EntityManager entityManager;

    public void updateTokens(List<Token> tokens) {
        String sql = "UPDATE tokens SET is_deleted = ? WHERE id = ?";

        jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                Token token = tokens.get(i);
                ps.setBoolean(1, token.isDeleted());
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
