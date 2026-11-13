#!/usr/bin/env bash
# OCI Vault 비밀을 읽어 env 파일로 쓴다. VM 의 인스턴스 주체(dynamic group ajouevent-vms)로 인증한다.
# 사용: render-env.sh <secret-name> <output-file>
#   예: render-env.sh ajouevent-app-env ~/ajouevent/.env
set -euo pipefail

secret_name=$1
output=$2
vault_id=${VAULT_ID:?VAULT_ID is required}
oci_cli_image=${OCI_CLI_IMAGE:-ghcr.io/oracle/oci-cli:20261028}

tmp=$(mktemp "${output}.XXXXXX")
trap 'rm -f "$tmp"' EXIT
chmod 600 "$tmp"

docker run --rm "$oci_cli_image" \
  secrets secret-bundle get-secret-bundle-by-name \
  --auth instance_principal \
  --vault-id "$vault_id" \
  --secret-name "$secret_name" \
  --query 'data."secret-bundle-content".content' --raw-output \
  | base64 -d > "$tmp"

mv "$tmp" "$output"
trap - EXIT
echo "rendered ${secret_name} -> ${output}"
