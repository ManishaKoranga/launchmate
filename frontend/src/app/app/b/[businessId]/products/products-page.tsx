"use client";

import { useCallback, useEffect, useState } from "react";
import { ImageIcon, Loader2, Package, Pencil, Plus, Trash2 } from "lucide-react";
import { toast } from "sonner";
import { useBusiness } from "@/components/app/business-context";
import { PageHeader } from "@/components/app/page-header";
import { EmptyState, ErrorState } from "@/components/app/states";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import { Switch } from "@/components/ui/switch";
import { Skeleton } from "@/components/ui/skeleton";
import { Dialog, DialogContent, DialogDescription, DialogFooter, DialogHeader, DialogTitle } from "@/components/ui/dialog";
import { api, errorMessage } from "@/lib/api";
import type { Page, Product } from "@/lib/types";
import { formatPrice } from "@/components/site/format";
import { ProductDialog } from "./product-dialog";

export function ProductsPage() {
  const { business } = useBusiness();
  const base = `/api/businesses/${business.id}/products`;
  const [items, setItems] = useState<Product[] | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [editing, setEditing] = useState<Product | null>(null);
  const [dialogOpen, setDialogOpen] = useState(false);
  const [deleting, setDeleting] = useState<Product | null>(null);
  const [busyId, setBusyId] = useState<string | null>(null);

  const load = useCallback(
    () =>
      api<Page<Product>>(`${base}?size=100`).then(
        (p) => {
          setItems(p.items);
          setError(null);
        },
        (e) => setError(errorMessage(e)),
      ),
    [base],
  );

  useEffect(() => {
    load();
  }, [load]);

  const upsert = (p: Product) =>
    setItems((list) => (list?.some((x) => x.id === p.id) ? list.map((x) => (x.id === p.id ? p : x)) : [...(list ?? []), p]));

  async function toggleVisible(p: Product, visible: boolean) {
    setBusyId(p.id);
    try {
      upsert(await api<Product>(`${base}/${p.id}`, {
        method: "PUT",
        body: { kind: p.kind, name: p.name, description: p.description, price: p.price, priceNote: p.priceNote, category: p.category, visible },
      }));
    } catch (e) {
      toast.error(errorMessage(e));
    } finally {
      setBusyId(null);
    }
  }

  async function confirmDelete() {
    if (!deleting) return;
    setBusyId(deleting.id);
    try {
      await api(`${base}/${deleting.id}`, { method: "DELETE" });
      setItems((list) => list?.filter((x) => x.id !== deleting.id) ?? null);
      toast.success(`Deleted “${deleting.name}”`);
      setDeleting(null);
    } catch (e) {
      toast.error(errorMessage(e));
    } finally {
      setBusyId(null);
    }
  }

  const openCreate = () => {
    setEditing(null);
    setDialogOpen(true);
  };

  return (
    <>
      <PageHeader
        title="Products & services"
        description="What you sell. Visible items appear on your website immediately."
        actions={<Button onClick={openCreate}><Plus /> Add item</Button>}
      />
      <div className="p-4 sm:p-8">
        {error ? (
          <ErrorState message={error} onRetry={() => void load()} className="mx-auto py-12" />
        ) : items === null ? (
          <div className="grid gap-2">{[0, 1, 2].map((i) => <Skeleton key={i} className="h-20 rounded-xl" />)}</div>
        ) : items.length === 0 ? (
          <EmptyState icon={Package} title="No products or services yet" description="Add what you sell with prices and photos so customers know what to ask for." action={<Button onClick={openCreate}><Plus /> Add your first item</Button>} />
        ) : (
          <ul className="divide-y rounded-xl border bg-card">
            {items.map((p) => (
              <li key={p.id} className="flex items-center gap-3 p-3 sm:gap-4 sm:p-4">
                {p.imageUrl ? (
                  // eslint-disable-next-line @next/next/no-img-element -- served by our API
                  <img src={p.imageUrl} alt="" className="size-14 shrink-0 rounded-lg border object-cover" />
                ) : (
                  <div className="grid size-14 shrink-0 place-items-center rounded-lg bg-muted text-muted-foreground"><ImageIcon className="size-5" /></div>
                )}
                <div className="min-w-0 flex-1">
                  <p className="flex items-center gap-2 truncate font-medium">
                    {p.name}
                    {p.kind === "SERVICE" && <Badge variant="secondary">Service</Badge>}
                  </p>
                  <p className="truncate text-sm text-muted-foreground">
                    {formatPrice(p.price) ?? "Ask for price"}
                    {p.priceNote ? ` · ${p.priceNote}` : ""}
                  </p>
                </div>
                <label className="hidden items-center gap-2 text-xs text-muted-foreground sm:flex">
                  {busyId === p.id ? <Loader2 className="size-4 animate-spin" /> : <Switch checked={p.visible} onCheckedChange={(v) => void toggleVisible(p, v)} aria-label={`Show ${p.name} on website`} />}
                  {p.visible ? "Visible" : "Hidden"}
                </label>
                <Button variant="ghost" size="icon" aria-label={`Edit ${p.name}`} onClick={() => { setEditing(p); setDialogOpen(true); }}><Pencil /></Button>
                <Button variant="ghost" size="icon" aria-label={`Delete ${p.name}`} onClick={() => setDeleting(p)}><Trash2 /></Button>
              </li>
            ))}
          </ul>
        )}
      </div>

      <ProductDialog businessId={business.id} product={editing} open={dialogOpen} onOpenChange={setDialogOpen} onSaved={upsert} />

      <Dialog open={!!deleting} onOpenChange={(o) => !o && setDeleting(null)}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>Delete “{deleting?.name}”?</DialogTitle>
            <DialogDescription>It will be removed from your website. Past inquiries keep the item name.</DialogDescription>
          </DialogHeader>
          <DialogFooter>
            <Button variant="outline" onClick={() => setDeleting(null)}>Cancel</Button>
            <Button variant="destructive" onClick={() => void confirmDelete()} disabled={busyId === deleting?.id}>Delete</Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </>
  );
}
