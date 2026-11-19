package com.launchmate.voice.provider;

import com.launchmate.shared.exception.BusinessRuleException;
import com.launchmate.shared.exception.ExternalServiceException;
import com.launchmate.voice.service.SpeechToText;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Speech-to-text with whisper.cpp running on the same server: audio never leaves LaunchMate and
 * there is no per-minute cost. ffmpeg normalises any browser format to 16 kHz mono WAV first.
 * Processes are started with argument lists (no shell), so file names cannot inject commands.
 */
@Component
public class WhisperCppSpeechToText implements SpeechToText {

    private static final Logger log = LoggerFactory.getLogger(WhisperCppSpeechToText.class);
    private static final Pattern LANGUAGE = Pattern.compile("auto-detected language: (\\w+)");
    /** Whisper emits these for silence/noise; they are not speech. */
    private static final Pattern NON_SPEECH = Pattern.compile("\\[[^\\]]*\\]|\\([^)]*(music|noise|silence|applause)[^)]*\\)",
            Pattern.CASE_INSENSITIVE);

    private final VoiceProperties props;
    private final boolean available;

    public WhisperCppSpeechToText(VoiceProperties props) {
        this.props = props;
        this.available = props.enabled() && checkInstallation();
        log.info("Local speech-to-text {}", available ? "available (model " + props.modelPath() + ")" : "not available");
    }

    @Override
    public boolean isAvailable() {
        return available;
    }

    @Override
    public Transcript transcribe(Path audioFile) {
        if (!available) {
            throw ExternalServiceException.notConfigured("Voice transcription");
        }
        Path work = null;
        try {
            work = Files.createTempDirectory("lm-voice-");
            Path wav = work.resolve("audio.wav");
            run(List.of(props.ffmpegBinary(), "-nostdin", "-hide_banner", "-loglevel", "error", "-i",
                    audioFile.toString(), "-t", String.valueOf(props.maxSeconds()),
                    "-af", "dynaudnorm=f=150:g=15", "-ar", "16000", "-ac", "1",
                    "-c:a", "pcm_s16le", wav.toString()), "audio conversion");
            if (!Files.exists(wav) || Files.size(wav) < 1000) {
                throw new BusinessRuleException("We couldn't read that recording. Please try again.");
            }
            Path outBase = work.resolve("transcript");
            List<String> whisper = new ArrayList<>(List.of(props.whisperBinary(), "-m", props.modelPath(),
                    "-f", wav.toString(), "-l", "auto", "-t", String.valueOf(props.threads()), "-nt", "-otxt",
                    "-of", outBase.toString()));
            if (props.initialPrompt() != null && !props.initialPrompt().isBlank()) {
                whisper.addAll(List.of("--prompt", props.initialPrompt()));
            }
            ProcessResult result = run(whisper, "transcription");
            Path txt = work.resolve("transcript.txt");
            String raw = Files.exists(txt) ? Files.readString(txt, StandardCharsets.UTF_8) : result.stdout();
            String text = clean(raw);
            if (text.isBlank()) {
                throw new BusinessRuleException("We couldn't hear any speech. Please try again, a little closer to the microphone.");
            }
            Matcher m = LANGUAGE.matcher(result.stderr() + result.stdout());
            return new Transcript(text, m.find() ? m.group(1) : null);
        } catch (IOException e) {
            throw ExternalServiceException.failed("Voice transcription", e);
        } finally {
            deleteQuietly(work);
        }
    }

    static String clean(String raw) {
        return NON_SPEECH.matcher(raw == null ? "" : raw).replaceAll(" ").replaceAll("\\s+", " ").trim();
    }

    private record ProcessResult(String stdout, String stderr) {
    }

    private ProcessResult run(List<String> command, String step) throws IOException {
        Path out = Files.createTempFile("lm-proc-", ".out");
        Path err = Files.createTempFile("lm-proc-", ".err");
        try {
            Process p = new ProcessBuilder(command).redirectOutput(out.toFile()).redirectError(err.toFile()).start();
            if (!p.waitFor(props.timeout().toMillis(), TimeUnit.MILLISECONDS)) {
                p.destroyForcibly();
                throw ExternalServiceException.failed("Voice transcription", new IllegalStateException(step + " timed out"));
            }
            String stderr = Files.readString(err);
            if (p.exitValue() != 0) {
                log.warn("{} failed (exit {}): {}", step, p.exitValue(), stderr.lines().limit(5).toList());
                throw new BusinessRuleException("We couldn't process that recording. Please try again.");
            }
            return new ProcessResult(Files.readString(out), stderr);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw ExternalServiceException.failed("Voice transcription", e);
        } finally {
            Files.deleteIfExists(out);
            Files.deleteIfExists(err);
        }
    }

    private boolean checkInstallation() {
        if (props.modelPath() == null || !Files.isRegularFile(Path.of(props.modelPath()))) {
            log.warn("Whisper model not found at {}", props.modelPath());
            return false;
        }
        return canRun(props.ffmpegBinary(), "-version") && canRun(props.whisperBinary(), "--help");
    }

    private static boolean canRun(String... command) {
        try {
            Process p = new ProcessBuilder(command).redirectErrorStream(true)
                    .redirectOutput(ProcessBuilder.Redirect.DISCARD).start();
            return p.waitFor(10, TimeUnit.SECONDS) && p.exitValue() == 0;
        } catch (IOException e) {
            log.warn("{} is not installed", command[0]);
            return false;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }

    private static void deleteQuietly(Path dir) {
        if (dir == null) return;
        try (var files = Files.walk(dir)) {
            List<Path> all = new ArrayList<>(files.toList());
            all.sort((a, b) -> b.getNameCount() - a.getNameCount());
            for (Path f : all) Files.deleteIfExists(f);
        } catch (IOException ignored) {
            // temp directory; the OS will reclaim it
        }
    }
}
