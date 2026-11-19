#!/usr/bin/env bash
# Points a GoDaddy domain at the VPS using the GoDaddy Domains API (sets @ and www A records).
#   GODADDY_API_KEY=... GODADDY_API_SECRET=... deploy/scripts/godaddy-dns.sh example.com 203.0.113.10
# Create a *Production* key at https://developer.godaddy.com/keys. GoDaddy announced in April 2026 that
# the DNS API works for accounts with a single domain (it previously required 10+). If you still get
# 403 ACCESS_DENIED, set the same two A records by hand in GoDaddy → My Products → Domain → DNS.
set -euo pipefail

DOMAIN="${1:?Usage: godaddy-dns.sh <domain> <vps-ip>}"
IP="${2:?Usage: godaddy-dns.sh <domain> <vps-ip>}"
: "${GODADDY_API_KEY:?Set GODADDY_API_KEY}" "${GODADDY_API_SECRET:?Set GODADDY_API_SECRET}"
API="${GODADDY_API_URL:-https://api.godaddy.com}"

[[ "$IP" =~ ^[0-9]{1,3}(\.[0-9]{1,3}){3}$ ]] || { echo "Not an IPv4 address: $IP" >&2; exit 1; }

for NAME in @ www; do
  echo "→ A $NAME.$DOMAIN → $IP"
  code=$(curl -s -o /tmp/godaddy-dns.out -w '%{http_code}' -X PUT "$API/v1/domains/$DOMAIN/records/A/$NAME" \
    -H "Authorization: sso-key $GODADDY_API_KEY:$GODADDY_API_SECRET" \
    -H "Content-Type: application/json" \
    -d "[{\"data\":\"$IP\",\"ttl\":600}]")
  if [ "$code" != "200" ]; then
    echo "GoDaddy API returned $code: $(cat /tmp/godaddy-dns.out)" >&2
    exit 1
  fi
done
rm -f /tmp/godaddy-dns.out

echo "→ Waiting for public DNS to resolve $DOMAIN to $IP (can take a few minutes)"
for i in $(seq 1 30); do
  if dig +short "$DOMAIN" @1.1.1.1 | grep -qx "$IP"; then echo "✓ DNS updated"; exit 0; fi
  sleep 20
done
echo "DNS not visible yet; it may take up to an hour. Re-run deploy.sh afterwards." >&2
