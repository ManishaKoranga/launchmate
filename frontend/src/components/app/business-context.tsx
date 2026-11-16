"use client";

import { createContext, useCallback, useContext, useEffect, useState } from "react";
import { api, ApiError, errorMessage } from "@/lib/api";
import type { Business } from "@/lib/types";
import { FullPageError, FullPageSpinner } from "@/components/app/states";

type BusinessContextValue = {
  business: Business;
  setBusiness: (b: Business) => void;
  reload: () => Promise<void>;
};

const BusinessContext = createContext<BusinessContextValue | null>(null);

export function BusinessProvider({ businessId, children }: { businessId: string; children: React.ReactNode }) {
  const [business, setBusiness] = useState<Business | null>(null);
  const [error, setError] = useState<string | null>(null);

  const load = useCallback(
    () =>
      api<Business>(`/api/businesses/${businessId}`).then(
        (b) => {
          setBusiness(b);
          setError(null);
        },
        (e) =>
          setError(
            e instanceof ApiError && e.status === 404
              ? "This business doesn't exist or you don't have access to it."
              : errorMessage(e),
          ),
      ),
    [businessId],
  );

  useEffect(() => {
    load();
  }, [load]);

  const reload = useCallback(async () => {
    await load();
  }, [load]);

  if (error) return <FullPageError message={error} />;
  if (!business) return <FullPageSpinner />;
  return <BusinessContext.Provider value={{ business, setBusiness, reload }}>{children}</BusinessContext.Provider>;
}

export function useBusiness(): BusinessContextValue {
  const ctx = useContext(BusinessContext);
  if (!ctx) throw new Error("useBusiness must be used inside BusinessProvider");
  return ctx;
}
