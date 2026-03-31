package com.example.ajouevent_be_v2.repository.adapter.push;

import java.time.LocalDateTime;
import java.util.List;

import com.example.ajouevent_be_v2.domain.clubevent.JobStatus;
import com.example.ajouevent_be_v2.domain.push.PushCluster;
import com.example.ajouevent_be_v2.domain.push.PushClusterToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PushClusterTokenJpaRepositoryAdapter extends JpaRepository<PushClusterToken, Long> {

    @Query("SELECT pct FROM PushClusterToken pct JOIN FETCH pct.member WHERE pct.pushCluster = :pushCluster")
    List<PushClusterToken> findAllByPushClusterWithMember(@Param("pushCluster") PushCluster pushCluster);

    @Query("""
        SELECT pct FROM PushClusterToken pct
        JOIN FETCH pct.member
        JOIN FETCH pct.pushCluster
        WHERE pct.jobStatus = :status
          AND pct.retryAfter <= :now
          AND pct.retryCount <= :maxRetryCount
        """)
    List<PushClusterToken> findRetryPendingTokensReady(
        @Param("status") JobStatus status,
        @Param("now") LocalDateTime now,
        @Param("maxRetryCount") int maxRetryCount
    );

    @Query("""
        SELECT pct FROM PushClusterToken pct
        WHERE pct.jobStatus = :status
          AND pct.processedTime < :threshold
        """)
    List<PushClusterToken> findStaleInProgressTokens(
        @Param("status") JobStatus status,
        @Param("threshold") LocalDateTime threshold
    );
}
