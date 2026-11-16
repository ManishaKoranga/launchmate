"use client";

import { createContext, useCallback, useContext, useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import { api, ApiError } from "@/lib/api";
import type { User } from "@/lib/types";
import { FullPageSpinner, FullPageError } from "@/components/app/states";

type Session = { user: User; signOut: () => Promise<void> };
const SessionContext = createContext<Session | null>(null);

/** Gate for the authenticated area. The backend remains the source of truth for authorisation. */
export function SessionProvider({ children }: { children: React.ReactNode }) {
  const router = useRouter();
  const [user, setUser] = useState<User | null>(null);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const controller = new AbortController();
    api<User>("/api/auth/me", { signal: controller.signal })
      .then(setUser)
      .catch((e) => {
        if (e instanceof DOMException) return;
        if (e instanceof ApiError && e.status === 401) {
          const next = encodeURIComponent(window.location.pathname);
          router.replace(`/login?next=${next}`);
        } else {
          setError(e instanceof Error ? e.message : "Could not load your session");
        }
      });
    return () => controller.abort();
  }, [router]);

  const signOut = useCallback(async () => {
    await api("/api/auth/logout", { method: "POST" });
    router.replace("/login");
  }, [router]);

  if (error) return <FullPageError message={error} />;
  if (!user) return <FullPageSpinner />;
  return <SessionContext.Provider value={{ user, signOut }}>{children}</SessionContext.Provider>;
}

export function useSession(): Session {
  const ctx = useContext(SessionContext);
  if (!ctx) throw new Error("useSession must be used inside SessionProvider");
  return ctx;
}
