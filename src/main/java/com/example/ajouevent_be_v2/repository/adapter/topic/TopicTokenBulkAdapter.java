package com.example.ajouevent_be_v2.repository.adapter.topic;

import com.example.ajouevent_be_v2.domain.topic.TopicToken;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class TopicTokenBulkAdapter {

    private final JdbcTemplate jdbcTemplate;

    public void saveAll(List<TopicToken> topicTokens) {
        String sql = "INSERT INTO topic_tokens (topic_id, token_value) VALUES (?, ?)";

        jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                TopicToken topicToken = topicTokens.get(i);
                ps.setLong(1, topicToken.getTopic().getId());
                ps.setString(2, topicToken.getTokenValue());
            }

            @Override
            public int getBatchSize() {
                return topicTokens.size();
            }
        });
    }
}
