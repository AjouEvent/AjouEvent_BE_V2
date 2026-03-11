package com.example.ajouevent_be_v2.repository.adapter.subscription;

import com.example.ajouevent_be_v2.domain.topic.TopicMember;
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
public class TopicMemberBulkAdapter {

    private final JdbcTemplate jdbcTemplate;
    private final EntityManager entityManager;

    public void updateTopicMembers(List<TopicMember> topicMembers) {
        String sql = "UPDATE topic_members SET is_read = ?, last_read_at = ? WHERE id = ?";

        jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                TopicMember topicMember = topicMembers.get(i);
                ps.setBoolean(1, topicMember.isRead());
                ps.setTimestamp(2, Timestamp.valueOf(topicMember.getLastReadAt()));
                ps.setLong(3, topicMember.getId());
            }

            @Override
            public int getBatchSize() {
                return topicMembers.size();
            }
        });

        topicMembers.forEach(entityManager::detach);
    }
}
