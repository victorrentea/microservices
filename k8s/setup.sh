#!/usr/bin/env bash
# Deploy all microservices to Docker Desktop Kubernetes.
# Builds Docker images locally (no push needed - Docker Desktop shares the daemon).
# Usage: ./setup.sh

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"
NAMESPACE="microservices"
IMAGE_REPO="victorrentea/victor"
VERSION="1.0"

SERVICES=(eureka api-gateway customer catalog order payment shipping inventory notification)

GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m'

log()  { printf "\n${YELLOW}==> %s${NC}\n" "$1"; }
ok()   { printf "${GREEN}✓ %s${NC}\n" "$1"; }
fail() { printf "${RED}✗ %s${NC}\n" "$1"; exit 1; }

# --- Prerequisites ---
log "Checking prerequisites"
command -v kubectl &>/dev/null || fail "kubectl not found"
command -v docker  &>/dev/null || fail "docker not found"
command -v mvn     &>/dev/null || fail "mvn not found"

# Verify Docker Desktop k8s is the current context
CONTEXT=$(kubectl config current-context)
if [[ "$CONTEXT" != "docker-desktop" ]]; then
  printf "${YELLOW}Warning: current context is '%s', not 'docker-desktop'${NC}\n" "$CONTEXT"
  read -rp "Continue anyway? [y/N] " ans
  [[ "$ans" =~ ^[Yy]$ ]] || exit 1
fi
ok "kubectl context: $CONTEXT"

# --- Maven build all modules ---
log "Maven build (all modules)"
(cd "$PROJECT_ROOT" && mvn clean package -DskipTests -q)
ok "All JARs built"

# --- Docker build all images locally ---
log "Docker build (local images)"
for service in "${SERVICES[@]}"; do
  docker build -t "${IMAGE_REPO}:${service}-${VERSION}" "$PROJECT_ROOT/$service/" -q
  ok "  ${IMAGE_REPO}:${service}-${VERSION}"
done

# --- Apply k8s manifests ---
log "Applying Kubernetes manifests"
kubectl apply -f "$SCRIPT_DIR/01-infrastructure.yaml"
kubectl create configmap wiremock-mappings \
  --from-file="$PROJECT_ROOT/wiremock/mappings/" \
  -n "$NAMESPACE" --dry-run=client -o yaml | kubectl apply -f -
kubectl apply -f "$SCRIPT_DIR/02-eureka-gateway.yaml"
ok "Manifests applied"

# --- Deploy microservices with Helm ---
log "Deploying microservices with Helm"
command -v helm &>/dev/null || fail "helm not found"
"$SCRIPT_DIR/helm-deploy.sh"
ok "Microservices deployed via Helm"

# --- Wait for pods ---
log "Waiting for infrastructure (postgres, rabbitmq, lgtm)..."
kubectl wait deployment/postgres  -n "$NAMESPACE" --for=condition=available --timeout=120s
kubectl wait deployment/rabbitmq  -n "$NAMESPACE" --for=condition=available --timeout=120s
kubectl wait deployment/lgtm      -n "$NAMESPACE" --for=condition=available --timeout=180s
ok "Infrastructure ready"

log "Waiting for services..."
for service in "${SERVICES[@]}"; do
  kubectl rollout status deployment/"$service" -n "$NAMESPACE" --timeout=180s && ok "$service"
done

printf "\n${GREEN}=== All services running! ===${NC}\n"
kubectl get pods -n "$NAMESPACE"
