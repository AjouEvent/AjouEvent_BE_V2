---
name: spring-api-rules
description: Define controllers, orchestrators, services, repositories, entities, DTOs for Spring Boot REST API. Use when user mentions API, endpoint, controller, orchestrator, service, repository, entity, DTO, CRUD, domain, feature, function, or REST creation.
allowed-tools: Read, Write, Edit, Glob, Grep, Bash
---

# Spring API Development Rules

Standard rules for Spring Boot REST API development in this project.

- Root Package: `com.example.ajouevent_be_v2`
- Java 21 / Spring Boot 4.0.3 / MySQL + Redis

---

## Package Structure

```
com.example.ajouevent_be_v2
├── controller/
│   ├── {Domain}Controller.java
│   └── docs/                         # Swagger interface
├── orchestrator/{domain}/
│   └── {Domain}Orchestrator.java
├── service/{domain}/
│   ├── {Domain}CommandService.java   # 쓰기 작업
│   └── {Domain}QueryService.java     # 읽기 작업
├── repository/
│   ├── port/{domain}/
│   │   ├── {Domain}Repository.java   # 순수 Java 인터페이스
│   │   └── {Domain}CachePort.java    # 캐시 포트 인터페이스
│   └── adapter/{domain}/
│       ├── {Domain}JpaRepository.java     # JpaRepository 인터페이스
│       ├── {Domain}RepositoryAdapter.java # Port 구현체 (JPA 위임)
│       ├── {Domain}BulkRepository.java    # Bulk JDBC 구현체 (필요시)
│       └── {Domain}CacheAdapter.java      # RedisTemplate 구현체
├── common/
│   ├── dto/        # SliceResponse, PageResponse, ResponseDto
│   ├── exception/
│   └── util/
├── config/
│   ├── SecurityConfig.java
│   ├── AsyncConfig.java
│   └── properties/
└── {domain}/
    ├── dto/
    ├── exception/
    └── config/     # 도메인 전용 Properties
```

---

## Layer Dependency (단방향 엄수)

```
Controller → Orchestrator → CommandService / QueryService → Repository
```

| 규칙 | 내용 |
|------|------|
| Controller | Orchestrator만 호출 — Service 직접 호출 금지 |
| Orchestrator | 같은 도메인 Command/QueryService 호출. 타 도메인은 Orchestrator 레벨에서만 주입 |
| Orchestrator → Orchestrator | 금지 (예외: WebhookOrchestrator → PushOrchestrator `@Async` 직접 호출) |
| Service → 타 도메인 Service | 금지 — 반드시 팀 회의 후 결정 |
| Service → Repository | 같은 도메인만. cross-domain 접근 시 ⚠️ 주석 필수 |

---

## Command / Query Service Split

```java
// CommandService — 쓰기 (INSERT / UPDATE / DELETE)
@Service
@RequiredArgsConstructor
public class MemberCommandService {
    // register(), update(), delete() 등 상태 변경 메서드
    // @Transactional은 상태 변경이 있는 메서드에만 선언
}

// QueryService — 읽기 (SELECT)
@Service
@RequiredArgsConstructor
public class MemberQueryService {
    // findById(), getList() 등 조회 전용 메서드 (@Transactional 불필요)
}
```

메서드가 2개 이하이고 모두 같은 성격이면 단일 `{Domain}Service`로 유지 가능.

---

## Controller

```java
@RestController
@RequiredArgsConstructor
public class MemberController implements MemberControllerDocs {

    private final MemberOrchestrator memberOrchestrator;

    @PostMapping("/api/v2/members")
    public ResponseEntity<Void> register(@RequestBody RegisterRequest request, @AuthUser Member member) {
        memberOrchestrator.register(request, member);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/api/v2/admin/members/{id}")
    public ResponseEntity<Void> deleteMember(@AuthAdmin Member admin, @PathVariable Long id) {
        memberOrchestrator.deleteMember(admin, id);
        return ResponseEntity.ok().build();
    }
}
```

