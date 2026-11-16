import type { Section, SectionType } from "@/lib/site-types";

export const SECTION_LABELS: Record<SectionType, string> = {
  HERO: "Hero",
  ABOUT: "About",
  HIGHLIGHTS: "Highlights",
  CATALOGUE: "Products & services",
  PROCESS: "How it works",
  FAQ: "FAQ",
  CTA_BANNER: "Call to action",
  HOURS_LOCATION: "Hours & location",
  CONTACT: "Contact & inquiry form",
};

/** Types that every site must have exactly once (enforced by the backend validator too). */
export const REQUIRED_TYPES: SectionType[] = ["HERO", "CONTACT"];

/** Fields the renderer actually uses for each section type — the editor shows only these. */
export const SECTION_FIELDS: Record<SectionType, { heading: boolean; subheading: boolean; body: boolean; cta: boolean; items: false | "items" | "faq" }> = {
  HERO: { heading: true, subheading: true, body: false, cta: true, items: false },
  ABOUT: { heading: true, subheading: false, body: true, cta: false, items: false },
  HIGHLIGHTS: { heading: true, subheading: true, body: false, cta: false, items: "items" },
  CATALOGUE: { heading: true, subheading: true, body: false, cta: false, items: false },
  PROCESS: { heading: true, subheading: true, body: false, cta: false, items: "items" },
  FAQ: { heading: true, subheading: true, body: false, cta: false, items: "faq" },
  CTA_BANNER: { heading: true, subheading: true, body: false, cta: true, items: false },
  HOURS_LOCATION: { heading: true, subheading: true, body: false, cta: false, items: false },
  CONTACT: { heading: true, subheading: true, body: false, cta: true, items: false },
};

const DEFAULT_IDS: Record<SectionType, string> = {
  HERO: "hero",
  ABOUT: "about",
  HIGHLIGHTS: "highlights",
  CATALOGUE: "catalogue",
  PROCESS: "process",
  FAQ: "faq",
  CTA_BANNER: "cta",
  HOURS_LOCATION: "hours",
  CONTACT: "contact",
};

export function newSection(type: SectionType, existingIds: string[]): Section {
  let id = DEFAULT_IDS[type];
  for (let n = 2; existingIds.includes(id); n++) id = `${DEFAULT_IDS[type]}-${n}`;
  const items =
    type === "FAQ"
      ? [{ title: "Your question here", body: "Your answer here." }]
      : type === "HIGHLIGHTS" || type === "PROCESS"
        ? [{ title: "Point one", body: "" }]
        : [];
  return { id, type, visible: true, heading: SECTION_LABELS[type], subheading: "", body: "", ctaLabel: type === "CTA_BANNER" ? "Get in touch" : "", items };
}
