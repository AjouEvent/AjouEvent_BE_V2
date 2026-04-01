package com.example.ajouevent_be_v2.repository.adapter.push;

import java.time.LocalDateTime;
import java.util.List;

import com.example.ajouevent_be_v2.domain.clubevent.JobStatus;
import com.example.ajouevent_be_v2.domain.push.PushCluster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PushClusterJpaRepositoryAdapter extends JpaRepository<PushCluster, Long> {

    List<PushCluster> findAllByJobStatus(JobStatus jobStatus);

    // 서버 장애 복구용: staleThreshold 이전에 등록된 PENDING 클러스터만 조회한다.
    List<PushCluster> findAllByJobStatusAndRegisteredAtBefore(JobStatus jobStatus, LocalDateTime threshold);

    @Modifying
    @Query("""
        UPDATE PushCluster p
        SET p.successCount = p.successCount + :successDelta,
            p.failCount    = p.failCount    + :failDelta,
            p.jobStatus    = CASE
                WHEN p.failCount + :failDelta > 0 THEN com.example.ajouevent_be_v2.domain.clubevent.JobStatus.PARTIAL_FAIL
                WHEN p.successCount + :successDelta > 0 THEN com.example.ajouevent_be_v2.domain.clubevent.JobStatus.SUCCESS
                ELSE com.example.ajouevent_be_v2.domain.clubevent.JobStatus.NONE END,
            p.endAt        = CURRENT_TIMESTAMP
        WHERE p.id = :id
        """)
    void incrementCountsAndUpdateStatus(
        @Param("id") Long id,
        @Param("successDelta") int successDelta,
        @Param("failDelta") int failDelta
    );
}
