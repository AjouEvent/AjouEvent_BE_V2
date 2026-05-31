-- =============================================
-- AjouEvent V2 — 인덱스 생성
-- IndexInitializer(ApplicationRunner)가 서비스 시작 시 실행
-- 이미 존재하는 인덱스는 error 1061을 잡아 건너뜀 (재시작 안전)
-- =============================================

-- members
-- findByEmail
ALTER TABLE members ADD UNIQUE KEY uq_members_email (email);

-- tokens
-- findByTokenValue... / batchSoftDeleteByTokenValues
ALTER TABLE tokens ADD UNIQUE KEY uq_tokens_token_value (token_value);

-- topics
-- uq_topics_type: 타입 컬럼 유일성 보장
-- findFirstByDepartment → department 필터
-- findByKoreanTopic → korean_topic 필터
ALTER TABLE topics ADD UNIQUE KEY uq_topics_type (type);
ALTER TABLE topics ADD INDEX idx_topics_department (department);
ALTER TABLE topics ADD INDEX idx_topics_korean_topic (korean_topic);

-- topic_members
-- existsByTopicAndMember / findByMemberAndTopic / deleteByTopicAndMember
--   → uq_topic_members (member_id, topic_id)  — unique + leading prefix (member_id) 커버
-- existsByMemberAndIsReadFalse
--   → idx_topic_members_member_isread (member_id, is_read)
-- findByTopicWithMemberAndReceiveNotificationTrue (push 발송 라우팅 핵심 쿼리)
--   → idx_topic_members_topic_notification (topic_id, receive_notification)
ALTER TABLE topic_members ADD UNIQUE KEY uq_topic_members (member_id, topic_id);
ALTER TABLE topic_members ADD INDEX idx_topic_members_member_isread (member_id, is_read);
ALTER TABLE topic_members ADD INDEX idx_topic_members_topic_notification (topic_id, receive_notification);

-- topic_tokens
-- deleteByTopicAndTokenValues → (topic_id, token_value)
-- deleteAllByTokenValues → token_value 단독
ALTER TABLE topic_tokens ADD INDEX idx_topic_tokens_topic_token (topic_id, token_value);
ALTER TABLE topic_tokens ADD INDEX idx_topic_tokens_token_value (token_value);

-- keywords
-- findByEncodedKeyword → encoded_keyword 단독
-- findBySearchKeyword → search_keyword 단독
-- findByTopic → topic_id 필터
ALTER TABLE keywords ADD UNIQUE KEY uq_keywords_encoded_keyword (encoded_keyword);
ALTER TABLE keywords ADD INDEX idx_keywords_search_keyword (search_keyword);
ALTER TABLE keywords ADD INDEX idx_keywords_topic_id (topic_id);

-- keyword_members
-- existsByKeywordAndMember / findByKeywordAndMemberAndIsReadFalse / deleteByKeywordAndMember
--   → uq_keyword_members (member_id, keyword_id)  — unique + leading prefix 커버
-- existsByMemberAndIsReadFalse
--   → idx_keyword_members_member_isread (member_id, is_read)
-- findByKeywordWithMember
--   → idx_keyword_members_keyword_id (keyword_id)
ALTER TABLE keyword_members ADD UNIQUE KEY uq_keyword_members (member_id, keyword_id);
ALTER TABLE keyword_members ADD INDEX idx_keyword_members_member_isread (member_id, is_read);
ALTER TABLE keyword_members ADD INDEX idx_keyword_members_keyword_id (keyword_id);

-- keyword_tokens
-- deleteByKeywordAndTokenValues → (keyword_id, token_value)
-- deleteAllByTokenValues / findKeywordTokensWithKeyword → token_value 단독
ALTER TABLE keyword_tokens ADD INDEX idx_keyword_tokens_keyword_token (keyword_id, token_value);
ALTER TABLE keyword_tokens ADD INDEX idx_keyword_tokens_token_value (token_value);

-- club_events
-- findByTypeIn / findByTypeAndTitleContaining / findByTypeInAndTitleContaining
-- findByTypeAndAnyKeyword / findTop10ByType
--   → idx_club_events_type_createdat (type, created_at)  — type 필터 후 created_at DESC 정렬 커버
-- findTop10ByCreatedAtBetween (주간 트렌딩)
--   → idx_club_events_createdat_viewcount (created_at, view_count)
ALTER TABLE club_events ADD INDEX idx_club_events_type_createdat (type, created_at);
ALTER TABLE club_events ADD INDEX idx_club_events_createdat_viewcount (created_at, view_count);

-- event_likes
-- existsByMemberAndClubEvent / findByClubEventAndMember / findByMemberWithClubEvent
--   → uq_event_likes (member_id, club_event_id)  — unique + leading prefix (member_id) 커버
ALTER TABLE event_likes ADD UNIQUE KEY uq_event_likes (member_id, club_event_id);

-- push_clusters
-- findAllByJobStatus
ALTER TABLE push_clusters ADD INDEX idx_push_clusters_job_status (job_status);

-- push_cluster_tokens
-- findAllByPushClusterWithMember → push_cluster_id 필터
-- findRecoverableTokens — OR 조건 3개를 각 인덱스로 개별 커버
--   PENDING      AND request_time   < ?  → (job_status, request_time)
--   IN_PROGRESS  AND processed_time < ?  → (job_status, processed_time)
--   RETRY_PENDING AND retry_after <= ? AND retry_count <= ?
--                                        → (job_status, retry_after, retry_count)
ALTER TABLE push_cluster_tokens ADD INDEX idx_pct_push_cluster_id (push_cluster_id);
ALTER TABLE push_cluster_tokens ADD INDEX idx_pct_status_request (job_status, request_time);
ALTER TABLE push_cluster_tokens ADD INDEX idx_pct_status_processed (job_status, processed_time);
ALTER TABLE push_cluster_tokens ADD INDEX idx_pct_status_retry (job_status, retry_after, retry_count);

-- push_notifications
-- findByMemberAndNotificationType / findTopicNotificationsByMemberWithTopic
-- findKeywordNotificationsByMemberWithTopicAndKeyword
--   → idx_pn_member_type_notified (member_id, notification_type, notified_at)
--     notification_type 필터 후 notified_at DESC 정렬 커버
-- countByMemberAndIsReadFalse / updateAllUnreadByMember
--   → idx_pn_member_isread (member_id, is_read)
-- findFirstByPushClusterId
--   → idx_pn_push_cluster_id (push_cluster_id)
ALTER TABLE push_notifications ADD INDEX idx_pn_member_type_notified (member_id, notification_type, notified_at);
ALTER TABLE push_notifications ADD INDEX idx_pn_member_isread (member_id, is_read);
ALTER TABLE push_notifications ADD INDEX idx_pn_push_cluster_id (push_cluster_id);
