package com.example.ajouevent_be_v2.repository.adapter.push;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.example.ajouevent_be_v2.domain.clubevent.JobStatus;
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

    private static final int CHUNK_SIZE = 500;

    private final JdbcTemplate jdbcTemplate;

    @PersistenceContext
    private EntityManager entityManager;

    public void saveAll(List<PushClusterToken> clusterTokens) {
        String sql = "INSERT INTO push_cluster_tokens (push_cluster_id, member_id, token_value, job_status, request_time, processed_time, retry_count, retry_after) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        for (int start = 0; start < clusterTokens.size(); start += CHUNK_SIZE) {
            List<PushClusterToken> chunk = clusterTokens.subList(start, Math.min(start + CHUNK_SIZE, clusterTokens.size()));
            jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
                @Override
                public void setValues(PreparedStatement ps, int i) throws SQLException {
                    PushClusterToken token = chunk.get(i);
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
                    return chunk.size();
                }
            });
        }
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

    /**
     * 주어진 토큰 행을 PK 오름차순으로 잠그고(SELECT ... FOR UPDATE) 현재 상태를 반환한다.
     * 호출자의 트랜잭션이 끝날 때까지 다른 트랜잭션은 같은 행을 잠그거나 갱신할 수 없다.
     * 잠금 순서를 PK 오름차순 하나로 통일해, 발송 선점과 폴링 릴레이 복구가 서로 교착하지 않게 한다.
     */
    public Map<Long, JobStatus> lockStatuses(List<Long> ids) {
        List<Long> sortedIds = new ArrayList<>(ids);
        Collections.sort(sortedIds);
        Map<Long, JobStatus> statuses = new HashMap<>();
        for (int start = 0; start < sortedIds.size(); start += CHUNK_SIZE) {
            List<Long> chunk = sortedIds.subList(start, Math.min(start + CHUNK_SIZE, sortedIds.size()));
            String placeholders = String.join(",", Collections.nCopies(chunk.size(), "?"));
            String sql = "SELECT id, job_status FROM push_cluster_tokens WHERE id IN (" + placeholders
                + ") ORDER BY id FOR UPDATE";
            jdbcTemplate.query(sql, rs -> {
                statuses.put(rs.getLong("id"), JobStatus.valueOf(rs.getString("job_status")));
            }, chunk.toArray());
        }
        return statuses;
    }
}
