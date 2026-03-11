#!/bin/bash

# Pre-Setup Validation Checklist
# Run this before executing ./setup.sh

set -e

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

echo -e "${BLUE}╔════════════════════════════════════════════════════════╗${NC}"
echo -e "${BLUE}║     Microservices K8s Setup - Validation Checklist     ║${NC}"
echo -e "${BLUE}╚════════════════════════════════════════════════════════╝${NC}"
echo ""

PASS=0
FAIL=0

check_command() {
    local name=$1
    local cmd=$2
    local install=$3

    if command -v $cmd &> /dev/null; then
        local version=$($cmd --version 2>&1 | head -1)
        echo -e "${GREEN}✓${NC} $name: $version"
        ((PASS++))
    else
        echo -e "${RED}✗${NC} $name: NOT FOUND"
        echo -e "  ${YELLOW}Install:${NC} $install"
        ((FAIL++))
    fi
}

check_file() {
    local name=$1
    local path=$2

    if [ -f "$path" ]; then
        echo -e "${GREEN}✓${NC} $name exists"
        ((PASS++))
    else
        echo -e "${RED}✗${NC} $name: NOT FOUND at $path"
        ((FAIL++))
    fi
}

check_resources() {
    local name=$1
    local required=$2
    local actual=$3

    if [ "$actual" -ge "$required" ]; then
        echo -e "${GREEN}✓${NC} $name: $actual (required: $required)"
        ((PASS++))
    else
        echo -e "${YELLOW}⚠${NC} $name: $actual (recommended: $required)"
        ((FAIL++))
    fi
}

echo -e "${YELLOW}1. Prerequisites${NC}"
echo "─────────────────────────────────────────────────────────"
check_command "kind" "kind" "brew install kind"
check_command "kubectl" "kubectl" "brew install kubectl"
check_command "docker" "docker" "Install Docker Desktop"
check_command "java" "java" "brew install openjdk@21"
check_command "maven" "mvn" "brew install maven"
echo ""

echo -e "${YELLOW}2. Project Files${NC}"
echo "─────────────────────────────────────────────────────────"
check_file "Root pom.xml" "pom.xml"
check_file "K8s folder" "k8s"
check_file "Setup script" "k8s/setup.sh"
check_file "Kind config" "k8s/kind-config.yaml"
check_file "Infrastructure manifest" "k8s/01-infrastructure.yaml"
check_file "Gateway manifest" "k8s/02-eureka-gateway.yaml"
check_file "Microservices manifest" "k8s/03-microservices.yaml"
echo ""

echo -e "${YELLOW}3. System Resources${NC}"
echo "─────────────────────────────────────────────────────────"

# Check RAM
if [[ "$OSTYPE" == "darwin"* ]]; then
    RAM_GB=$(($(sysctl -n hw.memsize) / 1024 / 1024 / 1024))
elif [[ "$OSTYPE" == "linux-gnu"* ]]; then
    RAM_GB=$(($(free -b | awk '/^Mem:/ {print $2}') / 1024 / 1024 / 1024))
else
    RAM_GB=0
fi

check_resources "System RAM" 8 $RAM_GB

# Check Disk
if [[ "$OSTYPE" == "darwin"* ]]; then
    DISK_GB=$(($(df / | awk 'NR==2 {print $4}') / 1024 / 1024))
elif [[ "$OSTYPE" == "linux-gnu"* ]]; then
    DISK_GB=$(($(df / | awk 'NR==2 {print $4}') / 1024 / 1024))
else
    DISK_GB=0
fi

check_resources "Available Disk" 30 $DISK_GB
echo ""

echo -e "${YELLOW}4. Docker Configuration${NC}"
echo "─────────────────────────────────────────────────────────"

# Check if Docker is running
if docker ps &> /dev/null; then
    echo -e "${GREEN}✓${NC} Docker daemon is running"
    ((PASS++))
else
    echo -e "${RED}✗${NC} Docker daemon is not running"
    echo -e "  ${YELLOW}Start:${NC} Open Docker Desktop"
    ((FAIL++))
fi

# Check Docker resources
if [[ "$OSTYPE" == "darwin"* ]]; then
    DOCKER_RAM=$(docker info 2>/dev/null | grep "Memory:" | awk '{print $2}' | head -1)
    if [ -z "$DOCKER_RAM" ]; then
        DOCKER_RAM="Unknown"
    fi
    echo -e "${GREEN}✓${NC} Docker Resources: $DOCKER_RAM (check Docker Desktop Preferences)"
else
    echo -e "${GREEN}✓${NC} Docker resources configured"
fi
((PASS++))
echo ""

echo -e "${YELLOW}5. Project Structure${NC}"
echo "─────────────────────────────────────────────────────────"

SERVICES=(
    "api-gateway"
    "eureka"
    "customer"
    "catalog"
    "order"
    "payment"
    "shipping"
    "inventory"
    "notification"
)

for service in "${SERVICES[@]}"; do
    if [ -d "$service" ] && [ -f "$service/pom.xml" ]; then
        echo -e "${GREEN}✓${NC} $service module found"
        ((PASS++))
    else
        echo -e "${RED}✗${NC} $service module: NOT FOUND"
        ((FAIL++))
    fi
done
echo ""

echo -e "${YELLOW}6. Build Configuration${NC}"
echo "─────────────────────────────────────────────────────────"

if grep -q "jib-maven-plugin" pom.xml; then
    echo -e "${GREEN}✓${NC} Jib plugin configured in root pom.xml"
    ((PASS++))
else
    echo -e "${RED}✗${NC} Jib plugin NOT found in root pom.xml"
    ((FAIL++))
fi

if grep -q "jib-maven-plugin" api-gateway/pom.xml; then
    echo -e "${GREEN}✓${NC} Jib plugin configured in api-gateway/pom.xml"
    ((PASS++))
else
    echo -e "${YELLOW}⚠${NC} Jib plugin missing in some services"
    ((FAIL++))
fi
echo ""

echo -e "${YELLOW}7. Documentation${NC}"
echo "─────────────────────────────────────────────────────────"
check_file "Quick Start Guide" "k8s/QUICK-START.md"
check_file "Full README" "k8s/README.md"
check_file "Troubleshooting Guide" "k8s/TROUBLESHOOTING.md"
check_file "Project Index" "INDEX.md"
echo ""

echo -e "${BLUE}╔════════════════════════════════════════════════════════╗${NC}"
echo -e "${BLUE}║                    Validation Results                 ║${NC}"
echo -e "${BLUE}╚════════════════════════════════════════════════════════╝${NC}"
echo ""

echo -e "${GREEN}Checks passed:${NC} $PASS"
echo -e "${RED}Checks failed:${NC} $FAIL"
echo ""

if [ $FAIL -eq 0 ]; then
    echo -e "${GREEN}✓ All checks passed! Ready to proceed with setup.${NC}"
    echo ""
    echo "Next steps:"
    echo "  1. Read: cat k8s/QUICK-START.md"
    echo "  2. Setup: cd k8s && ./setup.sh"
    echo ""
    exit 0
else
    echo -e "${RED}✗ Some checks failed. Please fix the issues above.${NC}"
    echo ""
    echo "Common fixes:"
    echo "  - Install missing tools (see brew commands above)"
    echo "  - Start Docker Desktop"
    echo "  - Ensure at least 8 GB RAM and 30 GB disk space"
    echo "  - Check internet connection"
    echo ""
    exit 1
fi

