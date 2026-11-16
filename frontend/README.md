# LaunchMate frontend

Next.js 16 (App Router, Cache Components), React 19, Tailwind 4, shadcn/ui.

* `src/app/page.tsx` — landing page
* `src/app/(auth)` — sign in / sign up
* `src/app/app` — authenticated dashboard (client components calling the Spring API via `/api`)
* `src/lib/api.ts` — API client (session cookie + CSRF header, problem-detail errors)

```bash
npm install
npm run dev     # http://localhost:3000 — expects the backend on BACKEND_URL (default :8080)
```
