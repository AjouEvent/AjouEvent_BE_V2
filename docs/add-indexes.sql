-- =============================================
-- AjouEvent V2 — 인덱스 추가 (운영 DB ALTER)
-- schema.sql은 CREATE TABLE IF NOT EXISTS 기반이므로
-- 기존 테이블에는 아래 ALTER로 직접 적용
-- =============================================

-- topics
ALTER TABLE topics
    ADD INDEX idx_topics_department   (department),
    ADD INDEX idx_topics_korean_topic (korean_topic);

-- topic_members
ALTER TABLE topic_members
    ADD UNIQUE KEY uq_topic_members                    (member_id, topic_id),
    ADD INDEX      idx_topic_members_member_isread      (member_id, is_read),
    ADD INDEX      idx_topic_members_topic_notification (topic_id, receive_notification);

-- topic_tokens
ALTER TABLE topic_tokens
    ADD INDEX idx_topic_tokens_topic_token (topic_id, token_value),
    ADD INDEX idx_topic_tokens_token_value (token_value);

-- keywords
ALTER TABLE keywords
    ADD UNIQUE KEY uq_keywords_encoded_keyword (encoded_keyword),
    ADD INDEX      idx_keywords_search_keyword (search_keyword),
    ADD INDEX      idx_keywords_topic_id       (topic_id);

-- keyword_members
ALTER TABLE keyword_members
    ADD UNIQUE KEY uq_keyword_members               (member_id, keyword_id),
    ADD INDEX      idx_keyword_members_member_isread (member_id, is_read),
    ADD INDEX      idx_keyword_members_keyword_id    (keyword_id);

-- keyword_tokens
ALTER TABLE keyword_tokens
    ADD INDEX idx_keyword_tokens_keyword_token (keyword_id, token_value),
    ADD INDEX idx_keyword_tokens_token_value   (token_value);

-- club_events
ALTER TABLE club_events
    ADD INDEX idx_club_events_type_createdat      (type, created_at),
    ADD INDEX idx_club_events_createdat_viewcount  (created_at, view_count);

-- event_likes
ALTER TABLE event_likes
    ADD UNIQUE KEY uq_event_likes (member_id, club_event_id);

-- push_clusters
ALTER TABLE push_clusters
    ADD INDEX idx_push_clusters_job_status (job_status);

-- push_cluster_tokens
ALTER TABLE push_cluster_tokens
    ADD INDEX idx_pct_push_cluster_id  (push_cluster_id),
    ADD INDEX idx_pct_status_request   (job_status, request_time),
    ADD INDEX idx_pct_status_processed (job_status, processed_time),
    ADD INDEX idx_pct_status_retry     (job_status, retry_after, retry_count);

-- push_notifications
ALTER TABLE push_notifications
    ADD INDEX idx_pn_member_type_notified (member_id, notification_type, notified_at),
    ADD INDEX idx_pn_member_isread        (member_id, is_read),
    ADD INDEX idx_pn_push_cluster_id      (push_cluster_id);
