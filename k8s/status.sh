#!/bin/bash

# Script to check the status of all microservices

NAMESPACE="microservices"

# Colors for output
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m'

echo -e "${YELLOW}=== Microservices Status ===${NC}"
echo ""

echo -e "${YELLOW}Pods:${NC}"
kubectl get pods -n $NAMESPACE -o wide

echo ""
echo -e "${YELLOW}Services:${NC}"
kubectl get svc -n $NAMESPACE

echo ""
echo -e "${YELLOW}Pod Details:${NC}"
kubectl describe pods -n $NAMESPACE | grep -E "^(Name|Status|Image|Events)" -A 5

echo ""
echo -e "${YELLOW}Recent Events:${NC}"
kubectl get events -n $NAMESPACE --sort-by='.lastTimestamp' | tail -20

echo ""
echo -e "${YELLOW}Node Status:${NC}"
kubectl get nodes -o wide

echo ""
echo -e "${YELLOW}Resource Usage:${NC}"
kubectl top pods -n $NAMESPACE 2>/dev/null || echo "Metrics not available yet (wait for metrics-server)"

