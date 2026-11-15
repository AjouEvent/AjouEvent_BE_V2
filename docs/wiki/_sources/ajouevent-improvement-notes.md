# AjouEvent 개선 정리 노트

- 작성일: 2026-11-13 (KST)
- 기준 문서: `~/Downloads/ajouevent-deferred-items.pdf` (보류 과제 설계, ①~⑥)
- 범위: 보류 과제 ④ · ⑤ · ⑥, 소프트웨어학과 크롤링 실패, 크롤러 전체 개선점, OCI 비용 · 무료 리소스, DB 백업
- 이 문서는 대화에서 나온 질문 · 확인 결과 · 결론을 모두 적은 기록입니다. 아래의 숫자는 모두 2026-11-13에 직접 측정하거나 조회한 값이고, 조회 방법은 12장에 적었습니다.
- 표기: **[확인]**은 직접 조회하거나 측정한 사실, **[추정]**은 계산이나 문서에서 나온 값, **[결정 대기]**는 사용자가 정해야 할 항목입니다.

---

## 0. 한눈에 보기

### 0.1 결정 대기 항목

1. ~~A1 인스턴스 크기~~ → **완료(2026-11-13 18:27~18:30 KST)**: 2 OCPU · 12GB로 줄였습니다. 무료 한도 안이라 과금되지 않습니다(2.5).
2. **DB 논리 백업 중단 여부** [결정 대기]: 사용자는 「하지 말자」고 했습니다. 아직 중단하지 않았습니다. 중단하면 HeatWave 자동 백업(보관 1일)만 남습니다(3장).
3. **크롤러 개선 착수 범위** [결정 대기]: 감지 로직(5장), 소프트웨어학과(4장), 연결 풀(7장) 중 어디부터 개발할지 정해야 합니다.
4. **이미지 처리 방식** [결정 대기]: 사용자는 이미지를 볼 때 학교 서버를 거치지 않는 방식을 원합니다. 크롤링할 때 한 번만 받아 저장하는 방식(④ 미러링)을 기본으로 하고, 변환 위치는 A1 imgproxy(권장) 또는 OCI Functions 중에서 정해야 합니다(10장).
5. **재게시 공지 처리 정책** [결정 대기]: 같은 내용을 지우고 다시 올린 글을 새 알림으로 보낼지, 기존 공지를 갱신만 하고 알림은 보내지 않을지 정해야 합니다(6장).

### 0.2 사용자 지시 기록

- 「개선점을 정리해 달라는 것이지 개발을 진행하라는 뜻이 아니었다」 → 크롤러 개발은 중단했습니다. 이미 만든 코드는 11장에 적었습니다.
- 「Rust 등 더 효율적인 코드로 바꿔도 좋다」 → 측정해 보니 Go를 유지하는 쪽이 낫다고 판단했습니다(8장).
- 「학교 서버를 경유하는 것은 좋지 않다」 → 이미지를 볼 때마다 학교 서버를 호출하는 ⑤ 프록시 방식 대신 ④ 미러링을 기본으로 삼습니다(10장).
- 「DB 백업을 왜 하는 거야? 하지 말자」 → 백업 구성과 이유를 3장에 정리했습니다. 실제 중단은 승인을 받은 뒤에 합니다.

---

## 1. 인프라 현황 [확인]

### 1.1 OCI 인스턴스 (테넌시 프로필 `AJOUEVENT`, 리전 ap-chuncheon-1)

| 이름 | Shape | OCPU / 메모리 | 생성 시각 (UTC) | 사설 IP | 공인 IP | 하는 일 |
| --- | --- | --- | --- | --- | --- | --- |
| ajouevent-core | VM.Standard.A1.Flex | **2 / 12GB** (11/13 18:27 KST에 4 / 24GB에서 축소) | 2026-11-13 05:05 | 10.0.0.170 | 146.56.101.16 | 앱 · 크롤러 · Caddy · Redis · 모니터링 전부 |
| always-free-e2-micro | VM.Standard.E2.1.Micro | 1 / 1GB | 2026-11-07 11:34 | 10.0.0.84 | 158.179.169.74 | 실행 중인 컨테이너 없음 |
| always-free-e2-micro-2 | VM.Standard.E2.1.Micro | 1 / 1GB | 2026-11-07 11:39 | 10.0.0.122 | 168.107.15.42 | 실행 중인 컨테이너 없음 |

- **크롤러는 Micro가 아니라 A1(ajouevent-core)에서 동작합니다.** `docker ps` 결과에 `ajouevent-crawling`이 A1에 있고, Micro 두 대에는 실행 중인 컨테이너가 없습니다.
- A1 컨테이너 목록: ajouevent-app, ajouevent-caddy, ajouevent-loki, ajouevent-prometheus, ajouevent-crawling, ajouevent-grafana, ajouevent-redis, ajouevent-cadvisor, ajouevent-node-exporter.
- A1 메모리(축소 전): 전체 23GB 중 사용 1.8GB, 캐시 10GB, 가용 21.6GB. 축소 직후: 전체 11.6GB 중 사용 1.4GB, 가용 10.3GB. 디스크: 부트 볼륨 50GB 중 15GB 사용.
- A1 CPU: Neoverse-N1(Ampere), 코어당 스레드 1개라 1 OCPU = vCPU 1개입니다.
- 리소스 사용량: 크롤러 17.45MiB / 128MiB(제한), CPU 0%. 앱 799.4MiB / 2GiB, CPU 0.29%.
- HeatWave: `ajouevent-mysql`(MySQL.Free, ACTIVE), 사설 IP 10.0.1.93.
- Object Storage 버킷: `ajouevent-backups`, `shared-objects`.

### 1.2 모니터링 이상: node-exporter 수집 실패 [확인]

- Prometheus 대상 `10.0.0.170:9100`(job `node`)이 `up=0`입니다. 그래서 서버 CPU · 메모리 지표(node_*)가 Grafana에 없습니다.
- 원인 [추정]: node-exporter는 호스트 네트워크로 `10.0.0.170:9100`에서 대기합니다. A1의 iptables INPUT은 `10.0.0.0/24`, 22, 80, 443만 허용합니다. Prometheus 컨테이너는 Docker 브리지(172.x)에서 접속하므로 차단됩니다.
- 고치는 방법 후보: iptables에 Docker 브리지 대역에서 9100으로 들어오는 접속을 허용하거나, Prometheus 대상을 `host.docker.internal` 등으로 바꿉니다. 아직 고치지 않았습니다.
- cAdvisor 지표에는 `name` 라벨이 없어 컨테이너별 집계 쿼리(`name=~"ajouevent.*"`)가 비어 나옵니다. 대시보드 쿼리를 확인해야 합니다.

### 1.3 질문: 크롤러를 Micro에서 돌리면 안 되나? [확인 + 판단]

- **기술적으로는 가능합니다.**
  - Micro(always-free-e2-micro, 10.0.0.84)에는 Docker가 설치돼 있고 x86_64입니다. 크롤러 이미지는 `linux/amd64, linux/arm64` 두 가지로 빌드됩니다(`deploy-oci.yml:48`).
  - Micro에서 Redis(10.0.0.170:6379), 웹훅(10.0.0.170:8080), HeatWave(10.0.1.93:3306)에 접속됩니다.
  - 자원: 크롤러는 메모리 17MB를 쓰고, Micro의 가용 메모리는 613MB입니다. 매분 받는 3.8MB는 50Mbps에서 약 0.6초면 받습니다.
