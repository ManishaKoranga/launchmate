#!/usr/bin/env bash
# Nightly PostgreSQL backup on the VPS, keeping 14 days. Install once as the deploy user:
#   (crontab -l 2>/dev/null; echo "15 2 * * * /opt/launchmate/deploy/scripts/backup-db.sh") | crontab -
# Restore: gunzip -c FILE | docker compose -f docker-compose.prod.yml exec -T postgres psql -U launchmate launchmate
set -euo pipefail
cd /opt/launchmate/deploy
OUT=/opt/launchmate-backups/launchmate-$(date +%F-%H%M).sql.gz
docker compose -f docker-compose.prod.yml --env-file .env.production exec -T postgres \
  pg_dump -U launchmate --no-owner launchmate | gzip > "$OUT"
find /opt/launchmate-backups -name 'launchmate-*.sql.gz' -mtime +14 -delete
echo "Backup written: $OUT"