- `@RequestMapping` 클래스 레벨 사용 금지 — 각 메서드에 **전체 경로** 작성 (예: `@PostMapping("/api/v2/members")`)
- `@RequestBody`, `@CookieValue`, `@PathVariable`, `@RequestParam` 등 **파라미터 레벨 애노테이션은 Docs 인터페이스에서 상속되지 않음** — 구현체 메서드에도 반드시 중복 선언할 것
- **모든 엔드포인트는 `/api/v2/`로 시작**
- 반환 타입은 반드시 `ResponseEntity<T>`
- Service 직접 호출 금지 — Orchestrator만 호출
- `Principal` 사용 금지 — 반드시 `@AuthUser` / `@AuthAdmin` 애노테이션으로 `Member` 객체를 직접 주입받을 것

## 인증 파라미터 애노테이션

컨트롤러에서 로그인 사용자를 `Principal`로 받지 않고, `@AuthUser` / `@AuthAdmin` / `@AuthOptional` 애노테이션으로 `Member` 엔티티를 바로 주입받는다.
내부적으로 `AuthArgumentResolver`가 `SecurityContextHolder`에서 이메일을 꺼내 DB 조회 후 반환한다.
`AuthArgumentResolver`는 `SecurityConfig.addArgumentResolvers()`에 등록되어 있다.

| 애노테이션 | 파일 | 비인증(Anonymous) | 비관리자 | 반환 |
|-----------|------|------------------|---------|------|
| `@AuthUser` | `config/auth/AuthUser.java` | `401` throw | — | `Member` |
| `@AuthAdmin` | `config/auth/AuthAdmin.java` | `401` throw | `403` throw | `Member` |
| `@AuthOptional` | `config/auth/AuthOptional.java` | `null` 반환 | — | `Member?` |

### 사용 기준

| 엔드포인트 성격 | 사용 애노테이션 |
|----------------|----------------|
| 로그인 필수 (일반 사용자) | `@AuthUser` |
| 로그인 필수 (관리자 전용) | `@AuthAdmin` |
| 비로그인도 접근 가능하나 로그인 여부에 따라 동작이 다름 | `@AuthOptional` |

```java
// ✅ 로그인 필수
@GetMapping("/api/v2/members")
public ResponseEntity<MemberGetDto> getMemberInfo(@AuthUser Member member) { ... }

// ✅ 관리자 전용
@DeleteMapping("/api/v2/admin/members/{id}")
public ResponseEntity<Void> deleteMember(@AuthAdmin Member admin, @PathVariable Long id) { ... }

// ✅ 비로그인 허용 — null 체크로 분기
@GetMapping("/api/v2/events/{eventId}")
public ResponseEntity<EventDetailResponse> getEventDetail(
    @PathVariable Long eventId,
    @AuthOptional Member member,   // 비인증 시 null
    HttpServletRequest request,
    HttpServletResponse response) { ... }

// ❌ 금지 — Principal 직접 사용
@GetMapping("/api/v2/members")
public ResponseEntity<MemberGetDto> getMemberInfo(Principal principal) { ... }
```

### @AuthOptional 사용 시 서비스 분기 패턴

```java
public EventDetailResponse getEventDetail(Long eventId, Member member, ...) {
    if (member == null) {
        // 비인증 사용자 처리 (쿠키/Redis 기반 조회수 등)
        handleAnonymousUser(request, response, event);
    } else {
        // 인증 사용자 처리
        handleAuthenticatedUser(member, event);
    }
}
```

---

## DTO

> **모든 DTO는 Java `record`로 작성. `@Data`/`@Getter` class 사용 금지.**

### 네이밍

| 접미사 | 기준 | 예시 |
|--------|------|------|
| `*Request` | Controller 메서드 파라미터로 직접 사용 (HTTP 요청 입력) | `OauthRequest`, `MemberUpdateRequest` |
| `*Response` | Controller 메서드 반환 타입으로 직접 사용 (HTTP 응답 출력) | `LoginResponse`, `MemberInfoResponse` |
| `*Result` | Controller 메서드 시그니처에 등장하지 않는 내부 결과 전달 | `MemberLoginResult`, `AuthTokenResult`, `GoogleUserInfoResult` |
| `*Command` | Controller 메서드 시그니처에 등장하지 않는 내부 명령 전달 | `SendNotificationCommand` |

