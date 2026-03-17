package com.example.ajouevent_be_v2.dto.push;

import com.example.ajouevent_be_v2.domain.member.Member;
import com.example.ajouevent_be_v2.domain.push.PushCluster;
import java.util.List;

public record PushClusterCreationResult(
    PushCluster cluster,
    List<Member> members,
    PushClusterSendRequest sendRequest
) {
}