- **막히는 점**
  - 로그: Loki가 A1의 `127.0.0.1:3100`에만 열려 있어 Micro에서 보낼 수 없습니다. Loki를 사설 IP에 열거나 다른 수집 방법이 필요합니다.
  - 배포: `deploy-oci.yml`이 `OCI_HOST` 한 곳에만 배포합니다. 크롤러용 대상과 SSH 키를 추가해야 합니다.
  - 유휴 회수: 공식 문서는 Always Free 인스턴스가 오래 놀면(CPU · 네트워크 사용률이 낮으면) 회수할 수 있다고 적고 있습니다. 크롤러는 CPU를 거의 쓰지 않으므로 해당될 수 있습니다. 유료 전환 계정에도 적용되는지는 문서에 나와 있지 않습니다.
- **지금 옮기면 위험한 이유**: 지금 크롤러는 웹훅이 실패하면 그 공지를 영구히 잃습니다(5.2). 같은 서버에 있을 때는 A1이 내려가면 크롤러도 같이 멈췄다가 돌아온 뒤 밀린 글을 이어서 처리합니다. 그런데 Micro로 옮기면 A1 배포나 재부팅 중에도 크롤러는 계속 돌면서 공지를 발견하고, 웹훅이 실패해 그 공지를 잃습니다.
- **얻는 것**: A1의 메모리 18MB와 학교 서버로 나가는 요청을 덜어 줍니다. A1에 배포할 때 크롤러가 재시작되지 않습니다. 이득이 크지는 않습니다.
- **권장**: 감지 로직을 고친 뒤(웹훅이 성공해야 기록, 실패하면 재시도) 옮깁니다. 그때는 A1이 잠시 내려가도 크롤러가 재시도로 메워 줍니다.

### 1.4 계정 유형 [확인]

- A1 서비스 한도가 **250코어 · 1,666GB**로 나옵니다. 무료 전용(Always Free) 계정이라면 한도가 무료 범위로 묶여 있으므로, 이 테넌시는 **유료 전환(종량제, PAYG) 계정**입니다.
- 비용 보고서의 통화 표시는 SGD입니다.

---

## 2. A1 과금 확인 결과

### 2.1 무료 한도 [확인: OCI 공식 문서, 2026-11-13 조회]

> All tenancies get the first 1,500 OCPU hours and 9,000 GB hours per month for free for VM instances using the VM.Standard.A1.Flex shape … For Always Free tenancies, this is equivalent to 2 OCPUs and 12 GB of memory.

- 공식 문서 기준 무료 한도는 **월 1,500 OCPU 시간, 9,000 GB 시간**입니다. 한 달 내내 켜 두면 2 OCPU · 12GB에 해당합니다.
- 예전 자료와 제3자 정리에는 3,000 OCPU 시간 · 18,000 GB 시간(= 4 OCPU · 24GB)으로 적힌 경우가 있습니다. 지금 공식 문서는 1,500 / 9,000이므로 **이 숫자를 기준으로 계산합니다.**
- 가격 [확인: oracle.com/cloud/compute/arm]: OCPU당 시간당 0.01달러, 메모리 GB당 시간당 0.0015달러입니다.

### 2.2 실제 사용량과 비용 [확인: Usage API]

- 10월 전체 비용: 0
- 11월 1일~12일 비용: 0. 모든 항목이 「Free」 SKU입니다(E2 Micro, HeatWave, MySQL 스토리지, Block Volume, Load Balancer 등).
- 11월 13일: A1 SKU `B93297 Standard - A1`(OCPU)과 `B93298 Standard - A1 - Memory`가 처음 나타났습니다. 이 두 SKU는 이름에 「Free」가 없는 일반 SKU입니다. 금액은 아직 0인데, 그 달의 무료 시간을 먼저 소진하기 때문입니다.

### 2.3 과금 예상 [추정: 무료 한도 1,500 / 9,000 기준]

- 4 OCPU · 24GB로 켜 두면 OCPU 무료 시간(1,500 ÷ 4 = 375시간)과 메모리 무료 시간(9,000 ÷ 24 = 375시간)이 같은 시점에 소진됩니다.
- **과금 시작 시점**: 생성 시각 2026-11-13 05:05 UTC + 375시간 = **2026-11-29 05:05 KST 무렵**입니다.
- **11월 예상**: 남은 약 427시간 × 4 = 1,708 OCPU 시간(초과 208 → 2.08달러), 427시간 × 24 = 10,248 GB 시간(초과 1,248 → 1.87달러). 합계 약 **3.95달러**입니다.
- **12월부터 매월(31일)**: 2,976 OCPU 시간(초과 1,476 → 14.76달러), 17,856 GB 시간(초과 8,856 → 13.28달러). 합계 약 **28달러**이고, 30일인 달은 약 26달러입니다.
- 실제 청구 통화는 SGD이므로 금액은 환율만큼 달라집니다.

### 2.4 선택지 [결정 대기]

- **A안 (권장): 2 OCPU · 12GB로 축소**
  - 비용이 0이 됩니다. 현재 메모리 사용량(가용 21GB, 앱 0.8GB, 크롤러 17MB)을 보면 12GB로도 충분합니다.
  - A1 Flex 크기를 바꾸면 인스턴스가 재부팅되므로 서비스가 몇 분 중단됩니다.
  - 보류 과제 문서의 「③ k3s: A1 여유 메모리 23GB」 전제는 약 10GB로 고쳐야 합니다.
  - 공식 문서에는 A1의 유휴 회수(reclaim) 기준(7일 동안 메모리 사용률 20% 미만 등)이 적혀 있습니다. 이 기준이 유료 전환 계정에도 적용되는지는 문서에 나와 있지 않습니다.
- **B안: 4 OCPU · 24GB 유지**: 월 약 26~28달러를 냅니다.
- **Micro 2대를 꺼도 A1 무료 한도는 늘어나지 않습니다** [확인]. 공식 문서는 E2 Micro(「up to two Always Free VM instances」)와 A1(「1,500 OCPU hours and 9,000 GB hours」)의 무료 한도를 따로 정합니다. 비용 보고서에서도 E2 Micro는 `B91444 … E2 Micro - Free`, A1은 `B93297` · `B93298`로 SKU가 다릅니다. Micro는 켜 두어도 0원이고, 꺼도 A1 비용은 그대로입니다.
- 크기별 월 비용(31일 기준, 무료 한도를 뺀 금액) [추정]:
  - 4 OCPU · 24GB: OCPU 14.76 + 메모리 13.28 = 약 28달러
  - 4 OCPU · 12GB: OCPU 14.76 + 메모리 0 = 약 15달러
  - 3 OCPU · 18GB: OCPU 7.32 + 메모리 6.59 = 약 14달러
  - 2 OCPU · 24GB: OCPU 0 + 메모리 13.28 = 약 13달러
  - 2 OCPU · 12GB: 0달러
- 무료로 쓸 수 있는 최대 구성: **A1 2 OCPU · 12GB 1대 + E2 Micro(1/8 OCPU · 1GB) 2대**. 한도 안에서 A1을 1 OCPU · 6GB 2대로 나눠 쓸 수도 있습니다.

### 2.5 축소 작업 기록 (2026-11-13) [확인]

