FROM eclipse-temurin:21-jdk-jammy AS builder

WORKDIR /app

COPY gradlew settings.gradle.kts build.gradle.kts ./
COPY gradle ./gradle
COPY src ./src

RUN chmod +x gradlew \
    && ./gradlew --no-daemon clean build \
    && install -D "$(find build/libs -maxdepth 1 -type f -name '*.jar' ! -name '*-plain.jar' -print -quit)" /app/app.jar

FROM eclipse-temurin:21-jre-alpine AS runtime

WORKDIR /app

COPY --from=builder --chown=10001:10001 /app/app.jar /app/app.jar

ENV DROPPERS_BIND_ADDRESS=0.0.0.0

EXPOSE 8765

USER 10001:10001

ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75.0", "-jar", "/app/app.jar"]
