#!/usr/bin/env bash
# Deploys (or updates) LaunchMate on the VPS from your local checkout.
#   deploy/scripts/deploy.sh deploy@<VPS_IP>
# Requires deploy/.env.production locally (copied to the server with mode 600) — see .env.production.example.
set -euo pipefail

TARGET="${1:?Usage: deploy.sh user@host}"
ROOT="$(cd "$(dirname "$0")/../.." && pwd)"
REMOTE_DIR=/opt/launchmate
ENV_FILE="$ROOT/deploy/.env.production"

[ -f "$ENV_FILE" ] || { echo "Missing $ENV_FILE (copy deploy/.env.production.example and fill it in)" >&2; exit 1; }
# shellcheck disable=SC1090
DOMAIN="$(grep -E '^DOMAIN=' "$ENV_FILE" | cut -d= -f2-)"
for var in DOMAIN POSTGRES_PASSWORD; do
  grep -qE "^${var}=.+" "$ENV_FILE" || { echo "$var is empty in $ENV_FILE" >&2; exit 1; }
done
grep -qE "^ANTHROPIC_API_KEY=.+" "$ENV_FILE" || echo "WARNING: ANTHROPIC_API_KEY is empty; AI features will report 'not configured' until it is set."

echo "→ Uploading source to $TARGET:$REMOTE_DIR"
rsync -az --delete \
  --exclude '.git' --exclude 'node_modules' --exclude '.next' --exclude 'target' --exclude 'uploads' \
  --exclude 'deploy/.env.production' \
  "$ROOT/" "$TARGET:$REMOTE_DIR/"
scp -q "$ENV_FILE" "$TARGET:$REMOTE_DIR/deploy/.env.production"
ssh "$TARGET" "chmod 600 $REMOTE_DIR/deploy/.env.production"

echo "→ Building and starting containers (first build takes several minutes)"
ssh "$TARGET" "cd $REMOTE_DIR/deploy && docker compose -f docker-compose.prod.yml --env-file .env.production up -d --build --remove-orphans && docker image prune -f >/dev/null"

echo "→ Waiting for https://$DOMAIN"
for i in $(seq 1 60); do
  # -k: when DOMAIN is an IP address, Caddy serves an internal (self-signed) certificate.
  if curl -fsSk -o /dev/null "https://$DOMAIN/api/auth/csrf" 2>/dev/null; then
    echo "✓ LaunchMate is live at https://$DOMAIN"; exit 0
  fi
  sleep 10
done
echo "Not reachable over HTTPS yet. Check DNS (deploy/scripts/godaddy-dns.sh) and: ssh $TARGET 'cd $REMOTE_DIR/deploy && docker compose -f docker-compose.prod.yml logs --tail 100'" >&2
exit 1
