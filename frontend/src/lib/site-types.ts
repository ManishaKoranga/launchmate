import type { BusinessCategory, DailyHours } from "@/lib/types";

export type SectionType =
  | "HERO"
  | "ABOUT"
  | "HIGHLIGHTS"
  | "CATALOGUE"
  | "PROCESS"
  | "FAQ"
  | "CTA_BANNER"
  | "HOURS_LOCATION"
  | "CONTACT";

export type ThemePalette = "LIGHT" | "WARM" | "COOL" | "DARK";
export type FontPairing = "MODERN" | "ELEGANT" | "FRIENDLY" | "CLASSIC";
export type CornerStyle = "ROUNDED" | "SOFT" | "SHARP";

export type Theme = {
  primaryColor: string;
  accentColor: string;
  palette: ThemePalette;
  fontPairing: FontPairing;
  corners: CornerStyle;
};

export type SectionItem = { title: string; body?: string | null };

export type Section = {
  id: string;
  type: SectionType;
  visible: boolean;
  heading?: string | null;
  subheading?: string | null;
  body?: string | null;
  ctaLabel?: string | null;
  items: SectionItem[];
};

export type SiteConfig = {
  templateId: string;
  theme: Theme;
  seo: { title: string; description: string };
  sections: Section[];
};

export type PublicProduct = {
  id: string;
  kind: "PRODUCT" | "SERVICE";
  name: string;
  description?: string | null;
  price?: number | null;
  priceNote?: string | null;
  category?: string | null;
  imageUrl?: string | null;
};

export type PublicSite = {
  slug: string;
  business: {
    name: string;
    category: BusinessCategory;
    tagline?: string | null;
    description?: string | null;
    city?: string | null;
    area?: string | null;
    address?: string | null;
    phone?: string | null;
    whatsapp?: string | null;
    email?: string | null;
    openingHours?: DailyHours[] | null;
  };
  config: SiteConfig;
  products: PublicProduct[];
  version: number;
  publishedAt: string;
  preview: boolean;
};

export type Website = {
  id: string;
  draft: SiteConfig;
  draftRevision: number;
  draftSource: "AI_GENERATED" | "TEMPLATE_STARTER" | "OWNER_EDITED";
  draftUpdatedAt: string;
  published?: { version: number; publishedAt: string } | null;
  hasUnpublishedChanges: boolean;
  publicUrl: string;
};

export type WebsiteTemplate = {
  id: string;
  name: string;
  category: BusinessCategory;
  description: string;
  defaultTheme: Theme;
};

export type GenerationJob = {
  id: string;
  mode: "AI" | "TEMPLATE";
  status: "QUEUED" | "RUNNING" | "SUCCEEDED" | "FAILED";
  steps: { key: string; label: string; status: "PENDING" | "RUNNING" | "DONE" | "FAILED" }[];
  errorMessage?: string | null;
  createdAt: string;
  completedAt?: string | null;
};
