package com.launchmate.voice;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.launchmate.support.ApiClient;
import com.launchmate.support.IntegrationTest;
import com.launchmate.voice.service.SpeechToText;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class VoiceIntegrationTest extends IntegrationTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;

    /** Minimal WebM/EBML header followed by padding — enough for signature detection. */
    static byte[] webm() {
        byte[] b = new byte[2048];
        b[0] = 0x1A; b[1] = 0x45; b[2] = (byte) 0xDF; b[3] = (byte) 0xA3;
        return b;
    }

    @Test
    void transcribesAndDeletesTheTemporaryAudio() throws Exception {
        ApiClient user = new ApiClient(mvc, json).registerNewUser();
        AtomicReference<Path> seen = new AtomicReference<>();
        when(speechToText.transcribe(any())).thenAnswer(inv -> {
            Path p = inv.getArgument(0);
            seen.set(p);
            assertThat(Files.exists(p)).isTrue();
            assertThat(p.toString()).endsWith(".webm");
            return new SpeechToText.Transcript("Main Deoband mein bakery chalati hoon", "hi");
        });
        user.multipart("/api/voice/transcriptions", new MockMultipartFile("file", "note.webm", "audio/webm", webm()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.text").value("Main Deoband mein bakery chalati hoon"))
                .andExpect(jsonPath("$.language").value("hi"));
        assertThat(Files.exists(seen.get())).as("audio is not retained").isFalse();
    }

    @Test
    void rejectsNonAudioAndAnonymousUploads() throws Exception {
        ApiClient user = new ApiClient(mvc, json).registerNewUser();
        user.multipart("/api/voice/transcriptions",
                        new MockMultipartFile("file", "x.webm", "audio/webm", "#!/bin/sh\nrm -rf /\n".getBytes()))
                .andExpect(status().isUnprocessableEntity());
        mvc.perform(multipart("/api/voice/transcriptions")
                        .file(new MockMultipartFile("file", "note.webm", "audio/webm", webm())).with(csrf()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void statusReflectsEngineAvailability() throws Exception {
        ApiClient user = new ApiClient(mvc, json).registerNewUser();
        when(speechToText.isAvailable()).thenReturn(false);
        user.get("/api/voice/status").andExpect(jsonPath("$.available").value(false));
    }
}
