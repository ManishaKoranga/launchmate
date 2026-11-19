package com.launchmate.voice.service;

import com.launchmate.shared.exception.BusinessRuleException;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/** Identifies browser recording formats by file signature, never by the client-supplied type. */
public final class AudioFormat {

    public static final int MAX_BYTES = 10 * 1024 * 1024;

    private AudioFormat() {
    }

    /** @return a file extension for the detected container */
    public static String detect(byte[] data) {
        if (data == null || data.length < 12) {
            throw new BusinessRuleException("The recording is empty. Please try again.");
        }
        if (data.length > MAX_BYTES) {
            throw new BusinessRuleException("Voice notes must be smaller than 10 MB (about 2 minutes).");
        }
        if (sig(data, 0, 0x1A, 0x45, 0xDF, 0xA3)) return "webm";          // WebM/Matroska (Chrome, Firefox)
        if (ascii(data, 0, "OggS")) return "ogg";                          // Ogg/Opus
        if (ascii(data, 4, "ftyp")) return "m4a";                          // MP4/M4A (Safari)
        if (ascii(data, 0, "RIFF") && ascii(data, 8, "WAVE")) return "wav";
        if (ascii(data, 0, "ID3") || sig(data, 0, 0xFF) && (data[1] & 0xE0) == 0xE0) return "mp3";
        throw new BusinessRuleException("Unsupported audio format. Please record again.");
    }

    private static boolean sig(byte[] d, int offset, int... bytes) {
        for (int i = 0; i < bytes.length; i++) {
            if ((d[offset + i] & 0xFF) != bytes[i]) return false;
        }
        return true;
    }

    private static boolean ascii(byte[] d, int offset, String s) {
        byte[] b = s.getBytes(StandardCharsets.US_ASCII);
        return Arrays.equals(Arrays.copyOfRange(d, offset, offset + b.length), b);
    }
}
