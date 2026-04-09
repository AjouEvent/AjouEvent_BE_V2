# Micrometer + Prometheus + Actuator 메트릭 추출 원리

## 1. 관련 라이브러리

### Spring Boot Actuator (`spring-boot-starter-actuator`)
Spring Boot 애플리케이션의 운영 정보를 HTTP 엔드포인트로 노출하는 공식 모듈.
`/actuator/health`, `/actuator/prometheus` 등의 엔드포인트를 제공한다.
메트릭 수집 자체는 하지 않고, **이미 수집된 메트릭을 외부에 노출하는 게이트웨이** 역할이다.

### Micrometer (`micrometer-core`)
JVM 기반 애플리케이션을 위한 **벤더 중립적 메트릭 수집 추상화 레이어**.
Prometheus, Datadog, CloudWatch 등 다양한 모니터링 시스템을 코드 변경 없이 교체할 수 있도록
SLF4J와 동일한 방식으로 설계되어 있다. 코드에서는 Micrometer API만 사용하고,
어떤 모니터링 시스템으로 전송할지는 의존성(registry)으로 결정한다.

### micrometer-registry-prometheus (`micrometer-registry-prometheus`)
Micrometer가 수집한 메트릭을 **Prometheus가 이해하는 텍스트 포맷**으로 직렬화하는 구현체.
이 의존성을 추가하는 것만으로 Spring Boot가 `PrometheusMeterRegistry` 빈을 자동 등록하고
`/actuator/prometheus` 엔드포인트를 활성화한다.

---

## 2. 핵심 클래스와 책임

### `Meter` (io.micrometer.core.instrument)
메트릭의 **최소 단위 추상 타입**. 모든 메트릭은 Meter의 하위 타입이다.

| 하위 타입 | 특징 | 사용 예 |
|---|---|---|
| `Gauge` | 현재 순간값을 실시간으로 읽음 (단조 증가 아님) | 스레드 수, 힙 메모리, 커넥션 수 |
| `Counter` | 단조 증가하는 누적 카운터 | 완료된 요청 수, 완료된 작업 수 |
| `Timer` | 실행 시간과 횟수를 함께 측정 | HTTP 요청 레이턴시 |
| `DistributionSummary` | 크기/양의 분포 측정 | 요청 페이로드 크기 |

---

### `MeterRegistry` (io.micrometer.core.instrument)
**모든 Meter의 저장소이자 팩토리**.
Meter를 등록하고 조회하는 중앙 레지스트리다.

```java
// Counter 등록 예시
Counter counter = Counter.builder("fcm.send.count")
    .tag("result", "success")
    .register(meterRegistry); // MeterRegistry에 등록

counter.increment(); // 값 증가
```

Spring Boot는 애플리케이션 컨텍스트에 `MeterRegistry` 빈을 자동 등록한다.
`micrometer-registry-prometheus`가 클래스패스에 있으면
실제 구현체로 `PrometheusMeterRegistry`가 등록된다.

```
MeterRegistry (추상)
    └── CompositeMeterRegistry   — 여러 Registry에 동시 전송
    └── PrometheusMeterRegistry  — Prometheus 포맷으로 직렬화 ← 이 프로젝트에서 사용
    └── DatadogMeterRegistry     — Datadog으로 전송
    └── CloudWatchMeterRegistry  — AWS CloudWatch로 전송
```

---

### `PrometheusMeterRegistry` (io.micrometer.prometheusmetrics)
`MeterRegistry`의 **Prometheus 구현체**.
내부적으로 prometheus-client 라이브러리의 `CollectorRegistry`를 래핑한다.

`/actuator/prometheus` 엔드포인트로 요청이 들어오면
`scrape()` 메서드가 호출되어 등록된 모든 Meter의 현재값을 Prometheus 텍스트 포맷으로 직렬화한다.

```
# Prometheus 텍스트 포맷 예시
executor_active_threads{name="fcm_callback_executor",pool="fcm-callback"} 3.0
executor_pool_size_threads{name="fcm_callback_executor",pool="fcm-callback"} 4.0
hikaricp_connections_active{pool="HikariPool-Main"} 5.0
```

---

### `MeterBinder` (io.micrometer.core.instrument.binder)
특정 컴포넌트의 메트릭을 `MeterRegistry`에 자동으로 등록해주는 **인터페이스**.
메서드는 `bindTo(MeterRegistry registry)` 하나뿐이다.

Spring Boot Actuator는 애플리케이션 시작 시 클래스패스에 있는 MeterBinder 구현체들을
자동으로 찾아 `bindTo()`를 호출한다 (JVM, Tomcat, HikariCP 등).

```
MeterBinder (인터페이스)
    └── JvmMemoryMetrics          — jvm_memory_used_bytes 등 자동 등록
    └── JvmGcMetrics              — jvm_gc_pause_seconds 등 자동 등록
    └── TomcatMetricsBinder       — tomcat_threads_busy_threads 등 자동 등록
    └── HikariDataSourceMetrics   — hikaricp_connections_* 자동 등록
    └── ExecutorServiceMetrics    — executor_* 등록 (수동 호출 필요)
```

