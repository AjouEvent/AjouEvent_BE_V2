package com.example.ajouevent_be_v2.domain.push;

import java.time.LocalDateTime;
import java.util.List;

import com.example.ajouevent_be_v2.domain.notice.ClubEvent;
import com.example.ajouevent_be_v2.domain.notice.JobStatus;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@Table(name = "push_clusters")
public class PushCluster {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "club_event_id", nullable = true)
    private ClubEvent clubEvent;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "body", nullable = false)
    private String body;

    @Column(name = "image_url", nullable = false)
    private String imageUrl;

    @Column(name = "click_url", nullable = false)
    private String clickUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "job_status", nullable = false)
    private JobStatus jobStatus = JobStatus.PENDING;

    @Column(name = "total_count", nullable = false)
    private int totalCount = 0;

    @Column(name = "registered_at", nullable = false)
    private LocalDateTime registeredAt;

    @Column(name = "success_count", nullable = false)
    private int successCount = 0;

    @Column(name = "fail_count", nullable = false)
    private int failCount = 0;

    @Column(name = "received_count", nullable = false)
    private int receivedCount = 0;

    @Column(name = "clicked_count", nullable = false)
    private int clickedCount = 0;

    @Column(name = "start_at", nullable = true)
    private LocalDateTime startAt = LocalDateTime.now();

    @Column(name = "end_at", nullable = true)
    private LocalDateTime endAt = LocalDateTime.now();

    @OneToMany(mappedBy = "pushCluster", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PushClusterToken> tokens;

    public void markAsInProgress() {
        this.startAt = LocalDateTime.now();
        this.jobStatus = JobStatus.IN_PROGRESS;
    }

    public void updateCountsAndStatus(int successCount, int failCount) {
        this.successCount += successCount;
        this.failCount += failCount;
        if (successCount == 0 && failCount == 0) {
            this.jobStatus = JobStatus.NONE;
        } else if (failCount > 0) {
            this.jobStatus = JobStatus.PARTIAL_FAIL;
        } else {
            this.jobStatus = JobStatus.SUCCESS;
        }

        this.endAt = LocalDateTime.now();
    }
}
