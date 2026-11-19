package com.launchmate.voice.controller;

import com.launchmate.shared.security.AuthenticatedUser;
import com.launchmate.voice.service.SpeechToText;
import com.launchmate.voice.service.TranscriptionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/api/voice")
@Tag(name = "Voice")
public class VoiceController {

    private final TranscriptionService transcription;

    public VoiceController(TranscriptionService transcription) {
        this.transcription = transcription;
    }

    @Operation(summary = "Whether voice notes can be transcribed on this server")
    @GetMapping("/status")
    public Map<String, Boolean> status() {
        return Map.of("available", transcription.isAvailable());
    }

    @Operation(summary = "Transcribe a voice note (webm/ogg/m4a/wav/mp3, max 10 MB). Audio is not stored.")
    @PostMapping(value = "/transcriptions", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public SpeechToText.Transcript transcribe(@AuthenticationPrincipal AuthenticatedUser user,
                                              @RequestPart("file") MultipartFile file) throws IOException {
        return transcription.transcribe(user.id(), file.getBytes());
    }
}
