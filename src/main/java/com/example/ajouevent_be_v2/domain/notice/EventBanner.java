package com.example.ajouevent_be_v2.domain.notice;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@Entity
@Table(name = "event_banners")
public class EventBanner {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "event_banner_id")
    private Long eventBannerId;

    @Column(name = "banner_order", nullable = false)
    private Long bannerOrder;

    @Column(name = "img_url", nullable = false)
    private String imgUrl;

    @Column(name = "site_url", nullable = false)
    private String siteUrl;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;
}
