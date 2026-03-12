#!/usr/bin/env bash
# Deploy (or upgrade) all microservices using Helm.
# Usage: ./helm-deploy.sh [service1 service2 ...]
#   No args = deploy ALL services

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
CHART="$SCRIPT_DIR/charts/microservice"
VALUES_DIR="$SCRIPT_DIR/charts/values"
NAMESPACE="microservices"
IMAGE_TAG="${IMAGE_TAG:-1.0}"

ALL_SERVICES=(customer catalog order payment shipping inventory notification)

SERVICES=("${@:-${ALL_SERVICES[@]}}")

log() { printf "\n\033[1;33m==> %s\033[0m\n" "$1"; }
ok()  { printf "\033[0;32m✓ %s\033[0m\n" "$1"; }

for svc in "${SERVICES[@]}"; do
  log "Deploying $svc"
  helm upgrade --install "$svc" "$CHART" \
    --namespace "$NAMESPACE" \
    --values "$VALUES_DIR/$svc.yaml" \
    --set "image.tag=$IMAGE_TAG" \
    --wait --timeout 120s
  ok "$svc deployed"
done
