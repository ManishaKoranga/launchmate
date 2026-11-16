import { Suspense } from "react";
import type { Metadata } from "next";
import { notFound } from "next/navigation";
import { SiteRenderer } from "@/components/site/site-renderer";
import { fetchPublishedSite } from "@/lib/server-api";

export async function generateMetadata({ params }: PageProps<"/s/[slug]">): Promise<Metadata> {
  const { slug } = await params;
  const site = await fetchPublishedSite(slug);
  if (!site) return { title: "Website not found" };
  return {
    title: { absolute: site.config.seo.title },
    description: site.config.seo.description,
    openGraph: { title: site.config.seo.title, description: site.config.seo.description, type: "website" },
  };
}

export default function PublicSitePage({ params }: PageProps<"/s/[slug]">) {
  return (
    <Suspense fallback={<div className="min-h-svh" aria-busy="true" />}>
      <PublishedSite params={params} />
    </Suspense>
  );
}

async function PublishedSite({ params }: { params: PageProps<"/s/[slug]">["params"] }) {
  const { slug } = await params;
  const site = await fetchPublishedSite(slug);
  if (!site) notFound();
  return <SiteRenderer site={site} mode="public" />;
}
