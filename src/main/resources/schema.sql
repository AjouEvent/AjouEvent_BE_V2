-- =============================================
-- AjouEvent V2 DDL
-- =============================================

-- members
CREATE TABLE IF NOT EXISTS members (
    id     BIGINT       NOT NULL AUTO_INCREMENT,
    email  VARCHAR(255) NOT NULL,
    name   VARCHAR(255),
    major  VARCHAR(255),
    role   VARCHAR(20)  NOT NULL DEFAULT 'USER',
    PRIMARY KEY (id),
    UNIQUE KEY uq_members_email (email)
);

-- tokens
CREATE TABLE IF NOT EXISTS tokens (
    id              BIGINT       NOT NULL AUTO_INCREMENT,
    token_value     VARCHAR(255),
    expiration_date DATE         NOT NULL,
    member_id       BIGINT,
    is_deleted      TINYINT(1)   NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE KEY uq_tokens_token_value (token_value),
    CONSTRAINT fk_tokens_member FOREIGN KEY (member_id) REFERENCES members (id)
);

-- topics
-- findFirstByDepartment        → idx_topics_department
-- findByKoreanTopic            → idx_topics_korean_topic
CREATE TABLE IF NOT EXISTS topics (
    id             BIGINT       NOT NULL AUTO_INCREMENT,
    department     VARCHAR(255),
    type           VARCHAR(100),
    classification VARCHAR(255),
    korean_topic   VARCHAR(255),
    korean_order   BIGINT,
    PRIMARY KEY (id),
    UNIQUE KEY uq_topics_type (type),
    INDEX idx_topics_department  (department),
    INDEX idx_topics_korean_topic (korean_topic)
);

-- topic_members
-- existsByTopicAndMember / findByMemberAndTopic / deleteByTopicAndMember
--   → uq_topic_members (member_id, topic_id)  — unique + leading prefix (member_id) 커버
-- existsByMemberAndIsReadFalse
--   → idx_topic_members_member_isread (member_id, is_read)
-- findByTopicWithMemberAndReceiveNotificationTrue (push 발송 라우팅 핵심 쿼리)
--   → idx_topic_members_topic_notification (topic_id, receive_notification)
CREATE TABLE IF NOT EXISTS topic_members (
    id                   BIGINT     NOT NULL AUTO_INCREMENT,
    topic_id             BIGINT,
    member_id            BIGINT,
    is_read              TINYINT(1) NOT NULL DEFAULT 0,
    last_read_at         DATETIME   NOT NULL,
    receive_notification TINYINT(1) NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE KEY uq_topic_members (member_id, topic_id),
    INDEX idx_topic_members_member_isread      (member_id, is_read),
    INDEX idx_topic_members_topic_notification (topic_id, receive_notification),
    CONSTRAINT fk_topic_members_topic  FOREIGN KEY (topic_id)  REFERENCES topics  (id),
    CONSTRAINT fk_topic_members_member FOREIGN KEY (member_id) REFERENCES members (id)
);

-- topic_tokens
-- deleteByTopicAndTokenValues  → idx_topic_tokens_topic_token (topic_id, token_value)
-- deleteAllByTokenValues       → idx_topic_tokens_token_value (token_value)
CREATE TABLE IF NOT EXISTS topic_tokens (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    topic_id    BIGINT,
    token_value VARCHAR(255),
    PRIMARY KEY (id),
    INDEX idx_topic_tokens_topic_token (topic_id, token_value),
    INDEX idx_topic_tokens_token_value (token_value),
    CONSTRAINT fk_topic_tokens_topic FOREIGN KEY (topic_id) REFERENCES topics (id)
);

-- keywords
-- findByEncodedKeyword         → uq_keywords_encoded_keyword (encoded_keyword)
-- findBySearchKeyword          → idx_keywords_search_keyword (search_keyword)
-- findByTopic                  → idx_keywords_topic_id (topic_id)
CREATE TABLE IF NOT EXISTS keywords (
    id               BIGINT       NOT NULL AUTO_INCREMENT,
    encoded_keyword  VARCHAR(255),
    korean_keyword   VARCHAR(255),
    search_keyword   VARCHAR(255),
    topic_id         BIGINT,
    PRIMARY KEY (id),
    UNIQUE KEY uq_keywords_encoded_keyword (encoded_keyword),
    INDEX idx_keywords_search_keyword (search_keyword),
    INDEX idx_keywords_topic_id       (topic_id),
    CONSTRAINT fk_keywords_topic FOREIGN KEY (topic_id) REFERENCES topics (id)
);