- 결정: 사용자 지시 「2 OCPU · 12GB로 하고, 충분한지 확인하고 전부 바꿔라」.
- **충분한지 판단한 근거 (축소 전 측정)**
  - 컨테이너 메모리 제한 합계 4.3GB: app 2g(JVM `-Xms512m -Xmx1g`), loki 512m, prometheus 512m, grafana 512m, redis 384m(`maxmemory 256mb`), caddy 128m, crawling 128m, cadvisor 128m, node-exporter 64m. 모든 컨테이너가 제한까지 써도 12GB 안에 들어갑니다.
  - 실제 사용: app 799MiB, grafana 117MiB, loki 61MiB, prometheus 43MiB, cadvisor 23MiB, crawling 18.7MiB, caddy 12.7MiB, redis 4.6MiB, node-exporter 2.8MiB.
  - JVM 최근 4시간 최대 사용량: heap 510MB, non-heap 214MB.
  - CPU: 앱 `process_cpu_usage` 최근 4시간 최대 0.045(4코어 기준 약 0.18코어). 부하 평균 0.08~0.09.
  - 그 밖: 네트워크 대역폭은 OCPU에 비례해 4Gbps에서 2Gbps로 줄지만, 크롤링 트래픽은 평균 0.5Mbps 수준입니다. 이미지는 GitHub Actions에서 빌드하므로 서버에서 빌드하지 않습니다.
  - 한계: A1은 11/13 14:05에 만들어져 측정 기간이 약 4시간입니다. FCM 대량 발송 같은 피크는 아직 측정하지 못했습니다. 발송 스레드는 I/O 대기형이라 CPU 2개로 충분할 것으로 봅니다 [추정].
- **실행**: `oci compute instance update --shape-config '{"ocpus": 2, "memoryInGBs": 12}'`. 18:27:27 시작 → 18:29:16 RUNNING → 18:29:37 SSH 접속(nproc 2, 메모리 11,925MB) → 18:30:03 앱 health UP. **서비스 중단은 약 2분 반**입니다.
- **확인 결과**: 컨테이너 9개 모두 자동 재시작(restart=always), Docker와 DB 백업 타이머 활성, 크롤러 DB 재연결과 크롤링 재개, `https://www.ajouevent.com` 200, `https://api.ajouevent.com/` 401(인증 필요, 정상), `https://grafana.ajouevent.com` 302(로그인 이동, 정상).
- **재부팅 뒤 상세 검증 (18:31~18:33 KST)**
  - OCI API: `VM.Standard.A1.Flex`, OCPU 2, 메모리 12GB, 네트워크 2Gbps, RUNNING.
  - 호스트: nproc 2, 메모리 11,925MB 중 사용 1,424MB · 가용 10,500MB, 스왑 사용 0, 디스크 48GB 중 15GB 사용(변화 없음).
  - 컨테이너 9개: 모두 Up, 재시작 횟수 0, OOM 0. Redis와 cAdvisor는 healthy.
  - 앱: 기동 18.2초, readiness · liveness 모두 UP. 공개 API `GET /api/event/popular` 200(0.48초)이고 실제 공지 데이터를 반환합니다.
  - 앱 로그의 ERROR 2건은 검증하면서 보낸 `GET /`(인증 없음 → 401) 요청이고, WARN 3건은 기동 시 원래 나오던 경고(Hibernate dialect, 미터 등록, CORS 캐시)입니다. 장애와 관련된 로그는 없습니다.
  - Redis: PONG, `crawling-token` 키 있음.
  - 크롤러: DB 재연결 후 6주기 동안 `crawl_started` 348건 = 58개 × 6으로 전부 실행됐습니다. 실패 6건은 원래부터 실패하던 소프트웨어학과(404)입니다.
  - 모니터링: Prometheus 대상 중 ajouevent-app · cadvisor · prometheus는 up입니다. node는 down인데, **축소 전부터 down**이었습니다(1.2). Loki에 크롤러(`service=ajouevent-crawling`)와 앱(`application=ajouevent-be-v2`) 로그가 계속 들어옵니다. Grafana health는 database ok.
  - Caddy: 오류 · 경고 로그 없음. 인증서를 다시 발급하지 않고 그대로 사용합니다.
  - DB 백업 타이머: 다음 실행 2026-11-14 04:31 KST로 정상 예약돼 있습니다.
- **바꾼 문서**: `AjouEvent_BE_V2/deploy/docker-compose.yml` 1행 주석을 「4 OCPU / 24GB」에서 「2 OCPU / 12GB — Always Free 한도」로 고쳤습니다(커밋 안 함). 서버의 `~/ajouevent/docker-compose.yml`은 다음 배포 때 같이 바뀝니다.
- **고칠 전제**: 보류 과제 PDF의 「③ k3s: A1 여유 메모리 23GB」는 「약 10GB」로 봐야 합니다. k3s(약 0.5~0.6GB)와 앱 레플리카 2개(각 limit 2Gi)를 올려도 들어가지만, 여유는 줄어듭니다.
- **확인 방법**: 12월 초에 Usage API로 A1 SKU(`B93297`, `B93298`)의 11월 금액을 봅니다. 11/13 14:05~18:27의 4 OCPU · 24GB 사용분까지 합쳐도 월 무료 한도 안입니다 [추정: 4.4시간 × 4 + 약 413시간 × 2 ≈ 844 OCPU 시간].
- **공통 권장**: OCI 예산 알림(Budget)을 1달러로 걸어 두면, 예상과 다르게 과금될 때 바로 알 수 있습니다.
- **최종 확인 방법**: 11월 29일 이후 Usage API에서 A1 SKU의 금액이 0보다 크면 과금이 시작된 것입니다(조회 명령은 12장).

---

## 3. DB 백업

### 3.1 현재 백업 두 가지 [확인]

1. **HeatWave 자동 백업 (OCI 기능)**
   - 켜져 있음, 보관 1일, 백업 창 04:23 UTC(13:23 KST), PITR(시점 복구) 꺼짐.
   - 최근 기록: 11/8~11/12에 매일 INCREMENTAL 백업(보관 1일), 11/13에 FULL 백업(보관 7일, 약 1GB).
   - 비용: `MySQL Database Service - Backup Storage - Free` SKU, 0원.
2. **논리 백업 (직접 만든 스크립트)**
   - systemd 타이머 `ajouevent-db-backup.timer`: 매일 04:30 KST(최대 2분 무작위 지연), `Persistent=true`.
   - 서비스 `ajouevent-db-backup.service` → `/home/ubuntu/ajouevent/scripts/backup-db.sh`.
   - 동작: `mysql:8.4` 컨테이너에서 `mysqldump --single-transaction`을 실행하고, `zstd -10`으로 압축한 뒤, 쓰기 전용 PAR URL로 `ajouevent-backups` 버킷의 `db/{DB}-{yyyyMMdd-HHmm}.sql.zst`에 올립니다.
   - 보관: 버킷 수명 주기 정책이 `db/`를 14일 뒤 지웁니다. `archive/`는 영구 보관됩니다.
   - 크기: 1회 21.7MB. 14일치는 약 300MB이고, Object Storage 요청은 하루 1건입니다.
   - 버킷의 현재 객체(총 64.2MB): `db/ajouevent-20261113-1436.sql.zst`(21.7MB), `archive/rds-ajouevent-v2-db-20261113/ajouevent-final.sql.zst`(21.7MB), `archive/rds-ajouevent-v2-db-20261113/ajouevent_v1-final.sql.zst`(20.9MB). archive의 두 파일은 AWS RDS에서 옮겨 올 때 받아 둔 최종 덤프입니다.

### 3.2 논리 백업을 해 온 이유

- HeatWave Free 자동 백업은 **보관 기간이 1일**이라서, 이틀 전에 잘못 지운 데이터는 되돌릴 수 없습니다. 논리 백업은 14일 동안 보관합니다.
- 자동 백업은 DB 시스템 단위로만 복원됩니다(새 DB 시스템을 만들어 복원). 논리 덤프는 테이블 하나만 골라서 되돌릴 수 있습니다.
- DB 시스템을 실수로 지우거나 Free 등급 정책이 바뀌어도 SQL 파일은 남습니다.
- 비용과 자원 부담은 거의 없습니다: 하루 21.7MB를 1번 올리고, mysqldump가 몇 초 동안 실행됩니다.

