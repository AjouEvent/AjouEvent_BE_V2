package com.example.ajouevent_be_v2.repository.adapter.clubevent;

import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class ClubEventJdbcRepositoryAdapter {

    private static final String BATCH_INCREMENT_SQL = "UPDATE club_events SET view_count = view_count + ? WHERE event_id = ?";

    private final JdbcTemplate jdbcTemplate;

    @Transactional
    public void batchIncrementViews(Map<Long, Long> deltaMap) {
        List<Object[]> batchArgs = deltaMap.entrySet().stream()
            .map(e -> new Object[]{e.getValue(), e.getKey()})
            .toList();
        jdbcTemplate.batchUpdate(BATCH_INCREMENT_SQL, batchArgs);
    }
}
