package com.example.ajouevent_be_v2.domain.notice;

import java.time.LocalDateTime;
import java.util.List;

import org.hibernate.annotations.BatchSize;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@Table(name = "club_events")
public class ClubEvent {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "event_id")
    private Long eventId;

    @Column(name = "title")
    private String title;

    @Column(name = "content", length = 50000)
    private String content;

    @Column(name = "writer")
    private String writer;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "subject")
    private String subject;

    @Column(name = "url")
    private String url;

    @Column(name = "likes_count")
    private Long likesCount;

    @Column(name = "view_count")
    private Long viewCount;

    @Enumerated(value = EnumType.STRING)
    @Column(name = "type", length = 50000)
    private Type type;

    @BatchSize(size = 100)
    @OneToMany(mappedBy = "clubEvent", fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.REMOVE}, orphanRemoval = true)
    @ToString.Exclude
    private List<ClubEventImage> clubEventImageList;

    public void incrementLikes() {
        this.likesCount++;
    }

    public void decreaseLikes() {
        this.likesCount--;
    }
}