### 3.3 중단할 경우 [결정 대기]

- 영향: 복구할 수 있는 범위가 「최근 1일(자동 백업)」로 줄어듭니다. 11/13 FULL 백업은 7일 보관이라 11/20까지는 남습니다.
- 중단 절차(승인 후 실행):
  1. `sudo systemctl disable --now ajouevent-db-backup.timer`
  2. (선택) `db/` 아래 기존 덤프는 수명 주기 정책이 14일 안에 지우므로 따로 지울 필요가 없습니다.
  3. `archive/`의 RDS 최종 덤프는 이전 기록이므로 남기는 것을 권장합니다.
- 대안: 논리 백업을 유지하되 주 1회로 줄이거나, HeatWave 자동 백업의 보관 기간을 늘리는 방법이 있습니다. Free 등급에서 늘릴 수 있는 범위는 아직 확인하지 않았습니다.

---

## 4. 소프트웨어학과 크롤링 [확인]

### 4.1 현상

- 크롤러 설정 `AjouEvent_Crawling_V2/config/notifierConfigs.json:300`의 `"noticeUrl": "http://software.ajou.ac.kr/bbs/board.php?tbl=notice"`가 404를 반환합니다.
- 최근 3시간 로그에서 `crawl_page_load_failed`가 180회(소프트웨어학과의 error 로그 362줄) 나왔고, 알림은 없었습니다.

### 4.2 새 게시판 (사용자가 알려 준 주소)

- 주소: `https://software.ajou.ac.kr/board/notice` (200, 53,920바이트)
- 서버: 121.78.88.78(다른 학교 사이트와 다른 서버), TLS 1.2, HTTP/1.1
- **주소만 바꾸면 동작하지 않습니다.** 구조가 통째로 바뀌었습니다.

| 항목 | 예전 (Type2 코드) | 새 사이트 |
| --- | --- | --- |
| 목록 표 | `#sub_contents > div > div.conbody > table:nth-child(2) …` | `table.basic_bbs_list` 안의 `tr[height="45"]` |
| 고정 공지 구분 | 첫 칸의 이미지 | 제목 칸의 `span.tag01`(「공지」). 고정 공지에도 번호가 붙음(현재 7건) |
| 링크 | `?tbl=notice&...` 쿼리 → `split[1:3]`으로 조립 | 절대 주소 `https://software.ajou.ac.kr/board/notice/1576?&page=1` → `split[1:3]` 방식이 깨짐 |
| 글 ID | 쿼리 파라미터 | 경로 마지막 숫자(1576) |
| 작성자 | 5번째 칸 | `td.responsive04`(「관리자」) |
| 본문 | `#DivContents > p, #DivContents > div` | `div.board_view_cont` |
| 첨부 | 없음 | `div.board_file_list a.board_file_item`(`/board/notice/download/{글ID}/{n}`) |
| 이미지 상대 경로 기준 | `http://software.ajou.ac.kr` | `https://software.ajou.ac.kr` (본문 이미지는 www.ajou.ac.kr 절대 주소인 경우도 있음) |
| 302 특수 처리 | `utils.go`의 `isSoftwareURL` · `ErrUseLastResponse` | 필요 없음(https 200) |

- 목록 번호: 고정 공지 1424~1418, 일반 글 1417부터 내려갑니다. 이 번호도 전체 글 수 기준의 순번입니다.
- 배포 전 확인: DB `notice` 테이블에 저장된 Software의 `num` 값이 새 번호 체계(1417)보다 작으면, 예전 로직에서는 첫 실행 때 한 페이지 분량의 알림이 한꺼번에 나갑니다. 5장의 새 로직으로 바꾸면 이 문제는 생기지 않습니다.
- Type2는 소프트웨어학과 하나만 씁니다(설정 58개 중 type 2는 1개).

---

## 5. 크롤러 감지 로직의 문제와 개선 방향

### 5.1 질문: 「제목만으로 공지를 인식하면, 공지를 지우고 다시 올릴 때 아래 공지가 유실되지 않나?」

- 크롤러는 제목이 아니라 **목록 첫 칸의 번호(최댓값)와 고정 공지 개수**로 새 글을 판단합니다.
  - 일반 글: `BaseNotifier.go:163` `scrapeNumNotice`. 첫 일반 글의 번호(maxNum)가 저장값보다 크면, 그 차이만큼(최대 10건, 소프트웨어학과는 15 − 고정 수) 위에서부터 새 글로 봅니다.
  - 고정 공지: `BaseNotifier.go:119` `scrapeBoxNotice`. 고정 공지 개수가 늘어난 만큼 위에서부터 새 글로 봅니다.
- **목록 번호는 글 고유 번호가 아닙니다** [확인]. 아주대 공지의 첫 일반 글은 순번이 29603이고, 실제 글 번호(articleNo)는 379314였습니다. 순번은 「전체 글 수」에서 매기므로 글을 지우면 줄어듭니다.

### 5.2 유실 · 중복이 생기는 경우

| 상황 | 예전 로직의 결과 | 원인 |
| --- | --- | --- |
| 한 글을 지우고 새로 올림 (1분 안) | **새 글 유실** | 전체 글 수가 그대로라 최대 번호가 같음 |
| 글 2개 지우고 1개 올림 | **새 글 유실** | 최대 번호가 줄어 저장값만 낮춤(`BaseNotifier.go:176`) |
| 고정 공지 하나 해제 + 다른 하나 고정 | **새 고정 공지 유실** | 고정 개수가 같음(`BaseNotifier.go:123`) |
| 예전 글을 새로 고정 | **예전 글을 새 공지로 재발송** | 고정 개수가 늘어남. 백엔드 중복 검사는 같은 토픽 최근 10건만 비교(`ClubEventCommandService.java:31`, `findTop10ByType`) |
| 고정 공지 순서가 최신순이 아님 | 엉뚱한 글을 새 글로 봄 | 「위에서부터 늘어난 개수만큼」 가져옴 |
| 1분 사이에 새 글 10건 초과 | 초과분 유실 | 1페이지(10건)만 봄 |
| 웹훅 실패 | **영구 유실** | DB 저장값을 먼저 올리고 나서 발송(`BaseNotifier.go:205` → `:63`) |
| 본문 페이지 로드 실패 | **빈 공지 발송** | `noticeChan <- Notice{}`(`Type1Notifier.go:66` 등 5곳) |

### 5.3 개선 방향 (권장안)

1. **글 고유 번호를 키로 쓴다.** 유형별로 바뀌지 않는 번호가 있습니다 [확인].
   - Type 1 · 5(아주대 CMS): `articleNo` 쿼리 파라미터
   - Type 2(소프트웨어학과): 경로의 글 ID(`/board/notice/1576`)
   - Type 3 · 4(의료원): `javascript: page_move('...View', { no: 72891 })`의 `no`
2. **「이미 본 글」 목록을 DB에 저장한다.** 예: `crawled_article(topic, article_key, title, status, first_seen_at, sent_at)`, 기본 키는 `(topic, article_key)`.
3. **목록의 모든 줄(고정 + 일반)을 키로 비교한다.** 기록에 없는 키만 새 글로 봅니다. 지우고 다시 올린 글은 키가 새로 생기므로 반드시 잡힙니다.
4. **발송이 끝난 뒤에 기록한다.** 웹훅이 2xx나 409(이미 처리됨)를 반환한 뒤에만 「본 글」로 저장합니다. 실패하면 다음 주기에 다시 시도하고, 일정 횟수(예: 30회 = 30분)를 넘기면 FAILED로 기록하고 알립니다.
5. **예전 글 고정 문제는 기준 번호로 막는다.** 처음 배포할 때 목록에 있던 글을 모두 「본 글」(SEEDED)로 넣고, 그중 가장 큰 번호를 기준선으로 둡니다. 기록에 없더라도 기준선보다 작은 번호는 예전 글이므로 발송하지 않습니다. 글 번호는 새 글일수록 커지기 때문에 이 규칙이 성립합니다.
6. **배포 공백 보완**: 처음 배포할 때 목록의 글 중 `club_events.url`에 있는 글은 이미 보낸 글로 처리하고, 가장 최근에 보낸 글보다 위에 있는 글만 새 글로 보내면 배포 중 1분 공백도 메울 수 있습니다.
7. **빈 공지를 보내지 않는다.** 본문을 읽지 못하면 발송하지 않고 다음 주기에 다시 시도합니다.