- **판단 기준: Controller 메서드의 파라미터/반환 타입에 직접 등장하면 `*Request` / `*Response`, 그 외 레이어 간 내부 전달이면 `*Result` / `*Command`**
- Controller 내부 지역 변수로만 사용하더라도 HTTP I/O 목적이 아니면 `*Result` / `*Command`
- `*Dto` 접미사 사용 금지

### Presentation DTO 조립 책임

> **Service는 `*Response` DTO를 생성하거나 반환하지 않는다. `*Response` 조립은 반드시 Orchestrator에서 수행한다.**

| 레이어 | 책임 |
|--------|------|
| Service | 비즈니스 로직 수행, 도메인 객체(Entity / `*Result`) 반환 |
| Orchestrator | Service 결과를 받아 `*Response` DTO로 조립 후 Controller에 전달 |

이유:
1. **계층 간 책임 분리** — Service는 비즈니스 로직, Orchestrator는 DTO 조립
2. **의존성 방향 유지** — 하위 계층(Service)이 상위 계층의 API 응답 포맷(`*Response`)을 알아서는 안 됨
3. **비즈니스 로직 재사용** — 동일한 Service를 여러 API(다른 응답 포맷)에서 재사용 가능

```java
// ✅ Service — 도메인 객체 반환
public class MemberService {
    public Member findByEmail(String email) { ... }
}

// ✅ Orchestrator — Response 조립
public class MemberOrchestrator {
    public MemberInfoResponse getMemberInfo(Member member) {
        return new MemberInfoResponse(member.getName(), member.getEmail(), member.getMajor());
    }
}

// ❌ 금지 — Service가 Response DTO 생성
public class MemberService {
    public MemberInfoResponse getMemberInfo(Member member) {
        return new MemberInfoResponse(member.getName(), member.getEmail(), member.getMajor());
    }
}
```

### Response — `from()` / `of()`

```java
// 단일 도메인
public record MemberResponse(Long id, String email) {
    public static MemberResponse from(Member member) {
        return new MemberResponse(member.getId(), member.getEmail());
    }
}

// 다중 도메인 조합
public record NotificationDetailResponse(Long id, String title, String email) {
    public static NotificationDetailResponse of(PushNotification n, ClubEvent e, Member m) {
        return new NotificationDetailResponse(n.getId(), e.getTitle(), m.getEmail());
    }
}
```

### Pagination 전달 객체

계층 간 페이지네이션 데이터 전달 시 역할에 따라 구분하여 사용한다.

| 객체 | 용도 | 방향 |
|------|------|------|
| `PageResult<T>` | 페이지 번호 내부 전달 | Service → Orchestrator |
| `SliceResult<T>` | 무한 스크롤 내부 전달 | Service → Orchestrator |
| `PageResponse<T>` | 페이지 번호 API 응답 | Orchestrator → Controller |
| `SliceResponse<T>` | 무한 스크롤 API 응답 | Orchestrator → Controller |

```java
// QueryService — PageResult 반환
public PageResult<Member> getMembers(Pageable pageable) {
    Page<Member> page = memberRepository.findAll(pageable);
    return new PageResult<>(page.getContent(), page.getNumber(),
        page.getTotalPages(), page.getTotalElements(), page.hasNext(), page.hasPrevious());
}

// Orchestrator — PageResult → PageResponse 변환
public PageResponse<MemberResponse> getMembers(Pageable pageable) {
    PageResult<Member> result = memberQueryService.getMembers(pageable);
    List<MemberResponse> responses = result.result().stream().map(MemberResponse::from).toList();
    return new PageResponse<>(responses, result.currentPage(), result.totalPages(),
        result.totalElements(), result.hasNext(), result.hasPrevious());
}
```

### Request → Entity 변환

