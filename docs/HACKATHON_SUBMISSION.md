# LaunchMate — Hackathon Submission

**Live demo:** https://sitelaunch.duckdns.org
**Demo website (sample business):** https://sitelaunch.duckdns.org/s/homebakers-nainital
**Repository:** https://github.com/ManishaKoranga/launchmate

## The problem

Millions of small home businesses in India — bakers, beauty-parlour owners, tutors, electricians, makers —
are not online. Building a website needs technical skills, a designer, and money. Customers ask for
details on WhatsApp and phone calls, and inquiries get lost.

## The solution

LaunchMate lets a non-technical owner describe their business in **English, Hindi or Hinglish — by typing or by voice** —
and turns that into:

1. A **professional website** (six industry templates, editable, with live preview and mobile view).
2. A **product and service catalogue** with prices and photos.
3. A **customer inquiry form** whose messages land in an **inbox (CRM)** with statuses, notes and quote drafts.
4. An **AI assistant** that proposes website edits for the owner to approve.

## How it works (demo flow, about 5 minutes)

1. Sign up and tap the microphone: *"Main Deoband mein home bakery chalati hoon..."*
2. LaunchMate extracts the business name, type, city, contact and products, and asks only for what's missing.
3. Review the details and tap **Build my website**. Progress is shown from real backend steps.
4. Edit the site in the editor, preview on desktop and mobile, then **publish**.
5. A customer opens the public site and sends an inquiry.
6. The owner sees the inquiry in the **Inquiries** inbox and can call or WhatsApp the customer.

## Judging criteria

| Criterion | How LaunchMate addresses it |
|---|---|
| **Problem and impact** | Targets an underserved segment (home and small businesses) with a concrete outcome: online presence plus lead capture. |
| **Innovation** | Hinglish and voice-first onboarding; AI produces structured, validated site configuration, not unchecked code. |
| **Technical quality** | Java 21 / Spring Boot modular monolith with clean-architecture ports for the LLM and speech-to-text; Next.js 16 frontend; Flyway migrations; 45 backend integration tests, including cross-tenant access tests. |
| **Working product** | Deployed on a VPS with HTTPS. The full flow was verified in a real browser. |
| **Security** | Session-based auth with CSRF protection; every business-scoped request checks membership on the server; AI tool results are validated; uploads are checked by file signature; rate limits on AI and public forms. |
| **Scalability and deployment** | One container stack (Caddy, Next.js, Spring Boot with whisper.cpp, PostgreSQL); daily-style backup script; documented VPS and DNS setup. |
| **Presentation** | See `docs/DEMO_SCRIPT.md`. |

## AI and speech usage

- **Claude (Anthropic API)** — onboarding extraction, website content generation and AI edit proposals.
  The model returns structured JSON; the backend validates it before anything is saved.
- **whisper.cpp (local, open source)** — voice notes are transcribed on the server. No third-party speech API is used
  and audio is deleted after transcription.

## Known limitations (honest status)

- The live demo uses a free DuckDNS subdomain, not a paid custom domain.
- Custom domains per business, GoDaddy DNS automation, and payments are designed but **not built**.
- The AI business assistant with typed tools and quotation generation is not yet built; the CRM has a quote-draft field.
- Visitor analytics are not yet tracked, so the dashboard shows only real lead and catalogue data.
- Voice transcription accuracy depends on the Whisper `small` model; the transcript is always editable before use.

## Running it locally

See `README.md`. Docker-based production setup: `docs/DEPLOYMENT.md`.