대부분의 MeterBinder는 Spring Boot가 자동으로 호출하지만,
`ExecutorServiceMetrics`는 커스텀 스레드풀을 대상으로 하므로 **직접 호출해야 한다**.

---

### `ExecutorServiceMetrics` (io.micrometer.core.instrument.binder.jvm)
`MeterBinder`의 구현체로, `ThreadPoolExecutor`의 public 메서드들을 **Gauge로 래핑**하여 등록한다.

```java
ExecutorServiceMetrics.monitor(
    meterRegistry,                    // 등록할 레지스트리
    executor.getThreadPoolExecutor(), // 대상 ThreadPoolExecutor
    "fcm_callback_executor",          // 메트릭 이름 prefix
    List.of(Tag.of("pool", "fcm-callback")) // 공통 태그
);
```

내부에서 아래와 같이 Gauge를 등록한다:

```java
// ExecutorServiceMetrics 내부 (간략화)
Gauge.builder("executor_pool_size_threads", executor, ThreadPoolExecutor::getPoolSize)
    .tags(tags)
    .register(registry);

Gauge.builder("executor_active_threads", executor, ThreadPoolExecutor::getActiveCount)
    .tags(tags)
    .register(registry);

Gauge.builder("executor_queued_tasks", executor, e -> e.getQueue().size())
    .tags(tags)
    .register(registry);

// completed는 Counter (단조 증가)
FunctionCounter.builder("executor_completed_tasks_total", executor, ThreadPoolExecutor::getCompletedTaskCount)
    .tags(tags)
    .register(registry);

Gauge.builder("executor_queue_remaining_tasks", executor, e -> e.getQueue().remainingCapacity())
    .tags(tags)
    .register(registry);
```

---

### `Tag` (io.micrometer.core.instrument)
메트릭에 붙는 **key-value 레이블**. Prometheus의 label과 동일한 개념이다.
같은 메트릭 이름을 여러 인스턴스에서 사용할 때 태그로 구분한다.

```java
Tag.of("pool", "fcm-callback")
// → Prometheus 포맷: {pool="fcm-callback"}
```

---

## 3. 전체 데이터 흐름

```
[Spring Boot 앱 시작]
        │
        ├─ Spring Boot가 PrometheusMeterRegistry 빈 자동 등록
        │
        ├─ MeterBinder들 자동 실행 (JVM, Tomcat, HikariCP)
        │   └─ 각 MeterBinder.bindTo(meterRegistry) 호출
        │       └─ Gauge/Counter/Timer 등록
        │
        └─ FcmConfig, SchedulerConfig에서 직접 등록
            └─ ExecutorServiceMetrics.monitor(...) 호출
                └─ Gauge들이 MeterRegistry에 등록됨


[Prometheus 스크래핑 — 15초마다]
        │
        └─ GET http://<앱서버>:9090/actuator/prometheus
                │
                └─ PrometheusMeterRegistry.scrape() 실행
                        │
                        ├─ 등록된 모든 Gauge의 supplier 함수 호출
                        │   └─ executor.getPoolSize(), getActiveCount(), ... 실행 (현재값 읽기)
                        │
                        └─ Prometheus 텍스트 포맷으로 직렬화하여 응답


[Grafana 시각화]
        │
        └─ PromQL로 Prometheus에 쿼리
            └─ 시계열 데이터 렌더링
```

---

## 4. Gauge가 "실시간"인 이유

`ExecutorServiceMetrics`가 등록하는 Gauge는 값을 **직접 저장하지 않는다**.
대신 `executor.getPoolSize()`처럼 값을 읽어오는 **함수(supplier)를 저장**한다.

Prometheus가 스크래핑할 때마다 그 supplier 함수가 호출되어 그 순간의 값이 반환된다.
따라서 Gauge는 항상 스크래핑 시점의 현재 상태를 반영한다.

반면 `Counter`(`executor_completed_tasks_total`)는 `getCompletedTaskCount()`의 누적값을 읽어
이전 스크래핑 값과의 차이를 Prometheus가 rate()로 계산한다.

---

## 5. 이 프로젝트에서 자동 vs 수동 등록 정리

| 메트릭 대상 | 등록 방식 | 담당 클래스 |
|---|---|---|
| JVM 힙/GC/스레드 | 자동 (Spring Boot) | `JvmMemoryMetrics`, `JvmGcMetrics` |
| Tomcat 스레드 | 자동 (Spring Boot) | `TomcatMetricsBinder` |
| HikariCP 커넥션 | 자동 (Spring Boot) | `HikariDataSourceMetrics` |
| HTTP 요청 레이턴시 | 자동 (Spring Boot) | `WebMvcMetricsFilter` |
| FCM 콜백 스레드풀 | **수동** (`FcmConfig.java`) | `ExecutorServiceMetrics` |
| Spring Scheduler | **수동** (`SchedulerConfig.java`) | `ExecutorServiceMetrics` |
