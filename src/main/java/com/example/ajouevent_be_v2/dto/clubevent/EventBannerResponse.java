package com.example.ajouevent_be_v2.dto.clubevent;

import com.example.ajouevent_be_v2.domain.clubevent.EventBanner;
import java.time.LocalDate;

public record EventBannerResponse(
    Long bannerOrder,
    String imgUrl,
    String siteUrl,
    LocalDate startDate,
    LocalDate endDate
) {
    public static EventBannerResponse from(EventBanner banner) {
        return new EventBannerResponse(
            banner.getBannerOrder(),
            banner.getImgUrl(),
            banner.getSiteUrl(),
            banner.getStartDate(),
            banner.getEndDate()
        );
    }
}
