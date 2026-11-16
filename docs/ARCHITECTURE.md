# LaunchMate — Architecture

LaunchMate is a multi-tenant SaaS that turns a natural-language description of a small
business (English, Hindi or Hinglish) into a working website with a catalogue, an inquiry
form wired to a CRM, and an AI assistant that can operate the business through typed tools.

## 1. Shape of the system

```
 Browser ──► Next.js (frontend/)  ──/api/* rewrite──►  Spring Boot (backend/)  ──►  PostgreSQL
              │  dashboard (auth'd)                     │  modular monolith            (Flyway)
              │  public sites /s/{slug}                 │  ├─ identity                 
              │  custom-domain middleware               │  ├─ business      ──► Anthropic API (adapter)
              └─ template renderer (React)              │  ├─ website       ──► GoDaddy API (adapter)
                                                        │  ├─ catalogue     ──► Object storage (adapter)
                                                        │  ├─ crm           ──► STT provider (adapter)
                                                        │  ├─ ai
                                                        │  ├─ publishing / domainmanagement
                                                        │  └─ analytics
```

* **One deployment for all tenants.** Every business's site is rendered by the same Next.js
  app from (template + saved config + live catalogue). No per-customer deployments or code.
* **Same origin.** Next.js proxies `/api/*` to Spring Boot, so the session cookie is
  first-party, CORS is unnecessary and CSRF protection works with a standard header.

## 2. Why a modular monolith

A hackathon team cannot afford distributed transactions, service discovery and multi-service
deploys. A modular monolith gives one deployable, one database transaction boundary and fast
refactoring, while package-per-domain keeps the seams visible so a module (e.g. `ai` or
`publishing`) could be extracted later if load demands it.

Rules between modules:

* A module exposes **application services and DTOs**; other modules never reach into its
  repositories or entities.
* `shared/` holds cross-cutting concerns only (security, tenancy, errors, audit) — no business
  rules.
* External systems (Claude, GoDaddy, S3, STT) are reached only through **ports** (interfaces)
  defined by the module that needs them, with adapters in a `provider/` package
  (Dependency Inversion). Swapping Claude for another LLM touches one adapter class.

Inside a module: `controller` (HTTP only) → `service` (use cases, `@Transactional`) →
`domain` (entities + rules) / `repository` (Spring Data). Controllers never touch repositories.

## 3. Multi-tenancy

* Tenant key: `business_id` (UUID) on every tenant-owned row.
* Membership: `business_members(business_id, user_id, role)`. A user may belong to several
  businesses; the dashboard operates on one at a time.
