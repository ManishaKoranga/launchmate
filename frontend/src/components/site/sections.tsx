import {
  Briefcase,
  CakeSlice,
  Check,
  Clock,
  Gem,
  GraduationCap,
  Mail,
  MapPin,
  MessageCircle,
  Phone,
  Scissors,
  Wrench,
  type LucideIcon,
} from "lucide-react";
import type { BusinessCategory } from "@/lib/types";
import type { PublicProduct, PublicSite, Section } from "@/lib/site-types";
import { formatPrice, hoursRows, telLink, whatsappLink } from "./format";
import { InquiryForm } from "./inquiry-form";

const CATEGORY_ICONS: Record<BusinessCategory, LucideIcon> = {
  BAKERY_FOOD: CakeSlice,
  BEAUTY_SALON: Scissors,
  HOME_SERVICES: Wrench,
  EDUCATION_TUTORING: GraduationCap,
  HANDMADE_RETAIL: Gem,
  PROFESSIONAL_SERVICES: Briefcase,
};

type SectionProps = { section: Section; site: PublicSite; mode: "public" | "preview" };

const heading = "font-[family-name:var(--site-heading-font)] tracking-tight";
const wrap = "mx-auto w-full max-w-5xl px-5 @3xl:px-8";
const card = "rounded-[var(--site-radius)] border border-[var(--site-border)] bg-[var(--site-bg)]";

export function SectionHeading({ section, center = false }: { section: Section; center?: boolean }) {
  return (
    <div className={center ? "mx-auto max-w-2xl text-center" : "max-w-2xl"}>
      {section.heading && <h2 className={`${heading} text-2xl font-semibold @3xl:text-3xl`}>{section.heading}</h2>}
      {section.subheading && <p className="mt-2 text-[var(--site-muted)]">{section.subheading}</p>}
    </div>
  );
}

function PrimaryButton({ href, children }: { href: string; children: React.ReactNode }) {
  return (
    <a
      href={href}
      className="inline-flex h-11 items-center justify-center gap-2 rounded-[var(--site-radius)] bg-[var(--site-primary)] px-5 text-sm font-medium text-white transition-opacity hover:opacity-90"
    >
      {children}
    </a>
  );
}

function SecondaryButton({ href, children, external }: { href: string; children: React.ReactNode; external?: boolean }) {
  return (
    <a
      href={href}
      {...(external ? { target: "_blank", rel: "noopener noreferrer" } : {})}
      className="inline-flex h-11 items-center justify-center gap-2 rounded-[var(--site-radius)] border border-[var(--site-border)] bg-[var(--site-bg)] px-5 text-sm font-medium transition-colors hover:bg-[var(--site-surface)]"
    >
      {children}
    </a>
  );
}

export function Hero({ section, site }: SectionProps) {
  const Icon = CATEGORY_ICONS[site.business.category];
  const featured = site.products.slice(0, 3);
  const location = [site.business.area, site.business.city].filter(Boolean).join(", ");
  return (
    <section id={section.id} className="bg-[var(--site-surface)]">
      <div className={`${wrap} grid items-center gap-10 py-14 @3xl:grid-cols-[1.2fr_1fr] @3xl:py-20`}>
        <div>
          {location && (
            <p className="mb-4 inline-flex items-center gap-1.5 text-sm text-[var(--site-muted)]">
              <MapPin className="size-4" /> {location}
            </p>
          )}
          <h1 className={`${heading} text-4xl font-semibold leading-tight text-balance @3xl:text-5xl`}>
            {section.heading || site.business.name}
          </h1>
          {section.subheading && <p className="mt-4 text-lg text-[var(--site-muted)] text-pretty">{section.subheading}</p>}
          <div className="mt-8 flex flex-wrap gap-3">
            <PrimaryButton href="#contact">{section.ctaLabel || "Get in touch"}</PrimaryButton>
            {site.business.whatsapp && (
              <SecondaryButton href={whatsappLink(site.business.whatsapp, `Hi ${site.business.name}!`)} external>
                <MessageCircle className="size-4" /> WhatsApp
              </SecondaryButton>
            )}
          </div>
        </div>
        <div className={`${card} p-6`}>
          <div className="flex items-center gap-3">
            <span className="grid size-12 place-items-center rounded-[var(--site-radius)] bg-[var(--site-accent)]/30 text-[var(--site-primary)]">
              <Icon className="size-6" />
            </span>
            <div>
              <p className={`${heading} font-semibold`}>{site.business.name}</p>
              {site.business.tagline && <p className="text-sm text-[var(--site-muted)]">{site.business.tagline}</p>}
            </div>
          </div>
          {featured.length > 0 && (
            <ul className="mt-6 divide-y divide-[var(--site-border)] text-sm">
              {featured.map((p) => (
                <li key={p.id} className="flex items-baseline justify-between gap-4 py-2.5">
                  <span>{p.name}</span>
                  {p.price != null && (
                    <span className="shrink-0 font-medium text-[var(--site-primary)]">{formatPrice(p.price)}</span>
                  )}
                </li>
              ))}
            </ul>
          )}
        </div>
      </div>
    </section>
  );
}

