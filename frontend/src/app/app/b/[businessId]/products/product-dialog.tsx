"use client";

import { useRef, useState } from "react";
import { Controller, useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { z } from "zod";
import { ImagePlus, Loader2, Trash2 } from "lucide-react";
import { toast } from "sonner";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Textarea } from "@/components/ui/textarea";
import { Switch } from "@/components/ui/switch";
import { Alert, AlertDescription } from "@/components/ui/alert";
import { Dialog, DialogContent, DialogDescription, DialogFooter, DialogHeader, DialogTitle } from "@/components/ui/dialog";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import { FormField } from "@/components/app/form-field";
import { api, errorMessage } from "@/lib/api";
import { applyServerErrors } from "@/lib/forms";
import type { Product } from "@/lib/types";

const schema = z.object({
  kind: z.enum(["PRODUCT", "SERVICE"]),
  name: z.string().trim().min(1, "Name is required").max(120),
  description: z.string().max(1000).optional(),
  price: z
    .string()
    .trim()
    .regex(/^(\d{1,10}(\.\d{1,2})?)?$/, "Enter a price like 650 or 99.50")
    .optional(),
  priceNote: z.string().max(60).optional(),
  category: z.string().max(60).optional(),
  visible: z.boolean(),
});
type Values = z.infer<typeof schema>;

const ACCEPT = "image/jpeg,image/png,image/webp";
const MAX_BYTES = 5 * 1024 * 1024;

type Props = {
  businessId: string;
  product: Product | null; // null = create
  open: boolean;
  onOpenChange: (open: boolean) => void;
  onSaved: (p: Product) => void;
};

export function ProductDialog({ businessId, product, open, onOpenChange, onSaved }: Props) {
  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="max-h-[90svh] overflow-y-auto sm:max-w-lg">
        {open && <ProductForm key={product?.id ?? "new"} businessId={businessId} product={product} onSaved={onSaved} onClose={() => onOpenChange(false)} />}
      </DialogContent>
    </Dialog>
  );
}

