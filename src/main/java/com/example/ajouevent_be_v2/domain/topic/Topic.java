package com.example.ajouevent_be_v2.domain.topic;

import java.util.List;

import com.example.ajouevent_be_v2.domain.notice.Type;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@Table(name = "topics")
public class Topic {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "department")
    private String department;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", unique = true)
    private Type type;

    @Column(name = "classification")
    private String classification;

    @Column(name = "korean_topic")
    private String koreanTopic;

    @Column(name = "korean_order")
    private Long koreanOrder;

    @OneToMany(mappedBy = "topic")
    private List<TopicToken> topicTokens;

    @OneToMany(mappedBy = "topic")
    private List<TopicMember> topicMembers;
}
