package com.example.ajouevent_be_v2.controller;

import com.example.ajouevent_be_v2.common.auth.AuthOptional;
import com.example.ajouevent_be_v2.common.auth.AuthUser;
import com.example.ajouevent_be_v2.common.dto.SliceResponse;
import com.example.ajouevent_be_v2.controller.docs.ClubEventControllerDocs;
import com.example.ajouevent_be_v2.domain.clubevent.Type;
import com.example.ajouevent_be_v2.domain.member.Member;
import com.example.ajouevent_be_v2.dto.clubevent.ClubEventDetailResponse;
import com.example.ajouevent_be_v2.dto.clubevent.ClubEventResponse;
import com.example.ajouevent_be_v2.dto.clubevent.ClubEventWithKeywordResponse;
import com.example.ajouevent_be_v2.orchestrator.ClubEventOrchestrator;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ClubEventController implements ClubEventControllerDocs {

    private final ClubEventOrchestrator clubEventOrchestrator;

    @Override
    @GetMapping("/api/v2/event/detail/{eventId}")
    public ResponseEntity<ClubEventDetailResponse> getEventDetail(
        @PathVariable Long eventId,
        @AuthOptional Member member,
        HttpServletRequest request) {
        String clientIp = resolveClientIp(request);
        String userAgent = request.getHeader("User-Agent");
        return ResponseEntity.ok(
            clubEventOrchestrator.getEventDetail(eventId, member, clientIp, userAgent));
    }

    @Override
    @GetMapping("/api/v2/event/{type}")
    public ResponseEntity<SliceResponse<ClubEventResponse>> getEventTypeList(
        @PathVariable(name = "type") String type,
        @RequestParam(defaultValue = "") String keyword,
        @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
        @AuthOptional Member member) {
        return ResponseEntity.ok(clubEventOrchestrator.getEventTypeList(type, keyword, pageable, member));
    }

    @Override
    @GetMapping("/api/v2/event/popular")
    public ResponseEntity<List<ClubEventResponse>> getTopPopularEvents(@AuthOptional Member member) {
        return ResponseEntity.ok(clubEventOrchestrator.getTopPopularEvents(member));
    }

    @Override
    @GetMapping("/api/v2/event/subscribed")
    public ResponseEntity<SliceResponse<ClubEventResponse>> getSubscribedEvents(
        @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
        @AuthOptional Member member,
        @RequestParam(required = false) String keyword) {
        if (member == null) {
            return ResponseEntity.ok(clubEventOrchestrator.getEventTypeList(
                Type.AJOUNORMAL.name(), keyword, pageable, null));
        }
        return ResponseEntity.ok(clubEventOrchestrator.getSubscribedEvents(pageable, member, keyword));
    }

    @Override
    @GetMapping("/api/v2/event/getSubscribedPostsByKeyword")
    public ResponseEntity<SliceResponse<ClubEventWithKeywordResponse>> getAllBySubscribedKeywords(
        @AuthUser Member member,
        @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(clubEventOrchestrator.getAllBySubscribedKeywords(member, pageable));
    }

    @Override
    @GetMapping("/api/v2/event/getSubscribedPostsByKeyword/{keyword}")
    public ResponseEntity<SliceResponse<ClubEventWithKeywordResponse>> getByKeyword(
        @PathVariable String keyword,
        @AuthUser Member member,
        @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(clubEventOrchestrator.getByKeyword(keyword, member, pageable));
    }

    @Override
    @PostMapping("/api/v2/event/like/{eventId}")
    public ResponseEntity<Void> likeEvent(
        @PathVariable Long eventId,
        @AuthUser Member member) {
        clubEventOrchestrator.likeEvent(eventId, member);
        return ResponseEntity.noContent().build();
    }

    @Override
    @DeleteMapping("/api/v2/event/like/{eventId}")
    public ResponseEntity<Void> cancelLikeEvent(
        @PathVariable Long eventId,
        @AuthUser Member member) {
        clubEventOrchestrator.cancelLikeEvent(eventId, member);
        return ResponseEntity.noContent().build();
    }

    @Override
    @GetMapping("/api/v2/event/liked")
    public ResponseEntity<SliceResponse<ClubEventResponse>> getLikedEvents(
        @RequestParam(required = false) String type,
        @RequestParam(defaultValue = "") String keyword,
        @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
        @AuthUser Member member) {
        return ResponseEntity.ok(clubEventOrchestrator.getLikedEvents(type, keyword, pageable, member));
    }

    private String resolveClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip.split(",")[0].trim();
    }
}