* **Tenant resolution:** all dashboard endpoints are `/api/businesses/{businessId}/...`.
  `TenantAccessInterceptor` takes the *authenticated principal from the session* and verifies
  membership before the controller runs; failure returns **404** (we don't reveal that another
  tenant's business exists). The verified id is placed in a request-scoped `TenantContext`.
  The browser-supplied id is therefore only a *selector*, never trusted on its own.
* **Repository discipline:** tenant-scoped repositories expose only `...ByBusinessId(...)` /
  `findByIdAndBusinessId(...)` methods. Loading a child entity by id alone is not possible
  through the public service API, which stops IDOR across tenants.
* **AI tools** never accept a `businessId` argument; the tool executor injects the tenant
  from the authenticated context.
* Public endpoints (`/api/public/...`) resolve the tenant from the site slug or verified custom
  hostname and expose only published data.
* Integration tests attempt cross-tenant reads/writes and assert rejection.

## 4. Website generation

Claude never writes executable code. It produces a **`SiteConfig` JSON document** (theme,
navigation, ordered sections, copy, SEO) that is:

1. Parsed into typed Java records.
2. Validated (Bean Validation + template rules: allowed section types, colour formats,
   max lengths, known template id).
3. Stored as the website's **draft**.

Templates (`bakery`, `salon`, `home-services`, `tutoring`, `handmade`, `professional`) are
data: default theme, default section order, allowed sections and tone. The React renderer
has one component per *section type*; a template changes styling variants and defaults, not
code paths. This makes adding a template a data change (Open/Closed).

**Draft vs published:** editing writes `websites.draft_config`. Publishing copies the draft
into an immutable `publications` row and points the website at it. The public site reads only
the published snapshot, so drafts never leak. The catalogue is live business data (visible
products appear immediately) — a deliberate trade-off: owners expect "add product" to show up
without republishing.

## 5. AI architecture

* `LlmClient` port (`ai/provider`) with `AnthropicLlmClient` adapter using the official
  `anthropic-java` SDK. Model is configurable (`launchmate.ai.model`, default
  `claude-opus-5-5`).
* Workflows (not five separate "agents"):
  * **BusinessUnderstanding** — structured extraction of a `BusinessProfileDraft` + list of
    missing fields from free text (Hinglish supported).
  * **WebsiteGeneration** — structured `SiteConfig` from the approved profile.
  * **Assistant** — tool-using loop for owner operations, lead qualification, quotations and
    growth advice over real data.
* **Controlled tool loop.** We do *not* use the SDK's auto tool runner. For each `tool_use`
  block the `ToolExecutor`:
  1. Validates arguments against a typed record (Bean Validation).
  2. Resolves user + tenant from the server-side session context.
  3. Checks role permission.
  4. If the tool is marked `requiresConfirmation` (destructive or publicly visible) it
     persists a `PROPOSED` `ai_actions` row and returns "awaiting confirmation" to Claude; the
     UI shows Confirm/Reject.
  5. Calls the same application service the REST API uses (no duplicated business logic).
  6. Writes an audit log entry.
  7. Returns a structured JSON result to Claude.
* There is no SQL, shell, filesystem or open HTTP tool. Conversation history is persisted
  per (user, business). Customer-submitted text is wrapped and labelled as untrusted data in
  prompts and can never change the tool set or permissions.

## 6. Publishing and domains

* Every business gets `https://<app-host>/s/<slug>` immediately on publish.
* Custom domains: `DomainRegistrarPort` with a `GoDaddyDomainAdapter` (enabled only when
  `GODADDY_API_KEY/SECRET` are present) and a **manual DNS** fallback that shows the exact
  records to add. Status moves `PENDING_DNS → VERIFIED → ACTIVE` only after a real DNS lookup
  confirms the CNAME/TXT and the routing check succeeds. Nothing is marked connected
  optimistically.
* GoDaddy's shared hosting cannot run a JVM; LaunchMate is deployed on a container host and
  GoDaddy is used only for domains/DNS.

## 7. Security

* Spring Security, server-side session (HttpOnly, SameSite=Lax cookie), BCrypt passwords,
  CSRF via `XSRF-TOKEN` cookie + `X-XSRF-TOKEN` header for state-changing dashboard calls.
* Public lead submission is CSRF-exempt but rate-limited and validated.
* AI endpoints are rate-limited per user (in-memory token bucket; a Redis-backed limiter is
  only needed with multiple instances).
* Secrets only from environment variables. Logs never include passwords, tokens or full
  customer messages.

## 8. Key version decisions

| Component | Version | Reason |
|---|---|---|
| Java | 21 (LTS) | mandated |
| Spring Boot | 3.5.x | latest 3.x line (4.x exists but 3.x was required) |
| anthropic-java | 3.8.0 | official SDK |
| springdoc-openapi | 2.8.x | line compatible with Boot 3.5 |
| PostgreSQL | 16 | |
| Next.js | 16 / React 19 / Tailwind 4 | current stable |

## 9. Not built (on purpose, YAGNI)

* Redis — no demonstrated need yet (single instance, in-memory rate limiter + sessions).
* Payments — entitlement model (`businesses.plan`) exists; no provider integration.
* Separate `customers` table — leads carry contact data; a customer entity is added when
  repeat-customer features need it.