export function About({ section }: SectionProps) {
  return (
    <section id={section.id} className="py-16">
      <div className={`${wrap} grid gap-6 @3xl:grid-cols-[1fr_1.4fr]`}>
        <SectionHeading section={section} />
        {section.body && <p className="whitespace-pre-line leading-relaxed text-[var(--site-muted)]">{section.body}</p>}
      </div>
    </section>
  );
}

export function Highlights({ section }: SectionProps) {
  return (
    <section id={section.id} className="py-16">
      <div className={wrap}>
        <SectionHeading section={section} />
        <div className="mt-8 grid gap-4 @2xl:grid-cols-2 @4xl:grid-cols-3">
          {section.items.map((item, i) => (
            <div key={i} className={`${card} p-5`}>
              <span className="grid size-8 place-items-center rounded-full bg-[var(--site-primary)] text-white">
                <Check className="size-4" />
              </span>
              <h3 className="mt-4 font-semibold">{item.title}</h3>
              {item.body && <p className="mt-1.5 text-sm text-[var(--site-muted)]">{item.body}</p>}
            </div>
          ))}
        </div>
      </div>
    </section>
  );
}

function ProductCard({ product }: { product: PublicProduct }) {
  const price = formatPrice(product.price);
  return (
    <div className={`${card} flex flex-col overflow-hidden`}>
      {product.imageUrl && (
        // eslint-disable-next-line @next/next/no-img-element -- user uploads served by the API
        <img src={product.imageUrl} alt={product.name} className="aspect-[4/3] w-full object-cover" loading="lazy" />
      )}
      <div className="flex flex-1 flex-col p-5">
        <h3 className="font-semibold">{product.name}</h3>
        {product.description && <p className="mt-1.5 text-sm text-[var(--site-muted)]">{product.description}</p>}
        <div className="mt-auto flex items-end justify-between gap-3 pt-4">
          {price ? (
            <p>
              <span className="font-semibold text-[var(--site-primary)]">{price}</span>
              {product.priceNote && <span className="ml-1 text-xs text-[var(--site-muted)]">{product.priceNote}</span>}
            </p>
          ) : (
            <p className="text-sm text-[var(--site-muted)]">{product.priceNote || "Ask for price"}</p>
          )}
          <a href="#contact" className="shrink-0 text-sm font-medium text-[var(--site-primary)] hover:underline">
            Enquire
          </a>
        </div>
      </div>
    </div>
  );
}

export function Catalogue({ section, site, mode }: SectionProps) {
  if (site.products.length === 0 && mode === "public") return null;
  return (
    <section id={section.id} className="bg-[var(--site-surface)] py-16">
      <div className={wrap}>
        <SectionHeading section={section} />
        {site.products.length === 0 ? (
          <p className={`${card} mt-8 border-dashed p-8 text-center text-sm text-[var(--site-muted)]`}>
            Your products and services will appear here. Add them from the Products page.
          </p>
        ) : (
          <div className="mt-8 grid gap-4 @2xl:grid-cols-2 @4xl:grid-cols-3">
            {site.products.map((p) => (
              <ProductCard key={p.id} product={p} />
            ))}
          </div>
        )}
      </div>
    </section>
  );
}

export function Process({ section }: SectionProps) {
  return (
    <section id={section.id} className="py-16">
      <div className={wrap}>
        <SectionHeading section={section} />
        <ol className="mt-8 grid gap-6 @3xl:grid-cols-3">
          {section.items.map((item, i) => (
            <li key={i} className="flex gap-4">
              <span className="grid size-9 shrink-0 place-items-center rounded-full border-2 border-[var(--site-primary)] text-sm font-semibold text-[var(--site-primary)]">
                {i + 1}
              </span>
              <div>
                <h3 className="font-semibold">{item.title}</h3>
                {item.body && <p className="mt-1 text-sm text-[var(--site-muted)]">{item.body}</p>}
              </div>
            </li>
          ))}
        </ol>
      </div>
    </section>
  );
}

