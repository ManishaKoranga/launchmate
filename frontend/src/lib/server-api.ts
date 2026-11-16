import "server-only";
import type { PublicSite } from "@/lib/site-types";

const BACKEND_URL = process.env.BACKEND_URL ?? "http://localhost:8080";

/** Published site for a slug, or null if it doesn't exist / isn't published. Not cached: publishing must show immediately. */
export async function fetchPublishedSite(slug: string): Promise<PublicSite | null> {
  const res = await fetch(`${BACKEND_URL}/api/public/sites/${encodeURIComponent(slug)}`, {
    cache: "no-store",
    headers: { Accept: "application/json" },
  });
  if (res.status === 404) return null;
  if (!res.ok) throw new Error(`Failed to load site (${res.status})`);
  return res.json();
}
