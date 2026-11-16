"use client";

import { useState } from "react";
import { Loader2, Sparkles } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Textarea } from "@/components/ui/textarea";
import { Alert, AlertDescription } from "@/components/ui/alert";
import { api, errorMessage } from "@/lib/api";
import type { SiteConfig } from "@/lib/site-types";

const SUGGESTIONS = [
  "Make the hero headline warmer and more inviting",
  "Add an FAQ about ordering ahead for festivals",
  "Use a deep green brand colour",
  "Make the About section shorter",
];

export type AiProposal = { config: SiteConfig; baseRevision: number; instruction: string };

type Props = { businessId: string; disabledReason?: string; onProposal: (p: AiProposal) => void };

export function AiPanel({ businessId, disabledReason, onProposal }: Props) {
  const [instruction, setInstruction] = useState("");
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  async function submit() {
    const text = instruction.trim();
    if (!text) return;
    setLoading(true);
    setError(null);
    try {
      const p = await api<{ config: SiteConfig; baseRevision: number }>(`/api/businesses/${businessId}/website/draft/ai-edit`, {
        method: "POST",
        body: { instruction: text },
      });
      onProposal({ ...p, instruction: text });
      setInstruction("");
    } catch (e) {
      setError(errorMessage(e));
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="grid gap-3">
      <p className="text-sm text-muted-foreground">
        Describe a change in your own words. You&apos;ll see a preview before anything is saved.
      </p>
      {disabledReason && <Alert><AlertDescription>{disabledReason}</AlertDescription></Alert>}
      <Textarea
        value={instruction}
        onChange={(e) => setInstruction(e.target.value)}
        rows={4}
        maxLength={1000}
        placeholder="e.g. Mention that we use eggless options on request"
        disabled={loading || !!disabledReason}
        aria-label="Describe your change"
      />
      <div className="flex flex-wrap gap-1.5">
        {SUGGESTIONS.map((s) => (
          <button
            key={s}
            type="button"
            disabled={loading || !!disabledReason}
            onClick={() => setInstruction(s)}
            className="rounded-full border px-2.5 py-1 text-xs text-muted-foreground hover:bg-muted hover:text-foreground disabled:opacity-50"
          >
            {s}
          </button>
        ))}
      </div>
      <Button onClick={() => void submit()} disabled={loading || !instruction.trim() || !!disabledReason}>
        {loading ? <Loader2 className="animate-spin" /> : <Sparkles />} {loading ? "Working on it…" : "Preview change"}
      </Button>
      {error && <Alert variant="destructive"><AlertDescription>{error}</AlertDescription></Alert>}
    </div>
  );
}
