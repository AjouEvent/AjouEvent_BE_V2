package com.example.ajouevent_be_v2.domain.push;

import java.time.LocalDateTime;

import com.example.ajouevent_be_v2.domain.member.Member;
import com.example.ajouevent_be_v2.domain.notice.JobStatus;

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
@Table(name = "push_cluster_tokens")
public class PushClusterToken {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "push_cluster_id", nullable = false)
    private PushCluster pushCluster;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(name = "token_value")
    private String tokenValue;

    @Enumerated(EnumType.STRING)
    @Column(name = "job_status", nullable = false)
    private JobStatus jobStatus = JobStatus.PENDING;

    @Column(name = "request_time", nullable = false)
    private LocalDateTime requestTime;

    @Column(name = "processed_time", nullable = true)
    private LocalDateTime processedTime;

    public void markAsSending() {
        this.jobStatus = JobStatus.IN_PROGRESS;
        this.processedTime = LocalDateTime.now();
    }

    public void markAsSuccess() {
        this.jobStatus = JobStatus.SUCCESS;
        this.processedTime = LocalDateTime.now();
    }

    public void markAsFail() {
        this.jobStatus = JobStatus.FAIL;
        this.processedTime = LocalDateTime.now();
    }
}
