"use client";

import { useCallback, useEffect, useMemo, useState } from "react";
import { ExternalLink, Laptop, Loader2, Smartphone, Sparkles, X } from "lucide-react";
import { toast } from "sonner";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs";
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog";
import { SiteRenderer } from "@/components/site/site-renderer";
import { api, ApiError, errorMessage } from "@/lib/api";
import type { PublicSite, SiteConfig, Website, WebsiteTemplate } from "@/lib/site-types";
import { cn } from "@/lib/utils";
import { ContentPanel } from "./content-panel";
import { DesignPanel } from "./design-panel";
import { AiPanel, type AiProposal } from "./ai-panel";

type Props = {
  businessId: string;
  initialWebsite: Website;
  initialPreview: PublicSite;
  templates: WebsiteTemplate[];
};

const SOURCE_LABEL: Record<Website["draftSource"], string> = {
  AI_GENERATED: "Written by AI",
  TEMPLATE_STARTER: "Template content",
  OWNER_EDITED: "Edited by you",
};

export function WebsiteEditor({ businessId, initialWebsite, initialPreview, templates }: Props) {
  const [website, setWebsite] = useState(initialWebsite);
  const [preview, setPreview] = useState(initialPreview);
  const [config, setConfig] = useState<SiteConfig>(initialWebsite.draft);
  const [device, setDevice] = useState<"desktop" | "mobile">("desktop");
  const [saving, setSaving] = useState(false);
  const [publishing, setPublishing] = useState(false);
  const [confirmPublish, setConfirmPublish] = useState(false);
  const [problems, setProblems] = useState<string[]>([]);
  const [conflict, setConflict] = useState(false);
  const [proposal, setProposal] = useState<AiProposal | null>(null);
  const [switchingTemplate, setSwitchingTemplate] = useState<string | null>(null);

  const dirty = useMemo(() => JSON.stringify(config) !== JSON.stringify(website.draft), [config, website.draft]);

  // Warn before leaving with unsaved changes.
  useEffect(() => {
    if (!dirty) return;
    const handler = (e: BeforeUnloadEvent) => e.preventDefault();
    window.addEventListener("beforeunload", handler);
    return () => window.removeEventListener("beforeunload", handler);
  }, [dirty]);

  const applySaved = useCallback((w: Website) => {
    setWebsite(w);
    setConfig(w.draft);
    setProblems([]);
    setConflict(false);
  }, []);

  async function reloadFromServer() {
    const [w, p] = await Promise.all([
      api<Website>(`/api/businesses/${businessId}/website`),
      api<PublicSite>(`/api/businesses/${businessId}/website/preview`),
    ]);
    applySaved(w);
    setPreview(p);
  }

  async function save(next: SiteConfig = config, expectedRevision = website.draftRevision): Promise<Website | null> {
    setSaving(true);
    try {
      const w = await api<Website>(`/api/businesses/${businessId}/website/draft`, {
        method: "PUT",
        body: { config: next, expectedRevision },
      });
      applySaved(w);
      return w;
    } catch (e) {
      if (e instanceof ApiError && e.status === 409) setConflict(true);
      else if (e instanceof ApiError && e.status === 422) setProblems(problemList(e));
      toast.error(errorMessage(e));
      return null;
    } finally {
      setSaving(false);
    }
  }

  async function publish() {
    setPublishing(true);
    try {
      if (dirty && !(await save())) return;
      const w = await api<Website>(`/api/businesses/${businessId}/website/publish`, { method: "POST" });
      applySaved(w);
      setConfirmPublish(false);
      toast.success(`Published version ${w.published?.version}`, {
        action: { label: "View", onClick: () => window.open(w.publicUrl, "_blank", "noopener") },
      });
    } catch (e) {
      if (e instanceof ApiError && e.status === 422) setProblems(problemList(e));
      toast.error(errorMessage(e));
    } finally {
      setPublishing(false);
    }
  }

  async function switchTemplate(templateId: string) {
    setSwitchingTemplate(templateId);
    try {
      applySaved(await api<Website>(`/api/businesses/${businessId}/website/draft/template`, { method: "POST", body: { templateId } }));
      toast.success("Template changed — your content was kept");
    } catch (e) {
      toast.error(errorMessage(e));
    } finally {
      setSwitchingTemplate(null);
    }
  }

  async function applyProposal() {
    if (!proposal) return;
    const saved = await save(proposal.config, proposal.baseRevision);
    if (saved) {
      setProposal(null);
      toast.success("AI change applied to your draft");
    }
  }

  const shownConfig = proposal?.config ?? config;
  const site: PublicSite = { ...preview, config: shownConfig };
  const busy = saving || publishing;
  const status = statusOf(website, dirty);

  return (
    <div className="flex h-svh flex-col">
      <div className="flex flex-wrap items-center gap-2 border-b px-4 py-2.5">
        <div className="mr-auto flex min-w-0 items-center gap-2">
          <h1 className="font-semibold">Website</h1>
          <Badge variant={status.variant}>{status.label}</Badge>
          <span className="hidden text-xs text-muted-foreground lg:inline">{SOURCE_LABEL[website.draftSource]}</span>
        </div>
        <div className="flex rounded-lg border p-0.5" role="radiogroup" aria-label="Preview size">
          {(["desktop", "mobile"] as const).map((d) => (
            <button
              key={d}
              role="radio"
              aria-checked={device === d}
              onClick={() => setDevice(d)}
              className={cn("flex items-center gap-1 rounded-md px-2 py-1 text-xs", device === d ? "bg-muted font-medium" : "text-muted-foreground")}
            >
              {d === "desktop" ? <Laptop className="size-3.5" /> : <Smartphone className="size-3.5" />}
              <span className="hidden sm:inline">{d === "desktop" ? "Desktop" : "Mobile"}</span>
            </button>
          ))}
        </div>
        {website.published && (
          <Button asChild variant="ghost" size="sm">
            <a href={website.publicUrl} target="_blank" rel="noopener noreferrer"><ExternalLink /> View live</a>
          </Button>
        )}
        <Button variant="outline" size="sm" disabled={!dirty || busy || !!proposal} onClick={() => void save()}>
          {saving && <Loader2 className="animate-spin" />} Save draft
        </Button>
        <Button size="sm" disabled={busy || !!proposal || (!dirty && !website.hasUnpublishedChanges)} onClick={() => setConfirmPublish(true)}>
          Publish
        </Button>
      </div>

      {conflict && (
        <div className="flex items-center justify-between gap-3 border-b bg-destructive/10 px-4 py-2 text-sm">
          <span>This draft was changed in another tab or by the AI assistant.</span>
          <Button size="sm" variant="outline" onClick={() => void reloadFromServer()}>Load latest (discards your edits)</Button>
        </div>
      )}
      {problems.length > 0 && (
        <div className="border-b bg-destructive/10 px-4 py-2 text-sm" role="alert">
          <p className="font-medium">Please fix these before saving:</p>
          <ul className="list-disc pl-5">{problems.map((p) => <li key={p}>{p}</li>)}</ul>
        </div>
      )}
      {proposal && (
        <div className="flex flex-wrap items-center gap-3 border-b bg-accent px-4 py-2 text-sm text-accent-foreground">
          <Sparkles className="size-4" />
          <span className="mr-auto">Previewing AI change: “{proposal.instruction}”</span>
          <Button size="sm" variant="outline" onClick={() => setProposal(null)}><X /> Discard</Button>
          <Button size="sm" onClick={() => void applyProposal()} disabled={saving}>{saving && <Loader2 className="animate-spin" />} Apply</Button>
        </div>
      )}

      <div className="flex min-h-0 flex-1 flex-col lg:flex-row">
        <aside className="max-h-[45vh] shrink-0 overflow-y-auto border-b p-4 lg:max-h-none lg:w-[22rem] lg:border-b-0 lg:border-r">
          <Tabs defaultValue="content">
            <TabsList className="mb-4 w-full">
              <TabsTrigger value="content">Content</TabsTrigger>
              <TabsTrigger value="design">Design</TabsTrigger>
              <TabsTrigger value="ai"><Sparkles /> AI edit</TabsTrigger>
            </TabsList>
            <TabsContent value="content">
              <ContentPanel config={config} onChange={setConfig} businessId={businessId} disabled={!!proposal} />
            </TabsContent>
            <TabsContent value="design">
              <DesignPanel
                config={config}
                onChange={setConfig}
                templates={templates}
                onTemplate={(id) => void switchTemplate(id)}
                switchingTemplate={switchingTemplate}
                templateDisabledReason={dirty ? "Save your changes before switching template." : undefined}
                disabled={!!proposal}
              />
            </TabsContent>
            <TabsContent value="ai">
              <AiPanel
                businessId={businessId}
                disabledReason={dirty ? "Save or undo your manual changes first — AI edits start from your saved draft." : proposal ? "Apply or discard the current suggestion first." : undefined}
                onProposal={setProposal}
              />
            </TabsContent>
          </Tabs>
        </aside>
        <div className="min-h-0 flex-1 overflow-auto bg-muted/50 p-3 sm:p-6">
          <div
            className={cn(
              "mx-auto h-full overflow-y-auto rounded-xl border bg-background shadow-sm transition-[max-width]",
              device === "mobile" ? "max-w-[390px]" : "max-w-6xl",
            )}
            aria-label={`${device} preview`}
          >
            <SiteRenderer site={site} mode="preview" />
          </div>
        </div>
      </div>

      <Dialog open={confirmPublish} onOpenChange={setConfirmPublish}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>Publish your website?</DialogTitle>
            <DialogDescription>
              Anyone with the link will see this version{dirty ? " (your unsaved changes will be saved first)" : ""}. Your website address:
            </DialogDescription>
          </DialogHeader>
          <p className="break-all rounded-lg bg-muted px-3 py-2 font-mono text-sm">{website.publicUrl}</p>
          <DialogFooter>
            <Button variant="outline" onClick={() => setConfirmPublish(false)}>Cancel</Button>
            <Button onClick={() => void publish()} disabled={publishing}>{publishing && <Loader2 className="animate-spin" />} Publish</Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </div>
  );
}

function problemList(e: ApiError): string[] {
  return e.problems.length ? e.problems : [e.message];
}

function statusOf(w: Website, dirty: boolean): { label: string; variant: "default" | "secondary" | "outline" } {
  if (dirty) return { label: "Unsaved changes", variant: "outline" };
  if (!w.published) return { label: "Not published", variant: "secondary" };
  if (w.hasUnpublishedChanges) return { label: `Live v${w.published.version} · draft has changes`, variant: "secondary" };
  return { label: `Live v${w.published.version}`, variant: "default" };
}
