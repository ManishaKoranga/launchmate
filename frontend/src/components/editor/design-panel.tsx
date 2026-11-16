"use client";

import { AlertTriangle, Check, Loader2 } from "lucide-react";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Textarea } from "@/components/ui/textarea";
import type { CornerStyle, FontPairing, SiteConfig, Theme, ThemePalette, WebsiteTemplate } from "@/lib/site-types";
import { contrastWithWhite } from "@/components/site/theme";
import { cn } from "@/lib/utils";

type Props = {
  config: SiteConfig;
  onChange: (c: SiteConfig) => void;
  templates: WebsiteTemplate[];
  onTemplate: (id: string) => void;
  switchingTemplate: string | null;
  templateDisabledReason?: string;
  disabled?: boolean;
};

const PALETTES: { value: ThemePalette; label: string; swatch: string }[] = [
  { value: "LIGHT", label: "Light", swatch: "#ffffff" },
  { value: "WARM", label: "Warm", swatch: "#f8eee3" },
  { value: "COOL", label: "Cool", swatch: "#edf2f7" },
  { value: "DARK", label: "Dark", swatch: "#1c1c20" },
];
const FONTS: { value: FontPairing; label: string; family: string }[] = [
  { value: "MODERN", label: "Modern", family: "var(--font-sans)" },
  { value: "ELEGANT", label: "Elegant", family: "var(--font-playfair)" },
  { value: "FRIENDLY", label: "Friendly", family: "var(--font-poppins)" },
  { value: "CLASSIC", label: "Classic", family: "var(--font-lora)" },
];
const CORNERS: { value: CornerStyle; label: string; radius: string }[] = [
  { value: "ROUNDED", label: "Rounded", radius: "10px" },
  { value: "SOFT", label: "Soft", radius: "6px" },
  { value: "SHARP", label: "Sharp", radius: "2px" },
];

export function DesignPanel({ config, onChange, templates, onTemplate, switchingTemplate, templateDisabledReason, disabled }: Props) {
  const setTheme = (patch: Partial<Theme>) => onChange({ ...config, theme: { ...config.theme, ...patch } });
  const contrast = contrastWithWhite(config.theme.primaryColor);

  return (
    <fieldset disabled={disabled} className="grid gap-6">
      <div className="grid gap-2">
        <Label>Template</Label>
        {templateDisabledReason && <p className="text-xs text-muted-foreground">{templateDisabledReason}</p>}
        <div className="grid grid-cols-2 gap-2">
          {templates.map((t) => {
            const active = t.id === config.templateId;
            return (
              <button
                key={t.id}
                type="button"
                disabled={active || !!templateDisabledReason || switchingTemplate !== null}
                onClick={() => onTemplate(t.id)}
                className={cn(
                  "rounded-xl border p-3 text-left transition-colors disabled:cursor-default",
                  active ? "border-primary ring-1 ring-primary" : "hover:border-primary/40 disabled:opacity-60",
                )}
                aria-pressed={active}
              >
                <span className="flex gap-1" aria-hidden>
                  <span className="size-4 rounded-full" style={{ background: t.defaultTheme.primaryColor }} />
                  <span className="size-4 rounded-full" style={{ background: t.defaultTheme.accentColor }} />
                </span>
                <span className="mt-2 flex items-center gap-1 text-sm font-medium">
                  {t.name}
                  {active && <Check className="size-3.5 text-primary" />}
                  {switchingTemplate === t.id && <Loader2 className="size-3.5 animate-spin" />}
                </span>
                <span className="mt-0.5 line-clamp-2 block text-xs text-muted-foreground">{t.description}</span>
              </button>
            );
          })}
        </div>
      </div>

      <div className="grid grid-cols-2 gap-3">
        <ColourField id="primary" label="Brand colour" value={config.theme.primaryColor} onChange={(v) => setTheme({ primaryColor: v })} />
        <ColourField id="accent" label="Accent colour" value={config.theme.accentColor} onChange={(v) => setTheme({ accentColor: v })} />
        {contrast < 4.5 && (
          <p className="col-span-2 flex items-start gap-1.5 text-xs text-destructive" role="alert">
            <AlertTriangle className="mt-0.5 size-3.5 shrink-0" />
            White text on this brand colour is hard to read (contrast {contrast.toFixed(1)}:1, needs 4.5:1). Choose a darker colour to save.
          </p>
        )}
      </div>

      <Segmented label="Background" value={config.theme.palette} onChange={(v) => setTheme({ palette: v })}
        options={PALETTES.map((p) => ({ value: p.value, label: p.label, adornment: <span className="size-3 rounded-full border" style={{ background: p.swatch }} /> }))} />
      <Segmented label="Fonts" value={config.theme.fontPairing} onChange={(v) => setTheme({ fontPairing: v })}
        options={FONTS.map((f) => ({ value: f.value, label: f.label, adornment: <span style={{ fontFamily: f.family }} className="font-semibold">Aa</span> }))} />
      <Segmented label="Corners" value={config.theme.corners} onChange={(v) => setTheme({ corners: v })}
        options={CORNERS.map((c) => ({ value: c.value, label: c.label, adornment: <span className="size-3 border-2 border-current" style={{ borderRadius: c.radius }} /> }))} />

      <div className="grid gap-3">
        <Label htmlFor="seo-title">Search engine title</Label>
        <Input id="seo-title" maxLength={70} value={config.seo.title} onChange={(e) => onChange({ ...config, seo: { ...config.seo, title: e.target.value } })} />
        <Label htmlFor="seo-desc">Search engine description</Label>
        <Textarea id="seo-desc" rows={3} maxLength={170} value={config.seo.description} onChange={(e) => onChange({ ...config, seo: { ...config.seo, description: e.target.value } })} />
      </div>
    </fieldset>
  );
}

function ColourField({ id, label, value, onChange }: { id: string; label: string; value: string; onChange: (v: string) => void }) {
  return (
    <div className="grid gap-1.5">
      <Label htmlFor={id}>{label}</Label>
      <div className="flex items-center gap-2">
        <input
          type="color"
          aria-label={`${label} picker`}
          value={/^#[0-9a-fA-F]{6}$/.test(value) ? value : "#000000"}
          onChange={(e) => onChange(e.target.value.toUpperCase())}
          className="h-8 w-9 shrink-0 cursor-pointer rounded-md border bg-transparent p-0.5"
        />
        <Input id={id} value={value} maxLength={7} onChange={(e) => onChange(e.target.value)} className="font-mono text-xs" />
      </div>
    </div>
  );
}

function Segmented<T extends string>({
  label,
  value,
  onChange,
  options,
}: {
  label: string;
  value: T;
  onChange: (v: T) => void;
  options: { value: T; label: string; adornment?: React.ReactNode }[];
}) {
  return (
    <div className="grid gap-2" role="radiogroup" aria-label={label}>
      <Label>{label}</Label>
      <div className="grid grid-cols-4 gap-1.5">
        {options.map((o) => (
          <button
            key={o.value}
            type="button"
            role="radio"
            aria-checked={o.value === value}
            onClick={() => onChange(o.value)}
            className={cn(
              "flex flex-col items-center gap-1 rounded-lg border px-1 py-2 text-xs transition-colors",
              o.value === value ? "border-primary bg-accent text-accent-foreground" : "hover:bg-muted",
            )}
          >
            {o.adornment}
            {o.label}
          </button>
        ))}
      </div>
    </div>
  );
}