> **Request DTO에 `toEntity()` 작성 금지.** 변환 책임은 **CommandService**가 진다.

```java
// ✅ CommandService에서 Builder로 직접 생성
@Transactional
public void register(RegisterRequest request) {
    Member member = Member.builder()
        .email(request.email())
        .password(passwordEncoder.encode(request.password()))
        .name(request.name())
        .build();
    memberRepository.save(member);
}

// ❌ 금지
public record RegisterRequest(String email, String password, String name) {
    public Member toEntity() { ... }  // DTO가 Entity를 알아서는 안 됨
}
```

---

## API Response Format

| 상황 | 반환 타입 |
|------|-----------|
| 명령 API, 응답 데이터 없음 | `ResponseEntity<Void>` |
| 명령 API, 응답 데이터 있음 | `ResponseEntity<XxxResponse>` |
| 단건 조회 | `ResponseEntity<XxxResponse>` |
| 불리언 확인 | `ResponseEntity<Boolean>` |
| 단순 문자열 반환 | `ResponseEntity<String>` |
| 무한 스크롤 페이지네이션 | `ResponseEntity<SliceResponse<XxxResponse>>` |
| 페이지 번호 페이지네이션 | `ResponseEntity<PageResponse<XxxResponse>>` |
| 소량 전체 목록 | `ResponseEntity<List<XxxResponse>>` |

---

## Entity

```java
@Entity
@Table(name = "member")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Member extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    public void updateName(String name) { this.name = name; }  // Setter 금지
}
```

- `@NoArgsConstructor(access = PROTECTED)` 필수
- ID 전략: `GenerationType.IDENTITY`
- **Setter 전면 금지** — 상태 변경은 명시적 메서드

---

## Repository Pattern (Port / Adapter)

```java
// Port — 순수 Java 인터페이스 (port/{domain}/)
public interface MemberRepository {
    Member save(Member member);
    Optional<Member> findById(Long id);
}

// JPA 인터페이스 (adapter/{domain}/)
public interface MemberJpaRepository extends JpaRepository<Member, Long> { }

// Adapter — Port 구현체, JPA 위임 (adapter/{domain}/)
@Repository
@RequiredArgsConstructor
public class MemberRepositoryAdapter implements MemberRepository {
    private final MemberJpaRepository memberJpaRepository;

    @Override
    public Member save(Member member) { return memberJpaRepository.save(member); }

    @Override
    public Optional<Member> findById(Long id) { return memberJpaRepository.findById(id); }
}
```

---

## Feign Client

- 현재 위치: `config/{Domain}FeignClient.java`
- Feign client가 여러 개로 늘어날 경우 `config/feign/` 하위로 분리 예정
- `@EnableFeignClients`는 메인 애플리케이션 클래스에 선언

---

## Redis Cache Port

> **`common/redis/RedisService` 사용 금지.** 도메인별 포트를 `port/`에 정의하고 `adapter/`에서 RedisTemplate으로 구현.

```java
// Port (port/{domain}/)
public interface NoticeCachePort {
    boolean isFirstIpRequest(String clientIp, Long eventId);
    void writeClientRequest(String clientIp, Long eventId);
}

// Adapter (adapter/{domain}/)
@Repository
@RequiredArgsConstructor
public class NoticeCacheAdapter implements NoticeCachePort {
    private final RedisTemplate<String, String> redisTemplate;

    @Override
    public boolean isFirstIpRequest(String clientIp, Long eventId) {
        String key = "ClubEvent:anon:" + eventId + ":" + clientIp;
        return Boolean.TRUE.equals(
            redisTemplate.opsForValue().setIfAbsent(key, "1", 1, TimeUnit.HOURS)
        );
    }
}

// Service에서 포트 인터페이스만 주입
@Service
@RequiredArgsConstructor
public class NoticeCommandService {
    private final NoticeCachePort noticeCachePort;
}
```

---

## Redis Key Naming Convention

### 키 구조

```
{도메인} : {목적} : {entityId} : {식별자...}
```

예시:

