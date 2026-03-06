---
name: spring-api-rules
description: Define controllers, orchestrators, services, repositories, entities, DTOs for Spring Boot REST API. Use when user mentions API, endpoint, controller, orchestrator, service, repository, entity, DTO, CRUD, domain, feature, function, or REST creation.
allowed-tools: Read, Write, Edit, Glob, Grep, Bash
---

# Spring API Development Rules

Standard rules for Spring Boot REST API development in this project.

- Root Package: `com.ajou.ajouevent`
- Java 21 / Spring Boot 4.0.3 / MySQL + Redis

---

## Package Structure

```
com.ajou.ajouevent
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

    @PostMapping("/api/members")
    public ResponseEntity<ResponseDto> register(@RequestBody RegisterRequest request) {
        memberOrchestrator.register(request);
        return ResponseEntity.ok(ResponseDto.success());
    }
}
```

- `@RequestMapping` 클래스 레벨 사용 금지 — 각 메서드에 전체 경로 작성
- 반환 타입은 반드시 `ResponseEntity<T>`
- Service 직접 호출 금지 — Orchestrator만 호출

---

## DTO

> **모든 DTO는 Java `record`로 작성. `@Data`/`@Getter` class 사용 금지.**

### 네이밍

| 용도 | 접미사 |
|------|--------|
| 외부 요청 입력 | `*Request` |
| API 응답 출력 | `*Response` |
| 도메인 간 내부 전달 | `*Dto` |

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
| 명령 API, 응답 데이터 없음 | `ResponseEntity<ResponseDto>` |
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
        String key = "view:" + eventId + ":" + clientIp;
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

## Exception Handling

```java
// ErrorCode 인터페이스
public interface ErrorCode {
    HttpStatus getStatus();
    String getCode();     // 형식: AE-{DOMAIN}-{ERROR-NAME}
    String getMessage();
}

// 도메인 ErrorCode
public enum MemberErrorCode implements ErrorCode {
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "AE-MEMBER-USER-NOT-FOUND", "사용자를 찾을 수 없습니다.");
}

// 도메인 Exception
public class MemberException extends AjouBaseException {
    public MemberException(MemberErrorCode errorCode) { super(errorCode); }
}
```

위치: `{domain}/exception/{Domain}Exception.java`, `{domain}/exception/{Domain}ErrorCode.java`

---

## @Transactional Rules

- **클래스 레벨 적용 금지** — 상태 변경 메서드에만 선언
- `@Transactional(readOnly = true)` 사용 금지
- 순수 조회 메서드에는 `@Transactional` 불필요
- **외부 I/O(FCM, 이메일, 외부 API) 절대 트랜잭션 블록 내 포함 금지** — Orchestrator에서 분리

```java
// ✅
@Transactional
public void register(RegisterRequest request) { ... }  // 쓰기 메서드에만

public Optional<Member> findById(Long id) { ... }  // 조회는 @Transactional 없음

// ❌
@Transactional
@Service
public class MemberCommandService { ... }  // 클래스 레벨 금지
```

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

```java
// ✅
@ConfigurationProperties(prefix = "ajou.fcm")
public record FcmProperties(String defaultImageUrl, String redirectionUrlPrefix) {}

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
