package com.example.ajouevent_be_v2.repository.port.clubevent;

import com.example.ajouevent_be_v2.repository.adapter.clubevent.ClubEventImageJpaRepositoryAdapter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ClubEventImageRepositoryPort {

    private final ClubEventImageJpaRepositoryAdapter clubEventImageJpaRepositoryAdapter;

    public Map<Long, List<String>> findImageUrlsByEventIds(List<Long> eventIds) {
        if (eventIds.isEmpty()) {
            return Map.of();
        }
        return clubEventImageJpaRepositoryAdapter.findEventIdAndUrlByEventIdIn(eventIds).stream()
            .collect(Collectors.groupingBy(
                row -> (Long) row[0],
                Collectors.mapping(row -> (String) row[1], Collectors.toList())
            ));
    }
}