-- keyword_members
-- existsByKeywordAndMember / findByKeywordAndMemberAndIsReadFalse / deleteByKeywordAndMember
--   → uq_keyword_members (member_id, keyword_id)  — unique + leading prefix 커버
-- existsByMemberAndIsReadFalse
--   → idx_keyword_members_member_isread (member_id, is_read)
-- findByKeywordWithMember
--   → idx_keyword_members_keyword_id (keyword_id)
CREATE TABLE IF NOT EXISTS keyword_members (
    id           BIGINT     NOT NULL AUTO_INCREMENT,
    keyword_id   BIGINT,
    member_id    BIGINT,
    is_read      TINYINT(1) NOT NULL DEFAULT 0,
    last_read_at DATETIME   NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uq_keyword_members (member_id, keyword_id),
    INDEX idx_keyword_members_member_isread (member_id, is_read),
    INDEX idx_keyword_members_keyword_id    (keyword_id),
    CONSTRAINT fk_keyword_members_keyword FOREIGN KEY (keyword_id) REFERENCES keywords (id),
    CONSTRAINT fk_keyword_members_member  FOREIGN KEY (member_id)  REFERENCES members  (id)
);

-- keyword_tokens
-- deleteByKeywordAndTokenValues  → idx_keyword_tokens_keyword_token (keyword_id, token_value)
-- deleteAllByTokenValues / findKeywordTokensWithKeyword
--   → idx_keyword_tokens_token_value (token_value)
CREATE TABLE IF NOT EXISTS keyword_tokens (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    keyword_id  BIGINT,
    token_value VARCHAR(255),
    PRIMARY KEY (id),
    INDEX idx_keyword_tokens_keyword_token (keyword_id, token_value),
    INDEX idx_keyword_tokens_token_value   (token_value),
    CONSTRAINT fk_keyword_tokens_keyword FOREIGN KEY (keyword_id) REFERENCES keywords (id)
);

-- club_events
-- findByTypeIn / findByTypeInAndTitleContaining / findTop10ByTypeOrderByCreatedAtDesc
-- findByTypeAndAnyKeyword
--   → idx_club_events_type_createdat (type, created_at)
--     type 필터 후 created_at DESC 정렬까지 인덱스 커버
-- findTop10ByCreatedAtBetweenOrderByViewCountDesc (주간 트렌딩)
--   → idx_club_events_createdat_viewcount (created_at, view_count)
CREATE TABLE IF NOT EXISTS club_events (
    event_id         BIGINT        NOT NULL AUTO_INCREMENT,
    title            VARCHAR(255),
    content          TEXT,
    content_preview  VARCHAR(200),
    writer           VARCHAR(255),
    created_at       DATETIME,
    subject          VARCHAR(255),
    url              VARCHAR(255),
    likes_count      BIGINT        NOT NULL DEFAULT 0,
    view_count       BIGINT        NOT NULL DEFAULT 0,
    type             VARCHAR(100),
    PRIMARY KEY (event_id),
    INDEX idx_club_events_type_createdat     (type, created_at),
    INDEX idx_club_events_createdat_viewcount (created_at, view_count)
);

-- club_event_images
CREATE TABLE IF NOT EXISTS club_event_images (
    image_id      BIGINT       NOT NULL AUTO_INCREMENT,
    url           VARCHAR(255),
    club_event_id BIGINT,
    PRIMARY KEY (image_id),
    CONSTRAINT fk_club_event_images_club_event FOREIGN KEY (club_event_id) REFERENCES club_events (event_id)
);

-- event_banners
CREATE TABLE IF NOT EXISTS event_banners (
    event_banner_id BIGINT       NOT NULL AUTO_INCREMENT,
    banner_order    BIGINT       NOT NULL,
    img_url         VARCHAR(255) NOT NULL,
    site_url        VARCHAR(255) NOT NULL,
    start_date      DATE         NOT NULL,
    end_date        DATE         NOT NULL,
    PRIMARY KEY (event_banner_id)
);

-- event_likes
-- existsByMemberAndClubEvent / findByClubEventAndMember / deleteByMemberAndClubEvent
--   → uq_event_likes (member_id, club_event_id) — unique + leading prefix(member_id) 커버
CREATE TABLE IF NOT EXISTS event_likes (
    event_like_id BIGINT NOT NULL AUTO_INCREMENT,
    club_event_id BIGINT,
    member_id     BIGINT,
    PRIMARY KEY (event_like_id),
    UNIQUE KEY uq_event_likes (member_id, club_event_id),
    CONSTRAINT fk_event_likes_club_event FOREIGN KEY (club_event_id) REFERENCES club_events (event_id),
    CONSTRAINT fk_event_likes_member     FOREIGN KEY (member_id)     REFERENCES members    (id)
);

-- push_clusters
-- findAllByJobStatus            → idx_push_clusters_job_status (job_status)
CREATE TABLE IF NOT EXISTS push_clusters (
    id                  BIGINT       NOT NULL AUTO_INCREMENT,
    club_event_id       BIGINT,
    title               VARCHAR(255) NOT NULL,
    body                VARCHAR(255) NOT NULL,
    image_url           VARCHAR(255) NOT NULL,
    click_url           VARCHAR(255) NOT NULL,
    job_status          VARCHAR(20)  NOT NULL DEFAULT 'PENDING',
    total_count         INT          NOT NULL DEFAULT 0,
    registered_at       DATETIME     NOT NULL,
    success_count       INT          NOT NULL DEFAULT 0,
    fail_count          INT          NOT NULL DEFAULT 0,
    start_at            DATETIME,
    end_at              DATETIME,
    retry_success_count INT          NOT NULL DEFAULT 0,
    retry_fail_count    INT          NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    INDEX idx_push_clusters_job_status (job_status),
    CONSTRAINT fk_push_clusters_club_event FOREIGN KEY (club_event_id) REFERENCES club_events (event_id)
);

