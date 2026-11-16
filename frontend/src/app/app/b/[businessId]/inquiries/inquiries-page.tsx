"use client";

import { useCallback, useEffect, useState } from "react";
import { Inbox, Loader2, Mail, MessageCircle, Phone, Plus } from "lucide-react";
import { toast } from "sonner";
import { useBusiness } from "@/components/app/business-context";
import { PageHeader } from "@/components/app/page-header";
import { EmptyState, ErrorState } from "@/components/app/states";
import { LeadStatusBadge } from "@/components/app/lead-status-badge";
import { Button } from "@/components/ui/button";
import { Textarea } from "@/components/ui/textarea";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Skeleton } from "@/components/ui/skeleton";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import { Sheet, SheetContent, SheetDescription, SheetHeader, SheetTitle } from "@/components/ui/sheet";
import { Dialog, DialogContent, DialogDescription, DialogFooter, DialogHeader, DialogTitle } from "@/components/ui/dialog";
import { api, errorMessage } from "@/lib/api";
import { LEAD_STATUSES, type Lead, type LeadStatus, type LeadSummary, type Page } from "@/lib/types";
import { formatDate, formatDateTime, relativeTime } from "@/lib/dates";
import { telLink, whatsappLink } from "@/components/site/format";
import { cn } from "@/lib/utils";

const PAGE_SIZE = 20;

export function InquiriesPage() {
  const { business } = useBusiness();
  const base = `/api/businesses/${business.id}/leads`;
  const [filter, setFilter] = useState<LeadStatus | "ALL">("ALL");
  const [leads, setLeads] = useState<Lead[] | null>(null);
  const [page, setPage] = useState<Page<Lead> | null>(null);
  const [summary, setSummary] = useState<LeadSummary | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [loadingMore, setLoadingMore] = useState(false);
  const [selected, setSelected] = useState<Lead | null>(null);
  const [adding, setAdding] = useState(false);

  const query = useCallback(
    (p: number) => `${base}?size=${PAGE_SIZE}&page=${p}${filter === "ALL" ? "" : `&status=${filter}`}`,
    [base, filter],
  );

  const refresh = useCallback(
    () =>
      Promise.all([api<Page<Lead>>(query(0)), api<LeadSummary>(`${base}/summary`)]).then(
        ([p, s]) => {
          setPage(p);
          setLeads(p.items);
          setSummary(s);
          setError(null);
        },
        (e) => setError(errorMessage(e)),
      ),
    [base, query],
  );

  useEffect(() => {
    refresh();
  }, [refresh]);

  async function loadMore() {
    if (!page) return;
    setLoadingMore(true);
    try {
      const next = await api<Page<Lead>>(query(page.page + 1));
      setPage(next);
      setLeads((l) => [...(l ?? []), ...next.items]);
    } catch (e) {
      toast.error(errorMessage(e));
    } finally {
      setLoadingMore(false);
    }
  }

  const onUpdated = (lead: Lead) => {
    setSelected(lead);
    setLeads((l) => l?.map((x) => (x.id === lead.id ? lead : x)) ?? null);
    api<LeadSummary>(`${base}/summary`).then(setSummary, () => undefined);
  };

  const tabs: { value: LeadStatus | "ALL"; label: string; count?: number }[] = [
    { value: "ALL", label: "All", count: summary?.total },
    ...LEAD_STATUSES.map((s) => ({ ...s, count: summary?.byStatus[s.value] })),
  ];

  return (
    <>
      <PageHeader
        title="Inquiries"
        description="Every inquiry from your website, in one place."
        actions={<Button variant="outline" onClick={() => setAdding(true)}><Plus /> Add inquiry</Button>}
      />
      <div className="border-b px-4 sm:px-8">
        <div className="-mb-px flex gap-1 overflow-x-auto" role="tablist" aria-label="Filter by status">
          {tabs.map((t) => (
            <button
              key={t.value}
              role="tab"
              aria-selected={filter === t.value}
              onClick={() => setFilter(t.value)}
              className={cn(
                "flex shrink-0 items-center gap-1.5 border-b-2 px-3 py-2.5 text-sm",
                filter === t.value ? "border-primary font-medium text-foreground" : "border-transparent text-muted-foreground hover:text-foreground",
              )}
            >
              {t.label}
              {t.count != null && <span className="rounded-full bg-muted px-1.5 text-xs tabular-nums">{t.count}</span>}
            </button>
          ))}
        </div>
      </div>
      <div className="p-4 sm:p-8">
        {error ? (
          <ErrorState message={error} onRetry={() => void refresh()} className="mx-auto py-12" />
        ) : leads === null ? (
          <div className="grid gap-2">{[0, 1, 2].map((i) => <Skeleton key={i} className="h-20 rounded-xl" />)}</div>
        ) : leads.length === 0 ? (
          <EmptyState
            icon={Inbox}
            title={filter === "ALL" ? "No inquiries yet" : "Nothing here"}
            description={filter === "ALL" ? "When customers send an inquiry from your published website, it will appear here." : "No inquiries have this status."}
          />
        ) : (
          <>
            <ul className="divide-y rounded-xl border bg-card">
              {leads.map((l) => (
                <li key={l.id}>
                  <button onClick={() => setSelected(l)} className="flex w-full items-start gap-3 p-4 text-left hover:bg-muted/50">
                    <div className="min-w-0 flex-1">
                      <p className="flex flex-wrap items-center gap-2">
                        <span className="font-medium">{l.customerName}</span>
                        <LeadStatusBadge status={l.status} />
                        {l.productName && <span className="text-xs text-muted-foreground">· {l.productName}</span>}
                      </p>
                      <p className="mt-1 line-clamp-2 text-sm text-muted-foreground">{l.message}</p>
                    </div>
                    <time className="shrink-0 text-xs text-muted-foreground" dateTime={l.createdAt}>{relativeTime(l.createdAt)}</time>
                  </button>
                </li>
              ))}
            </ul>
            {page && page.page + 1 < page.totalPages && (
              <div className="mt-4 flex justify-center">
                <Button variant="outline" onClick={() => void loadMore()} disabled={loadingMore}>
                  {loadingMore && <Loader2 className="animate-spin" />} Load more
                </Button>
              </div>
            )}
          </>
        )}
      </div>

      <Sheet open={!!selected} onOpenChange={(o) => !o && setSelected(null)}>
        <SheetContent className="w-full overflow-y-auto sm:max-w-md">
          {selected && <LeadDetail key={selected.id} base={base} lead={selected} businessName={business.name} onUpdated={onUpdated} />}
        </SheetContent>
      </Sheet>
      <AddLeadDialog open={adding} onOpenChange={setAdding} base={base} onCreated={() => { setAdding(false); void refresh(); }} />
    </>
  );
}