---

## 6. 질문: 「페이지 전체를 크롤링하거나, 해시와 거리로 변경 여부를 알 수 없나?」

### 6.1 목록 페이지 전체 해시

- 목록 HTML에는 조회수가 들어 있어서 매분 해시가 바뀝니다. 그래서 정규화(번호 · 제목 · 키만 남김)가 필요합니다.
- 해시는 「무언가 바뀌었다」만 알려 줄 뿐, **어느 글이 새 글인지는 알려 주지 않습니다.** 결국 줄 단위 비교(5장)가 필요합니다.
- 목록 해석 비용은 아주 작아서, 해시로 건너뛰어도 아끼는 자원이 거의 없습니다. 쓴다면 「정규화한 목록 해시가 같으면 DB 비교를 건너뛴다」 정도의 보조 수단입니다.

### 6.2 글마다 내용 해시 (권장, 보조 기능)

- 글 키마다 `hash(제목 + 본문)`을 저장하면 **글이 수정됐는지** 알 수 있습니다.
- 쓸 곳: 수정된 공지의 본문을 백엔드에 다시 반영할 때(알림은 다시 보내지 않음), 그리고 ⑥ 본문 시각화의 `content_html` 갱신에 씁니다.
- 해시만으로 글을 식별하면 안 됩니다. 제목을 고치면 해시가 바뀌어 새 글로 오인하고, 「장학금 신청 안내」처럼 해마다 같은 제목의 글이 올라오면 새 글을 놓칩니다. **식별은 글 번호로, 변경 감지는 해시로** 나눠야 합니다.

### 6.3 유사도 해시(SimHash · MinHash)와 거리

- 쓸모: **「같은 내용을 지우고 다시 올린 글」**을 찾을 수 있습니다. 새 키로 올라온 글이 같은 토픽의 최근 7일 글과 SimHash 해밍 거리 3 이하로 가깝다면 재게시로 봅니다.
- 처리 방식 [결정 대기]:
  - A안: 재게시는 기존 공지를 갱신하고 알림은 보내지 않는다(사용자는 이미 알림을 받았음).
  - B안: 재게시도 새 공지로 보낸다(지금 동작과 같음, 중복 알림이 생김).
- 비용: 새 글에만 계산하므로 하루 수십 건 수준이고, 자원 부담이 없습니다.

### 6.4 전체 페이지 크롤링

- 매분 전체 페이지를 받는 것은 비현실적입니다. 아주대 공지는 약 2,960페이지, 소프트웨어학과는 143페이지입니다.
- 대신 **정기 대조**를 권장합니다. 매분 1페이지를 보고, 매시간(또는 매일 밤) 1~3페이지를 다시 읽어 「본 글」 목록과 대조합니다. 장애 중에 한 페이지 이상 밀린 글을 이 방법으로 찾아냅니다.

### 6.5 결론

- 식별: 글 번호(5장)
- 수정 감지: 글별 내용 해시(6.2)
- 재게시 감지: SimHash 거리(6.3), 정책 결정 필요
- 누락 보완: 매시간 1~3페이지 대조(6.4)

---

## 7. HTTPS 연결과 커넥션 풀 [확인: A1에서 측정]

### 7.1 대상 서버 구성

- 크롤링 대상 58개 게시판, 호스트 56개.
- **54개 호스트(`*.ajou.ac.kr`)가 같은 IP `202.30.0.123`입니다.** 인증서는 와일드카드 `CN=*.ajou.ac.kr`(SAN: `*.ajou.ac.kr`, `ajou.ac.kr`)입니다.
- 나머지: `www.ajoumc.or.kr`(211.105.7.101, 게시판 2개), `software.ajou.ac.kr`(121.78.88.78).
- 모든 서버가 **HTTP/1.1만** 지원하고(HTTP/2 없음), gzip 압축을 지원하지 않습니다(`--compressed`를 붙여도 크기가 같음). `Cache-Control: no-cache no-store`를 보내고, ETag와 Last-Modified를 보내지 않습니다.
- TLS: `*.ajou.ac.kr`과 ajoumc는 TLS 1.3, software는 TLS 1.2입니다.

### 7.2 한 번 요청할 때 걸리는 시간 (A1 → 학교, 대표값)

- DNS 약 1ms, TCP 연결 약 5ms, **TLS 핸드셰이크 약 29~30ms**(일부 호스트는 240ms), 첫 바이트까지 100~180ms(ajou.ac.kr 메인은 약 530ms), 전체 120~200ms.

### 7.3 keep-alive (연결 재사용) 한도

| 서버 | 유휴 후 재사용 결과 | 서버 유휴 한도 |
| --- | --- | --- |
| *.ajou.ac.kr (202.30.0.123) | 45 · 50 · 55 · 60초 성공, 65초 실패 | 약 60~65초 |
| www.ajoumc.or.kr | 40초 성공, 65초 실패 | 40~65초 사이 |
| software.ajou.ac.kr | 0초 성공, 5초부터 실패 | 5초 미만 |

### 7.4 지금 크롤러의 연결 사용 방식

- `utils.go:240`의 `NewDocumentFromPage`가 **요청마다 `http.Client`를 새로 만듭니다.** 그래서 연결을 재사용하지 못하고, 매분 58번, **하루 약 8.4만 번 TLS 핸드셰이크**를 합니다.
- `main.go:52`가 매 주기 58개 게시판을 동시에 실행하므로, 같은 IP(202.30.0.123)로 **58개 요청이 한꺼번에 몰립니다.**
- 웹훅 전송(`utils.go:209`)도 요청마다 클라이언트를 새로 만들고, Redis 클라이언트도 매번 새로 만들어 닫지 않습니다(`utils.go:43`).

### 7.5 트래픽 양 [추정: 측정한 페이지 크기 합계]

- 1주기 3.79MB → **하루 5.46GB, 월 약 164GB**를 받습니다. 압축과 조건부 요청을 쓸 수 없으므로 이 양은 줄이기 어렵습니다.
- OCI는 수신 트래픽에 과금하지 않습니다. 다만 학교 서버 입장에서는 부담입니다.
- 줄이는 방법: 주기를 늘리거나(예: 업무 시간 1분, 야간 5분), 목록 페이지 크기를 줄이는 파라미터(`articleLimit`)를 조정하는 방법이 있습니다. 후자는 아직 확인하지 않았습니다.

### 7.6 커넥션 풀 설계안

1. **공용 Transport 하나**를 모든 게시판과 본문 요청이 함께 씁니다.
2. **`*.ajou.ac.kr` 연결 합치기 (측정으로 검증됨)**
   - `www.ajou.ac.kr`에 맺은 TLS 연결 하나로 `Host: mobility.ajou.ac.kr`, `security.ajou.ac.kr`, `ise.ajou.ac.kr`을 요청했더니 모두 200과 올바른 페이지를 받았습니다(재사용 시 약 110~130ms).
   - Go에서는 `req.URL.Host = "www.ajou.ac.kr"`, `req.Host = "<원래 학과 호스트>"`로 보내면, Transport가 연결을 `www.ajou.ac.kr` 하나의 풀로 관리합니다. 와일드카드 인증서라서 SNI 검증도 통과합니다.
   - 효과: 54개 호스트가 **연결 2~4개를 함께 씁니다.** 학교 쪽이 서버를 나누는 경우에 대비해 설정으로 끌 수 있게 합니다.
