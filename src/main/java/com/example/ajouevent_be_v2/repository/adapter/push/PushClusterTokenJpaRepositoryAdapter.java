package com.example.ajouevent_be_v2.repository.adapter.push;

import java.util.List;

import com.example.ajouevent_be_v2.domain.push.PushCluster;
import com.example.ajouevent_be_v2.domain.push.PushClusterToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PushClusterTokenJpaRepositoryAdapter extends JpaRepository<PushClusterToken, Long> {

    @Query("SELECT pct FROM PushClusterToken pct JOIN FETCH pct.member WHERE pct.pushCluster = :pushCluster")
    List<PushClusterToken> findAllByPushClusterWithMember(@Param("pushCluster") PushCluster pushCluster);
}
