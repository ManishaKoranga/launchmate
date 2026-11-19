# syntax=docker/dockerfile:1

# --- Java build ---------------------------------------------------------------
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /src
COPY pom.xml .
RUN --mount=type=cache,target=/root/.m2 mvn -q -B dependency:go-offline
COPY src ./src
RUN --mount=type=cache,target=/root/.m2 mvn -q -B -DskipTests package

# --- whisper.cpp (local speech-to-text), pinned release, static binary -------
FROM debian:bookworm-slim AS whisper
ARG WHISPER_CPP_VERSION=v1.9.5
RUN apt-get update && apt-get install -y --no-install-recommends build-essential cmake git ca-certificates \
 && rm -rf /var/lib/apt/lists/*
RUN git clone --depth 1 --branch ${WHISPER_CPP_VERSION} https://github.com/ggml-org/whisper.cpp /whisper
WORKDIR /whisper
# GGML_NATIVE=OFF: portable across VPS CPU models; static so the runtime image needs no extra libs.
RUN cmake -B build -DBUILD_SHARED_LIBS=OFF -DGGML_NATIVE=OFF -DWHISPER_BUILD_TESTS=OFF -DWHISPER_BUILD_EXAMPLES=ON \
 && cmake --build build -j"$(nproc)" --config Release --target whisper-cli

# --- runtime ------------------------------------------------------------------
FROM eclipse-temurin:21-jre-jammy
RUN apt-get update && apt-get install -y --no-install-recommends ffmpeg curl ca-certificates \
 && rm -rf /var/lib/apt/lists/* \
 && useradd --system --create-home --uid 10001 app \
 && mkdir -p /models /data/uploads && chown -R app:app /models /data
COPY --from=whisper /whisper/build/bin/whisper-cli /usr/local/bin/whisper-cli
COPY --from=build /src/target/launchmate-backend-*.jar /app/app.jar
COPY docker-entrypoint.sh /app/docker-entrypoint.sh
RUN chmod +x /app/docker-entrypoint.sh
USER app
ENV WHISPER_MODEL_PATH=/models/ggml-small.bin \
    STORAGE_LOCAL_PATH=/data/uploads \
    JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=60"
EXPOSE 8080
HEALTHCHECK --interval=30s --timeout=5s --start-period=60s CMD curl -fsS http://localhost:8080/actuator/health || exit 1
ENTRYPOINT ["/app/docker-entrypoint.sh"]
