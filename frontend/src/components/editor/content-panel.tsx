"use client";

import { useState } from "react";
import Link from "next/link";
import { ArrowDown, ArrowUp, ChevronDown, Eye, EyeOff, Plus, Trash2 } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Textarea } from "@/components/ui/textarea";
import { DropdownMenu, DropdownMenuContent, DropdownMenuItem, DropdownMenuTrigger } from "@/components/ui/dropdown-menu";
import type { Section, SectionItem, SectionType, SiteConfig } from "@/lib/site-types";
import { cn } from "@/lib/utils";
import { newSection, REQUIRED_TYPES, SECTION_FIELDS, SECTION_LABELS } from "./section-defaults";

const ALL_TYPES = Object.keys(SECTION_LABELS) as SectionType[];

type Props = {
  config: SiteConfig;
  onChange: (c: SiteConfig) => void;
  businessId: string;
  disabled?: boolean;
};

export function ContentPanel({ config, onChange, businessId, disabled }: Props) {
  const [open, setOpen] = useState<string | null>(config.sections[0]?.id ?? null);
  const sections = config.sections;
  const setSections = (next: Section[]) => onChange({ ...config, sections: next });
  const update = (id: string, patch: Partial<Section>) => setSections(sections.map((s) => (s.id === id ? { ...s, ...patch } : s)));

  const move = (index: number, delta: number) => {
    const target = index + delta;
    if (target < 1 || target >= sections.length) return; // HERO stays first
    const next = [...sections];
    [next[index], next[target]] = [next[target], next[index]];
    setSections(next);
  };

  const missingTypes = ALL_TYPES.filter((t) => !sections.some((s) => s.type === t));

  return (
    <div className="grid gap-2">
      {sections.map((s, i) => {
        const expanded = open === s.id;
        const required = REQUIRED_TYPES.includes(s.type);
        return (
          <div key={s.id} className={cn("rounded-xl border bg-card", !s.visible && "opacity-70")}>
            <div className="flex items-center gap-1 py-1.5 pl-3 pr-1.5">
              <button
                className="flex min-w-0 flex-1 items-center gap-2 py-1 text-left text-sm font-medium"
                onClick={() => setOpen(expanded ? null : s.id)}
                aria-expanded={expanded}
              >
                <ChevronDown className={cn("size-4 shrink-0 text-muted-foreground transition-transform", !expanded && "-rotate-90")} />
                <span className="truncate">{SECTION_LABELS[s.type]}</span>
                {!s.visible && <span className="text-xs font-normal text-muted-foreground">Hidden</span>}
              </button>
              <Button variant="ghost" size="icon-sm" disabled={disabled || i <= 1} onClick={() => move(i, -1)} aria-label="Move up"><ArrowUp /></Button>
              <Button variant="ghost" size="icon-sm" disabled={disabled || i === 0 || i === sections.length - 1} onClick={() => move(i, 1)} aria-label="Move down"><ArrowDown /></Button>
              <Button
                variant="ghost"
                size="icon-sm"
                disabled={disabled || required}
                onClick={() => update(s.id, { visible: !s.visible })}
                aria-label={s.visible ? "Hide section" : "Show section"}
              >
                {s.visible ? <Eye /> : <EyeOff />}
              </Button>
            </div>
            {expanded && (
              <SectionFields
                section={s}
                disabled={disabled}
                businessId={businessId}
                onChange={(patch) => update(s.id, patch)}
                onDelete={required ? undefined : () => setSections(sections.filter((x) => x.id !== s.id))}
              />
            )}
          </div>
        );
      })}
      {missingTypes.length > 0 && sections.length < 12 && (
        <DropdownMenu>
          <DropdownMenuTrigger asChild>
            <Button variant="outline" size="sm" className="mt-1 w-fit" disabled={disabled}><Plus /> Add section</Button>
          </DropdownMenuTrigger>
          <DropdownMenuContent align="start">
            {missingTypes.map((t) => (
              <DropdownMenuItem
                key={t}
                onSelect={() => {
                  const created = newSection(t, sections.map((s) => s.id));
                  const contactIndex = sections.findIndex((s) => s.type === "CONTACT");
                  const next = [...sections];
                  next.splice(contactIndex < 0 ? next.length : contactIndex, 0, created);
                  setSections(next);
                  setOpen(created.id);
                }}
              >
                {SECTION_LABELS[t]}
              </DropdownMenuItem>
            ))}
          </DropdownMenuContent>
        </DropdownMenu>
      )}
    </div>
  );
}

