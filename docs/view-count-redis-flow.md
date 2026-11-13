# 조회수 Redis 처리 흐름

## 개요

조회수는 Redis에 누적한 뒤 스케줄러가 3분마다 DB에 일괄 반영하는 구조다.
핵심 패턴은 세 가지다.

- **Dedup Key**: 회원/비회원 중복 조회를 TTL 기반으로 차단
- **Dirty Set 패턴**: 조회수가 증가한 공지사항 ID만 추적해 불필요한 DB 접근 제거
- **Read-Write-Subtract**: DB 반영 후 Redis 카운터에서 반영분만 차감해 배치 중 유입된 조회수 보존

---

## Redis 키 구조

| 키 패턴 | 자료구조 | TTL | 용도 |
|---|---|---|---|
| `ClubEvent:dedup:{eventId}:u:{email}` | String | 24시간 | 회원 중복 조회 방지 |
| `ClubEvent:dedup:{eventId}:a:{hash}` | String | 24시간 | 비회원 중복 조회 방지 (IP+UA SHA-256 앞 16자) |
| `ClubEvent:views:{eventId}` | String | 7일 (안전장치) | 조회수 누적 카운터 |
| `ClubEvent:dirty` | Set | 없음 (SREM으로 관리) | 조회수가 증가한 eventId 목록 |
| `ClubEvent:committed:{eventId}` | String | 10분 | DB 커밋 완료 대기 delta (멱등성용) |

---

## Phase 1 — 조회 요청 시 (increment_view.lua)

### 전제 상황

> 회원 A(`a@ajou.ac.kr`)와 비회원 B(`IP: 1.2.3.4, UA: Mozilla/5.0`)가 공지사항 ID `42`번을 조회한다.

### 회원 A 최초 조회

Java(`executeIncrementView`)가 Lua Script를 호출한다.

```
KEYS[1] = "ClubEvent:dedup:42:u:a@ajou.ac.kr"
KEYS[2] = "ClubEvent:views:42"
KEYS[3] = "ClubEvent:dirty"
ARGV    = ["86400", "42", "604800"]
```

Lua(`increment_view.lua`) 내부 실행:

```
SET ClubEvent:dedup:42:u:a@ajou.ac.kr 1 NX EX 86400  → OK (isNew = true)
INCR ClubEvent:views:42                               → 1
SADD ClubEvent:dirty 42                               → 1
EXPIRE ClubEvent:views:42 604800
return 1
```

### 회원 A 재조회 (24시간 이내)

```
SET ClubEvent:dedup:42:u:a@ajou.ac.kr 1 NX EX 86400  → nil (이미 존재, NX 실패)
→ 이후 명령 실행 안 함
return 0
```

### 비회원 B 조회

`IP + UA`를 SHA-256 해시한 뒤 앞 16자를 사용한다 (`ab12cd34ef56ab12`).

```
SET ClubEvent:dedup:42:a:ab12cd34ef56ab12 1 NX EX 86400  → OK
INCR ClubEvent:views:42                                   → 2
SADD ClubEvent:dirty 42                                   → 0 (이미 존재, 무해)
EXPIRE ClubEvent:views:42 604800
return 1
```

### 이 시점 Redis 상태

```
ClubEvent:dedup:42:u:a@ajou.ac.kr  = "1"   TTL ~86400s
ClubEvent:dedup:42:a:ab12cd34ef56  = "1"   TTL ~86400s
ClubEvent:views:42                 = "2"   TTL 7일
ClubEvent:dirty                    = { "42" }
```

---

## Phase 2 — 스케줄러 실행 (3분마다)

### [Read] 더티 셋 + 카운터 읽기

```
SMEMBERS ClubEvent:dirty    → { "42" }
GET ClubEvent:views:42      → "2"
GET ClubEvent:committed:42  → nil  (이전 실패 없음 → freshMap으로 분류)
```

`freshMap = { 42 → 2 }`

