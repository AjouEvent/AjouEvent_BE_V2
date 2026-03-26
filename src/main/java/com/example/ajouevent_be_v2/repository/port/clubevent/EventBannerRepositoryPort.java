package com.example.ajouevent_be_v2.repository.port.clubevent;

import com.example.ajouevent_be_v2.domain.clubevent.EventBanner;
import com.example.ajouevent_be_v2.repository.adapter.clubevent.EventBannerJpaRepositoryAdapter;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class EventBannerRepositoryPort {

    private final EventBannerJpaRepositoryAdapter eventBannerJpaRepositoryAdapter;

    public Optional<EventBanner> findById(Long id) {
        return eventBannerJpaRepositoryAdapter.findById(id);
    }

    public EventBanner save(EventBanner eventBanner) {
        return eventBannerJpaRepositoryAdapter.save(eventBanner);
    }

    public void delete(EventBanner eventBanner) {
        eventBannerJpaRepositoryAdapter.delete(eventBanner);
    }

    public List<EventBanner> findAllOrderByBannerOrder() {
        return eventBannerJpaRepositoryAdapter.findAllByOrderByBannerOrderAsc();
    }
}
