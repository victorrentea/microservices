#!/usr/bin/env bash
# Remove all microservices resources from Docker Desktop Kubernetes.
# Usage: ./teardown.sh

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
NAMESPACE="microservices"

GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

log() { printf "\n${YELLOW}==> %s${NC}\n" "$1"; }
ok()  { printf "${GREEN}✓ %s${NC}\n" "$1"; }

log "Deleting namespace '$NAMESPACE' and all its resources"
kubectl delete namespace "$NAMESPACE" --ignore-not-found
ok "Namespace '$NAMESPACE' deleted"

printf "\n${GREEN}=== Teardown complete ===${NC}\n"
