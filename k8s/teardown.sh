#!/bin/bash

# K8s Microservices Teardown Script
# This script removes the kind cluster and local registry

set -e

CLUSTER_NAME="microservices"
REGISTRY_NAME="local-registry"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo -e "${YELLOW}=== K8s Microservices Teardown ===${NC}"

# Delete kind cluster
delete_cluster() {
    echo -e "${YELLOW}Deleting kind cluster...${NC}"

    if kind get clusters | grep -q "^$CLUSTER_NAME$"; then
        kind delete cluster --name=$CLUSTER_NAME
        echo -e "${GREEN}✓ Cluster deleted${NC}"
    else
        echo -e "${YELLOW}Cluster not found${NC}"
    fi
}

# Stop and remove registry
delete_registry() {
    echo -e "${YELLOW}Stopping Docker registry...${NC}"

    if [ "$(docker inspect -f '{{.State.Running}}' $REGISTRY_NAME 2>/dev/null)" == "true" ]; then
        docker stop $REGISTRY_NAME
        docker rm $REGISTRY_NAME
        echo -e "${GREEN}✓ Registry stopped and removed${NC}"
    else
        echo -e "${YELLOW}Registry not running${NC}"
    fi
}

# Main execution
main() {
    delete_cluster
    delete_registry

    echo -e "${GREEN}=== Teardown complete ===${NC}"
}

main

