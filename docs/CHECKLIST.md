# Implementation checklist

Legend: `[x]` done and verified · `[~]` partially done / adapter only · `[ ]` not started

## Phase 0 — Inspection & plan
- [x] Repository inspected (greenfield)
- [x] Architecture document (`docs/ARCHITECTURE.md`)
- [x] Risks & external dependencies (`docs/RISKS.md`)

## Phase 1 — Foundation
- [x] Spring Boot backend skeleton (modular packages)
- [x] PostgreSQL + Flyway V1 (users, businesses, members, audit)
- [x] Auth: register / login / logout / me (session + CSRF)
- [x] Tenant access interceptor + business profile CRUD
- [x] Central error handling, OpenAPI, actuator health
- [x] Integration tests incl. cross-tenant rejection
- [x] Next.js frontend: design system, landing, auth, protected dashboard shell
- [~] docker-compose (Postgres) + Dockerfiles — written, **not yet run** (Docker not installed on dev machine)

## Phase 2 — Website creation
- [x] Conversational onboarding (Claude extraction, Hinglish, follow-up questions, review & approval) — verified with real Claude
- [x] Manual form fallback when AI is unavailable
- [x] Template registry (6 verticals) + SiteConfig validation (structure, WCAG contrast)
- [x] Website generation job with real persisted progress; one corrective AI retry; template fallback
- [x] Renderer (React section components, container queries) + draft preview
- [x] Editor: template switch, theme, content, desktop/mobile preview, AI edit proposals (preview → apply)

## Phase 3 — Business operations
- [x] Products/services CRUD, plan limits, image upload (magic-byte validation, local storage adapter)
- [~] Object storage: port + local filesystem adapter; **S3-compatible adapter not yet written**
- [x] Public inquiry form → leads (honeypot, per-IP/per-business rate limits)
- [x] CRM screen: statuses, notes, quote draft, call/WhatsApp, manual inquiries
- [ ] AI business assistant with typed tools, confirmation, audit (LlmClient tool-session port is built; orchestrator + tools + UI not yet)

## Phase 4 — Publishing
- [x] Publish immutable snapshot + /s/{slug} public routing (SSR, SEO metadata)
- [ ] Custom domain: manual DNS + verification + host routing (proxy.ts)
- [ ] GoDaddy adapter (requires credentials)

## Phase 5 — Voice & growth
- [ ] Voice notes (MediaRecorder → STT port)
- [ ] Page-view tracking + analytics (dashboard currently shows real lead/product data only)
- [ ] Growth recommendations
- [ ] LiveKit (optional)

## Phase 6 — Quality & demo
- [~] End-to-end demo run (Deoband bakery) — steps 1–10 verified in a real browser with Playwright; step 11 (AI quotation) pending assistant
- [ ] Mobile responsiveness pass
- [ ] Demo script
