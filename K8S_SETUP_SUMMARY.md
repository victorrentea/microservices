# Kubernetes Setup Summary

## What Was Created

### 1. **Jib Maven Plugin Configuration**
- Added to root `pom.xml` with pluginManagement
- Added to each microservice's `pom.xml`
- Configured to push images to `localhost:5000/<service>:1.0`
- Uses Eclipse Temurin Java 21 base image

### 2. **Kind Cluster Configuration** (`k8s/kind-config.yaml`)
- 3-node cluster (1 control-plane, 2 workers)
- Port mappings for Ingress (80, 443)
- Local registry access (5000)
- 4.5 GB total allocated

### 3. **Kubernetes Manifests**

#### `k8s/01-infrastructure.yaml`
Infrastructure services:
- **PostgreSQL**: Single database, auto-creates 6 schemas
- **RabbitMQ**: Message broker with management UI
- **Zipkin**: Distributed tracing
- **WireMock**: API mocking (async enabled)

#### `k8s/02-eureka-gateway.yaml`
Core services:
- **Eureka**: Service registry
- **API Gateway**: Routes requests to all services

#### `k8s/03-microservices.yaml`
Business services:
- **Customer** (2 replicas) - Uses PostgreSQL
- **Catalog** (2 replicas) - Uses PostgreSQL + RabbitMQ
- **Order** - Uses PostgreSQL + RabbitMQ
- **Payment** - Uses PostgreSQL + RabbitMQ
- **Shipping** - Uses PostgreSQL + RabbitMQ
- **Inventory** - Uses PostgreSQL
- **Notification** - Uses RabbitMQ only

All include:
- ConfigMaps for configuration
- Init containers (wait for dependencies)
- Liveness/Readiness probes
- Resource requests/limits
- Spring Boot health endpoints

### 4. **Automation Scripts**

#### `k8s/setup.sh`
Main setup script that:
1. Checks prerequisites (kind, kubectl, docker, mvn)
2. Creates local Docker registry
3. Creates kind cluster with 3 nodes
4. Builds all microservices with Jib
5. Deploys infrastructure
6. Deploys Eureka and Gateway
7. Deploys all microservices
8. Waits for services to be ready
9. Prints connection information

**Runtime:** ~25-30 minutes (first time, ~5-10 subsequent)

#### `k8s/teardown.sh`
Cleanup script that:
1. Deletes kind cluster
2. Stops/removes local Docker registry

#### `k8s/status.sh`
Verification script showing:
- Pod status
- Services
- Events
- Node status
- Resource usage (if metrics available)

#### `k8s/port-forward.sh`
Helper showing all port-forward commands needed

### 5. **Documentation**

#### `k8s/README.md`
Complete guide including:
- Prerequisites
- Quick start
- Architecture overview
- Configuration details
- Common commands
- Troubleshooting tips
- Performance optimization
- Monitoring setup

#### `k8s/QUICK-START.md`
Fast 5-minute guide with:
- Step-by-step setup
- Testing the services
- Common tasks
- Common errors
- Performance tips

#### `k8s/TROUBLESHOOTING.md`
In-depth troubleshooting:
- 10 categories of common issues
- Solutions for each
- Debug commands
- Prevention tips

#### `k8s/.env.example`
Reference configuration file with all variables

### 6. **Makefile**
Root-level Makefile with commands:
```bash
make k8s-setup          # Full setup
make k8s-teardown       # Full teardown
make k8s-status         # Check status
make k8s-logs-<svc>     # View logs
make k8s-restart-<svc>  # Restart service
make build              # Build locally
make build-<svc>        # Build specific service
```

## Architecture Overview

### Network Topology
```
Docker Desktop
├── Local Registry (localhost:5000)
└── Kind Cluster
    ├── API Gateway (LoadBalancer, port 80)
    ├── Eureka (ClusterIP, port 8761)
    ├── Microservices (ClusterIP, port 8080 each)
    ├── PostgreSQL (ClusterIP, port 5432)
    ├── RabbitMQ (ClusterIP, ports 5672/15672)
    ├── Zipkin (ClusterIP, port 9411)
    └── WireMock (ClusterIP, port 8080)
```

### Service Communication
```
API Gateway (8080)
    ├── /api/customers/* → Customer Service
    ├── /api/catalog/*   → Catalog Service
    ├── /api/orders/*    → Order Service
    ├── /api/inventory/* → Inventory Service
    ├── /api/payments/*  → Payment Service
    ├── /api/shipping/*  → Shipping Service
    └── /api/notifications/* → Notification Service

All services:
    ├── Register with Eureka
    ├── Send traces to Zipkin
    ├── Use PostgreSQL (where needed)
    └── Communicate via RabbitMQ
```

## Key Features

✅ **Fully Automated Setup**
- One command: `./k8s/setup.sh`
- Handles all prerequisites check, builds, deployment

✅ **Jib for Fast Image Building**
- No Dockerfile needed
- Faster than Docker build (layer caching)
- Direct push to registry