export function Faq({ section }: SectionProps) {
  return (
    <section id={section.id} className="bg-[var(--site-surface)] py-16">
      <div className={`${wrap} max-w-3xl`}>
        <SectionHeading section={section} />
        <div className="mt-8 space-y-3">
          {section.items.map((item, i) => (
            <details key={i} className={`${card} group p-5`}>
              <summary className="flex cursor-pointer list-none items-center justify-between gap-4 font-medium">
                {item.title}
                <span className="text-[var(--site-muted)] transition-transform group-open:rotate-45" aria-hidden>+</span>
              </summary>
              {item.body && <p className="mt-3 text-sm text-[var(--site-muted)]">{item.body}</p>}
            </details>
          ))}
        </div>
      </div>
    </section>
  );
}

export function CtaBanner({ section }: SectionProps) {
  return (
    <section id={section.id} className="py-16">
      <div className={wrap}>
        <div className="flex flex-col items-start gap-6 rounded-[var(--site-radius)] bg-[var(--site-primary)] p-8 text-white @3xl:flex-row @3xl:items-center @3xl:justify-between @3xl:p-10">
          <div>
            {section.heading && <h2 className={`${heading} text-2xl font-semibold`}>{section.heading}</h2>}
            {section.subheading && <p className="mt-2 text-white/85">{section.subheading}</p>}
          </div>
          <a
            href="#contact"
            className="inline-flex h-11 shrink-0 items-center rounded-[var(--site-radius)] bg-white px-5 text-sm font-medium text-[var(--site-primary)] hover:bg-white/90"
          >
            {section.ctaLabel || "Get in touch"}
          </a>
        </div>
      </div>
    </section>
  );
}

export function HoursLocation({ section, site, mode }: SectionProps) {
  const b = site.business;
  const rows = b.openingHours?.length ? hoursRows(b.openingHours) : [];
  const place = [b.address, b.area, b.city].filter(Boolean).join(", ");
  if (rows.length === 0 && !place) {
    if (mode === "public") return null;
    return (
      <section id={section.id} className="py-16">
        <div className={wrap}>
          <SectionHeading section={section} />
          <p className={`${card} mt-6 border-dashed p-6 text-sm text-[var(--site-muted)]`}>
            Add opening hours or an address in your business profile to show them here.
          </p>
        </div>
      </section>
    );
  }
  return (
    <section id={section.id} className="py-16">
      <div className={`${wrap} grid gap-8 @3xl:grid-cols-2`}>
        <div>
          <SectionHeading section={section} />
          {place && (
            <p className="mt-6 flex gap-2 text-[var(--site-muted)]">
              <MapPin className="mt-0.5 size-4 shrink-0" /> {place}
            </p>
          )}
        </div>
        {rows.length > 0 && (
          <div className={`${card} p-5`}>
            <p className="mb-3 flex items-center gap-2 font-medium"><Clock className="size-4" /> Opening hours</p>
            <dl className="grid grid-cols-[auto_1fr] gap-x-6 gap-y-1.5 text-sm">
              {rows.map((r) => (
                <div key={r.day} className="contents">
                  <dt className="text-[var(--site-muted)]">{r.day}</dt>
                  <dd className="text-right">{r.value}</dd>
                </div>
              ))}
            </dl>
          </div>
        )}
      </div>
    </section>
  );
}

export function Contact({ section, site, mode }: SectionProps) {
  const b = site.business;
  return (
    <section id="contact" className="bg-[var(--site-surface)] py-16">
      <div className={`${wrap} grid gap-10 @3xl:grid-cols-[1fr_1.3fr]`}>
        <div>
          <SectionHeading section={section} />
          <ul className="mt-6 space-y-3 text-sm">
            {b.phone && (
              <li>
                <a href={telLink(b.phone)} className="inline-flex items-center gap-2 hover:underline">
                  <Phone className="size-4 text-[var(--site-primary)]" /> {b.phone}
                </a>
              </li>
            )}
            {b.whatsapp && (
              <li>
                <a href={whatsappLink(b.whatsapp)} target="_blank" rel="noopener noreferrer" className="inline-flex items-center gap-2 hover:underline">
                  <MessageCircle className="size-4 text-[var(--site-primary)]" /> WhatsApp {b.whatsapp}
                </a>
              </li>
            )}
            {b.email && (
              <li>
                <a href={`mailto:${b.email}`} className="inline-flex items-center gap-2 hover:underline">
                  <Mail className="size-4 text-[var(--site-primary)]" /> {b.email}
                </a>
              </li>
            )}
          </ul>
        </div>
        <div className={`${card} p-6`}>
          <InquiryForm
            slug={site.slug}
            products={site.products}
            submitLabel={section.ctaLabel || "Send inquiry"}
            disabled={mode === "preview"}
          />
        </div>
      </div>
    </section>
  );
}
