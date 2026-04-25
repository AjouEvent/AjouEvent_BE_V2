package com.example.ajouevent_be_v2.service.clubevent;

import com.example.ajouevent_be_v2.repository.port.clubevent.ClubEventRepositoryPort;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ClubEventViewCountWriteService {

    private final ClubEventRepositoryPort clubEventRepositoryPort;

    @Transactional
    public void batchIncrementViews(Map<Long, Long> deltaMap) {
        clubEventRepositoryPort.batchIncrementViews(deltaMap);
    }
}