function LeadDetail({ base, lead, businessName, onUpdated }: { base: string; lead: Lead; businessName: string; onUpdated: (l: Lead) => void }) {
  const [notes, setNotes] = useState(lead.notes ?? "");
  const [quote, setQuote] = useState(lead.quoteDraft ?? "");
  const [busy, setBusy] = useState<string | null>(null);

  async function run(key: string, fn: () => Promise<Lead>, success: string) {
    setBusy(key);
    try {
      onUpdated(await fn());
      toast.success(success);
    } catch (e) {
      toast.error(errorMessage(e));
    } finally {
      setBusy(null);
    }
  }

  const greeting = `Hi ${lead.customerName}, thank you for your inquiry to ${businessName}.`;
  return (
    <div className="grid gap-6 p-1">
      <SheetHeader className="p-0">
        <SheetTitle>{lead.customerName}</SheetTitle>
        <SheetDescription>
          Received {formatDateTime(lead.createdAt)} · {lead.source === "WEBSITE" ? "Website form" : "Added manually"}
        </SheetDescription>
      </SheetHeader>

      <div className="flex flex-wrap gap-2">
        {lead.phone && <Button asChild size="sm" variant="outline"><a href={telLink(lead.phone)}><Phone /> Call</a></Button>}
        {lead.phone && (
          <Button asChild size="sm" variant="outline">
            <a href={whatsappLink(lead.phone, greeting)} target="_blank" rel="noopener noreferrer"><MessageCircle /> WhatsApp</a>
          </Button>
        )}
        {lead.email && <Button asChild size="sm" variant="outline"><a href={`mailto:${lead.email}`}><Mail /> Email</a></Button>}
      </div>

      <dl className="grid grid-cols-[7rem_1fr] gap-y-2 text-sm">
        {lead.phone && (<><dt className="text-muted-foreground">Phone</dt><dd>{lead.phone}</dd></>)}
        {lead.email && (<><dt className="text-muted-foreground">Email</dt><dd className="break-all">{lead.email}</dd></>)}
        {lead.productName && (<><dt className="text-muted-foreground">Interested in</dt><dd>{lead.productName}</dd></>)}
        {lead.preferredDate && (<><dt className="text-muted-foreground">Needed by</dt><dd>{formatDate(lead.preferredDate)}</dd></>)}
        {lead.budget && (<><dt className="text-muted-foreground">Budget</dt><dd>{lead.budget}</dd></>)}
      </dl>

      <div>
        <p className="mb-1.5 text-sm font-medium">Message</p>
        {/* Customer-written text: rendered as plain text only. */}
        <p className="whitespace-pre-wrap rounded-lg bg-muted p-3 text-sm">{lead.message}</p>
      </div>

      <div className="grid gap-1.5">
        <Label htmlFor="status">Status</Label>
        <Select
          value={lead.status}
          onValueChange={(v) => void run("status", () => api<Lead>(`${base}/${lead.id}/status`, { method: "PATCH", body: { status: v } }), "Status updated")}
          disabled={busy !== null}
        >
          <SelectTrigger id="status" className="w-full"><SelectValue /></SelectTrigger>
          <SelectContent>{LEAD_STATUSES.map((s) => <SelectItem key={s.value} value={s.value}>{s.label}</SelectItem>)}</SelectContent>
        </Select>
      </div>

      <div className="grid gap-1.5">
        <Label htmlFor="notes">Private notes</Label>
        <Textarea id="notes" rows={3} maxLength={4000} value={notes} onChange={(e) => setNotes(e.target.value)} />
        <Button size="sm" variant="outline" className="w-fit" disabled={busy !== null || notes === (lead.notes ?? "")}
          onClick={() => void run("notes", () => api<Lead>(`${base}/${lead.id}/notes`, { method: "PUT", body: { notes } }), "Notes saved")}>
          {busy === "notes" && <Loader2 className="animate-spin" />} Save notes
        </Button>
      </div>

      <div className="grid gap-1.5">
        <Label htmlFor="quote">Quotation draft</Label>
        <Textarea id="quote" rows={6} maxLength={4000} value={quote} onChange={(e) => setQuote(e.target.value)}
          placeholder="Write a quote here, or ask the AI assistant to prepare one from your prices." />
        <div className="flex flex-wrap gap-2">
          <Button size="sm" variant="outline" disabled={busy !== null || quote === (lead.quoteDraft ?? "")}
            onClick={() => void run("quote", () => api<Lead>(`${base}/${lead.id}/quote`, { method: "PUT", body: { quoteDraft: quote } }), "Quote saved")}>
            {busy === "quote" && <Loader2 className="animate-spin" />} Save quote
          </Button>
          {lead.phone && quote.trim() && (
            <Button asChild size="sm" variant="ghost">
              <a href={whatsappLink(lead.phone, quote)} target="_blank" rel="noopener noreferrer"><MessageCircle /> Send on WhatsApp</a>
            </Button>
          )}
        </div>
      </div>
    </div>
  );
}

