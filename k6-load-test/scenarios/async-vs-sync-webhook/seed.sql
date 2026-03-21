-- ============================================================
-- async-vs-sync-webhook 부하 테스트 시드 데이터
-- ============================================================
-- 목적: 테스트 멤버(test1 ~ testN@ajou.ac.kr)를 DB에 등록하고
--       AjouNormal 토픽 구독 + 가짜 FCM 토큰을 멤버별로 삽입합니다.
--
-- 실행 방법 (USER_COUNT=100 기준):
--   mysql -u <user> -p <database> < k6-load-test/scenarios/async-vs-sync-webhook/seed.sql
--
-- FCM 토큰 전략:
--   tokens.token_value 에는 단독 UNIQUE 제약이 있어 동일한 실제 FCM 토큰을
--   여러 멤버에게 등록하는 것이 불가합니다.
--   대신 'load_test_token_N' 형식의 가짜 고유 토큰을 직접 삽입합니다.
--   FCM은 유효하지 않은 토큰에 에러를 반환하지만, 서버는 N개 메시지를
--   빌드·전송 시도하므로 서블릿 스레드 점유 부하 측정에는 충분합니다.
--   → setup.js를 통한 API 등록은 불필요합니다.
--
-- 멱등성: 이미 존재하는 멤버·토큰·구독은 중복 삽입되지 않습니다.
-- ============================================================

DROP PROCEDURE IF EXISTS seed_async_vs_sync_webhook;

DELIMITER $$

CREATE PROCEDURE seed_async_vs_sync_webhook(IN user_count INT)
BEGIN
    DECLARE i INT DEFAULT 1;
    DECLARE v_member_id BIGINT;
    DECLARE v_topic_id BIGINT;

    -- AjouNormal 토픽 ID 조회
    SELECT id INTO v_topic_id FROM topics WHERE type = 'AjouNormal' LIMIT 1;

    WHILE i <= user_count DO
        -- 멤버 등록 (이미 존재하면 SKIP)
        INSERT IGNORE INTO members (email, name, major, role)
        VALUES (
            CONCAT('test', i, '@ajou.ac.kr'),
            CONCAT('테스트유저', i),
            '소프트웨어',
            'USER'
        );

        -- 멤버 ID 조회
        SELECT id INTO v_member_id
        FROM members
        WHERE email = CONCAT('test', i, '@ajou.ac.kr');

        -- 가짜 FCM 토큰 등록 (이미 존재하면 SKIP)
        INSERT IGNORE INTO tokens (token_value, member_id, expiration_date, is_deleted)
        VALUES (
            CONCAT('load_test_token_', i),
            v_member_id,
            DATE_ADD(CURDATE(), INTERVAL 70 DAY),
            0
        );

        -- AjouNormal 구독 등록 (이미 구독 중이면 SKIP)
        INSERT INTO topic_members (topic_id, member_id, is_read, last_read_at, receive_notification)
        SELECT v_topic_id, v_member_id, 0, NOW(), 1
        WHERE NOT EXISTS (
            SELECT 1
            FROM topic_members tm
            WHERE tm.topic_id = v_topic_id
              AND tm.member_id = v_member_id
        );

        SET i = i + 1;
    END WHILE;
END$$

DELIMITER ;

-- ============================================================
-- USER_COUNT 값을 변경하고 싶다면 아래 숫자를 수정하세요.
-- ============================================================
CALL seed_async_vs_sync_webhook(100);

DROP PROCEDURE IF EXISTS seed_async_vs_sync_webhook;
