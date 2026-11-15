---
type: 인프라
status: 확정
source: infra.json before_after · timeline · risks · OCI 이전 보고서
updated: 2026-11-15
tags: []
---

# AWS·아올다에서 OCI로 이전

## 이전과 이후

### 이전

- 아올다 클라우드 ajouevent-app m1.4xlarge (8 vCPU · 32GB, 2.6GB 사용, 컨테이너 7개, 512GiB 볼륨) — HTTPS는 아올다 프록시 210.107.196.188 (근거: `infra.json` before_after.before)
- AWS RDS ajouevent-v2-db (MySQL 8.4.8, db.t3.micro, 20GB, 퍼블릭 접근 켜짐, 앱·크롤러 공용 계정 1개, 7일 자동 백업) (근거: `infra.json` before_after.before)
- AWS Route 53 ajouevent.com (레코드 4개) (근거: `infra.json` before_after.before)
- AWS EC2 ajouevent-v2 (nginx만) · ajouevent-monitoring (중지) + EBS · 탄력적 IP (근거: `infra.json` before_after.before)
- AWS S3 버킷 0개 (SDK 의존성만) (근거: `infra.json` before_after.before)
- Vercel Hobby (개인 계정, 조직 저장소를 개인 저장소로 복사해 배포) — www · 루트 (근거: `infra.json` before_after.before)
- Docker Hub 공개 이미지 wa6221/ajouevent-be(Firebase 키 포함) · wa6221/ajouevent-crawling (근거: `infra.json` before_after.before)
- GitHub Actions deploy.yml → 아올다(포트 20000, EC2_* 시크릿), 이미지 amd64 단일 (근거: `infra.json` before_after.before)
- 비밀은 아올다 서버 .env 평문 (근거: `infra.json` before_after.before)
- 공개 포트 9개 (Redis 6379, Loki 3100, Grafana 3000, actuator 9090 등) (근거: `infra.json` before_after.before)
- AjouEvent_Monitering 저장소(AWS 모니터링 EC2용 Prometheus · Grafana) (근거: `infra.json` before_after.before)
### 이후

