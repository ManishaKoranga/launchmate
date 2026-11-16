"use client";

import Link from "next/link";
import { CheckCircle2, Circle } from "lucide-react";
import { useBusiness } from "@/components/app/business-context";
import { PageHeader } from "@/components/app/page-header";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { CATEGORY_LABELS, type Business } from "@/lib/types";

type ChecklistItem = { label: string; done: boolean; href: string };

/** Setup checklist derived purely from persisted business data. */
function checklist(b: Business, base: string): ChecklistItem[] {
  return [
    { label: "Describe your business", done: true, href: `${base}/settings` },
    { label: "Add contact details (phone or WhatsApp)", done: Boolean(b.phone || b.whatsapp), href: `${base}/settings` },
    { label: "Set opening hours", done: Boolean(b.openingHours?.length), href: `${base}/settings` },
  ];
}

export function Overview() {
  const { business } = useBusiness();
  const base = `/app/b/${business.id}`;
  const items = checklist(business, base);
  const done = items.filter((i) => i.done).length;

  return (
    <>
      <PageHeader title={`Welcome, ${business.name}`} description={`${CATEGORY_LABELS[business.category]}${business.city ? ` · ${business.city}` : ""}`} />
      <div className="grid gap-6 p-4 sm:p-8 lg:grid-cols-2">
        <Card>
          <CardHeader>
            <CardTitle>Setup checklist</CardTitle>
            <CardDescription>{done} of {items.length} complete</CardDescription>
          </CardHeader>
          <CardContent>
            <div className="mb-4 h-1.5 overflow-hidden rounded-full bg-muted" aria-hidden>
              <div className="h-full rounded-full bg-primary transition-all" style={{ width: `${(done / items.length) * 100}%` }} />
            </div>
            <ul className="grid gap-1">
              {items.map((item) => (
                <li key={item.label}>
                  <Link href={item.href} className="flex items-center gap-2.5 rounded-lg px-2 py-2 text-sm hover:bg-muted">
                    {item.done ? <CheckCircle2 className="size-4 text-success" /> : <Circle className="size-4 text-muted-foreground" />}
                    <span className={item.done ? "text-muted-foreground line-through" : ""}>{item.label}</span>
                  </Link>
                </li>
              ))}
            </ul>
          </CardContent>
        </Card>
      </div>
    </>
  );
}
