package com.example.ajouevent_be_v2.controller;

import com.example.ajouevent_be_v2.common.auth.AuthUser;
import com.example.ajouevent_be_v2.controller.docs.MemberControllerDocs;
import com.example.ajouevent_be_v2.domain.member.Member;
import com.example.ajouevent_be_v2.dto.auth.OauthRequest;
import com.example.ajouevent_be_v2.dto.member.MemberInfoResponse;
import com.example.ajouevent_be_v2.dto.member.MemberUpdateRequest;
import com.example.ajouevent_be_v2.orchestrator.member.MemberOrchestrator;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class MemberController implements MemberControllerDocs {

    private final MemberOrchestrator memberOrchestrator;

    @GetMapping("/api/v2/members")
    public ResponseEntity<MemberInfoResponse> getMember(@AuthUser Member member) {
        return ResponseEntity.ok(memberOrchestrator.getMemberInfo(member));
    }

    @PatchMapping("/api/v2/members")
    public ResponseEntity<Void> updateMember(@RequestBody MemberUpdateRequest request, @AuthUser Member member) {
        memberOrchestrator.updateMemberInfo(request, member);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/api/v2/members")
    public ResponseEntity<Void> deleteMember(@AuthUser Member member) {
        memberOrchestrator.deleteMember(member);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/api/v2/members/calendar")
    public ResponseEntity<Void> connectCalendar(@RequestBody OauthRequest request, @AuthUser Member member) {
        memberOrchestrator.connectCalendar(request);
        return ResponseEntity.noContent().build();
    }
}
