# Technical risks & external dependencies

| Risk / dependency | Impact | Mitigation |
|---|---|---|
| **Anthropic API** availability, latency (generation can take 20–60 s), cost | Onboarding/generation stall | Async generation job with persisted step status; per-user rate limiting; clear 503 when provider not configured; manual profile form fallback |
| **LLM output validity** | Broken site / bad data | Structured outputs + server-side Bean Validation + template rules; never render unvalidated JSON |
| **Prompt injection** via customer inquiries | AI performs unwanted actions | Customer text passed as labelled untrusted data; tools are tenant-bound and permissioned; publicly visible/destructive tools require explicit confirmation |
| **GoDaddy APIs** need production API key/secret; some endpoints (domain availability/purchase) require account conditions | Custom domain automation unavailable | `DomainRegistrarPort` adapter enabled only with credentials; manual DNS fallback always works; status set only after real DNS verification |
| **GoDaddy hosting** cannot run Java | Can't deploy there | Deploy containers elsewhere (Render/Fly/Railway/ECS); GoDaddy only for DNS |
| **Custom domain TLS** | HTTPS on customer hostnames needs on-demand certs | Requires a reverse proxy with on-demand TLS (e.g. Caddy) in production; documented, not automated in hackathon scope |
| **Speech-to-text** provider not chosen/credentialed | Voice notes unavailable | `SpeechToTextPort`; text input always available; voice UI disabled with explanation when unconfigured |
| **LiveKit** needs server + STT/TTS workers | Large scope | Optional, after core is stable |
| **Docker absent on dev machine** | Testcontainers cannot run | Tests use Testcontainers when Docker exists, otherwise an explicitly configured local Postgres (`LM_TEST_DB_URL`) |
| **Object storage** | Product images | `ObjectStoragePort` with local-filesystem adapter for dev and S3-compatible adapter for prod |
