"use client";

import { useState } from "react";
import { CheckCircle2, Loader2 } from "lucide-react";
import type { PublicProduct } from "@/lib/site-types";

type Props = { slug: string; products: PublicProduct[]; submitLabel: string; disabled?: boolean };
type Errors = Record<string, string>;

const input =
  "h-11 w-full rounded-[calc(var(--site-radius)*0.6)] border border-[var(--site-border)] bg-[var(--site-bg)] px-3 text-sm outline-none focus:border-[var(--site-primary)] focus:ring-2 focus:ring-[var(--site-primary)]/20 disabled:opacity-60";

/** Customer inquiry form for public sites. Anonymous: no session, no CSRF (endpoint is rate-limited). */
export function InquiryForm({ slug, products, submitLabel, disabled }: Props) {
  const [status, setStatus] = useState<"idle" | "sending" | "sent">("idle");
  const [errors, setErrors] = useState<Errors>({});
  const [formError, setFormError] = useState<string | null>(null);

  async function onSubmit(e: React.FormEvent<HTMLFormElement>) {
    e.preventDefault();
    if (disabled) return;
    const fd = new FormData(e.currentTarget);
    const body = Object.fromEntries(
      ["name", "phone", "email", "message", "productId", "preferredDate", "website"]
        .map((k) => [k, String(fd.get(k) ?? "").trim()])
        .filter(([, v]) => v !== ""),
    );
    const local: Errors = {};
    if (!body.name) local.name = "Please enter your name";
    if (!body.phone && !body.email) local.phone = "Please give a phone number or email";
    if (!body.message) local.message = "Please tell us what you need";
    setErrors(local);
    setFormError(null);
    if (Object.keys(local).length) return;

    setStatus("sending");
    try {
      const res = await fetch(`/api/public/sites/${encodeURIComponent(slug)}/inquiries`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(body),
      });
      if (res.ok) {
        setStatus("sent");
        return;
      }
      const problem = await res.json().catch(() => ({}));
      const fieldErrors: Errors = problem.errors ?? {};
      if (fieldErrors.contactable) fieldErrors.phone = fieldErrors.contactable;
      setErrors(fieldErrors);
      setFormError(Object.keys(fieldErrors).length ? null : problem.detail ?? "Could not send your inquiry. Please try again.");
    } catch {
      setFormError("Could not send your inquiry. Check your connection and try again.");
    }
    setStatus("idle");
  }

  if (status === "sent") {
    return (
      <div className="flex flex-col items-center gap-3 py-8 text-center" role="status">
        <CheckCircle2 className="size-10 text-[var(--site-primary)]" />
        <p className="text-lg font-semibold">Thank you! Your inquiry has been sent.</p>
        <p className="text-sm text-[var(--site-muted)]">The business will get back to you soon.</p>
      </div>
    );
  }

  const field = (name: string, label: string, el: React.ReactNode) => (
    <label className="grid gap-1.5 text-sm">
      <span className="font-medium">{label}</span>
      {el}
      {errors[name] && <span className="text-xs text-red-600" role="alert">{errors[name]}</span>}
    </label>
  );

  return (
    <form onSubmit={onSubmit} noValidate className="grid gap-4">
      {disabled && (
        <p className="rounded-[calc(var(--site-radius)*0.6)] bg-[var(--site-surface)] p-3 text-xs text-[var(--site-muted)]">
          Preview: the form becomes live when you publish your website.
        </p>
      )}
      <div className="grid gap-4 @xl:grid-cols-2">
        {field("name", "Your name", <input name="name" autoComplete="name" maxLength={120} className={input} disabled={disabled} aria-invalid={!!errors.name} />)}
        {field("phone", "Phone / WhatsApp", <input name="phone" type="tel" autoComplete="tel" maxLength={20} className={input} disabled={disabled} aria-invalid={!!errors.phone} />)}
      </div>
      <div className="grid gap-4 @xl:grid-cols-2">
        {field("email", "Email (optional)", <input name="email" type="email" autoComplete="email" maxLength={254} className={input} disabled={disabled} aria-invalid={!!errors.email} />)}
        {field("preferredDate", "Date needed (optional)", <input name="preferredDate" type="date" className={input} disabled={disabled} />)}
      </div>
      {products.length > 0 &&
        field(
          "productId",
          "Interested in",
          <select name="productId" className={input} disabled={disabled} defaultValue="">
            <option value="">Something else / not sure</option>
            {products.map((p) => (
              <option key={p.id} value={p.id}>{p.name}</option>
            ))}
          </select>,
        )}
      {field(
        "message",
        "Your message",
        <textarea name="message" rows={4} maxLength={2000} className={`${input} h-auto py-2.5`} disabled={disabled} aria-invalid={!!errors.message} />,
      )}
      {/* Honeypot: hidden from people, filled by naive bots */}
      <div aria-hidden className="absolute left-[-10000px] h-px w-px overflow-hidden">
        <label>Website<input name="website" tabIndex={-1} autoComplete="off" /></label>
      </div>
      {formError && <p className="text-sm text-red-600" role="alert">{formError}</p>}
      <button
        type="submit"
        disabled={disabled || status === "sending"}
        className="inline-flex h-11 items-center justify-center gap-2 rounded-[var(--site-radius)] bg-[var(--site-primary)] px-5 text-sm font-medium text-white transition-opacity hover:opacity-90 disabled:opacity-60"
      >
        {status === "sending" && <Loader2 className="size-4 animate-spin" />} {submitLabel}
      </button>
    </form>
  );
}
