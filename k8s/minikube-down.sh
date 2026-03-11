#!/usr/bin/env bash

set -euo pipefail

PROFILE="microservices"
NAMESPACE="microservices"

command -v minikube >/dev/null 2>&1 || { echo "minikube nu este instalat"; exit 1; }
command -v kubectl >/dev/null 2>&1 || { echo "kubectl nu este instalat"; exit 1; }

kubectl delete namespace "${NAMESPACE}" --ignore-not-found=true
minikube stop --profile="${PROFILE}" || true

cat <<EOF
Minikube oprit.
Pentru a șterge complet clusterul:
  minikube delete --profile=${PROFILE}
EOF