function ProductForm({ businessId, product, onSaved, onClose }: { businessId: string; product: Product | null; onSaved: (p: Product) => void; onClose: () => void }) {
  const [current, setCurrent] = useState<Product | null>(product);
  const [uploading, setUploading] = useState(false);
  const fileRef = useRef<HTMLInputElement>(null);
  const form = useForm<Values>({
    resolver: zodResolver(schema),
    defaultValues: {
      kind: product?.kind ?? "PRODUCT",
      name: product?.name ?? "",
      description: product?.description ?? "",
      price: product?.price != null ? String(product.price) : "",
      priceNote: product?.priceNote ?? "",
      category: product?.category ?? "",
      visible: product?.visible ?? true,
    },
  });
  const { errors, isSubmitting } = form.formState;
  const base = `/api/businesses/${businessId}/products`;

  async function submit(v: Values) {
    const body = {
      kind: v.kind,
      name: v.name.trim(),
      description: v.description?.trim() || undefined,
      price: v.price ? Number(v.price) : undefined,
      priceNote: v.priceNote?.trim() || undefined,
      category: v.category?.trim() || undefined,
      visible: v.visible,
    };
    try {
      const saved = current
        ? await api<Product>(`${base}/${current.id}`, { method: "PUT", body })
        : await api<Product>(base, { method: "POST", body });
      onSaved(saved);
      if (current) {
        toast.success("Saved");
        onClose();
      } else {
        setCurrent(saved); // stay open so a photo can be added
        toast.success("Added — you can add a photo now");
      }
    } catch (e) {
      if (!applyServerErrors(e, form.setError)) form.setError("root", { message: errorMessage(e) });
    }
  }

  async function upload(file: File) {
    if (!current) return;
    if (!ACCEPT.split(",").includes(file.type)) return toast.error("Please choose a JPEG, PNG or WebP image");
    if (file.size > MAX_BYTES) return toast.error("Images must be 5 MB or smaller");
    const fd = new FormData();
    fd.append("file", file);
    setUploading(true);
    try {
      const saved = await api<Product>(`${base}/${current.id}/image`, { method: "POST", body: fd });
      setCurrent(saved);
      onSaved(saved);
    } catch (e) {
      toast.error(errorMessage(e));
    } finally {
      setUploading(false);
      if (fileRef.current) fileRef.current.value = "";
    }
  }

  async function removeImage() {
    if (!current) return;
    setUploading(true);
    try {
      const saved = await api<Product>(`${base}/${current.id}/image`, { method: "DELETE" });
      setCurrent(saved);
      onSaved(saved);
    } catch (e) {
      toast.error(errorMessage(e));
    } finally {
      setUploading(false);
    }
  }

  return (
    <form onSubmit={form.handleSubmit(submit)} noValidate className="grid gap-4">
      <DialogHeader>
        <DialogTitle>{product ? "Edit item" : current ? "Item added" : "Add a product or service"}</DialogTitle>
        <DialogDescription>Visible items appear on your website straight away.</DialogDescription>
      </DialogHeader>
      {errors.root && <Alert variant="destructive"><AlertDescription>{errors.root.message}</AlertDescription></Alert>}

      <div className="grid gap-4 sm:grid-cols-[8rem_1fr]">
        <FormField id="kind" label="Type">
          <Controller control={form.control} name="kind" render={({ field }) => (
            <Select value={field.value} onValueChange={field.onChange}>
              <SelectTrigger id="kind" className="w-full"><SelectValue /></SelectTrigger>
              <SelectContent>
                <SelectItem value="PRODUCT">Product</SelectItem>
                <SelectItem value="SERVICE">Service</SelectItem>
              </SelectContent>
            </Select>
          )} />
        </FormField>
        <FormField id="name" label="Name" error={errors.name?.message}>
          <Input id="name" maxLength={120} aria-invalid={!!errors.name} {...form.register("name")} />
        </FormField>
      </div>
      <FormField id="description" label="Description" error={errors.description?.message}>
        <Textarea id="description" rows={3} maxLength={1000} {...form.register("description")} />
      </FormField>
      <div className="grid grid-cols-2 gap-4">
        <FormField id="price" label="Price (₹)" error={errors.price?.message} hint="Leave empty for “Ask for price”">
          <Input id="price" inputMode="decimal" aria-invalid={!!errors.price} {...form.register("price")} />
        </FormField>
        <FormField id="priceNote" label="Price note" error={errors.priceNote?.message} hint="e.g. per kg, starting from">
          <Input id="priceNote" maxLength={60} {...form.register("priceNote")} />
        </FormField>
      </div>
      <label className="flex items-center gap-3 text-sm">
        <Controller control={form.control} name="visible" render={({ field }) => <Switch checked={field.value} onCheckedChange={field.onChange} />} />
        Show on website
      </label>

      {current && (
        <div className="grid gap-2">
          <span className="text-sm font-medium">Photo</span>
          <div className="flex items-center gap-3">
            {current.imageUrl ? (
              // eslint-disable-next-line @next/next/no-img-element -- served by our API
              <img src={current.imageUrl} alt="" className="size-20 rounded-lg border object-cover" />
            ) : (
              <div className="grid size-20 place-items-center rounded-lg border border-dashed text-muted-foreground"><ImagePlus className="size-5" /></div>
            )}
            <div className="flex flex-col gap-1.5">
              <input ref={fileRef} type="file" accept={ACCEPT} className="sr-only" id="photo" onChange={(e) => e.target.files?.[0] && void upload(e.target.files[0])} />
              <Button type="button" variant="outline" size="sm" disabled={uploading} onClick={() => fileRef.current?.click()}>
                {uploading && <Loader2 className="animate-spin" />} {current.imageUrl ? "Replace photo" : "Upload photo"}
              </Button>
              {current.imageUrl && (
                <Button type="button" variant="ghost" size="sm" disabled={uploading} onClick={() => void removeImage()}><Trash2 /> Remove</Button>
              )}
              <span className="text-xs text-muted-foreground">JPEG, PNG or WebP, up to 5 MB</span>
            </div>
          </div>
        </div>
      )}

      <DialogFooter>
        <Button type="button" variant="outline" onClick={onClose}>{current && !product ? "Done" : "Cancel"}</Button>
        <Button type="submit" disabled={isSubmitting}>{isSubmitting && <Loader2 className="animate-spin" />} {current ? "Save" : "Add item"}</Button>
      </DialogFooter>
    </form>
  );
}
