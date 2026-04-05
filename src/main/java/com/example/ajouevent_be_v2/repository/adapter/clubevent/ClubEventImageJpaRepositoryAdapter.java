package com.example.ajouevent_be_v2.repository.adapter.clubevent;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.ajouevent_be_v2.domain.clubevent.ClubEventImage;

public interface ClubEventImageJpaRepositoryAdapter extends JpaRepository<ClubEventImage, Long> {

    @Query("SELECT ci.clubEvent.eventId, ci.url FROM ClubEventImage ci WHERE ci.clubEvent.eventId IN :eventIds")
    List<Object[]> findEventIdAndUrlByEventIdIn(@Param("eventIds") List<Long> eventIds);
}
