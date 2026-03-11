#!/usr/bin/env bash

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"

PROFILE="microservices"
NAMESPACE="microservices"
IMAGE_REPO="victorrentea/victor"
VERSION="1.0"

SKIP_BUILD=false
SKIP_PUSH=false
SKIP_APPLY=false
DRY_RUN=false

DEFAULT_SERVICES=(
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
SERVICES=("${DEFAULT_SERVICES[@]}")

usage() {
  cat <<'EOF'
Usage: ./rebuild-redeploy.sh [options]

Options:
  --services s1,s2,...   Build/redeploy only selected services
  --profile name         Minikube profile (default: microservices)
  --namespace name       Kubernetes namespace (default: microservices)
  --repo repo/name       Docker repo (default: victorrentea/victor)
  --version tag          Service version suffix (default: 1.0)
  --skip-build           Skip Maven + Docker build
  --skip-push            Skip docker push
  --skip-apply           Skip kubectl apply manifests
  --dry-run              Print commands only
  -h, --help             Show help

Image naming:
  <repo>:<service>-<version>
  Example: victorrentea/victor:customer-1.0
EOF
}

log() {
  printf "\n==> %s\n" "$1"
}

run() {
  if [ "$DRY_RUN" = true ]; then
    echo "[dry-run] $*"
  else
    eval "$@"
  fi
}

require_cmd() {
  command -v "$1" >/dev/null 2>&1 || {
    echo "Missing command: $1"
    exit 1
  }
}

parse_args() {
  while [ $# -gt 0 ]; do
    case "$1" in
      --services)
        IFS=',' read -r -a SERVICES <<< "$2"
        shift 2
        ;;
      --profile)
        PROFILE="$2"
        shift 2
        ;;
      --namespace)
        NAMESPACE="$2"
        shift 2
        ;;
      --repo)
        IMAGE_REPO="$2"
        shift 2
        ;;
      --version)
        VERSION="$2"
        shift 2
        ;;
      --skip-build)
        SKIP_BUILD=true
        shift
        ;;
      --skip-push)
        SKIP_PUSH=true
        shift
        ;;
      --skip-apply)
        SKIP_APPLY=true
        shift
        ;;
      --dry-run)
        DRY_RUN=true
        shift
        ;;
      -h|--help)
        usage
        exit 0
        ;;
      *)
        echo "Unknown option: $1"
        usage
        exit 1
        ;;
    esac
  done
}

check_prereqs() {
  require_cmd kubectl
  require_cmd minikube
  if [ "$SKIP_BUILD" = false ]; then
    require_cmd mvn
    require_cmd docker
  fi
  if [ "$SKIP_PUSH" = false ]; then
    require_cmd docker
  fi
}

ensure_context() {
  if [ "$DRY_RUN" = false ]; then
    minikube status -p "$PROFILE" >/dev/null
    kubectl config use-context "$PROFILE" >/dev/null 2>&1 || true
  fi
}

build_and_push() {
  for service in "${SERVICES[@]}"; do
    image_tag="${IMAGE_REPO}:${service}-${VERSION}"

    if [ "$SKIP_BUILD" = false ]; then
      log "Build ${service}"
      run "cd '$PROJECT_ROOT/$service' && mvn clean package -DskipTests=true"
      run "cd '$PROJECT_ROOT/$service' && docker build -t '$image_tag' ."
    fi

    if [ "$SKIP_PUSH" = false ]; then
      log "Push ${image_tag}"
      run "docker push '$image_tag'"
    fi
  done
}

apply_manifests() {
  if [ "$SKIP_APPLY" = true ]; then
    return
  fi

  log "Apply Kubernetes manifests"
  run "kubectl apply -f '$SCRIPT_DIR/01-infrastructure.yaml'"
  run "kubectl apply -f '$SCRIPT_DIR/02-eureka-gateway.yaml'"
  run "kubectl apply -f '$SCRIPT_DIR/03-microservices.yaml'"
}

restart_rollout() {
  log "Restart deployments"
  for service in "${SERVICES[@]}"; do
    run "kubectl rollout restart deployment/$service -n '$NAMESPACE'"
  done

  if [ "$DRY_RUN" = false ]; then
    for service in "${SERVICES[@]}"; do
      kubectl rollout status deployment/$service -n "$NAMESPACE" --timeout=300s || true
    done
  fi
}

summary() {
  log "Done"
  echo "Services: ${SERVICES[*]}"
  echo "Repo: $IMAGE_REPO"
  echo "Version: $VERSION"
  echo "Namespace: $NAMESPACE"
  echo
  echo "Check: kubectl get pods -n $NAMESPACE"
}

main() {
  parse_args "$@"
  check_prereqs
  ensure_context
  build_and_push
  apply_manifests
  restart_rollout
  summary
}

main "$@"

