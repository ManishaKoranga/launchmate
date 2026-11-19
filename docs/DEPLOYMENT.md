# Deploying LaunchMate on a GoDaddy VPS

LaunchMate runs as four containers on one server:

```
Internet ──HTTPS──► Caddy ──► frontend (Next.js) ──/api──► backend (Spring Boot + whisper.cpp) ──► PostgreSQL
            :80/:443           automatic Let's Encrypt       voice notes transcribed on the server
```

GoDaddy **shared/cPanel web hosting cannot run this** (it needs a JVM, a Node server and Docker).
Use a **GoDaddy VPS** with Ubuntu; the domain's DNS stays at GoDaddy.

## 1. What you need

| Item | Recommendation |
|---|---|
| GoDaddy VPS | Ubuntu 22.04 or 24.04, **4 vCPU / 8 GB RAM** recommended (2 vCPU / 4 GB works; voice notes are slower) and ≥ 60 GB disk |
| Domain | Any domain in your GoDaddy account |
| Anthropic API key | From console.anthropic.com — the server cannot use a laptop `ant auth login` profile |
| SSH key | Your `~/.ssh/id_ed25519.pub` |
| (optional) GoDaddy API key/secret | Production key from developer.godaddy.com/keys, to set DNS automatically |

## 2. Prepare the VPS (once)

In the GoDaddy VPS dashboard, note the server's **IP address** and **root password / SSH access**. Then:

```bash
ssh root@<VPS_IP> 'bash -s' < deploy/scripts/bootstrap-vps.sh "$(cat ~/.ssh/id_ed25519.pub)"
```

This creates a `deploy` user (key-only SSH), installs Docker, enables the firewall (22/80/443 only),
disables SSH password login, adds 4 GB swap and turns on automatic security updates.

## 3. Point the domain at the VPS

Automatically (GoDaddy API):

```bash
GODADDY_API_KEY=... GODADDY_API_SECRET=... deploy/scripts/godaddy-dns.sh yourdomain.com <VPS_IP>
```

Or by hand in GoDaddy → My Products → your domain → DNS: set an **A** record for `@` and for `www`
to the VPS IP (TTL 600). Remove any conflicting `@`/`www` records (e.g. GoDaddy "Parked").

## 4. Configure and deploy

```bash
cp deploy/.env.production.example deploy/.env.production
# edit: DOMAIN, ACME_EMAIL, POSTGRES_PASSWORD (openssl rand -base64 32), ANTHROPIC_API_KEY
deploy/scripts/deploy.sh deploy@<VPS_IP>
```

The first deploy builds the images on the server (≈ 10–15 minutes; whisper.cpp is compiled) and the
backend downloads the Whisper model (488 MB, checksum-verified) into a persistent volume. Caddy obtains
the HTTPS certificate as soon as DNS resolves. Re-run `deploy.sh` for every update; data volumes persist.

## 5. After the first deploy

```bash
# nightly database backups, 14 days kept in /opt/launchmate-backups
ssh deploy@<VPS_IP> '(crontab -l 2>/dev/null; echo "15 2 * * * /opt/launchmate/deploy/scripts/backup-db.sh") | crontab -'

# logs / status
ssh deploy@<VPS_IP> 'cd /opt/launchmate/deploy && docker compose -f docker-compose.prod.yml ps'
ssh deploy@<VPS_IP> 'cd /opt/launchmate/deploy && docker compose -f docker-compose.prod.yml logs -f backend'
```

Business websites are served at `https://yourdomain.com/s/<business-slug>`.

## Security notes

* Only Caddy is reachable from the internet; PostgreSQL and the backend are on the internal Docker network.
* Secrets live only in `deploy/.env.production` on the server (mode 600, excluded from git and rsync deletes).
* Session cookies are `Secure` + `HttpOnly` + `SameSite=Lax`; HSTS is enabled; Swagger UI is disabled in production.
* Voice recordings are transcribed on the server and deleted immediately; they never leave the VPS.

## Not covered yet

* Custom domains **for each business** (e.g. `meerabakes.in` → that business's site) need on-demand
  TLS in Caddy plus domain verification in the app — this is the "Domain and publishing" feature, still to build.
* Off-server backup copies (e.g. to object storage) — `backup-db.sh` keeps copies on the VPS only.
