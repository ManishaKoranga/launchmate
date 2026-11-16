"use client";

import { toast } from "sonner";
import { useBusiness } from "@/components/app/business-context";
import { PageHeader } from "@/components/app/page-header";
import { BusinessProfileForm } from "@/components/app/business-profile-form";
import { HoursEditor } from "@/components/app/hours-editor";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { api } from "@/lib/api";
import type { Business } from "@/lib/types";

export function Settings() {
  const { business, setBusiness } = useBusiness();
  return (
    <>
      <PageHeader title="Business profile" description="This information appears on your website." />
      <div className="grid max-w-3xl gap-6 p-4 sm:p-8">
        <Card>
          <CardHeader><CardTitle>Profile</CardTitle></CardHeader>
          <CardContent>
            <BusinessProfileForm
              key={business.id}
              initial={business}
              submitLabel="Save profile"
              onSubmit={async (profile) => {
                setBusiness(await api<Business>(`/api/businesses/${business.id}`, { method: "PUT", body: profile }));
                toast.success("Profile saved");
              }}
            />
          </CardContent>
        </Card>
        <Card>
          <CardHeader>
            <CardTitle>Opening hours</CardTitle>
            <CardDescription>Shown on your website so customers know when to reach you.</CardDescription>
          </CardHeader>
          <CardContent><HoursEditor business={business} onSaved={setBusiness} /></CardContent>
        </Card>
      </div>
    </>
  );
}