```
ClubEvent:views:{eventId}
ClubEvent:user:{eventId}:{email}
ClubEvent:anon:{eventId}:{ip}:{userAgent}
```

### 규칙

**1. prefix 상수는 어댑터 클래스 상단에 모두 선언한다**

```java
private static final String VIEW_KEY_PREFIX    = "ClubEvent:views:";
private static final String USER_REQUEST_PREFIX = "ClubEvent:user:";
private static final String ANON_REQUEST_PREFIX = "ClubEvent:anon:";
private static final long   VIEW_COUNT_TTL_MINUTES  = 4L;
private static final long   VIEW_DEDUP_TTL_SECONDS  = 86400L;
```

**2. 키에서 entityId를 파싱하는 로직은 Port 메서드로 제공한다**

Service가 `split(":")[n]` 인덱스로 직접 파싱하면 키 형식이 바뀔 때 런타임에 버그가 발생한다.
파싱 책임은 키를 정의한 어댑터가 갖고, Port 메서드로 노출한다.

```java
// ❌ 금지 — Service가 키 구조를 직접 알면 안 됨
Long eventId = Long.parseLong(key.split(":")[2]);

// ✅ Port 메서드로 추상화
Long eventId = noticeCachePort.extractEventIdFromViewKey(key);

// Adapter 구현 — prefix 상수로 파싱해 키 형식 변경에 자동 대응
@Override
public Long extractEventIdFromViewKey(String key) {
    return Long.parseLong(key.substring(VIEW_KEY_PREFIX.length()));
}
```

**3. SCAN은 스케줄러 flush 대상 키에만 허용한다**

단순 존재 여부 확인은 `hasKey`(O(1))를 사용한다. SCAN은 O(N)이므로 남용하면 Redis 성능에 영향을 준다.

**4. 모든 Redis 키에는 반드시 TTL을 설정한다**

TTL 없는 키는 Redis 메모리를 영구 점유한다. `setIfAbsent`, `set` 호출 시 항상 TTL 파라미터를 포함한다.

---

## Exception Handling

모든 예외 클래스는 **`common/exception/{subdomain}/`** 에 위치한다.

```
common/exception/
├── AjouBaseException.java      # 추상 베이스
├── ErrorCode.java              # 인터페이스
├── ErrorResponse.java
├── GlobalExceptionHandler.java
├── auth/                       # 인증 예외
│   ├── AuthErrorCode.java
│   └── AuthException.java
├── common/                     # 도메인 없는 인프라/공통 예외
│   ├── CommonErrorCode.java
│   └── CommonException.java
└── {subdomain}/                # 도메인별 예외 추가 시 여기에
    ├── {Domain}ErrorCode.java
    └── {Domain}Exception.java
```

```java
// ErrorCode 인터페이스
public interface ErrorCode {
    int getStatus();
    String getCode();     // 형식: AE-{DOMAIN}-{ERROR-NAME}
    String getMessage();
}

// 도메인 ErrorCode
@Getter
@AllArgsConstructor
public enum MemberErrorCode implements ErrorCode {
    USER_NOT_FOUND(404, "AE-MEMBER-USER-NOT-FOUND", "사용자를 찾을 수 없습니다.");

    private final int status;
    private final String code;
    private final String message;
}

// 도메인 Exception
public class MemberException extends AjouBaseException {
    public MemberException(MemberErrorCode errorCode) { super(errorCode); }
}
```

---

## @Transactional Rules

### 전제 조건

`open-in-view: false` 설정으로 인해 트랜잭션이 종료되는 순간 영속성 컨텍스트가 닫힌다.
트랜잭션 밖에서 Lazy 연관 필드에 접근하면 `LazyInitializationException`이 발생하므로,
**Orchestrator에서 트랜잭션 경계를 명시적으로 선언해야 한다.**

### 레이어별 규칙

