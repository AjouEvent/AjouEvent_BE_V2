# DB Migration V2 Schema Workflow

## 목적

V1 데이터를 V2 DB로 이관하기 전에, V2 백엔드 코드와 DB schema를 마이그레이션에 맞게 정리한다.

핵심 방향:

```text
1. V2 코드가 기대하는 구조는 유지한다.
2. V1의 불필요한 createdAt/modifiedAt/password/phone 등은 버린다.
3. 다른 테이블에서 조회 가능한 중복 컬럼은 V2에 굳이 추가하지 않는다.
4. URL 길이, NULL 보정처럼 실제 데이터 손실이나 import 실패가 발생할 수 있는 부분은 V2 schema를 확장한다.
5. token_id 기반 구조는 V2의 tokenValue/member 구조로 transform한다.
6. V2의 기존 인덱싱은 반드시 유지한다.
```

---

## 기존 컨벤션

### Issue

제목 형식:

```text
[Feat] 한 일
[Refactor] 한 일
```

이번 작업은 기능 추가보다는 마이그레이션을 위한 schema/entity 정합성 보완이므로 `Refactor` 또는 `Feat` 중 선택 가능하다.

추천:

```text
[Refactor] V1 데이터 마이그레이션을 위한 V2 스키마 정합성 보완
```

### Branch

형식:

```text
feat/#[github issue number]/하고자하는 일 간략히
```

예시:

```text
feat/#123/v1-migration-schema
```

주의:

```text
issue 생성 후 번호를 확인한 다음 브랜치를 생성한다.
```

### PR

제목 형식:

```text
[Type] 한 일 #[github issue number]
```

추천:

```text
[Refactor] V1 데이터 마이그레이션을 위한 V2 스키마 정합성 보완 #123
```

---

## 진행 순서

### 1. Issue 생성

issue에는 다음 내용을 포함한다.

```text
V1 DB 데이터를 V2 DB로 이관하기 전에 V2 엔티티와 schema.sql을 정리한다.

불필요한 created_at/modified_at/password/phone은 V2에서 사용하지 않으므로 이관하지 않는다.
반대로 URL 길이처럼 데이터 손실 가능성이 있는 항목은 V2 schema를 확장한다.
token_id 기반 테이블은 V2의 tokenValue/member 구조를 유지하면서 transform SQL로 이관한다.
V2의 기존 인덱스는 유지한다.
```

체크리스트:

```text
- [ ] V1/V2 schema 차이 기준으로 V2 엔티티 변경 범위 확정
- [ ] URL 길이 손실 가능 컬럼 확장
- [ ] nullable/import 실패 가능 컬럼 보정 전략 반영
- [ ] src/main/resources/schema.sql 업데이트
- [ ] V2 기존 인덱스 유지 확인
- [ ] 로컬 또는 개발 DB에서 schema 반영 확인
```

---

### 1-2. Issue 번호 기반 브랜치 생성

issue 번호가 `123`이라면:

```bash
git switch -c feat/#123/v1-migration-schema
```

현재 작업 트리에 다른 변경사항이 있을 수 있으므로, 브랜치 생성 전 반드시 `git status`를 확인한다.

---

### 2. V2 엔티티 변경

현재 결정된 변경 방향:

```text
1. club_event_images.url 길이 확장
2. event_banners.img_url 타입 확장
3. created_at/modified_at 계열은 추가하지 않음
4. Member.password/phone은 추가하지 않음
5. push_clusters.received_count/clicked_count는 추가하지 않음
   - V1 dump 기준 실제 값이 모두 0
6. push_clusters.keyword_id/topic_id는 추가하지 않음
   - V2에서는 push_notifications 기준으로 조회 가능
7. token_id 기반 구조는 되살리지 않음
   - keyword_tokens/topic_tokens/push_cluster_tokens는 V2의 tokenValue/member 구조 유지
```

변경 후보:

```text
ClubEventImage.url
- V1: varchar(2083)
- V2 현재: varchar(255)
- 결정: varchar(2083)로 확장

EventBanner.imgUrl
- V1: text
- V2 현재: varchar(255)
- 결정: text로 확장
```

주의:

```text
엔티티 변경은 DB schema 변경과 반드시 함께 맞춘다.
```

---

### 3. schema.sql 업데이트

대상 파일:

```text
src/main/resources/schema.sql
```

반영 원칙:

```text
1. V2가 최종적으로 원하는 DB schema를 명시한다.
2. V2의 기존 인덱스는 반드시 유지한다.
3. 마이그레이션을 위해 확장한 컬럼 타입을 반영한다.
4. V1에서 버리기로 결정한 컬럼은 추가하지 않는다.
5. token_id 기반 FK는 되살리지 않는다.
```

