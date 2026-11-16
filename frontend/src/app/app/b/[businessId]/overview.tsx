"use client";

import Link from "next/link";
import { useEffect, useState } from "react";
import { ArrowRight, CheckCircle2, Circle, ExternalLink } from "lucide-react";
import { useBusiness } from "@/components/app/business-context";
import { PageHeader } from "@/components/app/page-header";
import { LeadStatusBadge } from "@/components/app/lead-status-badge";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Skeleton } from "@/components/ui/skeleton";
import { api, ApiError } from "@/lib/api";
import { CATEGORY_LABELS, type Business, type Lead, type LeadSummary, type Page, type Product } from "@/lib/types";
import type { Website } from "@/lib/site-types";
import { relativeTime } from "@/lib/dates";

type Data = { website: Website | null; products: number; leads: LeadSummary; recent: Lead[] };
type ChecklistItem = { label: string; done: boolean; href: string };

/** Everything on this page is derived from persisted data — no placeholder metrics. */
function checklist(b: Business, d: Data, base: string): ChecklistItem[] {
  return [
    { label: "Describe your business", done: true, href: `${base}/settings` },
    { label: "Add contact details (phone or WhatsApp)", done: Boolean(b.phone || b.whatsapp), href: `${base}/settings` },
    { label: "Create your website", done: d.website !== null, href: `${base}/website` },
    { label: "Add products or services", done: d.products > 0, href: `${base}/products` },
    { label: "Set opening hours", done: Boolean(b.openingHours?.length), href: `${base}/settings` },
    { label: "Publish your website", done: Boolean(d.website?.published), href: `${base}/website` },
    { label: "Receive your first inquiry", done: d.leads.total > 0, href: `${base}/inquiries` },
  ];
}

export function Overview() {
  const { business } = useBusiness();
  const base = `/app/b/${business.id}`;
  const api_ = `/api/businesses/${business.id}`;
  const [data, setData] = useState<Data | null>(null);
  const [error, setError] = useState(false);

  useEffect(() => {
    Promise.all([
      api<Website>(`${api_}/website`).catch((e) => (e instanceof ApiError && e.status === 404 ? null : Promise.reject(e))),
      api<Page<Product>>(`${api_}/products?size=1`),
      api<LeadSummary>(`${api_}/leads/summary`),
      api<Page<Lead>>(`${api_}/leads?size=5`),
    ]).then(
      ([website, products, leads, recent]) => setData({ website, products: products.totalItems, leads, recent: recent.items }),
      () => setError(true),
    );
  }, [api_]);

  return (
    <>
      <PageHeader
        title={business.name}
        description={`${CATEGORY_LABELS[business.category]}${business.city ? ` · ${business.city}` : ""}`}
        actions={
          data?.website?.published ? (
            <Button asChild variant="outline"><a href={data.website.publicUrl} target="_blank" rel="noopener noreferrer"><ExternalLink /> View website</a></Button>
          ) : null
        }
      />
      <div className="grid gap-6 p-4 sm:p-8">
        {error && <p className="text-sm text-destructive">Some information couldn&apos;t be loaded. Refresh to try again.</p>}
        <div className="grid gap-4 sm:grid-cols-3">
          <Stat label="Inquiries (last 7 days)" value={data?.leads.lastSevenDays} href={`${base}/inquiries`} />
          <Stat label="Open inquiries" value={data ? data.leads.byStatus.NEW + data.leads.byStatus.CONTACTED + data.leads.byStatus.QUALIFIED + data.leads.byStatus.QUOTED : undefined} href={`${base}/inquiries`} />
          <Stat label="Converted" value={data?.leads.byStatus.CONVERTED} href={`${base}/inquiries`} />
        </div>

        <div className="grid gap-6 lg:grid-cols-2">
          <Card>
            <CardHeader>
              <CardTitle>Recent inquiries</CardTitle>
              <CardDescription>Latest messages from customers</CardDescription>
            </CardHeader>
            <CardContent>
              {!data ? (
                <Skeleton className="h-24" />
              ) : data.recent.length === 0 ? (
                <p className="text-sm text-muted-foreground">
                  {data.website?.published ? "No inquiries yet. Share your website link with customers." : "Publish your website to start receiving inquiries."}
                </p>
              ) : (
                <ul className="divide-y">
                  {data.recent.map((l) => (
                    <li key={l.id} className="flex items-center justify-between gap-3 py-2.5 text-sm">
                      <span className="min-w-0">
                        <span className="block truncate font-medium">{l.customerName}</span>
                        <span className="block truncate text-muted-foreground">{l.productName ?? l.message}</span>
                      </span>
                      <span className="flex shrink-0 flex-col items-end gap-1">
                        <LeadStatusBadge status={l.status} />
                        <span className="text-xs text-muted-foreground">{relativeTime(l.createdAt)}</span>
                      </span>
                    </li>
                  ))}
                </ul>
              )}
              <Link href={`${base}/inquiries`} className="mt-3 inline-flex items-center gap-1 text-sm font-medium text-primary hover:underline">
                All inquiries <ArrowRight className="size-3.5" />
              </Link>
            </CardContent>
          </Card>

          <Card>
            <CardHeader>
              <CardTitle>Setup checklist</CardTitle>
              {data && <CardDescription>{checklist(business, data, base).filter((i) => i.done).length} of 7 complete</CardDescription>}
            </CardHeader>
            <CardContent>
              {!data ? <Skeleton className="h-40" /> : <Checklist items={checklist(business, data, base)} />}
            </CardContent>
          </Card>
        </div>
      </div>
    </>
  );
}

function Stat({ label, value, href }: { label: string; value?: number; href: string }) {
  return (
    <Link href={href} className="rounded-xl border bg-card p-5 transition-colors hover:border-primary/30">
      <p className="text-sm text-muted-foreground">{label}</p>
      {value === undefined ? <Skeleton className="mt-2 h-8 w-12" /> : <p className="mt-1 text-3xl font-semibold tabular-nums">{value}</p>}
    </Link>
  );
}

function Checklist({ items }: { items: ChecklistItem[] }) {
  const done = items.filter((i) => i.done).length;
  return (
    <>
      <div className="mb-4 h-1.5 overflow-hidden rounded-full bg-muted" aria-hidden>
        <div className="h-full rounded-full bg-primary transition-all" style={{ width: `${(done / items.length) * 100}%` }} />
      </div>
      <ul className="grid gap-0.5">
        {items.map((item) => (
          <li key={item.label}>
            <Link href={item.href} className="flex items-center gap-2.5 rounded-lg px-2 py-2 text-sm hover:bg-muted">
              {item.done ? <CheckCircle2 className="size-4 text-success" /> : <Circle className="size-4 text-muted-foreground" />}
              <span className={item.done ? "text-muted-foreground line-through" : ""}>{item.label}</span>
            </Link>
          </li>
        ))}
      </ul>
    </>
  );
}
