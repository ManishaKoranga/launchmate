import type { FieldValues, Path, UseFormSetError } from "react-hook-form";
import { ApiError } from "@/lib/api";

/** Copies server-side field validation errors onto react-hook-form fields. Returns true if any matched. */
export function applyServerErrors<T extends FieldValues>(e: unknown, setError: UseFormSetError<T>): boolean {
  if (!(e instanceof ApiError)) return false;
  const entries = Object.entries(e.fieldErrors);
  entries.forEach(([field, message]) => setError(field as Path<T>, { type: "server", message }));
  return entries.length > 0;
}

/** Safe post-login redirect: only same-site relative paths are allowed. */
export function nextPathFromLocation(fallback = "/app"): string {
  if (typeof window === "undefined") return fallback;
  const next = new URLSearchParams(window.location.search).get("next");
  return next && next.startsWith("/") && !next.startsWith("//") ? next : fallback;
}