검토해야 할 항목:

```text
club_event_images.url varchar(2083)
event_banners.img_url text
push_clusters에는 received_count/clicked_count 추가하지 않음
push_clusters에는 keyword_id/topic_id 추가하지 않음
keyword_tokens/topic_tokens는 token_value 구조 유지
push_cluster_tokens는 member_id + token_value 구조 유지
기존 인덱스 유지
```

---

### 4. PR 작성

PR에는 반드시 다음 내용을 포함한다.

```text
1. 왜 변경했는지
2. V1의 어떤 데이터를 손실 없이 유지하기 위한 변경인지
3. 어떤 V1 컬럼은 왜 버리는지
4. token_id 구조를 왜 되살리지 않는지
5. V2 기존 인덱스를 유지했다는 점
```

PR 본문 초안:

```markdown
## 🔗 관련 이슈

> #123

## 💡 작업 내용

V1 DB 데이터를 V2 DB로 이관하기 전에 V2 엔티티와 schema.sql의 정합성을 보완했습니다.

- V1 이미지 URL 데이터 손실을 막기 위해 `club_event_images.url` 길이를 확장했습니다.
- V1 배너 이미지 URL 데이터 손실을 막기 위해 `event_banners.img_url` 타입을 확장했습니다.
- V2에서 사용하지 않는 `created_at`, `modified_at`, `password`, `phone` 계열 컬럼은 추가하지 않았습니다.
- V2에서 `push_notifications`를 통해 조회 가능한 `push_clusters.topic_id`, `push_clusters.keyword_id`는 중복 확장하지 않았습니다.
- V1 dump 기준 실제 값이 모두 0인 `received_count`, `clicked_count`는 V2에 추가하지 않았습니다.
- `token_id` 기반 구조는 되살리지 않고 V2의 `tokenValue/member` 구조를 유지했습니다.
- 기존 V2 인덱스는 유지했습니다.

## 📝 추가 설명(선택)

이번 변경은 V1 dump를 V2 DB에 직접 import하기 위한 작업이 아닙니다.
V1 dump는 staging DB에 원형 import한 뒤, transform SQL을 통해 V2 schema에 맞게 이관할 예정입니다.

## 📚 참고 자료(선택)

- V1 schema: `ajouevent-v1-schema.sql.gz`
- V2 schema: `ajouevent-v2-schema.sql.gz`
- 마이그레이션 의사결정 문서: `07-결정해야하는것.md`
```

---

### 5. Push 후 DB 확인

주의:

```text
AGENTS.md 기준으로 AI Agent는 git push/git commit을 직접 수행하지 않는다.
필요 시 사용자가 직접 수행한다.
```

push 이후 확인할 것:

```text
1. 개발 DB에 schema.sql 기준으로 schema가 정상 반영되었는지 확인
2. 확장 컬럼 타입이 기대대로 생성되었는지 확인
3. 기존 V2 인덱스가 유지되었는지 확인
4. V2 애플리케이션이 정상 기동되는지 확인
```

확인 SQL 예시:

```sql
SHOW CREATE TABLE club_event_images;
SHOW CREATE TABLE event_banners;
SHOW CREATE TABLE push_clusters;
SHOW INDEX FROM club_event_images;
SHOW INDEX FROM event_banners;
SHOW INDEX FROM push_clusters;
```

---

### 6. 마이그레이션 진행

DB schema 반영 확인 후 진행한다.

```text
1. V1 dump를 staging DB인 ajouevent_v1에 import
2. V2 실제 DB인 ajouevent는 schema.sql 기준으로 준비
3. transform SQL 작성
4. row count 검증
5. FK 검증
6. 주요 API 검증
7. 개발 서버에서 전체 동작 확인
```

---

## 현재 결정사항 요약

```text
1. created_at/modified_at은 버린다.
2. Member.password/phone은 버린다.
3. club_event_images.url은 varchar(2083)로 확장한다.
4. event_banners.img_url은 text로 확장한다.
5. push_clusters.received_count/clicked_count는 버린다.
6. push_clusters.keyword_id/topic_id는 버린다.
7. token_id 기반 구조는 되살리지 않고 V2 구조로 transform한다.
8. V2 기존 인덱스는 유지한다.
9. V1 dump는 staging DB에 import 후 transform한다.
```

---

## 다음 작업 시작 시 확인할 것

```text
1. 현재 git status 확인
2. 기존 작업 변경분이 있는지 확인
3. issue 생성
4. issue 번호 기반 브랜치 생성
5. entity/schema.sql 변경
6. 테스트 및 schema 검증
7. PR 작성
```
