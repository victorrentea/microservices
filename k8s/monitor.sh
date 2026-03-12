#!/bin/bash

# Monitor setup progress
echo "=== K8s Setup Progress Monitor ==="
echo ""
echo "Checking cluster..."
kubectl cluster-info --context kind-microservices 2>/dev/null && echo "✓ Cluster is running" || echo "✗ Cluster not yet ready"

echo ""
echo "Checking registry..."
docker ps -f "name=local-registry" --format "table {{.Names}}\t{{.Status}}" 2>/dev/null || echo "Checking..."

echo ""
echo "Checking images in registry..."
curl -s http://localhost:5001/v2/_catalog 2>/dev/null | grep -o '"name":"[^"]*"' | cut -d'"' -f4 | wc -l | xargs -I {} echo "Images built: {}"

echo ""
echo "Checking pods..."
kubectl get pods -n microservices 2>/dev/null | wc -l | xargs -I {} echo "Pods: {}"

echo ""
echo "Running processes..."
ps aux | grep -E "mvn|docker build" | grep -v grep | wc -l | xargs -I {} echo "Build processes: {}"

echo ""
echo "Last 5 docker images:"
docker images | grep "localhost:5001" | head -5

