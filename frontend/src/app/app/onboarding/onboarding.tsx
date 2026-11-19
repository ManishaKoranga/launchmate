"use client";

import { useEffect, useRef, useState } from "react";
import { useRouter } from "next/navigation";
import { ArrowLeft, ArrowUp, CheckCircle2, Circle, Loader2, Plus, Sparkles, Trash2 } from "lucide-react";
import { toast } from "sonner";
import { Logo } from "@/components/marketing/logo";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import { Input } from "@/components/ui/input";
import { Textarea } from "@/components/ui/textarea";
import { Alert, AlertDescription } from "@/components/ui/alert";
import { BusinessProfileForm } from "@/components/app/business-profile-form";
import { FullPageError, FullPageSpinner } from "@/components/app/states";
import { VoiceNoteButton } from "@/components/app/voice-note-button";
import { api, ApiError, errorMessage } from "@/lib/api";
import { CATEGORY_LABELS, type Business, type BusinessProfile } from "@/lib/types";
import type { GenerationJob } from "@/lib/site-types";
import { cn } from "@/lib/utils";

type Offering = {
  kind: "PRODUCT" | "SERVICE";
  name: string;
  description?: string | null;
  price?: number | null;
  priceNote?: string | null;
  visible?: boolean;
};

type OnboardingState = {
  id: string;
  profile: Partial<BusinessProfile> | null;
  offerings: Offering[];
  missingFields: string[];
  readyForReview: boolean;
  messages: { role: "user" | "assistant"; content: string; createdAt: string }[];
  aiAvailable: boolean;
};

const EXAMPLES = [
  "Main Deoband mein home bakery chalati hoon — customised cakes, cupcakes aur brownies.",
  "I run a beauty parlour in Lucknow offering bridal makeup, facials and threading.",
  "मैं जयपुर में बच्चों के लिए गणित और विज्ञान की ट्यूशन क्लास चलाता हूँ।",
];

const FIELD_LABELS: Record<string, string> = {
  name: "Business name",
  category: "Type of business",
  city: "City",
  contact: "Phone or WhatsApp",
};

type Step = "chat" | "review" | "manual";

export function Onboarding() {
  const router = useRouter();
  const [state, setState] = useState<OnboardingState | null>(null);
  const [loadError, setLoadError] = useState<string | null>(null);
  const [step, setStep] = useState<Step>("chat");

  useEffect(() => {
    api<OnboardingState>("/api/onboarding/conversations/latest")
      .catch((e) => {
        if (e instanceof ApiError && e.status === 404) {
          return api<OnboardingState>("/api/onboarding/conversations", { method: "POST" });
        }
        throw e;
      })
      .then((s) => {
        setState(s);
        if (!s.aiAvailable) setStep("manual");
      })
      .catch((e) => setLoadError(errorMessage(e)));
  }, []);

  /** After the business exists, kick off website generation and go to the real progress screen. */
  async function startWebsite(business: Business, useAi: boolean) {
    try {
      const job = await api<GenerationJob>(`/api/businesses/${business.id}/website/generation-jobs`, {
        method: "POST",
        body: { mode: useAi ? "AI" : "TEMPLATE" },
      });
      router.replace(`/app/b/${business.id}/website/generate/${job.id}`);
    } catch (e) {
      toast.error(`Your business was saved, but we couldn't start building the website: ${errorMessage(e)}`);
      router.replace(`/app/b/${business.id}/website`);
    }
  }

  if (loadError) return <FullPageError message={loadError} />;
  if (!state) return <FullPageSpinner />;

  return (
    <div className="flex min-h-svh flex-col">
      <header className="flex h-14 items-center justify-between border-b px-4 sm:px-6">
        <Logo href="/app" />
        {step === "chat" && (
          <Button variant="ghost" size="sm" onClick={() => setStep("manual")}>Prefer a form?</Button>
        )}
      </header>
      {step === "chat" && <ChatStep state={state} setState={setState} onReview={() => setStep("review")} />}
      {step === "review" && (
        <ReviewStep state={state} onBack={() => setStep("chat")} onCreated={(b) => startWebsite(b, state.aiAvailable)} />
      )}
      {step === "manual" && (
        <ManualStep
          aiAvailable={state.aiAvailable}
          onBack={state.aiAvailable ? () => setStep("chat") : undefined}
          onCreated={(b) => startWebsite(b, state.aiAvailable)}
        />
      )}
    </div>
  );
}