### [Write] DB 배치 업데이트

```sql
UPDATE club_events SET view_count = view_count + 2 WHERE event_id = 42;
```

트랜잭션 커밋 성공 직후 멱등성 키 기록:

```
SET ClubEvent:committed:42 2 EX 600
```

### [Subtract] subtract_view_chunk.lua 실행

```
KEYS = ["ClubEvent:views:42", "ClubEvent:dirty"]
ARGV = ["2", "42"]   ← delta=2, eventId=42
```

Lua(`subtract_view_chunk.lua`) 내부 실행:

```
DECRBY ClubEvent:views:42 2  → 0  (remaining = 0)
DEL    ClubEvent:views:42        ← remaining <= 0 이므로 삭제
SREM   ClubEvent:dirty 42
return 1
```

성공 후 멱등성 키 정리:

```
DEL ClubEvent:committed:42
```

### 최종 Redis 상태

```
ClubEvent:dirty            = {}   (비어있음)
ClubEvent:views:42         → 삭제됨
ClubEvent:committed:42     → 삭제됨
```

---

## Phase 3 — 장애 시나리오 (Subtract 실패 후 재시도)

DB 반영은 성공했으나 Redis Subtract 도중 연결이 끊긴 경우다.

```
[Write]    DB 반영 성공                          ✅
           SET ClubEvent:committed:42 2 EX 600  ✅
[Subtract] Lua Script 실행 중 Redis 연결 끊김    ❌
```

다음 스케줄러 실행 시:

```
SMEMBERS ClubEvent:dirty    → { "42" }   ← 아직 남아있음
GET ClubEvent:committed:42  → "2"        ← 존재하면 pendingMap으로 분류
```

`pendingMap = { 42 → 2 }` → **DB Write 없이 Subtract만 재시도**:

```
DECRBY ClubEvent:views:42 2
SREM   ClubEvent:dirty 42
DEL    ClubEvent:committed:42
```

`committed` 키가 DB 커밋 사실의 증거로 남아 이중 반영을 막는다.

---

## 전체 흐름 요약

```
조회 요청
└─ Lua(increment_view.lua)
   ├─ SET dedupKey NX EX 86400   → 중복이면 여기서 종료 (return 0)
   ├─ INCR views:{id}
   ├─ SADD dirty {id}
   └─ EXPIRE views:{id} 604800

스케줄러 (3분마다, 청크 단위 100개)
├─ SMEMBERS dirty
├─ GET committed:{id}  → 존재하면 pendingMap (Subtract만 재시도)
├─ GET views:{id}      → 존재하면 freshMap   (Write + Subtract)
│
├─ [pendingMap] Lua(subtract_view_chunk.lua) → DECRBY + 조건부 DEL/SREM
│              DEL committed:{id}
│
└─ [freshMap]  SQL: UPDATE club_events SET view_count = view_count + delta
               SET committed:{id} delta EX 600
               Lua(subtract_view_chunk.lua) → DECRBY + 조건부 DEL/SREM
               DEL committed:{id}
```

---

## Lua Script가 필요한 이유

두 곳 모두 **원자성**이 핵심이다.

| 위치 | 스크립트 | 원자성이 필요한 이유 |
|---|---|---|
| 조회 요청 | `increment_view.lua` | dedup 확인과 INCR/SADD 사이에 다른 요청이 끼어들면 중복 집계 발생 |
| Subtract | `subtract_view_chunk.lua` | DECRBY와 SREM 사이에 새 조회가 유입되면 dirty set에서 잘못 제거될 수 있음 |

---

## 멱등성 보장 범위와 한계

`committed` 키는 Redis에 저장되므로, **Redis 재시작으로 해당 키가 유실되면 이중 반영이 발생할 수 있다.**
현재 AOF(`everysec`) 정책으로 최대 1초치 유실 가능성이 존재한다.
조회수 특성상 소량 오차는 허용 가능한 수준으로 판단하고 있다.
