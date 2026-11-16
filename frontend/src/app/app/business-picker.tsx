"use client";

import Link from "next/link";
import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import { ChevronRight, Plus } from "lucide-react";
import { api, errorMessage } from "@/lib/api";
import { CATEGORY_LABELS, type BusinessSummary } from "@/lib/types";
import { FullPageError, FullPageSpinner } from "@/components/app/states";
import { Button } from "@/components/ui/button";
import { Logo } from "@/components/marketing/logo";

/** Routes the user to their business, to onboarding if they have none, or shows a picker. */
export function BusinessPicker() {
  const router = useRouter();
  const [businesses, setBusinesses] = useState<BusinessSummary[] | null>(null);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    api<BusinessSummary[]>("/api/businesses")
      .then((list) => {
        if (list.length === 0) router.replace("/app/onboarding");
        else if (list.length === 1) router.replace(`/app/b/${list[0].id}`);
        else setBusinesses(list);
      })
      .catch((e) => setError(errorMessage(e)));
  }, [router]);

  if (error) return <FullPageError message={error} />;
  if (!businesses) return <FullPageSpinner />;

  return (
    <div className="mx-auto w-full max-w-lg px-4 py-16">
      <Logo href="/app" />
      <h1 className="mt-8 text-xl font-semibold">Choose a business</h1>
      <ul className="mt-4 divide-y rounded-xl border bg-card">
        {businesses.map((b) => (
          <li key={b.id}>
            <Link href={`/app/b/${b.id}`} className="flex items-center justify-between px-4 py-3 hover:bg-muted/60">
              <span>
                <span className="block font-medium">{b.name}</span>
                <span className="text-sm text-muted-foreground">{CATEGORY_LABELS[b.category]}</span>
              </span>
              <ChevronRight className="size-4 text-muted-foreground" />
            </Link>
          </li>
        ))}
      </ul>
      <Button asChild variant="outline" className="mt-4"><Link href="/app/onboarding"><Plus /> Add another business</Link></Button>
    </div>
  );
}
