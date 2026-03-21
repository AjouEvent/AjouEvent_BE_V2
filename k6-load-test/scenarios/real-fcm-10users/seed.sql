-- ============================================================
-- real-fcm-10users 부하 테스트 시드 데이터
-- ============================================================
-- 목적: real1~real10@ajou.ac.kr 멤버를 DB에 등록합니다.
--       FCM 토큰은 seed.sql에서 삽입하지 않습니다.
--       실제 FCM 토큰 등록은 setup.js가 API를 통해 처리합니다.
--
-- 실행 방법:
--   mysql -u <user> -p <database> < k6-load-test/scenarios/real-fcm-10users/seed.sql
--
-- 멱등성: 이미 존재하는 멤버는 중복 삽입되지 않습니다.
-- ============================================================

INSERT IGNORE INTO members (email, name, major, role) VALUES
  ('real1@ajou.ac.kr',  '실유저1',  '소프트웨어', 'USER'),
  ('real2@ajou.ac.kr',  '실유저2',  '소프트웨어', 'USER'),
  ('real3@ajou.ac.kr',  '실유저3',  '소프트웨어', 'USER'),
  ('real4@ajou.ac.kr',  '실유저4',  '소프트웨어', 'USER'),
  ('real5@ajou.ac.kr',  '실유저5',  '소프트웨어', 'USER'),
  ('real6@ajou.ac.kr',  '실유저6',  '소프트웨어', 'USER'),
  ('real7@ajou.ac.kr',  '실유저7',  '소프트웨어', 'USER'),
  ('real8@ajou.ac.kr',  '실유저8',  '소프트웨어', 'USER'),
  ('real9@ajou.ac.kr',  '실유저9',  '소프트웨어', 'USER'),
  ('real10@ajou.ac.kr', '실유저10', '소프트웨어', 'USER');
