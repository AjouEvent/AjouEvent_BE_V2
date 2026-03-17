package com.example.ajouevent_be_v2.service.clubevent;

import com.example.ajouevent_be_v2.domain.clubevent.ClubEvent;
import com.example.ajouevent_be_v2.dto.clubevent.ClubEventCommand;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ClubEventCommandService {

    public ClubEvent createClubEvent(ClubEventCommand command) {

        validateDuplicateNotice(command.title(), command.url());

        // TODO : 공지사항 저장

        return null;
    }

    private void validateDuplicateNotice(String title, String url) {
        // TODO: 검증 로직 작성
    }
}
