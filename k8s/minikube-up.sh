#!/usr/bin/env bash

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
NAMESPACE="microservices"
PROFILE="microservices"
CPUS="${MINIKUBE_CPUS:-4}"
MEMORY_MB="${MINIKUBE_MEMORY_MB:-6144}"
DISK_SIZE="${MINIKUBE_DISK_SIZE:-20g}"

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

log() {
  echo -e "${YELLOW}$1${NC}"
}

ok() {
  echo -e "${GREEN}$1${NC}"
}

fail() {
  echo -e "${RED}$1${NC}"
  exit 1
}

command -v minikube >/dev/null 2>&1 || fail "minikube nu este instalat"
command -v kubectl >/dev/null 2>&1 || fail "kubectl nu este instalat"

log "Pornesc Minikube (${PROFILE})..."
minikube start \
  --profile="${PROFILE}" \
  --driver=docker \
  --cpus="${CPUS}" \
  --memory="${MEMORY_MB}" \
  --disk-size="${DISK_SIZE}"

kubectl config use-context "${PROFILE}" >/dev/null 2>&1 || true

log "Aplic infrastructura..."
kubectl apply -f "${SCRIPT_DIR}/01-infrastructure.yaml"

log "Aștept PostgreSQL și RabbitMQ..."
kubectl wait --for=condition=available deployment/postgres -n "${NAMESPACE}" --timeout=300s || true
kubectl wait --for=condition=available deployment/rabbitmq -n "${NAMESPACE}" --timeout=300s || true
kubectl wait --for=condition=available deployment/zipkin -n "${NAMESPACE}" --timeout=300s || true

log "Aplic Eureka și API Gateway..."
kubectl apply -f "${SCRIPT_DIR}/02-eureka-gateway.yaml"
kubectl wait --for=condition=available deployment/eureka -n "${NAMESPACE}" --timeout=300s || true

log "Aplic microserviciile..."
kubectl apply -f "${SCRIPT_DIR}/03-microservices.yaml"

ok "Deploy finalizat."
echo
log "Verifică starea:"
echo "  kubectl get pods -n ${NAMESPACE}"
echo
log "Acces local recomandat prin port-forward:"
echo "  kubectl port-forward -n ${NAMESPACE} svc/api-gateway 8080:80"
echo "  kubectl port-forward -n ${NAMESPACE} svc/eureka 8761:8761"
echo "  kubectl port-forward -n ${NAMESPACE} svc/rabbitmq 15672:15672"
echo "  kubectl port-forward -n ${NAMESPACE} svc/zipkin 9411:9411"
echo
log "Alternativ, pentru gateway:"
echo "  minikube service api-gateway -n ${NAMESPACE} --url --profile=${PROFILE}"
