#!/usr/bin/env bash
# One-time preparation of a fresh GoDaddy VPS (Ubuntu 22.04 / 24.04). Run as root:
#   ssh root@<VPS_IP> 'bash -s' < deploy/scripts/bootstrap-vps.sh "<your-ssh-public-key>"
# What it does: creates a non-root "deploy" user with your SSH key, installs Docker, enables a
# firewall (SSH/HTTP/HTTPS only), disables SSH password login, adds swap on small plans and turns on
# automatic security updates. Safe to re-run.
set -euo pipefail

PUBKEY="${1:?Pass your SSH public key (contents of ~/.ssh/id_ed25519.pub) as the first argument}"
DEPLOY_USER=deploy

if [ "$(id -u)" -ne 0 ]; then echo "Run as root" >&2; exit 1; fi
. /etc/os-release
if [ "${ID:-}" != "ubuntu" ]; then echo "This script supports Ubuntu (found ${ID:-unknown})" >&2; exit 1; fi

export DEBIAN_FRONTEND=noninteractive
apt-get update -y
apt-get install -y ca-certificates curl gnupg ufw unattended-upgrades rsync

# Docker Engine + Compose plugin from Docker's official repository
if ! command -v docker >/dev/null; then
  install -m 0755 -d /etc/apt/keyrings
  curl -fsSL https://download.docker.com/linux/ubuntu/gpg -o /etc/apt/keyrings/docker.asc
  chmod a+r /etc/apt/keyrings/docker.asc
  echo "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.asc] https://download.docker.com/linux/ubuntu ${VERSION_CODENAME} stable" \
    > /etc/apt/sources.list.d/docker.list
  apt-get update -y
  apt-get install -y docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin
fi
systemctl enable --now docker

# Deploy user with key-only SSH and docker access
id "$DEPLOY_USER" >/dev/null 2>&1 || adduser --disabled-password --gecos "" "$DEPLOY_USER"
usermod -aG docker "$DEPLOY_USER"
install -d -m 700 -o "$DEPLOY_USER" -g "$DEPLOY_USER" /home/$DEPLOY_USER/.ssh
grep -qxF "$PUBKEY" /home/$DEPLOY_USER/.ssh/authorized_keys 2>/dev/null || echo "$PUBKEY" >> /home/$DEPLOY_USER/.ssh/authorized_keys
chown "$DEPLOY_USER:$DEPLOY_USER" /home/$DEPLOY_USER/.ssh/authorized_keys
chmod 600 /home/$DEPLOY_USER/.ssh/authorized_keys
install -d -o "$DEPLOY_USER" -g "$DEPLOY_USER" /opt/launchmate /opt/launchmate-backups

# Firewall: only SSH, HTTP, HTTPS (+ HTTP/3)
ufw allow OpenSSH
ufw allow 80/tcp
ufw allow 443/tcp
ufw allow 443/udp
ufw --force enable

# SSH hardening: keys only (the deploy user's key was installed above)
sed -i -E 's/^#?PasswordAuthentication .*/PasswordAuthentication no/' /etc/ssh/sshd_config
sed -i -E 's/^#?PermitRootLogin .*/PermitRootLogin prohibit-password/' /etc/ssh/sshd_config
systemctl reload ssh || systemctl reload sshd

# Swap: Docker builds and Whisper need headroom on 2–4 GB plans
if ! swapon --show | grep -q /swapfile; then
  fallocate -l 4G /swapfile && chmod 600 /swapfile && mkswap /swapfile && swapon /swapfile
  echo '/swapfile none swap sw 0 0' >> /etc/fstab
fi

dpkg-reconfigure -f noninteractive unattended-upgrades

echo
echo "VPS ready. Next, from your computer: deploy/scripts/deploy.sh $DEPLOY_USER@<VPS_IP>"