3. **주기 안에서 요청을 고르게 나눕니다.** 매분 한꺼번에 보내지 말고 58개 게시판을 60초에 나눠(약 1초에 1개) 순서대로 요청합니다.
   - 연결이 1초 이상 쉬지 않으므로 서버 유휴 한도(60~65초)에 걸리지 않고, **핸드셰이크가 하루 8.4만 번에서 수십 번 수준으로 줄어듭니다.**
   - 학교 서버가 받는 순간 부하도 「58개 동시」에서 「초당 1개」로 줄어듭니다.
4. **Transport 설정값 제안**
   - `MaxConnsPerHost: 4`: 같은 서버로 동시에 여는 연결을 4개로 제한합니다.
   - `MaxIdleConnsPerHost: 4`
   - `IdleConnTimeout: 55s`: 서버 한도(약 60초)보다 짧게 잡아, 서버가 이미 닫은 연결로 보내지 않게 합니다.
   - `TLSHandshakeTimeout: 5s`, `ResponseHeaderTimeout: 10s`, 전체 `Timeout: 15s`
   - `TLSClientConfig.ClientSessionCache = tls.NewLRUClientSessionCache(64)`: 연결을 새로 맺을 때 TLS 세션을 재개합니다.
   - 응답 본문 최대 8MB 제한.
5. **software.ajou.ac.kr**: 서버 유휴 한도가 5초 미만이라 재사용할 수 없습니다. 분당 1번 새로 연결하는 것은 불가피하고, 부담도 작습니다.
6. 웹훅과 Redis: 클라이언트를 하나만 만들어 재사용합니다. 웹훅은 같은 서버의 사설 IP(10.0.0.170:8080)로 가므로 HTTP keep-alive만 켜면 됩니다.

### 7.7 기대 효과 (정리)

- TLS 핸드셰이크: 하루 약 8.4만 번 → 수십 번
- 학교 서버의 동시 요청: 58개 → 최대 4개
- 크롤러 CPU · 메모리: 이미 거의 0이라 체감 차이는 작습니다. 효과는 주로 학교 서버 부하와 안정성(차단 위험 감소)에서 나옵니다.

---

## 8. 리소스와 언어 선택 (Rust 전환 여부)

- 측정: 크롤러 메모리 **17.45MiB**(제한 128MiB), CPU 0% [확인].
- Rust로 다시 짜도 메모리는 10MB 안팎 줄어드는 데 그칩니다 [추정]. A1에 여유 메모리가 있으므로(축소 후에도 약 10GB) 의미 있는 차이가 아닙니다.
- 크롤러는 I/O를 기다리는 시간이 대부분이라 언어보다 **요청 방식(7장)과 감지 로직(5장)**의 영향이 훨씬 큽니다.
- 결론: **Go를 유지하고 구조를 고칩니다.** 전면 재작성은 기간과 회귀 위험만 늘립니다.

---

## 9. 크롤러의 그 밖의 개선점 [확인: 코드 위치]

### 9.1 안정성

- **별도 고루틴의 패닉으로 프로세스 전체가 종료될 수 있음**: `getNotice`가 `go`로 실행됩니다(`BaseNotifier.go:146`, `:196`). 링크 형식이 예상과 다르면 `split[1:3]`(`Type2Notifier.go:54`), `split[5]`(`Type3Notifier.go:51`, `Type4Notifier.go:51`), `title[:len(title)-17]`(`Type1Notifier.go:48`, `Type5Notifier.go:44`)에서 범위 초과 패닉이 납니다. `Notify`의 recover는 다른 고루틴의 패닉을 잡지 못합니다.
- **Redis 장애 시 크롤러 전체 종료**: `utils.go:50` `log.Fatalf`. 웹훅 실패도 `ErrorLogger.Panic`(`utils.go:213`)입니다.
- **같은 게시판이 겹쳐 실행됨**: `main.go:52`가 앞 주기가 끝났는지 보지 않고 매분 `go notifier.Notify()`를 호출합니다. 겹치면 같은 공지를 두 번 보낼 수 있습니다.
- **구조 변경 알림 없음**: 실패를 로그로만 남깁니다. 같은 게시판이 연속 N회 실패하면 Discord 운영 채널로 알려야 합니다(소프트웨어학과는 알림 없이 계속 실패했습니다).
- **종료 처리 없음**: SIGTERM을 받으면 진행 중인 발송을 마치고 끝내야 합니다.

### 9.2 수집 품질

- **사이버보안학과(type 5): 작성일이 작성자 칸에 들어감.** type 5 사이트마다 칸 구성이 다릅니다: 사이버보안학과는 「번호 · 제목 · 파일 · 작성일 · 조회」, ise는 「번호 · 제목 · 파일 · 공지부서 · 공지마감일」입니다. 코드는 4번째 칸을 고정으로 읽습니다(`Type5Notifier.go`). → **표 머리글(제목 · 분류 · 공지부서)로 칸을 찾아야 합니다.**
- **의과대학(type 4): 본문 중복.** 본문 선택자가 `div.txt span`이라 중첩된 span의 글이 두 번 들어갑니다. → 본문 영역을 한 번만 훑어 블록 단위로 줄을 나눠야 합니다.
- **게시일 대신 크롤링 시각**: `date := time.Now()`. 목록에 게시일이 있으므로 그 값을 쓸 수 있습니다.
- **이미지 절대 주소를 호스트 하드코딩으로 만듦**: `"https://www.ajou.ac.kr" + image`, `"http://software.ajou.ac.kr" + image`. → 본문 페이지 주소 기준으로 `url.ResolveReference`를 써야 합니다.
- **간호대학(type 3) 이미지가 http 주소**(`http://www.ajoumc.or.kr/crosseditor/...`): HTTPS 페이지에서 차단될 수 있습니다. ④ 미러링이나 https 변환이 필요합니다.
- 첨부파일 목록을 수집하지 않습니다(⑥에서 필요).

### 9.3 코드 구조

- Type1~5 파일 5개가 선택자와 칸 번호만 다른 거의 같은 코드입니다. 파서 하나로 합치고, 유형별 차이는 설정으로 두는 게 좋습니다.
- 테스트가 없습니다. 실제 HTML을 저장해 둔 고정 데이터로 파서 테스트를 만들어야 합니다(11장에서 일부 작성).

---

## 10. 이미지 처리 (보류 과제 ④ · ⑤ · ⑥)

### 10.1 방향: 볼 때 학교 서버를 거치지 않는다

- ⑤(이미지 캐시 프록시)는 사용자가 볼 때 Cloudflare 캐시에 없으면 학교 서버로 요청이 갑니다. 사용자는 이 방식을 원하지 않습니다.
- **④ 미러링을 기본으로 합니다.** 크롤러가 새 공지를 발견할 때 이미지를 **한 번만** 받아 변환하고 우리 저장소에 둡니다. 사용자는 Cloudflare → 우리 저장소에서 받습니다.
- A1 → 학교 서버는 0.17~1.65초면 응답합니다. 사용자 네트워크(Mac) → 학교 서버는 20초 시간 초과가 났습니다(보류 과제 문서의 측정값, 이번에도 이 Mac에서 `*.ajou.ac.kr` 접속이 시간 초과됨).

