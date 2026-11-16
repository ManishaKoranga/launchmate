"use client";

import { Controller, useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { Loader2 } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Textarea } from "@/components/ui/textarea";
import { Alert, AlertDescription } from "@/components/ui/alert";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import { FormField } from "@/components/app/form-field";
import { businessProfileSchema, compact, type BusinessProfileValues } from "@/lib/schemas";
import { CATEGORY_LABELS, type BusinessCategory, type BusinessProfile } from "@/lib/types";
import { applyServerErrors } from "@/lib/forms";
import { errorMessage } from "@/lib/api";

type Props = {
  initial?: Partial<BusinessProfile>;
  submitLabel: string;
  onSubmit: (profile: BusinessProfile) => Promise<void>;
};

export function BusinessProfileForm({ initial, submitLabel, onSubmit }: Props) {
  const form = useForm<BusinessProfileValues>({
    resolver: zodResolver(businessProfileSchema),
    defaultValues: {
      name: initial?.name ?? "",
      category: initial?.category ?? "BAKERY_FOOD",
      tagline: initial?.tagline ?? "",
      description: initial?.description ?? "",
      city: initial?.city ?? "",
      area: initial?.area ?? "",
      address: initial?.address ?? "",
      phone: initial?.phone ?? "",
      whatsapp: initial?.whatsapp ?? "",
      email: initial?.email ?? "",
      targetCustomers: initial?.targetCustomers ?? "",
      languages: initial?.languages ?? "",
    },
  });
  const { errors, isSubmitting } = form.formState;

  async function submit(values: BusinessProfileValues) {
    try {
      await onSubmit({ ...compact(values), openingHours: initial?.openingHours } as BusinessProfile);
    } catch (e) {
      if (!applyServerErrors(e, form.setError)) form.setError("root", { message: errorMessage(e) });
    }
  }

  const field = (name: keyof BusinessProfileValues, label: string, opts: { hint?: string; type?: string; autoComplete?: string } = {}) => (
    <FormField id={name} label={label} error={errors[name]?.message} hint={opts.hint}>
      <Input id={name} type={opts.type} autoComplete={opts.autoComplete} aria-invalid={!!errors[name]} {...form.register(name)} />
    </FormField>
  );

  return (
    <form onSubmit={form.handleSubmit(submit)} className="grid gap-6" noValidate>
      {errors.root && <Alert variant="destructive"><AlertDescription>{errors.root.message}</AlertDescription></Alert>}

      <fieldset className="grid gap-4 sm:grid-cols-2">
        <legend className="mb-3 text-sm font-medium">About the business</legend>
        {field("name", "Business name")}
        <FormField id="category" label="Type of business" error={errors.category?.message}>
          <Controller
            control={form.control}
            name="category"
            render={({ field: f }) => (
              <Select value={f.value} onValueChange={(v) => f.onChange(v as BusinessCategory)}>
                <SelectTrigger id="category" className="w-full"><SelectValue /></SelectTrigger>
                <SelectContent>
                  {Object.entries(CATEGORY_LABELS).map(([value, label]) => (
                    <SelectItem key={value} value={value}>{label}</SelectItem>
                  ))}
                </SelectContent>
              </Select>
            )}
          />
        </FormField>
        <div className="sm:col-span-2">{field("tagline", "Tagline", { hint: "One line customers will remember" })}</div>
        <FormField id="description" label="Description" error={errors.description?.message} className="sm:col-span-2">
          <Textarea id="description" rows={4} aria-invalid={!!errors.description} {...form.register("description")} />
        </FormField>
        <div className="sm:col-span-2">{field("targetCustomers", "Who are your customers?")}</div>
        {field("languages", "Languages you serve in", { hint: "e.g. Hindi, English" })}
      </fieldset>

      <fieldset className="grid gap-4 sm:grid-cols-2">
        <legend className="mb-3 text-sm font-medium">Location & contact</legend>
        {field("city", "City", { autoComplete: "address-level2" })}
        {field("area", "Area / locality")}
        <div className="sm:col-span-2">{field("address", "Address", { autoComplete: "street-address", hint: "Optional — leave blank for home businesses" })}</div>
        {field("phone", "Phone", { type: "tel", autoComplete: "tel" })}
        {field("whatsapp", "WhatsApp", { type: "tel" })}
        {field("email", "Email", { type: "email", autoComplete: "email" })}
      </fieldset>

      <div>
        <Button type="submit" size="lg" disabled={isSubmitting}>
          {isSubmitting && <Loader2 className="animate-spin" />} {submitLabel}
        </Button>
      </div>
    </form>
  );
}
