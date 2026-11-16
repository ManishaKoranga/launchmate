import Link from "next/link";
import {
  ArrowRight,
  Check,
  Globe,
  Inbox,
  Languages,
  LayoutTemplate,
  MessageSquareText,
  Sparkles,
  TrendingUp,
} from "lucide-react";
import { Button } from "@/components/ui/button";
import { Logo } from "@/components/marketing/logo";

const steps = [
  {
    title: "Describe your business",
    body: "Type or speak naturally — English, Hindi or Hinglish. LaunchMate asks only what it still needs.",
  },
  {
    title: "Review your website",
    body: "Get a professional site built from proven templates, with your products, prices and contact details.",
  },
  {
    title: "Publish and receive inquiries",
    body: "Go live on a LaunchMate link or your own domain. Customer inquiries land in your inbox.",
  },
];

const features = [
  { icon: Languages, title: "Speaks your language", body: "Hinglish in, polished website out. No forms, no jargon." },
  { icon: LayoutTemplate, title: "Templates that convert", body: "Designed for bakeries, salons, tutors, home services, makers and freelancers." },
  { icon: Inbox, title: "Built-in inquiry inbox", body: "Every website inquiry is saved, tracked from New to Converted, never lost in chats." },
  { icon: MessageSquareText, title: "AI business assistant", body: "“Add a red velvet cake for ₹650” — and it's on your website. You confirm every change." },
  { icon: Globe, title: "Your own domain", body: "Start on a free LaunchMate link, connect your domain when you're ready." },
  { icon: TrendingUp, title: "Growth advice from real data", body: "Suggestions based on your actual inquiries and visits — never made-up numbers." },
];

const plans = [
  { name: "Free", price: "₹0", note: "forever", items: ["1 website on a LaunchMate link", "Up to 10 products", "Inquiry inbox", "Limited AI assistant"] },
  { name: "Starter", price: "₹299", note: "/month", items: ["Custom domain", "Up to 100 products", "More AI actions", "Remove LaunchMate badge"], featured: true },
  { name: "Growth", price: "₹799", note: "/month", items: ["Everything in Starter", "Growth recommendations", "Voice notes", "Priority support"] },
  { name: "Business", price: "Talk to us", note: "", items: ["Multiple businesses", "Team members", "Advanced analytics"] },
];

