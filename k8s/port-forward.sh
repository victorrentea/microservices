#!/bin/bash

# Utility script to set up port forwarding for all services

set -e

NAMESPACE="microservices"

# Colors for output
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

echo -e "${YELLOW}=== Setting up port forwarding ===${NC}"
echo ""

# Function to port-forward and show info
forward_service() {
    local service=$1
    local port=$2
    local description=$3

    echo -e "${GREEN}Port forwarding:${NC} $description"
    echo "  Command: kubectl port-forward -n $NAMESPACE svc/$service $port:$port"
    echo "  Access: http://localhost:$port"
    echo ""
}

echo -e "${YELLOW}Copy and run these commands in separate terminals:${NC}"
echo ""

forward_service "api-gateway" "8080" "API Gateway"
forward_service "eureka" "8761" "Eureka Dashboard"
forward_service "rabbitmq" "15672" "RabbitMQ Management (guest:guest)"
forward_service "zipkin" "9411" "Zipkin"
forward_service "postgres" "5432" "PostgreSQL"

echo -e "${YELLOW}Or run individually:${NC}"
echo ""
echo "API Gateway:"
echo "  kubectl port-forward -n $NAMESPACE svc/api-gateway 8080:8080"
echo ""
echo "Eureka:"
echo "  kubectl port-forward -n $NAMESPACE svc/eureka 8761:8761"
echo ""
echo "RabbitMQ:"
echo "  kubectl port-forward -n $NAMESPACE svc/rabbitmq 15672:15672"
echo ""
echo "Zipkin:"
echo "  kubectl port-forward -n $NAMESPACE svc/zipkin 9411:9411"
echo ""
echo "PostgreSQL:"
echo "  kubectl port-forward -n $NAMESPACE svc/postgres 5432:5432"
echo ""