function ChatStep({
  state,
  setState,
  onReview,
}: {
  state: OnboardingState;
  setState: (s: OnboardingState) => void;
  onReview: () => void;
}) {
  const [text, setText] = useState("");
  const [sending, setSending] = useState(false);
  const [pending, setPending] = useState<string | null>(null);
  const [error, setError] = useState<string | null>(null);
  const endRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    endRef.current?.scrollIntoView({ behavior: "smooth", block: "end" });
  }, [state.messages.length, pending]);

  async function send(message: string) {
    const trimmed = message.trim();
    if (!trimmed || sending) return;
    setSending(true);
    setPending(trimmed);
    setText("");
    setError(null);
    try {
      setState(await api<OnboardingState>(`/api/onboarding/conversations/${state.id}/messages`, {
        method: "POST",
        body: { text: trimmed },
      }));
    } catch (e) {
      setText(trimmed); // give the owner their words back
      setError(errorMessage(e));
    } finally {
      setPending(null);
      setSending(false);
    }
  }

  const empty = state.messages.length === 0 && !pending;

  return (
    <div className="mx-auto grid w-full max-w-6xl flex-1 gap-6 px-4 py-6 sm:px-6 lg:grid-cols-[1fr_22rem]">
      <section className="flex min-h-[60vh] flex-col rounded-2xl border bg-card" aria-label="Conversation">
        <div className="flex-1 space-y-4 overflow-y-auto p-4 sm:p-6" aria-live="polite">
          {empty ? (
            <div className="mx-auto max-w-lg py-8 text-center">
              <Sparkles className="mx-auto size-6 text-primary" />
              <h1 className="mt-3 text-xl font-semibold">Tell us about your business</h1>
              <p className="mt-2 text-sm text-muted-foreground">
                Write or speak the way you talk — English, Hindi or Hinglish. What do you sell, where are you, and who are your customers?
              </p>
              <div className="mt-6 grid gap-2 text-left">
                {EXAMPLES.map((ex) => (
                  <button
                    key={ex}
                    onClick={() => setText(ex)}
                    className="rounded-xl border px-4 py-3 text-sm text-muted-foreground transition-colors hover:border-primary/40 hover:bg-accent hover:text-foreground"
                  >
                    {ex}
                  </button>
                ))}
              </div>
            </div>
          ) : (
            <>
              {state.messages.map((m, i) => <Bubble key={i} role={m.role} text={m.content} />)}
              {pending && <Bubble role="user" text={pending} />}
              {pending && (
                <div className="flex items-center gap-2 text-sm text-muted-foreground">
                  <Loader2 className="size-4 animate-spin" /> Understanding your business…
                </div>
              )}
            </>
          )}
          <div ref={endRef} />
        </div>
        {error && (
          <Alert variant="destructive" className="mx-4 mb-2 w-auto">
            <AlertDescription>{error}</AlertDescription>
          </Alert>
        )}
        <form
          className="flex items-end gap-2 border-t p-3"
          onSubmit={(e) => {
            e.preventDefault();
            void send(text);
          }}
        >
          <Textarea
            value={text}
            onChange={(e) => setText(e.target.value)}
            onKeyDown={(e) => {
              if (e.key === "Enter" && !e.shiftKey) {
                e.preventDefault();
                void send(text);
              }
            }}
            placeholder="Describe your business…"
            rows={2}
            maxLength={4000}
            className="max-h-40 min-h-11 resize-none"
            aria-label="Message"
          />
          <VoiceNoteButton disabled={sending} onTranscript={(t) => setText((cur) => (cur.trim() ? `${cur.trim()} ${t}` : t))} />
          <Button type="submit" size="icon-lg" disabled={sending || !text.trim()} aria-label="Send">
            {sending ? <Loader2 className="animate-spin" /> : <ArrowUp />}
          </Button>
        </form>
      </section>

      <aside className="h-fit rounded-2xl border bg-card p-5 lg:sticky lg:top-6" aria-label="What LaunchMate understood">
        <h2 className="text-sm font-semibold">What we understood</h2>
        <dl className="mt-4 grid gap-3 text-sm">
          <Fact label="Business name" value={state.profile?.name} />
          <Fact label="Type" value={state.profile?.category ? CATEGORY_LABELS[state.profile.category] : undefined} />
          <Fact label="City" value={[state.profile?.area, state.profile?.city].filter(Boolean).join(", ") || undefined} />
          <Fact label="Contact" value={state.profile?.whatsapp ?? state.profile?.phone ?? undefined} />
          {state.profile?.targetCustomers && <Fact label="Customers" value={state.profile.targetCustomers} />}
        </dl>
        {state.offerings.length > 0 && (
          <div className="mt-5">
            <p className="text-xs font-medium text-muted-foreground">Products & services</p>
            <ul className="mt-2 space-y-1 text-sm">
              {state.offerings.map((o, i) => (
                <li key={i} className="flex justify-between gap-2">
                  <span className="truncate">{o.name}</span>
                  {o.price != null && <span className="shrink-0 text-muted-foreground">₹{o.price}</span>}
                </li>
              ))}
            </ul>
          </div>
        )}
        <div className="mt-6 border-t pt-4">
          {state.readyForReview ? (
            <p className="mb-3 flex items-center gap-1.5 text-sm text-success"><CheckCircle2 className="size-4" /> Ready to build</p>
          ) : (
            <p className="mb-3 text-xs text-muted-foreground">
              Still needed: {state.missingFields.map((f) => FIELD_LABELS[f] ?? f).join(", ")}
            </p>
          )}
          <Button className="w-full" disabled={!state.readyForReview} onClick={onReview}>Review details</Button>
        </div>
      </aside>
    </div>
  );
}

