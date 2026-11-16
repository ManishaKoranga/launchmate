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
- [ ] Conversational onboarding (Claude extraction, follow-up questions, approval)
- [ ] Template registry (6 verticals) + SiteConfig validation
- [ ] Website generation job with real progress
- [ ] Renderer (React section components) + draft preview
- [ ] Editor: template, theme, content, desktop/mobile preview, AI edit

## Phase 3 — Business operations
- [ ] Products/services CRUD + image upload (storage port)
- [ ] Public inquiry form → leads
- [ ] CRM with statuses
- [ ] AI assistant with typed tools, confirmation, audit

## Phase 4 — Publishing
- [ ] Publish snapshot + /s/{slug} routing
- [ ] Custom domain: manual DNS + verification
- [ ] GoDaddy adapter (requires credentials)

## Phase 5 — Voice & growth
- [ ] Voice notes (MediaRecorder → STT port)
- [ ] Page-view tracking + dashboard analytics
- [ ] Growth recommendations
- [ ] LiveKit (optional)

## Phase 6 — Quality & demo
- [ ] End-to-end demo run (Deoband bakery)
- [ ] Mobile responsiveness pass
- [ ] Demo script
