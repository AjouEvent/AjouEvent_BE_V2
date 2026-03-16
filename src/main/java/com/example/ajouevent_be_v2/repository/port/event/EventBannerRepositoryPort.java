package com.example.ajouevent_be_v2.repository.port.event;

import com.example.ajouevent_be_v2.domain.event.EventBanner;
import com.example.ajouevent_be_v2.repository.adapter.event.EventBannerJpaRepositoryAdapter;
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
}