function Bubble({ role, text }: { role: "user" | "assistant"; text: string }) {
  return (
    <div className={cn("flex", role === "user" ? "justify-end" : "justify-start")}>
      <p
        className={cn(
          "max-w-[85%] whitespace-pre-wrap rounded-2xl px-4 py-2.5 text-sm",
          role === "user" ? "rounded-br-md bg-primary text-primary-foreground" : "rounded-bl-md bg-muted",
        )}
      >
        {text}
      </p>
    </div>
  );
}

function Fact({ label, value }: { label: string; value?: string | null }) {
  return (
    <div className="flex items-start gap-2">
      {value ? <CheckCircle2 className="mt-0.5 size-4 shrink-0 text-success" /> : <Circle className="mt-0.5 size-4 shrink-0 text-muted-foreground" />}
      <div className="min-w-0">
        <dt className="text-xs text-muted-foreground">{label}</dt>
        <dd className={cn("truncate", !value && "text-muted-foreground")}>{value || "Not yet"}</dd>
      </div>
    </div>
  );
}

function ReviewStep({
  state,
  onBack,
  onCreated,
}: {
  state: OnboardingState;
  onBack: () => void;
  onCreated: (b: Business) => Promise<void>;
}) {
  const [offerings, setOfferings] = useState<Offering[]>(state.offerings);
  const update = (i: number, patch: Partial<Offering>) =>
    setOfferings((os) => os.map((o, j) => (j === i ? { ...o, ...patch } : o)));

  return (
    <div className="mx-auto w-full max-w-3xl px-4 py-8 sm:px-6">
      <Button variant="ghost" size="sm" onClick={onBack} className="-ml-2"><ArrowLeft /> Back to chat</Button>
      <h1 className="mt-4 text-2xl font-semibold tracking-tight">Check your details</h1>
      <p className="mt-1 text-muted-foreground">Fix anything we got wrong. This is what your website will be built from.</p>

      <div className="mt-8 rounded-2xl border bg-card p-6">
        <h2 className="mb-4 text-sm font-medium">Products & services</h2>
        {offerings.length === 0 && <p className="mb-3 text-sm text-muted-foreground">None yet — you can add them later too.</p>}
        <div className="grid gap-3">
          {offerings.map((o, i) => (
            <div key={i} className="grid grid-cols-[1fr_6.5rem_auto] items-start gap-2 sm:grid-cols-[1fr_7rem_9rem_auto]">
              <Input aria-label="Name" value={o.name} maxLength={120} onChange={(e) => update(i, { name: e.target.value })} />
              <Input
                aria-label="Price in rupees"
                inputMode="decimal"
                placeholder="₹ Price"
                value={o.price ?? ""}
                onChange={(e) => {
                  const v = e.target.value.replace(/[^\d.]/g, "");
                  update(i, { price: v === "" ? null : Number(v) });
                }}
              />
              <Input aria-label="Price note" className="hidden sm:block" placeholder="e.g. per kg" maxLength={60} value={o.priceNote ?? ""} onChange={(e) => update(i, { priceNote: e.target.value })} />
              <Button variant="ghost" size="icon" aria-label={`Remove ${o.name}`} onClick={() => setOfferings((os) => os.filter((_, j) => j !== i))}>
                <Trash2 />
              </Button>
            </div>
          ))}
        </div>
        {offerings.length < 20 && (
          <Button variant="outline" size="sm" className="mt-3" onClick={() => setOfferings((os) => [...os, { kind: "PRODUCT", name: "" }])}>
            <Plus /> Add item
          </Button>
        )}
      </div>

      <div className="mt-6 rounded-2xl border bg-card p-6">
        <BusinessProfileForm
          initial={state.profile ?? undefined}
          submitLabel={state.aiAvailable ? "Build my website" : "Create my website"}
          onSubmit={async (profile) => {
            const cleaned = offerings
              .filter((o) => o.name.trim())
              .map((o) => ({ ...o, name: o.name.trim(), priceNote: o.priceNote?.trim() || undefined }));
            const business = await api<Business>(`/api/onboarding/conversations/${state.id}/complete`, {
              method: "POST",
              body: { profile, offerings: cleaned },
            });
            await onCreated(business);
          }}
        />
      </div>
    </div>
  );
}

