# LaunchMate

**Live demo:** https://sitelaunch.duckdns.org · **Sample website:** https://sitelaunch.duckdns.org/s/homebakers-nainital

Hackathon submission details: [docs/HACKATHON_SUBMISSION.md](docs/HACKATHON_SUBMISSION.md) · Demo script: [docs/DEMO_SCRIPT.md](docs/DEMO_SCRIPT.md)

AI-powered business launch platform for non-technical entrepreneurs. Describe your business in
English, Hindi or Hinglish; LaunchMate builds a website, catalogue and inquiry inbox, and gives you an
AI assistant that operates your business through safe, typed tools.

* `backend/` — Java 21, Spring Boot 3.5 modular monolith (PostgreSQL, Flyway, Spring Security)
* `frontend/` — Next.js 16, React 19, Tailwind 4, shadcn/ui
* `docs/` — [architecture](docs/ARCHITECTURE.md), [checklist](docs/CHECKLIST.md), [risks](docs/RISKS.md)

## Run locally

Prerequisites: Java 21, Maven, Node 20+, PostgreSQL 16 (or Docker).

```bash
# 1. Database (either)
docker compose up -d postgres
#   …or a local Postgres with role/db `launchmate` / password `launchmate`

# 2. Backend  → http://localhost:8080  (Swagger UI: /swagger-ui.html)
cd backend && mvn spring-boot:run

# 3. Frontend → http://localhost:3000
cd frontend && npm install && npm run dev
```

The frontend proxies `/api/*` to the backend (`BACKEND_URL`, default `http://localhost:8080`).

Claude credentials: set `ANTHROPIC_API_KEY`, or log in once with `ant auth login` — the official
Java SDK picks up either.

## Voice notes (local speech-to-text)

Voice notes are transcribed on the server with [whisper.cpp](https://github.com/ggml-org/whisper.cpp).
Locally: `brew install whisper-cpp ffmpeg` and download the model to
`~/.cache/launchmate/whisper/ggml-small.bin` (or set `WHISPER_MODEL_PATH`). Without them, the mic button
is hidden and text input works as usual. The Docker image builds whisper.cpp and fetches the model itself.

## Deploy

See [docs/DEPLOYMENT.md](docs/DEPLOYMENT.md) (GoDaddy VPS + domain).

## Tests

Integration tests run against real PostgreSQL. With Docker running they use Testcontainers;
without Docker, point them at a database you created for tests:

```bash
createdb -O launchmate launchmate_test
cd backend && LM_TEST_DB_URL=jdbc:postgresql://localhost:5432/launchmate_test mvn test
```
