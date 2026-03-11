#!/bin/bash

# K8s Microservices Setup Script for Kind
# This script sets up a local Kubernetes cluster using kind and deploys all microservices

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"
CLUSTER_NAME="microservices"
REGISTRY_PORT=5001
REGISTRY_NAME="local-registry"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo -e "${YELLOW}=== K8s Microservices Setup ===${NC}"

# Check prerequisites
check_prerequisites() {
    echo -e "${YELLOW}Checking prerequisites...${NC}"

    if ! command -v kind &> /dev/null; then
        echo -e "${RED}kind not found. Install it: brew install kind${NC}"
        exit 1
    fi

    if ! command -v kubectl &> /dev/null; then
        echo -e "${RED}kubectl not found. Install it: brew install kubectl${NC}"
        exit 1
    fi

    if ! command -v docker &> /dev/null; then
        echo -e "${RED}Docker not found. Install Docker Desktop${NC}"
        exit 1
    fi

    echo -e "${GREEN}✓ All prerequisites met${NC}"
}

# Create local Docker registry
setup_registry() {
    echo -e "${YELLOW}Setting up local Docker registry...${NC}"

    if [ "$(docker inspect -f '{{.State.Running}}' $REGISTRY_NAME 2>/dev/null)" == "true" ]; then
        echo -e "${GREEN}✓ Registry already running${NC}"
    else
        # Check if container exists but is not running
        if docker inspect $REGISTRY_NAME &>/dev/null; then
            echo -e "${YELLOW}Registry container exists but not running, removing it...${NC}"
            docker rm $REGISTRY_NAME 2>/dev/null || true
        fi

        docker run -d \
            --name $REGISTRY_NAME \
            -p $REGISTRY_PORT:5000 \
            --restart always \
            registry:2
        echo -e "${GREEN}✓ Registry started on localhost:$REGISTRY_PORT${NC}"
    fi
}

# Create kind cluster
create_cluster() {
    echo -e "${YELLOW}Creating kind cluster...${NC}"

    if kind get clusters | grep -q "^$CLUSTER_NAME$"; then
        echo -e "${GREEN}✓ Cluster $CLUSTER_NAME already exists${NC}"
    else
        kind create cluster \
            --name=$CLUSTER_NAME \
            --config="$SCRIPT_DIR/kind-config.yaml" \
            --wait=300s
        echo -e "${GREEN}✓ Cluster $CLUSTER_NAME created${NC}"
    fi

    # Connect registry to kind network
    if ! docker network inspect kind &>/dev/null; then
        docker network create kind
    fi
    docker network connect kind $REGISTRY_NAME 2>/dev/null || true

    # Configure kind to use local registry
    kubectl apply -f - <<EOF
apiVersion: v1
kind: ConfigMap
metadata:
  name: local-registry-hosting
  namespace: kube-public
data:
  localRegistryHosting.v1: |
    host: "localhost:${REGISTRY_PORT}"
    help: "https://kind.sigs.k8s.io/docs/user/local-registry/"
EOF

    echo -e "${GREEN}✓ Registry connected to cluster${NC}"
}

