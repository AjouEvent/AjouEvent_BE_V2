# syntax=docker/dockerfile:1.7

# Build stage — 빌드 머신의 아키텍처에서 한 번만 돈다 (arm64 이미지도 에뮬레이션 없이 빌드)
FROM --platform=$BUILDPLATFORM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /app

# 의존성 레이어: 빌드 스크립트가 바뀌지 않으면 캐시를 그대로 쓴다
COPY gradlew settings.gradle build.gradle ./
COPY gradle gradle
RUN --mount=type=cache,target=/root/.gradle \
    chmod +x gradlew && ./gradlew dependencies --no-daemon -q > /dev/null

COPY src src
RUN --mount=type=cache,target=/root/.gradle \
    ./gradlew bootJar -x test --no-daemon -q \
 && java -Djarmode=tools -jar build/libs/*.jar extract --layers --launcher --destination extracted

# Runtime stage — Spring Boot 레이어 순서대로 복사해 앱 코드만 바뀐 배포는 마지막 레이어(수백 KB)만 받는다
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
RUN addgroup -S -g 10001 app && adduser -S -u 10001 -G app app \
 && mkdir -p /app/tokens /app/secrets && chown -R app:app /app
COPY --from=builder --chown=app:app /app/extracted/dependencies/ ./
COPY --from=builder --chown=app:app /app/extracted/spring-boot-loader/ ./
COPY --from=builder --chown=app:app /app/extracted/snapshot-dependencies/ ./
COPY --from=builder --chown=app:app /app/extracted/application/ ./
USER app
EXPOSE 8080 9090
ENTRYPOINT ["java", "org.springframework.boot.loader.launch.JarLauncher"]
