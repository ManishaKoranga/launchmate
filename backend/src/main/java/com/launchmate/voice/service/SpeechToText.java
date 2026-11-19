package com.launchmate.voice.service;

import java.nio.file.Path;

/**
 * Port for speech-to-text. The voice module depends on this interface; adapters (local whisper.cpp
 * today, a cloud provider later) can be swapped without touching callers.
 */
public interface SpeechToText {

    Transcript transcribe(Path audioFile);

    boolean isAvailable();

    /** @param language ISO code detected by the engine (e.g. "hi", "en"), or null if unknown */
    record Transcript(String text, String language) {
    }
}