function ManualStep({
  aiAvailable,
  onBack,
  onCreated,
}: {
  aiAvailable: boolean;
  onBack?: () => void;
  onCreated: (b: Business) => Promise<void>;
}) {
  return (
    <div className="mx-auto w-full max-w-2xl px-4 py-8 sm:px-6">
      {onBack && <Button variant="ghost" size="sm" onClick={onBack} className="-ml-2"><ArrowLeft /> Describe it in chat instead</Button>}
      <h1 className="mt-4 text-2xl font-semibold tracking-tight">Tell us about your business</h1>
      {!aiAvailable && (
        <Alert className="mt-4">
          <AlertDescription>
            The AI assistant isn&apos;t available right now, so we&apos;ll use a simple form. Your website will start from template content you can edit.
          </AlertDescription>
        </Alert>
      )}
      <div className="mt-6 rounded-2xl border bg-card p-6">
        <BusinessProfileForm
          submitLabel="Create my website"
          onSubmit={async (profile) => {
            const created = await api<Business>("/api/businesses", { method: "POST", body: profile });
            await api(`/api/businesses/${created.id}/confirm-profile`, { method: "POST" });
            await onCreated(created);
          }}
        />
      </div>
      <p className="mt-3 text-xs text-muted-foreground">
        <Badge variant="secondary" className="mr-1">Tip</Badge> You can add products, prices and opening hours after this step.
      </p>
    </div>
  );
}
