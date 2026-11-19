package com.launchmate.voice;

import com.launchmate.voice.provider.WhisperCppSpeechToText;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;

class WhisperOutputCleaningTest {

    static String clean(String s) throws Exception {
        Method m = WhisperCppSpeechToText.class.getDeclaredMethod("clean", String.class);
        m.setAccessible(true);
        return (String) m.invoke(null, s);
    }

    @Test
    void stripsNonSpeechMarkersAndWhitespace() throws Exception {
        assertThat(clean("[BLANK_AUDIO]\n")).isEmpty();
        assertThat(clean(" (music playing)  Mera naam Meera hai.\n [Music] ")).isEqualTo("Mera naam Meera hai.");
        assertThat(clean("मैं देवबंद में बेकरी चलाती हूँ।")).isEqualTo("मैं देवबंद में बेकरी चलाती हूँ।");
    }
}
