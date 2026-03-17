package com.example.ajouevent_be_v2.service.clubevent;

import com.example.ajouevent_be_v2.common.exception.clubevent.ClubEventErrorCode;
import com.example.ajouevent_be_v2.common.exception.clubevent.ClubEventException;
import com.example.ajouevent_be_v2.domain.clubevent.ClubEvent;
import com.example.ajouevent_be_v2.domain.clubevent.ClubEventLike;
import com.example.ajouevent_be_v2.domain.member.Member;
import com.example.ajouevent_be_v2.repository.port.clubevent.ClubEventRepositoryPort;
import com.example.ajouevent_be_v2.repository.port.clubevent.ClubEventLikeRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ClubEventLikeCommandService {

    private final ClubEventRepositoryPort clubEventRepositoryPort;
    private final ClubEventLikeRepositoryPort clubEventLikeRepositoryPort;

    //TODO: 동시 요청 상황에서 check - then - set 이 비원자적이므로 중복데이터 발생 가능 -> 추후 DB 제약 또는 락으로 해결필요
    @Transactional
    public void likeEvent(ClubEvent clubEvent, Member member) {
        if (clubEventLikeRepositoryPort.existsByMemberAndClubEvent(member, clubEvent)) {
            throw new ClubEventException(ClubEventErrorCode.ALREADY_LIKED);
        }
        clubEvent.incrementLikes();
        clubEventRepositoryPort.save(clubEvent);
        clubEventLikeRepositoryPort.save(ClubEventLike.builder()
            .clubEvent(clubEvent)
            .member(member)
            .build());
    }

    @Transactional
    public void cancelLike(ClubEvent clubEvent, ClubEventLike clubEventLike) {
        clubEvent.decreaseLikes();
        clubEventRepositoryPort.save(clubEvent);
        clubEventLikeRepositoryPort.delete(clubEventLike);
    }
}
