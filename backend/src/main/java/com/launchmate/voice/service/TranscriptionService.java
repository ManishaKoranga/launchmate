package com.launchmate.voice.service;

import com.launchmate.shared.exception.ExternalServiceException;
import com.launchmate.shared.exception.RateLimitedException;
import com.launchmate.shared.ratelimit.SlidingWindowLimiter;
import com.launchmate.voice.provider.VoiceProperties;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/**
 * Transcribes a voice note. Audio is kept only for the duration of the request and then deleted;
 * the transcript is returned to the owner to check and correct before it is used.
 */
@Service
public class TranscriptionService {

    static final int PER_USER_PER_HOUR = 60;

    private final SpeechToText stt;
    private final SlidingWindowLimiter limiter;
    private final Semaphore slots;

    public TranscriptionService(SpeechToText stt, VoiceProperties props, Clock clock) {
        this.stt = stt;
        this.limiter = new SlidingWindowLimiter(Duration.ofHours(1), clock);
        this.slots = new Semaphore(props.concurrency());
    }

    public boolean isAvailable() {
        return stt.isAvailable();
    }

    public SpeechToText.Transcript transcribe(UUID userId, byte[] audio) {
        String ext = AudioFormat.detect(audio);
        if (!limiter.tryAcquire(userId.toString(), PER_USER_PER_HOUR)) {
            throw new RateLimitedException("You've sent a lot of voice notes this hour. Please type, or try again later.");
        }
        boolean acquired = false;
        Path file = null;
        try {
            acquired = slots.tryAcquire(30, TimeUnit.SECONDS);
            if (!acquired) {
                throw ExternalServiceException.failed("Voice transcription", new IllegalStateException("busy"));
            }
            file = Files.createTempFile("lm-voice-", "." + ext);
            Files.write(file, audio);
            return stt.transcribe(file);
        } catch (IOException e) {
            throw ExternalServiceException.failed("Voice transcription", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw ExternalServiceException.failed("Voice transcription", e);
        } finally {
            if (acquired) slots.release();
            if (file != null) {
                try {
                    Files.deleteIfExists(file);
                } catch (IOException ignored) {
                    // temp file
                }
            }
        }
    }
}