function SectionFields({
  section,
  onChange,
  onDelete,
  disabled,
  businessId,
}: {
  section: Section;
  onChange: (patch: Partial<Section>) => void;
  onDelete?: () => void;
  disabled?: boolean;
  businessId: string;
}) {
  const f = SECTION_FIELDS[section.type];
  const id = (name: string) => `${section.id}-${name}`;
  return (
    <fieldset disabled={disabled} className="grid gap-3 border-t px-3 pb-3 pt-3">
      {f.heading && (
        <div className="grid gap-1.5">
          <Label htmlFor={id("heading")}>Heading</Label>
          <Input id={id("heading")} maxLength={120} value={section.heading ?? ""} onChange={(e) => onChange({ heading: e.target.value })} />
        </div>
      )}
      {f.subheading && (
        <div className="grid gap-1.5">
          <Label htmlFor={id("sub")}>Subheading</Label>
          <Textarea id={id("sub")} rows={2} maxLength={300} value={section.subheading ?? ""} onChange={(e) => onChange({ subheading: e.target.value })} />
        </div>
      )}
      {f.body && (
        <div className="grid gap-1.5">
          <Label htmlFor={id("body")}>Text</Label>
          <Textarea id={id("body")} rows={5} maxLength={2000} value={section.body ?? ""} onChange={(e) => onChange({ body: e.target.value })} />
        </div>
      )}
      {f.cta && (
        <div className="grid gap-1.5">
          <Label htmlFor={id("cta")}>Button label</Label>
          <Input id={id("cta")} maxLength={40} value={section.ctaLabel ?? ""} onChange={(e) => onChange({ ctaLabel: e.target.value })} />
        </div>
      )}
      {f.items && <ItemsEditor items={section.items} faq={f.items === "faq"} onChange={(items) => onChange({ items })} />}
      {section.type === "CATALOGUE" && (
        <p className="text-xs text-muted-foreground">
          Items come from your <Link className="text-primary hover:underline" href={`/app/b/${businessId}/products`}>products & services</Link>.
        </p>
      )}
      {(section.type === "HOURS_LOCATION" || section.type === "CONTACT") && (
        <p className="text-xs text-muted-foreground">
          Contact details and hours come from your <Link className="text-primary hover:underline" href={`/app/b/${businessId}/settings`}>business profile</Link>.
        </p>
      )}
      {onDelete && (
        <Button variant="ghost" size="sm" className="w-fit text-destructive hover:text-destructive" onClick={onDelete}>
          <Trash2 /> Remove section
        </Button>
      )}
    </fieldset>
  );
}

function ItemsEditor({ items, faq, onChange }: { items: SectionItem[]; faq: boolean; onChange: (items: SectionItem[]) => void }) {
  const set = (i: number, patch: Partial<SectionItem>) => onChange(items.map((it, j) => (j === i ? { ...it, ...patch } : it)));
  return (
    <div className="grid gap-2">
      <Label>{faq ? "Questions" : "Items"}</Label>
      {items.map((it, i) => (
        <div key={i} className="grid gap-1.5 rounded-lg bg-muted/60 p-2">
          <div className="flex gap-1.5">
            <Input aria-label={faq ? "Question" : "Title"} maxLength={100} value={it.title} onChange={(e) => set(i, { title: e.target.value })} placeholder={faq ? "Question" : "Title"} className="bg-background" />
            <Button variant="ghost" size="icon" aria-label="Remove item" disabled={items.length <= 1} onClick={() => onChange(items.filter((_, j) => j !== i))}><Trash2 /></Button>
          </div>
          <Textarea aria-label={faq ? "Answer" : "Description"} rows={2} maxLength={600} value={it.body ?? ""} onChange={(e) => set(i, { body: e.target.value })} placeholder={faq ? "Answer" : "Description"} className="bg-background" />
        </div>
      ))}
      {items.length < 12 && (
        <Button variant="outline" size="sm" className="w-fit" onClick={() => onChange([...items, { title: "", body: "" }])}><Plus /> Add</Button>
      )}
    </div>
  );
}