export default function LandingPage() {
  return (
    <div className="flex min-h-full flex-col">
      <header className="sticky top-0 z-30 border-b bg-background/85 backdrop-blur">
        <div className="mx-auto flex h-14 max-w-6xl items-center justify-between px-4 sm:px-6">
          <Logo />
          <nav className="hidden items-center gap-6 text-sm text-muted-foreground md:flex">
            <a href="#how" className="hover:text-foreground">How it works</a>
            <a href="#features" className="hover:text-foreground">Features</a>
            <a href="#pricing" className="hover:text-foreground">Pricing</a>
          </nav>
          <div className="flex items-center gap-2">
            <Button asChild variant="ghost" size="sm"><Link href="/login">Sign in</Link></Button>
            <Button asChild size="sm"><Link href="/signup">Start free</Link></Button>
          </div>
        </div>
      </header>

      <main className="flex-1">
        <section className="mx-auto grid max-w-6xl items-center gap-12 px-4 pb-20 pt-16 sm:px-6 lg:grid-cols-[1.1fr_1fr] lg:pt-24">
          <div>
            <p className="mb-4 inline-flex items-center gap-1.5 rounded-full border bg-muted px-3 py-1 text-xs font-medium text-muted-foreground">
              <Sparkles className="size-3.5 text-primary" /> For home businesses, shops and freelancers
            </p>
            <h1 className="text-4xl font-semibold tracking-tight text-balance sm:text-5xl">
              Your business deserves to be online. Let AI take you there.
            </h1>
            <p className="mt-5 max-w-xl text-lg text-muted-foreground text-pretty">
              Tell LaunchMate about your business in your own words. Get a professional website, a product catalogue
              and an inbox for customer inquiries — in minutes, without technical skills.
            </p>
            <div className="mt-8 flex flex-wrap gap-3">
              <Button asChild size="lg" className="h-11 px-5 text-base">
                <Link href="/signup">Start Building for Free <ArrowRight /></Link>
              </Button>
              <Button asChild size="lg" variant="outline" className="h-11 px-5 text-base">
                <a href="#how">See how it works</a>
              </Button>
            </div>
          </div>

          <div className="rounded-2xl border bg-card p-5 shadow-sm">
            <div className="rounded-xl bg-muted p-4 text-sm">
              <p className="mb-1 text-xs font-medium text-muted-foreground">You</p>
              <p>
                Main Deoband mein home bakery start karna chahti hoon. Customised cakes, cupcakes aur brownies banati
                hoon, mostly local families ke liye.
              </p>
            </div>
            <div className="mt-3 rounded-xl border p-4 text-sm">
              <p className="mb-2 text-xs font-medium text-muted-foreground">LaunchMate understood</p>
              <dl className="grid grid-cols-[auto_1fr] gap-x-4 gap-y-1.5">
                <dt className="text-muted-foreground">Business</dt><dd>Home bakery</dd>
                <dt className="text-muted-foreground">Location</dt><dd>Deoband</dd>
                <dt className="text-muted-foreground">Offers</dt><dd>Custom cakes, cupcakes, brownies</dd>
                <dt className="text-muted-foreground">Customers</dt><dd>Local families</dd>
              </dl>
              <p className="mt-3 text-muted-foreground">Which phone number should customers use to reach you?</p>
            </div>
            <p className="mt-3 text-center text-xs text-muted-foreground">Example conversation</p>
          </div>
        </section>

        <section id="how" className="border-t bg-muted/40">
          <div className="mx-auto max-w-6xl px-4 py-20 sm:px-6">
            <h2 className="text-2xl font-semibold tracking-tight sm:text-3xl">From idea to inquiries in three steps</h2>
            <ol className="mt-10 grid gap-6 md:grid-cols-3">
              {steps.map((s, i) => (
                <li key={s.title} className="rounded-xl border bg-card p-6">
                  <span className="grid size-8 place-items-center rounded-full bg-primary/10 text-sm font-semibold text-primary">
                    {i + 1}
                  </span>
                  <h3 className="mt-4 font-medium">{s.title}</h3>
                  <p className="mt-2 text-sm text-muted-foreground">{s.body}</p>
                </li>
              ))}
            </ol>
          </div>
        </section>

        <section id="features" className="mx-auto max-w-6xl px-4 py-20 sm:px-6">
          <h2 className="text-2xl font-semibold tracking-tight sm:text-3xl">Everything a small business needs to get found</h2>
          <div className="mt-10 grid gap-x-8 gap-y-10 sm:grid-cols-2 lg:grid-cols-3">
            {features.map((f) => (
              <div key={f.title}>
                <f.icon className="size-5 text-primary" />
                <h3 className="mt-3 font-medium">{f.title}</h3>
                <p className="mt-1.5 text-sm text-muted-foreground">{f.body}</p>
              </div>
            ))}
          </div>
        </section>

        <section id="pricing" className="border-t bg-muted/40">
          <div className="mx-auto max-w-6xl px-4 py-20 sm:px-6">
            <h2 className="text-2xl font-semibold tracking-tight sm:text-3xl">Simple pricing</h2>
            <p className="mt-2 text-muted-foreground">
              Start free. Paid plans are coming soon — everything is free during early access.
            </p>
            <div className="mt-10 grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
              {plans.map((p) => (
                <div
                  key={p.name}
                  className={`flex flex-col rounded-xl border bg-card p-6 ${p.featured ? "border-primary ring-1 ring-primary" : ""}`}
                >
                  <h3 className="font-medium">{p.name}</h3>
                  <p className="mt-3 text-2xl font-semibold">
                    {p.price} <span className="text-sm font-normal text-muted-foreground">{p.note}</span>
                  </p>
                  <ul className="mt-5 space-y-2 text-sm">
                    {p.items.map((it) => (
                      <li key={it} className="flex gap-2"><Check className="mt-0.5 size-4 shrink-0 text-primary" />{it}</li>
                    ))}
                  </ul>
                </div>
              ))}
            </div>
            <div className="mt-10">
              <Button asChild size="lg"><Link href="/signup">Start Building for Free <ArrowRight /></Link></Button>
            </div>
          </div>
        </section>
      </main>

      <footer className="border-t">
        <div className="mx-auto flex max-w-6xl flex-col items-start justify-between gap-2 px-4 py-8 text-sm text-muted-foreground sm:flex-row sm:px-6">
          <Logo />
          <p>Made for India&apos;s small businesses.</p>
        </div>
      </footer>
    </div>
  );
}
