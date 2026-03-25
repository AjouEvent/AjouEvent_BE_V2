#!/bin/bash
set -e

echo "=== [1/3] Pull latest image ==="
docker compose pull app

echo "=== [2/3] Restart app (Redis untouched) ==="
docker compose up -d --no-deps app

echo "=== [3/3] Remove unused images ==="
docker image prune -f

echo "=== Deploy complete ==="
docker compose ps
