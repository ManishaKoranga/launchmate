#!/bin/sh
# Downloads the Whisper model into the persistent /models volume on first start (verified by checksum),
# then starts the application. Voice notes stay disabled — text still works — if the download fails.
set -eu

MODEL="${WHISPER_MODEL_PATH:-/models/ggml-small.bin}"
URL="${WHISPER_MODEL_URL:-https://huggingface.co/ggerganov/whisper.cpp/resolve/main/ggml-small.bin}"
SHA1="${WHISPER_MODEL_SHA1:-55356645c2b361a969dfd0ef2c5a50d530afd8d5}"

if [ "${VOICE_ENABLED:-true}" = "true" ] && [ ! -f "$MODEL" ]; then
  echo "Downloading Whisper model to $MODEL ..."
  if curl -fsSL --retry 3 -o "$MODEL.part" "$URL" && echo "$SHA1  $MODEL.part" | sha1sum -c - >/dev/null; then
    mv "$MODEL.part" "$MODEL"
    echo "Whisper model ready."
  else
    rm -f "$MODEL.part"
    echo "WARNING: Whisper model download/verification failed; voice notes will be unavailable." >&2
  fi
fi

exec java -jar /app/app.jar
