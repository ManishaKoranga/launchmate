"use client";

import { useEffect, useState } from "react";
import { useParams, useRouter } from "next/navigation";
import { CheckCircle2, Circle, Loader2, XCircle } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Alert, AlertDescription } from "@/components/ui/alert";
import { api, errorMessage } from "@/lib/api";
import type { GenerationJob } from "@/lib/site-types";
import { cn } from "@/lib/utils";

const POLL_MS = 1500;

/** Shows the persisted status of each backend step — no simulated progress. */
export function GenerationProgress() {
  const { businessId, jobId } = useParams<{ businessId: string; jobId: string }>();
  const router = useRouter();
  const [job, setJob] = useState<GenerationJob | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [restarting, setRestarting] = useState(false);

  useEffect(() => {
    let timer: ReturnType<typeof setTimeout> | undefined;
    let active = true;
    const poll = () =>
      api<GenerationJob>(`/api/businesses/${businessId}/website/generation-jobs/${jobId}`)
        .then((j) => {
          if (!active) return;
          setJob(j);
          setError(null);
          if (j.status === "SUCCEEDED") router.replace(`/app/b/${businessId}/website`);
          else if (j.status !== "FAILED") timer = setTimeout(poll, POLL_MS);
        })
        .catch((e) => {
          if (!active) return;
          setError(errorMessage(e));
          timer = setTimeout(poll, POLL_MS * 2);
        });
    poll();
    return () => {
      active = false;
      clearTimeout(timer);
    };
  }, [businessId, jobId, router]);

  async function restart(mode: "AI" | "TEMPLATE") {
    setRestarting(true);
    try {
      const next = await api<GenerationJob>(`/api/businesses/${businessId}/website/generation-jobs`, { method: "POST", body: { mode } });
      router.replace(`/app/b/${businessId}/website/generate/${next.id}`);
    } catch (e) {
      setError(errorMessage(e));
      setRestarting(false);
    }
  }

  const failed = job?.status === "FAILED";
  return (
    <div className="mx-auto flex w-full max-w-md flex-col px-4 py-16">
      <h1 className="text-xl font-semibold tracking-tight">
        {failed ? "We couldn't build your website" : job?.status === "SUCCEEDED" ? "Your website is ready" : "Building your website"}
      </h1>
      <p className="mt-1 text-sm text-muted-foreground">
        {job?.mode === "AI" ? "Claude is writing content tailored to your business. This usually takes under a minute." : "Preparing your website from a template."}
      </p>
      <ol className="mt-8 space-y-4" aria-live="polite">
        {(job?.steps ?? []).map((s) => (
          <li key={s.key} className="flex items-center gap-3 text-sm">
            {s.status === "DONE" && <CheckCircle2 className="size-5 text-success" aria-label="Done" />}
            {s.status === "RUNNING" && <Loader2 className="size-5 animate-spin text-primary" aria-label="In progress" />}
            {s.status === "PENDING" && <Circle className="size-5 text-muted-foreground/50" aria-label="Waiting" />}
            {s.status === "FAILED" && <XCircle className="size-5 text-destructive" aria-label="Failed" />}
            <span className={cn(s.status === "PENDING" && "text-muted-foreground", s.status === "RUNNING" && "font-medium")}>{s.label}</span>
          </li>
        ))}
        {!job && !error && <Loader2 className="size-5 animate-spin text-muted-foreground" />}
      </ol>
      {error && <Alert variant="destructive" className="mt-6"><AlertDescription>{error}</AlertDescription></Alert>}
      {failed && (
        <div className="mt-8 grid gap-3">
          {job?.errorMessage && <p className="text-sm text-muted-foreground">{job.errorMessage}</p>}
          <Button onClick={() => void restart("AI")} disabled={restarting}>Try again with AI</Button>
          <Button variant="outline" onClick={() => void restart("TEMPLATE")} disabled={restarting}>Start from template content</Button>
        </div>
      )}
    </div>
  );
}
