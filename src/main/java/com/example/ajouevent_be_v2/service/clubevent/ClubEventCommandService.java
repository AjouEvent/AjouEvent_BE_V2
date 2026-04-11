package com.example.ajouevent_be_v2.service.clubevent;

import com.example.ajouevent_be_v2.common.exception.clubevent.ClubEventErrorCode;
import com.example.ajouevent_be_v2.common.exception.clubevent.ClubEventException;
import com.example.ajouevent_be_v2.domain.clubevent.ClubEvent;
import com.example.ajouevent_be_v2.domain.clubevent.ClubEventImage;
import com.example.ajouevent_be_v2.domain.clubevent.Type;
import com.example.ajouevent_be_v2.dto.clubevent.ClubEventCommand;
import com.example.ajouevent_be_v2.dto.clubevent.ClubEventSummaryResult;
import com.example.ajouevent_be_v2.repository.port.clubevent.ClubEventRepositoryPort;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ClubEventCommandService {

    private static final String DEFAULT_IMAGE_URL = "https://www.ajou.ac.kr/_res/ajou/kr/img/intro/img-symbol.png";
    private static final int CONTENT_PREVIEW_LENGTH = 200;

    private final ClubEventRepositoryPort clubEventRepositoryPort;

    public void isDuplicateNotice(String englishTopic, String title, String url) {
        Type type = parseType(englishTopic);
        List<ClubEventSummaryResult> recentEvents = clubEventRepositoryPort.findTop10ByType(type);

        boolean isDuplicate = recentEvents.stream()
                .anyMatch(e -> e.title().equals(title) && e.url().equals(url));
        if (isDuplicate) {
            throw new ClubEventException(ClubEventErrorCode.DUPLICATE_NOTICE);
        }
    }

    @Transactional
    public ClubEvent save(ClubEventCommand command) {
        Type type = parseType(command.englishTopic());
        List<String> imageUrls = resolveImages(command.images());

        String content = command.content();
        String contentPreview = content != null && content.length() > CONTENT_PREVIEW_LENGTH
                ? content.substring(0, CONTENT_PREVIEW_LENGTH)
                : content;

        ClubEvent clubEvent = ClubEvent.builder()
                .title(command.title())
                .content(content)
                .contentPreview(contentPreview)
                .writer(command.department())
                .subject(command.koreanTopic())
                .url(command.url())
                .type(type)
                .createdAt(LocalDateTime.now())
                .likesCount(0L)
                .viewCount(0L)
                .clubEventImageList(new ArrayList<>())
                .build();

        List<ClubEventImage> images = imageUrls.stream()
                .map(url -> ClubEventImage.builder()
                        .url(url)
                        .clubEvent(clubEvent)
                        .build())
                .toList();

        clubEvent.getClubEventImageList().addAll(images);

        ClubEvent saved = clubEventRepositoryPort.save(clubEvent);
        log.info("공지사항 저장 완료 - eventId: {}, type: {}", saved.getEventId(), type);
        return saved;
    }

    private Type parseType(String englishTopic) {
        try {
            return Type.valueOf(englishTopic.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ClubEventException(ClubEventErrorCode.INVALID_TYPE);
        }
    }

    private List<String> resolveImages(List<String> images) {
        if (images == null || images.isEmpty()) {
            return List.of(DEFAULT_IMAGE_URL);
        }
        return images;
    }
}