### 10.2 OCI 무료 리소스 중 이미지 변환에 쓸 수 있는 것

- **A1 Compute + imgproxy (권장)**
  - 이미 운영 중인 서버에 컨테이너를 하나 추가합니다(메모리 약 100~200MB, `mem_limit 512m`).
  - **외부에 공개하지 않고 크롤러만 내부망(`http://ajouevent-imgproxy:8080`)으로 호출합니다.** 학교 서버 호출은 크롤링할 때 한 번뿐입니다.
  - WebP로 변환할 수 있습니다. Go의 `golang.org/x/image/webp`는 디코딩만 지원하므로, 보류 과제 문서의 「Go 크롤러에서 x/image로 변환」안은 WebP를 만들 수 없습니다. 순수 Go로 10MB PNG를 줄이면 메모리도 많이 씁니다.
- **OCI Functions (Lambda에 해당)**
  - 무료: 월 200만 회 호출, 400,000 GB-초 [확인: Oracle Functions 가격 페이지].
  - 구성: 크롤러가 원본을 `originals/`에 올리면, Object Storage 이벤트가 함수를 실행해 변환본을 `notices/`에 저장합니다. 원본은 수명 주기 정책으로 1일 뒤 지웁니다.
  - 백필 11,500장을 1GB × 2초로 잡으면 약 23,000 GB-초로 한도 안입니다 [추정].
  - 단점: 이미지 1장마다 Object Storage 요청이 3~4건 생깁니다(원본 PUT, 함수 GET, 변환본 PUT 등). 백필을 한 달에 하면 무료 요청 한도(월 5만 건)에 가까우므로 두 달로 나눠야 합니다. OCIR 이미지 등록, VCN 서브넷, 동적 그룹 정책을 추가로 설정해야 하고, 콜드 스타트가 수 초 걸립니다(크롤링 시점 처리라 문제는 작음).
- **E2 Micro (1GB)**: 현재 놀고 있지만, 10MB PNG를 디코딩하기에는 메모리가 빠듯합니다.
- OCI에는 Cloudflare Images 같은 이미지 변환 관리형 서비스가 없습니다.

### 10.3 Object Storage를 무료로 쓰는 방법

- **OCI Always Free** [확인: 공식 문서]
  - 유료 전환 계정 기준: Standard 10GB + Infrequent Access 10GB + Archive 10GB, API 요청 월 5만 건.
  - 무료 전용 계정 기준: 세 등급 합계 20GB, API 요청 월 5만 건.
  - 이 테넌시는 유료 전환 계정이므로 **Standard 10GB**가 기준입니다. 현재 사용량은 64.2MB(백업)입니다.
  - 공지 이미지 변환본은 연간 약 1GB 늘어날 것으로 추정합니다(보류 과제 문서 기준) [추정].
  - 요청 한도: 응답에 `Cache-Control: public, max-age=31536000, immutable`을 붙이고 Cloudflare에 캐시하면 조회 요청은 거의 Object Storage까지 오지 않습니다. 쓰기는 월 약 550건입니다.
- **A1 블록 볼륨** [확인: 공식 문서 「200 GB total of combined boot volume and block volume」]
  - 부트 볼륨(현재 50GB, 15GB 사용)을 포함해 200GB가 무료입니다.
  - 이미지를 디스크에 두고 Caddy `file_server`로 제공하면 **요청 수 제한이 없습니다.** 가장 단순합니다.
  - 단점: A1이 없어지면 함께 사라지므로 볼륨 백업(무료 5개)으로 보완해야 합니다.
- **Cloudflare R2**: 10GB까지 무료이고 전송 비용이 없습니다. 이미 Cloudflare를 쓰고 있어서 연동하기 쉽습니다.

### 10.4 ⑥ 본문 시각화와의 관계

- ⑥은 크롤러가 본문 HTML(`content_html`)과 첨부파일을 함께 보내야 합니다. 크롤러 개선(5 · 9장)과 같이 진행하면 한 번에 끝납니다.
- 본문 속 이미지 주소도 ④에서 저장한 주소로 바꿉니다.

### 10.5 권장 순서

1. 크롤러 감지 로직 + 소프트웨어학과 (5 · 4장)
2. 커넥션 풀과 요청 분산 (7장)
3. ④ 미러링(A1 imgproxy 내부 전용 + 저장소)
4. ⑥ 본문 시각화

---

## 11. 진행했던 코드 (중단 상태)

- 저장소: `~/Desktop/Study/AjouEvent/AjouEvent_Crawling_V2`
- 브랜치: `refactor/article-key-dedup` (main에서 분기, **커밋하지 않음**, 추가 파일만 있음)
- 기존 `src/`, `main.go`, `config/`는 건드리지 않았습니다. 그래서 지금 빌드해도 예전 크롤러가 그대로 동작합니다.
- 추가한 파일
  - `internal/board/board.go`: 유형 1~5 목록 · 본문 파서
    - 글 고유 번호(articleNo · no · 경로 ID)를 키로 뽑습니다.
    - CMS(type 1 · 5)는 표 머리글로 칸을 찾습니다(사이버보안학과 작성일 문제 해결).
    - 소프트웨어학과 새 구조를 지원합니다.
    - 본문은 DOM을 한 번만 훑어 블록 단위 줄로 만듭니다(의과대학 중복 해결). 줄 구분자는 기존과 같은 리터럴 `\n`입니다.
    - 이미지는 본문 주소 기준 절대 주소로 바꿉니다.
  - `internal/board/board_test.go` + `internal/board/testdata/t{1..5}_{list,detail}.html`: 2026-11-13에 A1에서 받은 실제 HTML로 만든 테스트. `go test ./...` 통과.
  - `internal/fetch/fetch.go`: 공용 HTTP 클라이언트(재시도, charset 변환, 8MB 제한). 아직 아무 곳에서도 호출하지 않습니다.
  - `internal/logx/logx.go`: 기존과 같은 `level=… event=…` 형식의 로그 모듈.
- 남은 작업(미착수): 「이미 본 글」 저장소, 웹훅 전송기, 감지 로직, 스케줄러(요청 분산 · 겹침 방지), `main.go` 교체, `crawled_article` 테이블 DDL, 기존 `src/` 삭제.
- 되돌리기: `git switch main && git branch -D refactor/article-key-dedup && rm -rf internal`

---

## 12. 다시 확인할 때 쓰는 명령

모든 명령은 읽기 전용입니다. SSH는 `ubuntu@146.56.101.16`(A1)에 기본 키로 접속됩니다. OCI CLI는 `~/.oci/config`의 `AJOUEVENT` 프로필을 씁니다.

