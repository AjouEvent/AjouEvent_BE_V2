#!/usr/bin/env bash
# HeatWave MySQL 논리 백업 → OCI Object Storage (쓰기 전용 PAR URL)
# cron: 30 4 * * * /home/ubuntu/ajouevent/scripts/backup-db.sh >> /home/ubuntu/ajouevent/backup.log 2>&1
# 보관 기간은 버킷의 수명 주기 정책(14일)이 관리한다.
set -euo pipefail

cd "$(dirname "$0")/.."
set -a
. ./.env.backup   # DB_HOST, DB_USER, DB_PASSWORD, DB_NAME, BACKUP_PAR_URL
set +a

stamp=$(TZ=Asia/Seoul date +%Y%m%d-%H%M)
object="db/${DB_NAME}-${stamp}.sql.zst"

docker run --rm -e MYSQL_PWD="$DB_PASSWORD" mysql:8.4 \
  mysqldump -h "$DB_HOST" -u "$DB_USER" \
  --single-transaction --quick --set-gtid-purged=OFF --hex-blob \
  --default-character-set=utf8mb4 --no-tablespaces "$DB_NAME" \
  | zstd -q -T1 -10 \
  | curl -sS --fail -X PUT --data-binary @- "${BACKUP_PAR_URL}${object}"

echo "$(date -Is) uploaded ${object}"