- OCI 춘천 A1 ajouevent-core 2 OCPU/12GB arm64 — Caddy · app · Redis · Prometheus · Loki · Grafana · cAdvisor · node-exporter · crawler (9개 컨테이너) (근거: `infra.json` before_after.after)
- HeatWave MySQL.Free ajouevent-mysql (사설 10.0.1.93, 계정 3개 분리) (근거: `infra.json` before_after.after)
- OCI Vault ajouevent-vault (비밀 7개, 인스턴스 주체) (근거: `infra.json` before_after.after)
- Object Storage ajouevent-backups (db/ 14일, archive/ 영구) + shared-objects (근거: `infra.json` before_after.after)
- E2 Micro 2대 (유휴) (근거: `infra.json` before_after.after)
- Cloudflare DNS (Free, 프록시 전부 끔) + Cloudflare Pages(www) (근거: `infra.json` before_after.after)
- Caddy가 루트 도메인 301 리다이렉트 · Let's Encrypt TLS (근거: `infra.json` before_after.after)
- GHCR ghcr.io/ajouevent/* 공개, 멀티 아키(arm64+amd64), GITHUB_TOKEN 인증 (근거: `infra.json` before_after.after)
- GitHub Actions deploy-oci.yml (BE · 크롤러) → SSH(OCI_HOST/OCI_DEPLOY_KEY), FE deploy-pages.yml → wrangler (근거: `infra.json` before_after.after)
- 공개 포트 3개 (22, 80, 443 + UDP 443) (근거: `infra.json` before_after.after)
- Firebase 서비스 계정 키 교체 (firebase@ajou-event, FCM 전용) (근거: `infra.json` before_after.after)

## 시간순 기록

| 시각 | 사건 | 근거 |
| --- | --- | --- |
| 2026-11-07 11:34/11:39 UTC | E2 Micro 2대 생성 (always-free-e2-micro, -2) | notes §1.1 |
| 2026-11-13 14:05 KST (05:05 UTC) | A1 ajouevent-core 생성 (4 OCPU/24GB), 예약 IP 이동, 리허설 | notes §1.1, §2.5 |
| 2026-11-13 14:30:00 | 아올다 크롤러 정지 | report §전환 기록 |
| 2026-11-13 14:30:59 | 아올다 앱 정지 — 서비스 중단 시작 | report §전환 기록 |
| 2026-11-13 14:31~14:32 | RDS 최종 덤프(21MB, 39초) · Redis · 캘린더 토큰 · Grafana DB 추출 | report §전환 기록 |
| 2026-11-13 14:33~14:35 | HeatWave 최종 복원 85초, 행 수 일치 | report §전환 기록 |
| 2026-11-13 14:35:34 / 14:35:50 | A1 앱 기동(13초, 실제 Firebase 키) / 크롤러 기동 | report §전환 기록 |
| 2026-11-13 14:36:40 | Route 53 api · admin.api → 146.56.101.16 (TTL 60) | report §전환 기록 |
| 2026-11-13 14:37:27 | Caddy 운영 인증서 발급 — 서비스 재개 (중단 약 6분 30초) | report §전환 기록 |
| 2026-11-13 14:38~14:40 | 크롤러 웹훅 401(경로 누락) 수정, Vault crawling-env v4, 14:40 새 공지 발송 성공 | report §웹훅 401 |
| 2026-11-13 15:44 | www → Cloudflare Pages, 루트 → Caddy 리다이렉트 (www 인증서 발급까지 약 4분 무응답) | report §DNS 진행 순서 |
| 2026-11-13 (오후) | 가비아 NS → Cloudflare, Route 53 호스팅 영역 삭제, www 522 장애(최대 약 40분) → 회색 구름으로 복구, AWS · 아올다 · Docker Hub 리소스 삭제 | report §522 복구 · §정리 결과 |
| 2026-11-13 17:13 | Firebase 키 b0cef1로 교체 (Vault v2, 앱 재시작 13초), 옛 키 삭제 | report §Firebase 키 |
| 2026-11-13 18:27~18:30 | A1 2 OCPU/12GB로 축소, 중단 약 2분 30초 | notes §2.5 |
| 2026-11-14 04:31 | 논리 백업 타이머 다음 실행 예정 | notes §2.5 |
| 2026-11-20 | 11/13 HeatWave FULL 백업(7일 보관) 만료 | notes §3.3 |
| 2026-11-29 / 12월 초 | A1 과금 여부 확인 (Usage API, SKU B93297 · B93298) | notes §2.5 |
| 2027-02-11 | Let's Encrypt 인증서 만료 (Caddy 자동 갱신) | report §리허설 검증 |
| 2027-05-28 | ajouevent.com 도메인 만료 (가비아) | report §DNS·비용 |

## 롤백과 남은 위험

이전과 롤백 상태는 OCI 이전 보고서의 컷오버 기록을 기준으로 본다. 근거: `_sources/ajouevent-oci-migration.html` 이전·전환 탭.

| 위험 | 조치안 | 상태 | 근거 |
| --- | --- | --- | --- |
| node-exporter 수집 실패 (up=0) — 호스트 CPU · 메모리 지표 없음 | iptables에 Docker 브리지 → 9100 허용 또는 대상 변경 | 미수정 | notes §1.2 |
| cAdvisor name 라벨 없음 → 컨테이너 패널 비어 있음 | 대시보드 쿼리 확인 | 미확인 | notes §1.2 |
| 단일 인스턴스 SPOF (A1 하나에 전부, 외부 상태 점검 없음) | Micro 2에 Gatus, Redis Sentinel 등 (계획) | 보류 | report §아키텍처 변경안 |
| 크롤러 웹훅 실패 시 공지 영구 유실, 삭제·재게시 시 유실, 빈 공지 발송, 고루틴 패닉으로 전체 종료, Redis 장애 시 log.Fatalf | 글 고유 번호 키 + 발송 후 기록 + 재시도 | 결정 대기 (개발 중단, 브랜치 refactor/article-key-dedup 미커밋) | notes §5.2, §9.1, §11 |
| 소프트웨어학과 게시판 404 (구조 변경) | 새 주소 https://software.ajou.ac.kr/board/notice + 파서 변경 | 미수정 | notes §4 |
| 크롤러 요청마다 http.Client 생성 — 하루 약 8.4만 TLS 핸드셰이크, 58개 동시 요청이 한 IP로 몰림 (월 약 164GB 수신) | 공용 Transport · 요청 분산 | 결정 대기 | notes §7 |
| 논리 백업 중단 여부 미결 — 중단 시 복구 범위 1일 | 유지 또는 주 1회 | 결정 대기 | notes §3.3 |
| A1 과금 가능성 — B93297/B93298 일반 SKU | 12월 초 Usage API 확인, OCI Budget $1 알림 권장 | 확인 대기 | notes §2.5 |
| 유휴 인스턴스 회수 정책 (A1 7일 메모리 20% 미만 등) — PAYG 적용 여부 불명 | - | 불명 | notes §2.4, §1.3 |
| 공지 이미지가 학교 서버 원본 직접 로드 (일부 네트워크 20초 시간 초과, http 4,476행, 깨진 주소 232행) | ④ 미러링 + A1 imgproxy (내부 전용) | 결정 대기 | notes §10, report §④ · ⑤ |
| 재게시 공지 알림 정책 미정 | A안 갱신만 / B안 새 알림 | 결정 대기 | notes §6.3 |
| Redis 비밀번호 없음 + 10.0.0.170:6379 VCN 노출 | requirepass 또는 바인딩 축소 | 코드상 미설정 | deploy/docker-compose.yml:58-60 |
| 앱 → HeatWave JDBC useSSL=false (VCN 내부 평문) | - | 코드상 | src/main/resources/config/db.yaml |
| Grafana 공개 도메인에 자체 로그인만 (추가 접근 제한 없음) | - | 코드상 | deploy/caddy/Caddyfile:57-60 |
| CORS에 죽은 주소 dev.ajouevent.com, dev.fe.ajouevent.com 남음 | Vault app-env 정리 | 별도 작업 | report §정리 결과 |
| 도메인 소유자 심재엽 님, Google OAuth 클라이언트 소유자 미상, Vercel 프로젝트 타인 계정 잔존 | 소유권 이전 | 미해결 | report §DNS·비용 · §Google |
| 로컬 볼륨(calendar_tokens, grafana_data, redis_data)은 A1 소실 시 함께 사라짐 | 볼륨 백업(무료 5개) | 미구성 | docker-compose.yml:167-174, notes §10.3 |
| Loki가 127.0.0.1에만 열려 있어 크롤러를 Micro로 옮기면 로그 전송 불가, deploy-oci.yml은 OCI_HOST 한 곳만 배포 | - | 참고 | notes §1.3 |
| 보류 과제 ①~⑥ (알림 보관 배치, Redis Streams 큐, k3s, 이미지 미러링, 이미지 프록시, 본문 시각화) — k3s 전제 '여유 23GB'는 약 10GB로 수정 필요 | - | 보류 | report §보류 과제, notes §2.5 |
