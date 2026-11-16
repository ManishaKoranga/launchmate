/**
 * Browser-side API client for the Spring Boot backend (same origin via the /api rewrite).
 * - Sends the session cookie and the CSRF header on state-changing requests.
 * - Converts RFC 7807 problem responses into ApiError with per-field messages.
 */

export class ApiError extends Error {
  constructor(
    public readonly status: number,
    message: string,
    public readonly fieldErrors: Record<string, string> = {},
    public readonly notConfigured = false,
    public readonly problems: string[] = [],
  ) {
    super(message);
    this.name = "ApiError";
  }
}

const SAFE_METHODS = new Set(["GET", "HEAD", "OPTIONS"]);

function readCookie(name: string): string | undefined {
  if (typeof document === "undefined") return undefined;
  return document.cookie
    .split("; ")
    .find((c) => c.startsWith(`${name}=`))
    ?.split("=")[1];
}

async function ensureCsrfToken(): Promise<string> {
  let token = readCookie("XSRF-TOKEN");
  if (!token) {
    await fetch("/api/auth/csrf", { credentials: "same-origin" });
    token = readCookie("XSRF-TOKEN");
  }
  if (!token) throw new ApiError(0, "Could not establish a secure session. Please reload the page.");
  return decodeURIComponent(token);
}

type RequestOptions = {
  method?: string;
  body?: unknown;
  signal?: AbortSignal;
};

export async function api<T>(path: string, options: RequestOptions = {}): Promise<T> {
  const method = (options.method ?? "GET").toUpperCase();
  const headers: Record<string, string> = { Accept: "application/json" };
  const isForm = typeof FormData !== "undefined" && options.body instanceof FormData;

  if (options.body !== undefined && !isForm) headers["Content-Type"] = "application/json";
  if (!SAFE_METHODS.has(method)) headers["X-XSRF-TOKEN"] = await ensureCsrfToken();

  let response: Response;
  try {
    response = await fetch(path, {
      method,
      headers,
      credentials: "same-origin",
      signal: options.signal,
      body:
        options.body === undefined
          ? undefined
          : isForm
            ? (options.body as FormData)
            : JSON.stringify(options.body),
    });
  } catch (e) {
    if (e instanceof DOMException && e.name === "AbortError") throw e;
    throw new ApiError(0, "Cannot reach LaunchMate. Check your connection and try again.");
  }

  if (response.status === 204) return undefined as T;

  const text = await response.text();
  const data = text ? safeJson(text) : undefined;

  if (!response.ok) {
    const problem = (data ?? {}) as {
      detail?: string;
      errors?: Record<string, string>;
      notConfigured?: boolean;
      problems?: string[];
    };
    throw new ApiError(
      response.status,
      problem.detail ?? defaultMessage(response.status),
      problem.errors ?? {},
      problem.notConfigured ?? false,
      problem.problems ?? [],
    );
  }
  return data as T;
}

function safeJson(text: string): unknown {
  try {
    return JSON.parse(text);
  } catch {
    return undefined;
  }
}

function defaultMessage(status: number): string {
  if (status === 401) return "Please sign in to continue.";
  if (status === 403) return "You don't have permission to do that.";
  if (status === 404) return "Not found.";
  if (status === 429) return "Too many requests. Please wait a moment.";
  if (status >= 500) return "Something went wrong on our side. Please try again.";
  return "Request failed.";
}

export function errorMessage(e: unknown): string {
  if (e instanceof ApiError) return e.message;
  if (e instanceof Error) return e.message;
  return "Something went wrong.";
}
