package com.example.ajouevent_be_v2.repository.adapter.clubevent;

import com.example.ajouevent_be_v2.domain.clubevent.EventBanner;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EventBannerJpaRepositoryAdapter extends JpaRepository<EventBanner, Long> {

    List<EventBanner> findAllByOrderByBannerOrderAsc();
}
