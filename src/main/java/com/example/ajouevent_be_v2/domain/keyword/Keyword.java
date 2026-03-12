package com.example.ajouevent_be_v2.domain.keyword;

import com.example.ajouevent_be_v2.domain.topic.Topic;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
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
@Table(name = "keywords")
public class Keyword {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "encoded_keyword")
    private String encodedKeyword;

    @Column(name = "korean_keyword")
    private String koreanKeyword;

    @Column(name = "search_keyword")
    private String searchKeyword;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "topic_id")
    private Topic topic;

    public static String buildFormattedKeyword(String koreanKeyword, String topicName) {
        String encoded = URLEncoder.encode(koreanKeyword, StandardCharsets.UTF_8).replace("+", "%20");
        return encoded + "_" + topicName;
    }

    public static Keyword create(Topic topic, String koreanKeyword, String topicName) {
        return Keyword.builder()
            .encodedKeyword(buildFormattedKeyword(koreanKeyword, topicName))
            .koreanKeyword(koreanKeyword)
            .searchKeyword(koreanKeyword + "_" + topicName)
            .topic(topic)
            .build();
    }
}
