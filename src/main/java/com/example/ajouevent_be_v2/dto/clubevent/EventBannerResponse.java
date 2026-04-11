package com.example.ajouevent_be_v2.dto.clubevent;

import com.example.ajouevent_be_v2.domain.clubevent.EventBanner;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;

@Schema(description = "홈 화면 이벤트 배너 응답")
public record EventBannerResponse(
    @Schema(description = "배너 노출 순서", example = "1")
    Long bannerOrder,

    @Schema(description = "배너 이미지 URL", example = "https://www.ajou.ac.kr/_res/ajou/kr/img/banner/banner-01.png")
    String imgUrl,

    @Schema(description = "배너 클릭 시 이동할 사이트 URL", example = "https://www.ajouevent.com")
    String siteUrl,

    @Schema(description = "배너 노출 시작일", example = "2024-03-01")
    LocalDate startDate,

    @Schema(description = "배너 노출 종료일", example = "2024-03-31")
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