-- push_cluster_tokens
-- findAllByPushClusterWithMember
--   → idx_pct_push_cluster_id (push_cluster_id)
-- findRecoverableTokens — OR 조건 3개를 각 인덱스로 개별 커버
--   PENDING      AND request_time   < ?  → idx_pct_status_request_time   (job_status, request_time)
--   IN_PROGRESS  AND processed_time < ?  → idx_pct_status_processed_time (job_status, processed_time)
--   RETRY_PENDING AND retry_after <= ? AND retry_count <= ?
--                                        → idx_pct_status_retry          (job_status, retry_after, retry_count)
CREATE TABLE IF NOT EXISTS push_cluster_tokens (
    id              BIGINT       NOT NULL AUTO_INCREMENT,
    push_cluster_id BIGINT       NOT NULL,
    member_id       BIGINT       NOT NULL,
    token_value     VARCHAR(255),
    job_status      VARCHAR(20)  NOT NULL DEFAULT 'PENDING',
    request_time    DATETIME     NOT NULL,
    processed_time  DATETIME,
    retry_count     INT          NOT NULL DEFAULT 0,
    retry_after     DATETIME,
    PRIMARY KEY (id),
    INDEX idx_pct_push_cluster_id    (push_cluster_id),
    INDEX idx_pct_status_request     (job_status, request_time),
    INDEX idx_pct_status_processed   (job_status, processed_time),
    INDEX idx_pct_status_retry       (job_status, retry_after, retry_count),
    CONSTRAINT fk_push_cluster_tokens_push_cluster FOREIGN KEY (push_cluster_id) REFERENCES push_clusters (id),
    CONSTRAINT fk_push_cluster_tokens_member       FOREIGN KEY (member_id)       REFERENCES members       (id)
);

-- push_notifications
-- findByMemberAndNotificationType / findTopicNotificationsByMemberWithTopic
-- findKeywordNotificationsByMemberWithTopicAndKeyword
--   → idx_pn_member_type_notified (member_id, notification_type, notified_at)
--     notification_type = 'TOPIC'/'KEYWORD' 필터 후 notified_at DESC 정렬 커버
-- countByMemberAndIsReadFalse / updateAllUnreadByMember
--   → idx_pn_member_isread (member_id, is_read)
-- findFirstByPushClusterId
--   → idx_pn_push_cluster_id (push_cluster_id)
CREATE TABLE IF NOT EXISTS push_notifications (
    id                BIGINT       NOT NULL AUTO_INCREMENT,
    push_cluster_id   BIGINT       NOT NULL,
    topic_id          BIGINT,
    keyword_id        BIGINT,
    member_id         BIGINT       NOT NULL,
    notification_type VARCHAR(20)  NOT NULL,
    title             VARCHAR(255) NOT NULL,
    body              VARCHAR(255) NOT NULL,
    image_url         VARCHAR(255) NOT NULL,
    click_url         VARCHAR(255) NOT NULL,
    is_read           TINYINT(1)   NOT NULL DEFAULT 0,
    clicked_at        DATETIME,
    notified_at       DATETIME,
    PRIMARY KEY (id),
    INDEX idx_pn_member_type_notified (member_id, notification_type, notified_at),
    INDEX idx_pn_member_isread        (member_id, is_read),
    INDEX idx_pn_push_cluster_id      (push_cluster_id),
    CONSTRAINT fk_push_notifications_push_cluster FOREIGN KEY (push_cluster_id) REFERENCES push_clusters (id),
    CONSTRAINT fk_push_notifications_topic        FOREIGN KEY (topic_id)        REFERENCES topics        (id),
    CONSTRAINT fk_push_notifications_keyword      FOREIGN KEY (keyword_id)      REFERENCES keywords      (id),
    CONSTRAINT fk_push_notifications_member       FOREIGN KEY (member_id)       REFERENCES members       (id)
);

-- notice (crawling server)
CREATE TABLE IF NOT EXISTS notice (
    notice_id BIGINT       NOT NULL,
    topic_id  BIGINT       NOT NULL,
    type      VARCHAR(255) NOT NULL,
    value     BIGINT       NOT NULL,
    PRIMARY KEY (notice_id)
);

-- shedlock (for scheduler / distributed lock)
CREATE TABLE IF NOT EXISTS shedlock (
    name       VARCHAR(64)  NOT NULL,
    lock_until TIMESTAMP(3) NOT NULL,
    locked_at  TIMESTAMP(3) NOT NULL,
    locked_by  VARCHAR(255) NOT NULL,
    PRIMARY KEY (name)
);