✅ **Production-Ready Manifests**
- Health checks (liveness/readiness)
- Resource limits and requests
- Init containers for dependency ordering
- ConfigMaps for configuration management

✅ **Multiple Replicas**
- Customer: 2 replicas (for load balancing)
- Catalog: 2 replicas (for load balancing)
- Others: 1 replica each (development)

✅ **Comprehensive Documentation**
- Quick start guide (5 minutes)
- Full reference documentation
- Detailed troubleshooting guide

✅ **Development-Friendly**
- Local registry (no push to Docker Hub)
- Port forwarding for easy access
- Helper scripts for common tasks
- Makefile for convenience

## What Happens During Setup

1. **Prerequisite Check** (1 min)
   - Verify kind, kubectl, docker, maven installed

2. **Registry Setup** (1 min)
   - Start local Docker registry on port 5000

3. **Cluster Creation** (3-5 min)
   - Create kind cluster with 3 nodes
   - Connect registry to cluster

4. **Image Building** (15-20 min, first time)
   - Build 9 microservices with Jib
   - Push images to local registry
   - Each service: ~2-3 minutes (first time)

5. **Infrastructure Deployment** (2-3 min)
   - Deploy Postgres, RabbitMQ, Zipkin, WireMock
   - Wait for Postgres to be ready

6. **Core Services Deployment** (2-3 min)
   - Deploy Eureka and API Gateway
   - Wait for Eureka to be ready

7. **Microservices Deployment** (2-3 min)
   - Deploy all business services
   - Wait for all to be ready

8. **Verification** (ongoing)
   - Print connection information
   - Ready for use

## Resource Requirements

### Minimum
- **CPU**: 4 cores
- **RAM**: 8 GB
- **Disk**: 20 GB (includes all build artifacts and images)

### Recommended
- **CPU**: 6+ cores
- **RAM**: 12+ GB
- **Disk**: 30 GB

### Per Service (Pod Resources)
- **Memory Request**: 512 MB
- **Memory Limit**: 1 GB
- **CPU Request**: 250m (0.25 cores)
- **CPU Limit**: 500m (0.5 cores)

## Networking Details

### DNS Resolution (within cluster)
```
Service URLs:
- postgres:5432
- rabbitmq:5672
- eureka:8761
- api-gateway:8080
- customer:8080
- catalog:8080
... etc
```

### External Access (from host)
Port forward required:
```bash
kubectl port-forward -n microservices svc/<service> <local-port>:<pod-port>
```

### Service Discovery
- Eureka registry at `http://eureka:8761/eureka/`
- All services auto-register on startup
- Services discover each other via Eureka

## Files Modified

### pom.xml Files
- `/pom.xml` - Added Jib pluginManagement
- `api-gateway/pom.xml` - Added Jib plugin
- `eureka/pom.xml` - Added Jib plugin
- `customer/pom.xml` - Added Jib plugin
- `catalog/pom.xml` - Added Jib plugin
- `order/pom.xml` - Added Jib plugin
- `payment/pom.xml` - Added Jib plugin
- `shipping/pom.xml` - Added Jib plugin
- `inventory/pom.xml` - Added Jib plugin
- `notification/pom.xml` - Added Jib plugin

### New Files Created (12 files in k8s/ folder)
- `k8s/kind-config.yaml` - Cluster configuration
- `k8s/01-infrastructure.yaml` - Infrastructure services
- `k8s/02-eureka-gateway.yaml` - Core services
- `k8s/03-microservices.yaml` - Business services
- `k8s/setup.sh` - Main setup script
- `k8s/teardown.sh` - Teardown script
- `k8s/status.sh` - Status verification
- `k8s/port-forward.sh` - Port forward helper
- `k8s/README.md` - Full documentation
- `k8s/QUICK-START.md` - Quick start guide
- `k8s/TROUBLESHOOTING.md` - Troubleshooting guide
- `k8s/.env.example` - Configuration reference
- `Makefile` - Root-level convenience commands

## Next Steps

### To Start the Cluster
```bash
cd k8s
./setup.sh
```

### To Access Services
```bash
# In separate terminals:
kubectl port-forward -n microservices svc/api-gateway 8080:80
kubectl port-forward -n microservices svc/eureka 8761:8761
kubectl port-forward -n microservices svc/rabbitmq 15672:15672
```

### To Stop Everything
```bash
cd k8s
./teardown.sh
```

### To Debug Issues
```bash
./k8s/status.sh           # Check status
kubectl logs -n microservices <pod>  # View logs
kubectl describe pod <pod> -n microservices  # Get details
```

## Performance Notes

✅ **Fast Builds**: Jib uses layer caching for incremental builds
✅ **No Docker CLI Needed**: Jib builds directly to registry
✅ **Optimized Startup**: Init containers prevent startup race conditions
✅ **Health Checks**: Kubernetes restarts unhealthy pods
✅ **Graceful Scaling**: Ready probes ensure traffic routing to healthy pods

---

**Setup Complete!** 🎉

Your microservices are ready to deploy. Start with:
```bash
cd k8s && ./setup.sh
```

For detailed instructions, see `k8s/QUICK-START.md`

