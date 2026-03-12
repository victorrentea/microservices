#!/bin/bash

echo "=== Prerequisites Check ==="
echo ""

echo "1. kind:"
if command -v kind &> /dev/null; then
    echo "   ✓ Installed: $(kind --version)"
else
    echo "   ✗ NOT FOUND - Install: brew install kind"
fi

echo ""
echo "2. kubectl:"
if command -v kubectl &> /dev/null; then
    echo "   ✓ Installed: $(kubectl version --client --short 2>/dev/null || echo 'version check error')"
else
    echo "   ✗ NOT FOUND - Install: brew install kubectl"
fi

echo ""
echo "3. docker:"
if command -v docker &> /dev/null; then
    echo "   ✓ Installed: $(docker --version)"
else
    echo "   ✗ NOT FOUND - Install: Docker Desktop"
fi

echo ""
echo "4. java:"
if command -v java &> /dev/null; then
    echo "   ✓ Installed: $(java -version 2>&1 | head -1)"
else
    echo "   ✗ NOT FOUND - Install: brew install openjdk@21"
fi

echo ""
echo "5. mvn:"
if command -v mvn &> /dev/null; then
    echo "   ✓ Installed: $(mvn --version 2>&1 | head -1)"
else
    echo "   ✗ NOT FOUND - Install: brew install maven"
fi

echo ""
echo "=== System Resources ==="
if [[ "$OSTYPE" == "darwin"* ]]; then
    RAM_GB=$(($(sysctl -n hw.memsize) / 1024 / 1024 / 1024))
    echo "RAM: ${RAM_GB} GB"
fi

echo ""
echo "=== Setup Ready? ==="
if command -v kind &> /dev/null && \
   command -v kubectl &> /dev/null && \
   command -v docker &> /dev/null && \
   command -v java &> /dev/null && \
   command -v mvn &> /dev/null; then
    echo "✓ All prerequisites installed!"
    echo ""
    echo "Ready to run: ./setup.sh"
else
    echo "✗ Some prerequisites missing - install them first"
fi

