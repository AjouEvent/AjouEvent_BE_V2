package com.example.ajouevent_be_v2.repository.adapter.push;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

import com.example.ajouevent_be_v2.domain.push.PushClusterToken;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class PushClusterTokenBulkRepositoryAdapter {

    private final JdbcTemplate jdbcTemplate;

    @PersistenceContext
    private EntityManager entityManager;

    public void saveAll(List<PushClusterToken> clusterTokens) {
        String sql = "INSERT INTO push_cluster_tokens (push_cluster_id, member_id, token_value, job_status, request_time, processed_time, retry_count, retry_after) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                PushClusterToken token = clusterTokens.get(i);
                ps.setLong(1, token.getPushCluster().getId());
                ps.setLong(2, token.getMember().getId());
                ps.setString(3, token.getTokenValue());
                ps.setString(4, token.getJobStatus().name());
                ps.setTimestamp(5, java.sql.Timestamp.valueOf(token.getRequestTime()));
                ps.setTimestamp(6, token.getProcessedTime() != null
                    ? java.sql.Timestamp.valueOf(token.getProcessedTime()) : null);
                ps.setInt(7, token.getRetryCount());
                ps.setTimestamp(8, token.getRetryAfter() != null
                    ? java.sql.Timestamp.valueOf(token.getRetryAfter()) : null);
            }

            @Override
            public int getBatchSize() {
                return clusterTokens.size();
            }
        });
    }

    public void updateAll(List<PushClusterToken> clusterTokens) {
        String sql = "UPDATE push_cluster_tokens SET job_status = ?, processed_time = ?, retry_count = ?, retry_after = ? WHERE id = ?";

        jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                PushClusterToken token = clusterTokens.get(i);
                ps.setString(1, token.getJobStatus().name());
                ps.setTimestamp(2, token.getProcessedTime() != null
                    ? java.sql.Timestamp.valueOf(token.getProcessedTime()) : null);
                ps.setInt(3, token.getRetryCount());
                ps.setTimestamp(4, token.getRetryAfter() != null
                    ? java.sql.Timestamp.valueOf(token.getRetryAfter()) : null);
                ps.setLong(5, token.getId());
            }

            @Override
            public int getBatchSize() {
                return clusterTokens.size();
            }
        });

        clusterTokens.forEach(entityManager::detach);
    }
}
