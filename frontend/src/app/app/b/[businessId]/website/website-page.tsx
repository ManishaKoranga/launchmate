"use client";

import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import { LayoutTemplate, Loader2, Sparkles } from "lucide-react";
import { useBusiness } from "@/components/app/business-context";
import { EmptyState, FullPageError, FullPageSpinner } from "@/components/app/states";
import { PageHeader } from "@/components/app/page-header";
import { WebsiteEditor } from "@/components/editor/website-editor";
import { Button } from "@/components/ui/button";
import { api, ApiError, errorMessage } from "@/lib/api";
import type { GenerationJob, PublicSite, Website, WebsiteTemplate } from "@/lib/site-types";

type Loaded = { website: Website; preview: PublicSite; templates: WebsiteTemplate[] } | "none";

export function WebsitePage() {
  const { business } = useBusiness();
  const router = useRouter();
  const [data, setData] = useState<Loaded | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [starting, setStarting] = useState<"AI" | "TEMPLATE" | null>(null);

  useEffect(() => {
    const base = `/api/businesses/${business.id}`;
    Promise.all([api<Website>(`${base}/website`), api<PublicSite>(`${base}/website/preview`), api<WebsiteTemplate[]>("/api/templates")])
      .then(([website, preview, templates]) => setData({ website, preview, templates }))
      .catch((e) => {
        if (e instanceof ApiError && e.status === 404) setData("none");
        else setError(errorMessage(e));
      });
  }, [business.id]);

  async function generate(mode: "AI" | "TEMPLATE") {
    setStarting(mode);
    try {
      const job = await api<GenerationJob>(`/api/businesses/${business.id}/website/generation-jobs`, { method: "POST", body: { mode } });
      router.push(`/app/b/${business.id}/website/generate/${job.id}`);
    } catch (e) {
      setError(errorMessage(e));
      setStarting(null);
    }
  }

  if (error) return <FullPageError message={error} />;
  if (!data) return <FullPageSpinner />;
  if (data === "none") {
    return (
      <>
        <PageHeader title="Website" description="Create your website from your business profile." />
        <div className="p-4 sm:p-8">
          <EmptyState
            icon={LayoutTemplate}
            title="You don't have a website yet"
            description="Let AI write your website from your business profile, or start from a ready-made template and edit it yourself."
            action={
              <div className="flex flex-wrap justify-center gap-2">
                <Button onClick={() => void generate("AI")} disabled={starting !== null}>
                  {starting === "AI" ? <Loader2 className="animate-spin" /> : <Sparkles />} Create with AI
                </Button>
                <Button variant="outline" onClick={() => void generate("TEMPLATE")} disabled={starting !== null}>
                  {starting === "TEMPLATE" && <Loader2 className="animate-spin" />} Use template content
                </Button>
              </div>
            }
          />
        </div>
      </>
    );
  }
  return <WebsiteEditor businessId={business.id} initialWebsite={data.website} initialPreview={data.preview} templates={data.templates} />;
}
