-- =============================================
-- AjouEvent V2 DDL
-- =============================================

-- members
CREATE TABLE IF NOT EXISTS members (
    id     BIGINT       NOT NULL AUTO_INCREMENT,
    email  VARCHAR(255) NOT NULL,
    name   VARCHAR(255),
    major  VARCHAR(255),
    phone  VARCHAR(255),
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
CREATE TABLE IF NOT EXISTS topics (
    id            BIGINT       NOT NULL AUTO_INCREMENT,
    department    VARCHAR(255),
    type          VARCHAR(100),
    classification VARCHAR(255),
    korean_topic  VARCHAR(255),
    korean_order  BIGINT,
    PRIMARY KEY (id),
    UNIQUE KEY uq_topics_type (type)
);

-- topic_members
CREATE TABLE IF NOT EXISTS topic_members (
    id                   BIGINT     NOT NULL AUTO_INCREMENT,
    topic_id             BIGINT,
    member_id            BIGINT,
    is_read              TINYINT(1) NOT NULL DEFAULT 0,
    last_read_at         DATETIME   NOT NULL,
    receive_notification TINYINT(1) NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    CONSTRAINT fk_topic_members_topic  FOREIGN KEY (topic_id)  REFERENCES topics  (id),
    CONSTRAINT fk_topic_members_member FOREIGN KEY (member_id) REFERENCES members (id)
);

-- topic_tokens
CREATE TABLE IF NOT EXISTS topic_tokens (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    topic_id    BIGINT,
    token_value VARCHAR(255),
    PRIMARY KEY (id),
    CONSTRAINT fk_topic_tokens_topic FOREIGN KEY (topic_id) REFERENCES topics (id)
);

-- keywords
CREATE TABLE IF NOT EXISTS keywords (
    id               BIGINT       NOT NULL AUTO_INCREMENT,
    encoded_keyword  VARCHAR(255),
    korean_keyword   VARCHAR(255),
    search_keyword   VARCHAR(255),
    topic_id         BIGINT,
    PRIMARY KEY (id),
    CONSTRAINT fk_keywords_topic FOREIGN KEY (topic_id) REFERENCES topics (id)
);

-- keyword_members
CREATE TABLE IF NOT EXISTS keyword_members (
    id           BIGINT     NOT NULL AUTO_INCREMENT,
    keyword_id   BIGINT,
    member_id    BIGINT,
    is_read      TINYINT(1) NOT NULL DEFAULT 0,
    last_read_at DATETIME   NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_keyword_members_keyword FOREIGN KEY (keyword_id) REFERENCES keywords (id),
    CONSTRAINT fk_keyword_members_member  FOREIGN KEY (member_id)  REFERENCES members  (id)
);

-- keyword_tokens
CREATE TABLE IF NOT EXISTS keyword_tokens (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    keyword_id  BIGINT,
    token_value VARCHAR(255),
    PRIMARY KEY (id),
    CONSTRAINT fk_keyword_tokens_keyword FOREIGN KEY (keyword_id) REFERENCES keywords (id)
);

-- club_events
CREATE TABLE IF NOT EXISTS club_events (
    event_id    BIGINT        NOT NULL AUTO_INCREMENT,
    title       VARCHAR(255),
    content     TEXT,
    writer      VARCHAR(255),
    created_at  DATETIME,
    subject     VARCHAR(255),
    url         VARCHAR(255),
    likes_count BIGINT        NOT NULL DEFAULT 0,
    view_count  BIGINT        NOT NULL DEFAULT 0,
    type        VARCHAR(100),
    PRIMARY KEY (event_id)
);

-- club_event_images
CREATE TABLE IF NOT EXISTS club_event_images (
    image_id     BIGINT       NOT NULL AUTO_INCREMENT,
    url          VARCHAR(255),
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
CREATE TABLE IF NOT EXISTS event_likes (
    event_like_id BIGINT NOT NULL AUTO_INCREMENT,
    club_event_id BIGINT,
    member_id     BIGINT,
    PRIMARY KEY (event_like_id),
    CONSTRAINT fk_event_likes_club_event FOREIGN KEY (club_event_id) REFERENCES club_events (event_id),
    CONSTRAINT fk_event_likes_member     FOREIGN KEY (member_id)     REFERENCES members    (id)
);

-- push_clusters
CREATE TABLE IF NOT EXISTS push_clusters (
    id             BIGINT       NOT NULL AUTO_INCREMENT,
    club_event_id  BIGINT,
    title          VARCHAR(255) NOT NULL,
    body           VARCHAR(255) NOT NULL,
    image_url      VARCHAR(255) NOT NULL,
    click_url      VARCHAR(255) NOT NULL,
    job_status     VARCHAR(20)  NOT NULL DEFAULT 'PENDING',
    total_count    INT          NOT NULL DEFAULT 0,
    registered_at  DATETIME     NOT NULL,
    success_count  INT          NOT NULL DEFAULT 0,
    fail_count     INT          NOT NULL DEFAULT 0,
    received_count INT          NOT NULL DEFAULT 0,
    clicked_count  INT          NOT NULL DEFAULT 0,
    start_at       DATETIME,
    end_at         DATETIME,
    PRIMARY KEY (id),
    CONSTRAINT fk_push_clusters_club_event FOREIGN KEY (club_event_id) REFERENCES club_events (event_id)
);

-- push_cluster_tokens
CREATE TABLE IF NOT EXISTS push_cluster_tokens (
    id               BIGINT       NOT NULL AUTO_INCREMENT,
    push_cluster_id  BIGINT       NOT NULL,
    member_id        BIGINT       NOT NULL,
    token_value      VARCHAR(255),
    job_status       VARCHAR(20)  NOT NULL DEFAULT 'PENDING',
    request_time     DATETIME     NOT NULL,
    processed_time   DATETIME,
    PRIMARY KEY (id),
    CONSTRAINT fk_push_cluster_tokens_push_cluster FOREIGN KEY (push_cluster_id) REFERENCES push_clusters (id),
    CONSTRAINT fk_push_cluster_tokens_member       FOREIGN KEY (member_id)       REFERENCES members       (id)
);

-- push_notifications
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
    CONSTRAINT fk_push_notifications_push_cluster FOREIGN KEY (push_cluster_id) REFERENCES push_clusters (id),
    CONSTRAINT fk_push_notifications_topic        FOREIGN KEY (topic_id)        REFERENCES topics        (id),
    CONSTRAINT fk_push_notifications_keyword      FOREIGN KEY (keyword_id)      REFERENCES keywords      (id),
    CONSTRAINT fk_push_notifications_member       FOREIGN KEY (member_id)       REFERENCES members       (id)
);
