#!/usr/bin/env bash
# Rebuild, docker build (local only - no push needed for Docker Desktop k8s),
# and rollout restart the given services.
# Usage: ./rebuild-redeploy.sh [service1 service2 ...]
#   No args = rebuild ALL services

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"
NAMESPACE="microservices"
IMAGE_REPO="victorrentea/victor"
VERSION="1.0"

ALL_SERVICES=(
  eureka
  api-gateway
  customer
  catalog
  order
  payment
  shipping
  inventory
  notification
)

# Use args as service list, or default to ALL
if [ $# -gt 0 ]; then
  SERVICES=("$@")
else
  SERVICES=("${ALL_SERVICES[@]}")
fi

log() {
  printf "\n\033[1;33m==> %s\033[0m\n" "$1"
}
ok() {
  printf "\033[0;32m✓ %s\033[0m\n" "$1"
}

log "Services to rebuild: ${SERVICES[*]}"

for service in "${SERVICES[@]}"; do
  log "[$service] Maven build"
  (cd "$PROJECT_ROOT/$service" && mvn clean package -DskipTests -q)
  ok "$service JAR built"

  log "[$service] Docker build (local)"
  docker build -t "${IMAGE_REPO}:${service}-${VERSION}" "$PROJECT_ROOT/$service/" -q
  ok "$service image built: ${IMAGE_REPO}:${service}-${VERSION}"

  # Redeploy via Helm if a values file exists, otherwise plain kubectl restart
  HELM_SERVICES=(customer catalog order payment shipping inventory notification)
  if [[ " ${HELM_SERVICES[*]} " =~ " ${service} " ]]; then
    log "[$service] Helm upgrade"
    helm upgrade --install "$service" "$SCRIPT_DIR/charts/microservice" \
      -f "$SCRIPT_DIR/charts/values/$service.yaml" \
      -n "$NAMESPACE" --wait --timeout 120s
  else
    log "[$service] Rollout restart"
    kubectl rollout restart deployment/"$service" -n "$NAMESPACE"
    kubectl rollout status deployment/"$service" -n "$NAMESPACE" --timeout=120s
  fi
  ok "$service is Ready"
done

printf "\n\033[0;32mAll done! Check: kubectl get pods -n %s\033[0m\n" "$NAMESPACE"