```bash
# 테넌시 OCID 를 변수로
T=$(awk '/^\[AJOUEVENT\]/{f=1;next} /^\[/{f=0} f&&/^tenancy/{sub(/^tenancy *= */,"");print}' ~/.oci/config)

# 인스턴스 목록 (Shape · OCPU · 메모리)
oci --profile AJOUEVENT compute instance list --compartment-id "$T" --all \
  --query 'data[].{name:"display-name",shape:shape,ocpu:"shape-config".ocpus,mem:"shape-config"."memory-in-gbs",state:"lifecycle-state"}' --output table

# A1 서비스 한도 (250 / 1666 이면 유료 전환 계정)
oci --profile AJOUEVENT limits value list --compartment-id "$T" --service-name compute --name standard-a1-core-count --all

# 일자별 비용 (A1 SKU B93297 · B93298 의 금액이 0보다 크면 과금 시작)
oci --profile AJOUEVENT usage-api usage-summary request-summarized-usages --tenant-id "$T" \
  --time-usage-started 2026-11-25T00:00:00Z --time-usage-ended 2026-12-02T00:00:00Z \
  --granularity DAILY --query-type COST --group-by '["skuPartNumber","skuName"]'

# HeatWave 자동 백업 정책 · 백업 목록
oci --profile AJOUEVENT mysql db-system list --compartment-id "$T" --query 'data[].{name:"display-name",backup:"backup-policy"}'
oci --profile AJOUEVENT mysql backup list --compartment-id "$T" --output table

# 백업 버킷 객체
oci --profile AJOUEVENT os object list --bucket-name ajouevent-backups --all --fields name,size,timeCreated

# A1: 컨테이너 · 자원 · 크롤링 주기 · 크롤러 로그 이벤트 집계
ssh ubuntu@146.56.101.16 'docker ps; docker stats --no-stream ajouevent-crawling ajouevent-app; docker exec ajouevent-crawling printenv CRAWLING_PERIOD'
ssh ubuntu@146.56.101.16 'docker logs --since 3h ajouevent-crawling 2>&1 | grep -oE "event=[a-z_]+" | sort | uniq -c | sort -rn'

# A1: DB 논리 백업 타이머 · 스크립트
ssh ubuntu@146.56.101.16 'systemctl list-timers --all | grep backup; systemctl cat ajouevent-db-backup.service; cat ~/ajouevent/scripts/backup-db.sh'

# A1: 호스트별 HTTPS 측정 (urls.txt 는 notifierConfigs.json 의 noticeUrl 목록)
curl -sS -o /dev/null -A Mozilla/5.0 -w "%{http_code} v%{http_version} tcp=%{time_connect} tls=%{time_appconnect} ttfb=%{time_starttransfer} total=%{time_total} size=%{size_download}\n" "<URL>"

# 목록 번호(순번)와 글 번호(articleNo) 비교는 A1 에서 목록 HTML 을 받아 확인 (이 Mac 에서는 *.ajou.ac.kr 접속이 시간 초과)
```

---

## 13. 대화 기록 (질문과 답)

1. **보류 과제 PDF의 ④ · ⑤ · ⑥과 소프트웨어학과 크롤링 이슈를 해결하려 한다. 이미지 변환에 쓸 수 있는 OCI 무료 리소스(Lambda 등)는? 제시한 방법은 무엇인가?**
   - OCI Functions(Lambda에 해당)와 A1 Compute를 쓸 수 있고, A1 imgproxy를 권장한다(10.2).
   - 문서의 ⑤는 Cloudflare 캐시 + A1 imgproxy로 볼 때 변환하는 방식이고, ④는 크롤러가 저장하는 방식이다. ④의 「Go x/image 변환」은 WebP를 만들 수 없으므로 imgproxy 재사용으로 바꿔야 한다.
2. **소프트웨어학과의 새 주소는 https://software.ajou.ac.kr/board/notice**
   - 200 응답을 확인했다. 구조가 통째로 바뀌어 주소만 바꿔서는 안 된다(4장).
3. **제목만으로 공지를 인식하면, 고정 공지 등을 지우고 다시 올릴 때 아래 공지가 유실되지 않나? 크게 개선할 방법은? Rust도 좋다. 개선할 부분을 전부 개선해 달라.**
   - 제목이 아니라 순번과 고정 개수로 판단하며, 지우고 다시 올리면 실제로 유실된다(5장). 글 번호 기반으로 바꾸는 것을 권장하고, Go를 유지한다(8장).
   - 이때 개발을 시작했다(11장).
4. **Object Storage를 무료로 쓸 방법은?**
   - Always Free 한도(유료 전환 계정은 Standard 10GB, 월 5만 요청), A1 블록 볼륨, Cloudflare R2(10.3).
5. **OCI Functions와 A1이 좋아 보이지만 학교 서버를 경유하는 것은 좋지 않다. 개선점을 정리해 달라는 것이지 개발하라는 뜻이 아니었다. 거기까지만 진행해 달라.**
   - 개발을 중단했다(11장). 이미지는 크롤링할 때 한 번만 받는 ④를 기본으로 한다(10.1).
6. **A1 무료 한도(2 OCPU · 12GB)로 과금되는지 지금 확인해 봐라.**
   - 유료 전환 계정이고, 4 OCPU · 24GB이므로 11/29 무렵부터 과금되어 12월부터 월 약 26~28달러가 나온다(2장).
7. **글 번호 대신 페이지 전체를 크롤링하거나 해시와 거리로 변경을 알 수 없나?**
   - 식별은 글 번호, 수정 감지는 내용 해시, 재게시 감지는 SimHash, 누락 보완은 매시간 1~3페이지 대조로 나눠 쓰는 것을 권장한다(6장).
8. **HTTPS 연결까지 확인해서 커넥션 풀을 고민해 달라.**
   - 54개 호스트가 한 IP · 와일드카드 인증서이고, 연결 하나로 여러 학과를 요청할 수 있음을 확인했다. 연결 합치기와 요청 분산을 제안한다(7장).
9. **크롤러가 Micro 인스턴스에서 동작하나?**
   - 아니다. A1(ajouevent-core)에서 동작한다. Micro 두 대에는 실행 중인 컨테이너가 없다(1.1).
10. **DB 백업을 왜 하나? 하지 말자.**
    - HeatWave 자동 백업이 1일만 보관해서 14일 보관용 논리 백업을 추가했다. 중단 절차와 영향은 3장에 있다. 승인 후 중단한다.
11. **이 내용을 모두 다운로드 폴더의 md로 정리해 달라. 다시 확인하지 않아도 되게 전부 적어 달라. 계속 질문하겠다.**
    - 이 문서다. 이후 질문과 답도 이 문서에 이어서 적는다.
12. **A1은 유료 계정이어야 열려서 유료로 전환했다. 그러면 Micro 2대를 끄고 좋은 것(A1) 1대를 무료로 쓸 수 있나?**
    - 안 된다. Micro와 A1의 무료 한도는 따로 정해져 있어서 서로 옮겨 쓸 수 없다. Micro를 꺼도 A1 무료 한도(월 1,500 OCPU 시간 · 9,000 GB 시간 = 2 OCPU · 12GB)는 그대로다.
    - 유료 전환 계정이어도 무료 한도 안의 사용량은 청구되지 않는다. A1을 2 OCPU · 12GB로 줄이면 Micro 2대를 켜 둔 채로도 0원이다. 크기별 비용은 2.4에 적었다.
13. **2 OCPU · 12GB로 하고, 정말 충분한지 확인하고 전부 바꿔 달라.**
    - 측정 결과 충분해서 18:27에 축소했다. 약 2분 반 중단 뒤 모든 서비스가 정상으로 돌아왔다. 근거와 기록은 2.5에 있다.
14. **크롤러를 Micro에서 돌리면 안 되나?**
    - 돌릴 수는 있다. 다만 지금의 감지 로직에서는 옮기면 오히려 공지를 잃을 위험이 커서, 새 감지 로직을 적용한 뒤 옮기는 것을 권장한다(1.3).

---

## 14. 출처

- OCI Always Free Resources (A1 · Block Volume · Load Balancer · Object Storage 한도): https://docs.oracle.com/en-us/iaas/Content/FreeTier/freetier_topic-Always_Free_Resources.htm
- Oracle Ampere A1 가격 (OCPU 0.01달러/시간, 메모리 0.0015달러/GB·시간): https://www.oracle.com/cloud/compute/arm/
- OCI Functions 가격 (월 200만 회 · 400,000 GB-초 무료): https://www.oracle.com/cloud-native/functions/
- OCI Functions vs AWS Lambda 비교 (Oracle 블로그): https://blogs.oracle.com/cloud-infrastructure/post/oci-functions-v-aws-lambda-comparing-cost-value
