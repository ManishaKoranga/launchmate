import type { PublicSite, Section, SectionType } from "@/lib/site-types";
import { About, Catalogue, Contact, CtaBanner, Faq, Hero, Highlights, HoursLocation, Process } from "./sections";
import { themeStyle } from "./theme";

type SectionComponent = (props: { section: Section; site: PublicSite; mode: "public" | "preview" }) => React.ReactNode;

/** One component per section type; templates differ by data (theme, order, copy), not by code. */
const SECTIONS: Record<SectionType, SectionComponent> = {
  HERO: Hero,
  ABOUT: About,
  HIGHLIGHTS: Highlights,
  CATALOGUE: Catalogue,
  PROCESS: Process,
  FAQ: Faq,
  CTA_BANNER: CtaBanner,
  HOURS_LOCATION: HoursLocation,
  CONTACT: Contact,
};

const NAV_TYPES: SectionType[] = ["ABOUT", "CATALOGUE", "PROCESS", "FAQ", "HOURS_LOCATION"];
const NAV_LABELS: Partial<Record<SectionType, string>> = {
  ABOUT: "About",
  CATALOGUE: "Menu",
  PROCESS: "How it works",
  FAQ: "FAQ",
  HOURS_LOCATION: "Visit",
};

/**
 * Renders a business website from template + saved config + live catalogue. Uses container queries
 * (`@3xl:` etc.) so the same markup is responsive inside the editor's device frames.
 */
export function SiteRenderer({ site, mode }: { site: PublicSite; mode: "public" | "preview" }) {
  const sections = site.config.sections.filter((s) => s.visible);
  const nav = sections.filter((s) => NAV_TYPES.includes(s.type) && !(s.type === "CATALOGUE" && site.products.length === 0 && mode === "public"));
  const catalogueLabel = site.business.category === "BAKERY_FOOD" ? "Menu" : site.products.some((p) => p.kind === "SERVICE") ? "Services" : "Products";

  return (
    <div
      style={themeStyle(site.config.theme)}
      className="@container min-h-full bg-[var(--site-bg)] font-[family-name:var(--site-body-font)] text-[var(--site-fg)]"
    >
      <header className="sticky top-0 z-20 border-b border-[var(--site-border)] bg-[var(--site-bg)]/90 backdrop-blur">
        <div className="mx-auto flex h-16 w-full max-w-5xl items-center justify-between gap-4 px-5 @3xl:px-8">
          <a href="#" className="truncate font-[family-name:var(--site-heading-font)] text-lg font-semibold">
            {site.business.name}
          </a>
          <nav className="hidden items-center gap-6 text-sm text-[var(--site-muted)] @3xl:flex">
            {nav.map((s) => (
              <a key={s.id} href={`#${s.id}`} className="hover:text-[var(--site-fg)]">
                {s.type === "CATALOGUE" ? catalogueLabel : NAV_LABELS[s.type]}
              </a>
            ))}
          </nav>
          <a
            href="#contact"
            className="inline-flex h-9 shrink-0 items-center rounded-[var(--site-radius)] bg-[var(--site-primary)] px-4 text-sm font-medium text-white hover:opacity-90"
          >
            Contact
          </a>
        </div>
      </header>
      <main>
        {sections.map((s) => {
          const Component = SECTIONS[s.type];
          return <Component key={s.id} section={s} site={site} mode={mode} />;
        })}
      </main>
      <footer className="border-t border-[var(--site-border)] py-8 text-center text-xs text-[var(--site-muted)]">
        <p>© {site.business.name}{site.business.city ? ` · ${site.business.city}` : ""}</p>
        <p className="mt-1">
          Website made with{" "}
          <a href={process.env.NEXT_PUBLIC_APP_URL ?? "/"} className="underline underline-offset-2">LaunchMate</a>
        </p>
      </footer>
    </div>
  );
}
