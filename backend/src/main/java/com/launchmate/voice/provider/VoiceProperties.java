package com.launchmate.voice.provider;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

/**
 * Local speech-to-text configuration.
 *
 * @param maxSeconds  longer recordings are cut at this length
 * @param concurrency simultaneous transcriptions (CPU-bound — keep low on a small VPS)
 * @param initialPrompt text that primes Whisper's vocabulary/style; a Hinglish sentence makes it write
 *                      romanised Hinglish ("aur", "hai") instead of anglicising it
 */
@ConfigurationProperties(prefix = "launchmate.voice")
public record VoiceProperties(boolean enabled, String whisperBinary, String ffmpegBinary, String modelPath,
                              int threads, Duration timeout, int maxSeconds, int concurrency,
                              String initialPrompt) {

    public VoiceProperties {
        whisperBinary = whisperBinary == null ? "whisper-cli" : whisperBinary;
        ffmpegBinary = ffmpegBinary == null ? "ffmpeg" : ffmpegBinary;
        threads = threads <= 0 ? 4 : threads;
        timeout = timeout == null ? Duration.ofSeconds(90) : timeout;
        maxSeconds = maxSeconds <= 0 ? 120 : maxSeconds;
        concurrency = concurrency <= 0 ? 2 : concurrency;
    }
}
