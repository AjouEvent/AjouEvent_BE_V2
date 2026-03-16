package com.example.ajouevent_be_v2.repository.adapter.event;

import com.example.ajouevent_be_v2.domain.event.EventBanner;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EventBannerJpaRepositoryAdapter extends JpaRepository<EventBanner, Long> {
}
