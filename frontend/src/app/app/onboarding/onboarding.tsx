"use client";

import { useRouter } from "next/navigation";
import { BusinessProfileForm } from "@/components/app/business-profile-form";
import { Logo } from "@/components/marketing/logo";
import { api } from "@/lib/api";
import type { Business } from "@/lib/types";

export function Onboarding() {
  const router = useRouter();
  return (
    <div className="mx-auto w-full max-w-2xl px-4 py-12">
      <Logo href="/app" />
      <h1 className="mt-8 text-2xl font-semibold tracking-tight">Tell us about your business</h1>
      <p className="mt-1 text-muted-foreground">You can change any of this later.</p>
      <div className="mt-8 rounded-2xl border bg-card p-6">
        <BusinessProfileForm
          submitLabel="Create business"
          onSubmit={async (profile) => {
            const created = await api<Business>("/api/businesses", { method: "POST", body: profile });
            router.replace(`/app/b/${created.id}`);
          }}
        />
      </div>
    </div>
  );
}