| 레이어 | @Transactional | @Transactional(readOnly = true) |
|--------|---------------|--------------------------------|
| Orchestrator (쓰기 포함) | ✅ 선언 | ❌ |
| Orchestrator (순수 조회) | ❌ | ✅ 선언 필수 |
| CommandService | ✅ 상태 변경 메서드에만 | ❌ |
| QueryService | ❌ 불필요 | ❌ |
| 클래스 레벨 (어디서든) | ❌ 금지 | ❌ 금지 |

### 이유

- **Orchestrator**: 여러 Service를 거쳐 반환된 엔티티의 Lazy 필드를 DTO 변환 시 접근하므로 트랜잭션 범위가 필요. 순수 조회는 `readOnly = true`로 dirty checking 비활성화 + DB 슬레이브 라우팅 가능.
- **QueryService**: Repository 메서드 자체가 단건 트랜잭션으로 실행되므로 별도 선언 불필요. 조회 후 추가 Lazy 접근은 Orchestrator 트랜잭션에 위임.
- **CommandService**: 상태 변경 메서드에만 선언. 조회 전용 메서드가 섞여 있으면 Command/Query 분리를 재검토.

### 예시

```java
// ✅ Orchestrator — 순수 조회 (readOnly = true 필수)
@Transactional(readOnly = true)
public SliceResponse<ClubEventResponse> getSubscribedEvents(Pageable pageable, Member member) {
    List<Type> types = topicQueryService.getSubscribedTopics(member).stream()
        .map(tm -> tm.getTopic().getType())  // Lazy 접근 — 트랜잭션 필요
        .toList();
    ...
}

// ✅ Orchestrator — 쓰기 포함 (일반 @Transactional)
@Transactional
public SliceResponse<ClubEventResponse> getEventTypeList(String type, ...) {
    ...
    topicCommandService.markTopicAsRead(member, eventType);  // isRead 갱신 포함
    ...
}

// ✅ CommandService — 쓰기 메서드에만
@Transactional
public void register(RegisterRequest request) { ... }

// ✅ QueryService — @Transactional 없음
public List<TopicMember> getSubscribedTopics(Member member) { ... }

// ❌ 금지 — 클래스 레벨
@Transactional
@Service
public class MemberCommandService { ... }

// ❌ 금지 — QueryService에 readOnly = true
@Transactional(readOnly = true)
public List<TopicMember> getSubscribedTopics(Member member) { ... }
```

### 외부 I/O

**외부 I/O(FCM, 이메일, 외부 API) 절대 트랜잭션 블록 내 포함 금지** — Orchestrator에서 트랜잭션 메서드와 분리해 호출

---

## @Async Rules

- `@Async` 사용 가능 위치: **`PushOrchestrator`만 허용**
- `@EnableAsync`는 `config/AsyncConfig.java`에서 활성화

---

## Lombok Rules

| 클래스 타입 | 어노테이션 |
|-------------|------------|
| Entity | `@Getter` `@NoArgsConstructor(access = PROTECTED)` `@Builder` `@AllArgsConstructor` |
| Service / Orchestrator / Controller | `@RequiredArgsConstructor` |
| Config / Component | `@RequiredArgsConstructor` |
| DTO | Java `record` — Lombok 불필요 |

> `@Autowired` 필드 주입 금지 — `@RequiredArgsConstructor`로 생성자 주입 필수

---

## Properties

> **하드코딩 금지.** URL, 토큰 키, 사이즈 등은 반드시 `@ConfigurationProperties` 사용.
> 위치: `config/properties/{Domain}Properties.java`

```java
// ✅
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "ajou.fcm")
public class FcmProperties {
    private String certification;
    private String defaultImageUrl;
    private String redirectionUrlPrefix;
    private String defaultClickActionUrl;
}

// ❌
private static final String DEFAULT_IMAGE_URL = "https://...";
```

---

## Logging (@Slf4j)

> **커스텀 Logger 클래스 작성 금지.** `@Slf4j` (Logback) 사용.

| 레벨 | 사용 상황 |
|------|-----------|
| `log.debug()` | 개발 환경 디버깅 |
| `log.info()` | 정상 처리 흐름 |
| `log.warn()` | 주의 필요 상황 |
| `log.error()` | 예외 발생, 장애 |
