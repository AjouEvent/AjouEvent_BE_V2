-- =============================================
-- AjouEvent V2 — 성능 최적화 및 부하 테스트 스크립트
-- schema.sql은 CREATE TABLE IF NOT EXISTS 기반이므로
-- 기존 테이블에는 아래 ALTER 및 UPDATE 구문으로 직접 적용합니다.
-- =============================================

-- ---------------------------------------------
-- 1. 스키마 마이그레이션 (Schema Migration)
-- ---------------------------------------------
-- 목적: 읽기(Read) 성능 향상 및 Sort Buffer 오버헤드 감소를 위한 페이로드 분리
ALTER TABLE club_events
    ADD COLUMN content_preview VARCHAR(200) AFTER content;

-- 기존 데이터 백필 (Backfill)
UPDATE club_events
SET content_preview = LEFT(content, 200)
WHERE content_preview IS NULL;


-- ---------------------------------------------
-- 2. 부하 테스트용 더미 데이터 적재 (Dummy Data Seeding)
-- ---------------------------------------------
-- 목적: Full Table Scan 및 Using filesort 병목을 검증하기 위한 5만 건 데이터 Bulk Insert
-- 주의: CTE 무한 루프 방지용 세션 변수(cte_max_recursion_depth) 선행 조정 필수
SET SESSION cte_max_recursion_depth = 100000;

INSERT INTO club_events (title, content, content_preview, writer, created_at, type, subject, url, likes_count, view_count)
WITH RECURSIVE seq(n) AS (
    SELECT 1
    UNION ALL
    SELECT n + 1 FROM seq WHERE n < 50000
)
SELECT
    CONCAT('공지사항 ', n),
    REPEAT('본문 내용입니다. ', 200),
    LEFT(REPEAT('본문 내용입니다. ', 200), 200),
    '소프트웨어학과',
    NOW() - INTERVAL n SECOND, -- 인덱스 정렬 테스트를 위한 타임스탬프 분산
    'AJOUNORMAL',
    '소프트웨어학과',
    CONCAT('https://example.com/', n),
    0, 0
FROM seq;


-- ---------------------------------------------
-- 3. 실행 계획 및 성능 비교 분석 (EXPLAIN ANALYZE)
-- ---------------------------------------------

-- [Test A: 개선 전 모델] Heavy Payload 조회
-- 목적: content(TEXT) 포함 시 메모리 적재량 증가에 따른 Sort 연산 병목 및 디스크 I/O 소요 시간 측정
EXPLAIN ANALYZE
SELECT event_id, title, content, writer, created_at, likes_count, view_count, type, subject, url
FROM club_events
WHERE type IN ('AJOUNORMAL')
ORDER BY created_at DESC
    LIMIT 20;

-- [Test B: 개선 후 모델] Light Payload 조회
-- 목적: content_preview(VARCHAR) 대체 시 레코드 크기 축소로 인한 메모리 내 정렬(In-memory Sort) 효율 및 속도 단축 비율 검증
EXPLAIN ANALYZE
SELECT event_id, title, content_preview, writer, created_at, likes_count, view_count, type, subject, url
FROM club_events
WHERE type IN ('AJOUNORMAL')
ORDER BY created_at DESC
    LIMIT 20;