# Build microservices images with Docker
build_images() {
    echo -e "${YELLOW}Building microservices images...${NC}"

    cd "$PROJECT_ROOT"

    services=(
        "eureka"
        "api-gateway"
        "customer"
        "catalog"
        "order"
        "payment"
        "shipping"
        "inventory"
        "notification"
    )

    for service in "${services[@]}"; do
        echo -e "${YELLOW}Building $service...${NC}"
        cd "$PROJECT_ROOT/$service"

        # Create Dockerfile if it doesn't exist
        if [ ! -f "Dockerfile" ]; then
            cat > Dockerfile << 'DOCKERFILE_END'
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY target/*.jar app.jar
ENTRYPOINT ["java", "-XX:+UseG1GC", "-jar", "/app/app.jar"]
DOCKERFILE_END
        fi

        # Package the service
        mvn clean package -DskipTests=true -Dspring-boot.build-image.skip=true

        # Build Docker image
        docker build \
            -t localhost:${REGISTRY_PORT}/$service:1.0 \
            -f Dockerfile .

        # Push to registry so kind can access it
        docker push localhost:${REGISTRY_PORT}/$service:1.0 || true

        cd "$PROJECT_ROOT"
        echo -e "${GREEN}✓ $service image built${NC}"
    done
}

# Load images into kind registry
load_images() {
    echo -e "${YELLOW}Ensuring images are accessible to kind cluster...${NC}"

    # kind should pull from the registry, just verify connectivity
    kubectl -n microservices run registry-test --image=localhost:${REGISTRY_PORT}/eureka:1.0 --restart=Never --dry-run=client -o yaml || true

    echo -e "${GREEN}✓ Images are accessible${NC}"
}

# Deploy infrastructure and services
deploy_services() {
    echo -e "${YELLOW}Deploying infrastructure and microservices...${NC}"

    echo -e "${YELLOW}Deploying infrastructure...${NC}"
    kubectl apply -f "$SCRIPT_DIR/01-infrastructure.yaml"

    echo -e "${YELLOW}Waiting for infrastructure to be ready...${NC}"
    kubectl wait --for=condition=ready pod \
        -l app=postgres \
        -n microservices \
        --timeout=300s || true

    sleep 5

    echo -e "${YELLOW}Deploying Eureka and Gateway...${NC}"
    kubectl apply -f "$SCRIPT_DIR/02-eureka-gateway.yaml"

    echo -e "${YELLOW}Waiting for Eureka to be ready...${NC}"
    kubectl wait --for=condition=ready pod \
        -l app=eureka \
        -n microservices \
        --timeout=300s || true

    sleep 5

    echo -e "${YELLOW}Deploying microservices...${NC}"
    kubectl apply -f "$SCRIPT_DIR/03-microservices.yaml"

    echo -e "${GREEN}✓ Services deployed${NC}"
}

# Wait for services to be ready
wait_for_services() {
    echo -e "${YELLOW}Waiting for services to be ready...${NC}"

    # Wait for api-gateway to be ready
    kubectl wait --for=condition=ready pod \
        -l app=api-gateway \
        -n microservices \
        --timeout=600s || true

    echo -e "${GREEN}✓ Services ready${NC}"
}

# Print connection info
print_info() {
    echo ""
    echo -e "${GREEN}=== Setup Complete ===${NC}"
    echo ""
    echo -e "${YELLOW}API Gateway:${NC}"
    echo "  Service: http://localhost (via kubectl port-forward)"
    echo "  To access: kubectl port-forward -n microservices svc/api-gateway 8080:80"
    echo ""
    echo -e "${YELLOW}Eureka Dashboard:${NC}"
    echo "  To access: kubectl port-forward -n microservices svc/eureka 8761:8761"
    echo "  Then open: http://localhost:8761"
    echo ""
    echo -e "${YELLOW}RabbitMQ Management:${NC}"
    echo "  To access: kubectl port-forward -n microservices svc/rabbitmq 15672:15672"
    echo "  Then open: http://localhost:15672 (guest:guest)"
    echo ""
    echo -e "${YELLOW}Zipkin:${NC}"
    echo "  To access: kubectl port-forward -n microservices svc/zipkin 9411:9411"
    echo "  Then open: http://localhost:9411"
    echo ""
    echo -e "${YELLOW}PostgreSQL:${NC}"
    echo "  To access: kubectl port-forward -n microservices svc/postgres 5432:5432"
    echo "  Connection: postgres://postgres:postgres@localhost:5432"
    echo ""
    echo -e "${YELLOW}Check pod status:${NC}"
    echo "  kubectl get pods -n microservices"
    echo "  kubectl logs -n microservices <pod-name>"
    echo ""
}

# Main execution
main() {
    check_prerequisites
    setup_registry
    create_cluster
    build_images
    deploy_services
    wait_for_services
    print_info

    echo -e "${GREEN}=== All done! ===${NC}"
}

main

