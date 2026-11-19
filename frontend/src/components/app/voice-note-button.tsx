"use client";

import { useEffect, useRef, useState } from "react";
import { Loader2, Mic, Send, X } from "lucide-react";
import { toast } from "sonner";
import { Button } from "@/components/ui/button";
import { Tooltip, TooltipContent, TooltipTrigger } from "@/components/ui/tooltip";
import { api, errorMessage } from "@/lib/api";

const MAX_SECONDS = 120;
const MIME_CANDIDATES = ["audio/webm;codecs=opus", "audio/webm", "audio/mp4", "audio/ogg;codecs=opus"];

type State = "idle" | "recording" | "transcribing";

/**
 * Records a voice note in the browser and returns the server-side transcript. The transcript is
 * handed to the caller so the owner can check and correct it before anything is sent to the AI.
 * Renders nothing when the browser can't record or the server can't transcribe — text input
 * always remains available.
 */
export function VoiceNoteButton({ onTranscript, disabled }: { onTranscript: (text: string) => void; disabled?: boolean }) {
  const [available, setAvailable] = useState(false);
  const [state, setState] = useState<State>("idle");
  const [seconds, setSeconds] = useState(0);
  const recorder = useRef<MediaRecorder | null>(null);
  const chunks = useRef<Blob[]>([]);
  const cancelled = useRef(false);
  const timer = useRef<ReturnType<typeof setInterval> | null>(null);

  useEffect(() => {
    if (typeof window === "undefined" || !("MediaRecorder" in window) || !navigator.mediaDevices?.getUserMedia) return;
    api<{ available: boolean }>("/api/voice/status").then((s) => setAvailable(s.available), () => setAvailable(false));
  }, []);

  useEffect(() => () => stopTracks(), []);

  function stopTracks() {
    if (timer.current) clearInterval(timer.current);
    recorder.current?.stream.getTracks().forEach((t) => t.stop());
  }

  async function start() {
    let stream: MediaStream;
    try {
      stream = await navigator.mediaDevices.getUserMedia({ audio: { echoCancellation: true, noiseSuppression: true } });
    } catch (e) {
      const denied = e instanceof DOMException && (e.name === "NotAllowedError" || e.name === "SecurityError");
      toast.error(denied ? "Microphone access is blocked. Allow it in your browser settings, or type instead." : "No microphone found. You can type instead.");
      return;
    }
    const mimeType = MIME_CANDIDATES.find((m) => MediaRecorder.isTypeSupported(m));
    const rec = new MediaRecorder(stream, mimeType ? { mimeType } : undefined);
    chunks.current = [];
    cancelled.current = false;
    rec.ondataavailable = (e) => e.data.size > 0 && chunks.current.push(e.data);
    rec.onstop = () => {
      stopTracks();
      if (cancelled.current) {
        setState("idle");
        return;
      }
      void upload(new Blob(chunks.current, { type: rec.mimeType || "audio/webm" }));
    };
    recorder.current = rec;
    rec.start(1000);
    setSeconds(0);
    setState("recording");
    const startedAt = Date.now();
    timer.current = setInterval(() => {
      const elapsed = Math.floor((Date.now() - startedAt) / 1000);
      setSeconds(elapsed);
      if (elapsed >= MAX_SECONDS && rec.state === "recording") rec.stop();
    }, 500);
  }

  function stop(cancel: boolean) {
    cancelled.current = cancel;
    if (recorder.current?.state === "recording") recorder.current.stop();
  }

  async function upload(blob: Blob) {
    if (blob.size < 1000) {
      toast.error("That recording was too short. Hold on a moment longer.");
      setState("idle");
      return;
    }
    setState("transcribing");
    const ext = blob.type.includes("mp4") ? "m4a" : blob.type.includes("ogg") ? "ogg" : "webm";
    const fd = new FormData();
    fd.append("file", blob, `voice-note.${ext}`);
    try {
      const t = await api<{ text: string; language?: string }>("/api/voice/transcriptions", { method: "POST", body: fd });
      onTranscript(t.text);
    } catch (e) {
      toast.error(errorMessage(e));
    } finally {
      setState("idle");
    }
  }

  if (!available) return null;

  if (state === "recording") {
    const mm = String(Math.floor(seconds / 60)).padStart(1, "0");
    const ss = String(seconds % 60).padStart(2, "0");
    return (
      <div className="flex items-center gap-1.5 rounded-lg border bg-background px-2 py-1" role="status" aria-live="polite">
        <span className="size-2 animate-pulse rounded-full bg-destructive" aria-hidden />
        <span className="w-10 text-sm tabular-nums" aria-label={`Recording, ${seconds} seconds`}>{mm}:{ss}</span>
        <Button type="button" size="icon-sm" variant="ghost" onClick={() => stop(true)} aria-label="Cancel recording"><X /></Button>
        <Button type="button" size="icon-sm" onClick={() => stop(false)} aria-label="Stop and transcribe"><Send /></Button>
      </div>
    );
  }

  return (
    <Tooltip>
      <TooltipTrigger asChild>
        <Button
          type="button"
          size="icon-lg"
          variant="outline"
          disabled={disabled || state === "transcribing"}
          onClick={() => void start()}
          aria-label={state === "transcribing" ? "Transcribing voice note" : "Record a voice note"}
        >
          {state === "transcribing" ? <Loader2 className="animate-spin" /> : <Mic />}
        </Button>
      </TooltipTrigger>
      <TooltipContent>{state === "transcribing" ? "Transcribing…" : "Speak instead of typing (up to 2 minutes)"}</TooltipContent>
    </Tooltip>
  );
}
