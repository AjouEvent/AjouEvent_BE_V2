package com.example.ajouevent_be_v2.repository.adapter.keyword;

import com.example.ajouevent_be_v2.domain.keyword.KeywordMember;
import jakarta.persistence.EntityManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class KeywordMemberBulkAdapter {

    private final JdbcTemplate jdbcTemplate;
    private final EntityManager entityManager;

    public void updateKeywordMembers(List<KeywordMember> keywordMembers) {
        String sql = "UPDATE keyword_members SET is_read = ?, last_read_at = ? WHERE id = ?";

        jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                KeywordMember keywordMember = keywordMembers.get(i);
                ps.setBoolean(1, keywordMember.isRead());
                ps.setTimestamp(2, Timestamp.valueOf(keywordMember.getLastReadAt()));
                ps.setLong(3, keywordMember.getId());
            }

            @Override
            public int getBatchSize() {
                return keywordMembers.size();
            }
        });

        keywordMembers.forEach(entityManager::detach);
    }
}