function AddLeadDialog({ open, onOpenChange, base, onCreated }: { open: boolean; onOpenChange: (o: boolean) => void; base: string; onCreated: () => void }) {
  const [form, setForm] = useState({ name: "", phone: "", email: "", message: "" });
  const [errors, setErrors] = useState<Record<string, string>>({});
  const [saving, setSaving] = useState(false);

  async function submit(e: React.FormEvent) {
    e.preventDefault();
    setSaving(true);
    setErrors({});
    try {
      const body = Object.fromEntries(Object.entries(form).filter(([, v]) => v.trim() !== ""));
      await api(base, { method: "POST", body });
      setForm({ name: "", phone: "", email: "", message: "" });
      toast.success("Inquiry added");
      onCreated();
    } catch (err) {
      const fe = (err as { fieldErrors?: Record<string, string> }).fieldErrors ?? {};
      if (fe.contactable) fe.phone = fe.contactable;
      setErrors(Object.keys(fe).length ? fe : { form: errorMessage(err) });
    } finally {
      setSaving(false);
    }
  }

  const field = (key: keyof typeof form, label: string, type = "text") => (
    <div className="grid gap-1.5">
      <Label htmlFor={`lead-${key}`}>{label}</Label>
      {key === "message" ? (
        <Textarea id={`lead-${key}`} rows={3} value={form[key]} onChange={(e) => setForm({ ...form, [key]: e.target.value })} />
      ) : (
        <Input id={`lead-${key}`} type={type} value={form[key]} onChange={(e) => setForm({ ...form, [key]: e.target.value })} />
      )}
      {errors[key] && <p className="text-xs text-destructive" role="alert">{errors[key]}</p>}
    </div>
  );

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent>
        <form onSubmit={submit} className="grid gap-4" noValidate>
          <DialogHeader>
            <DialogTitle>Add an inquiry</DialogTitle>
            <DialogDescription>For inquiries you received by phone, WhatsApp or in person.</DialogDescription>
          </DialogHeader>
          {field("name", "Customer name")}
          <div className="grid grid-cols-2 gap-3">{field("phone", "Phone", "tel")}{field("email", "Email", "email")}</div>
          {field("message", "What do they need?")}
          {errors.form && <p className="text-sm text-destructive">{errors.form}</p>}
          <DialogFooter>
            <Button type="button" variant="outline" onClick={() => onOpenChange(false)}>Cancel</Button>
            <Button type="submit" disabled={saving}>{saving && <Loader2 className="animate-spin" />} Add</Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  );
}
