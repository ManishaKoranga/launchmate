import type { CSSProperties } from "react";
import type { Theme } from "@/lib/site-types";

const PALETTES = {
  LIGHT: { bg: "#ffffff", surface: "#f6f6f7", fg: "#18181b", muted: "#52525b", border: "#e4e4e7" },
  WARM: { bg: "#fffaf5", surface: "#f8eee3", fg: "#2b1d16", muted: "#6b5a4f", border: "#eaddcf" },
  COOL: { bg: "#f8fafc", surface: "#edf2f7", fg: "#0f172a", muted: "#475569", border: "#dbe3ec" },
  DARK: { bg: "#111113", surface: "#1c1c20", fg: "#f4f4f5", muted: "#a1a1aa", border: "#2e2e33" },
} as const;

const FONTS = {
  MODERN: { heading: "var(--font-sans)", body: "var(--font-sans)" },
  ELEGANT: { heading: "var(--font-playfair)", body: "var(--font-sans)" },
  FRIENDLY: { heading: "var(--font-poppins)", body: "var(--font-poppins)" },
  CLASSIC: { heading: "var(--font-lora)", body: "var(--font-sans)" },
} as const;

const RADII = { ROUNDED: "1.25rem", SOFT: "0.75rem", SHARP: "0.25rem" } as const;

/** Theme → CSS custom properties consumed by the section components (all colours validated server-side). */
export function themeStyle(theme: Theme): CSSProperties {
  const p = PALETTES[theme.palette];
  const f = FONTS[theme.fontPairing];
  return {
    "--site-primary": theme.primaryColor,
    "--site-accent": theme.accentColor,
    "--site-bg": p.bg,
    "--site-surface": p.surface,
    "--site-fg": p.fg,
    "--site-muted": p.muted,
    "--site-border": p.border,
    "--site-radius": RADII[theme.corners],
    "--site-heading-font": f.heading,
    "--site-body-font": f.body,
  } as CSSProperties;
}

/** WCAG contrast ratio of a #RRGGBB colour against white (mirrors the backend rule). */
export function contrastWithWhite(hex: string): number {
  if (!/^#[0-9a-fA-F]{6}$/.test(hex)) return 0;
  const n = parseInt(hex.slice(1), 16);
  const ch = (c: number) => {
    const s = c / 255;
    return s <= 0.03928 ? s / 12.92 : Math.pow((s + 0.055) / 1.055, 2.4);
  };
  const l = 0.2126 * ch((n >> 16) & 255) + 0.7152 * ch((n >> 8) & 255) + 0.0722 * ch(n & 255);
  return 1.05 / (l + 0.05);
}
