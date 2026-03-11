# Microservices Kubernetes Setup - Complete Index

## 📋 Quick Links

### 🚀 Getting Started
1. **[QUICK-START.md](k8s/QUICK-START.md)** - 5-minute setup guide (START HERE)
2. **[K8S_SETUP_SUMMARY.md](K8S_SETUP_SUMMARY.md)** - Overview of what was created

### 📚 Full Documentation
3. **[README.md](k8s/README.md)** - Complete reference guide
4. **[TROUBLESHOOTING.md](k8s/TROUBLESHOOTING.md)** - Debug common issues

### 🛠 Scripts & Configuration
5. **[setup.sh](k8s/setup.sh)** - One-command full setup
6. **[teardown.sh](k8s/teardown.sh)** - Clean up everything
7. **[status.sh](k8s/status.sh)** - Check cluster status
8. **[port-forward.sh](k8s/port-forward.sh)** - Helper for port forwarding

## 📁 Project Structure

```
microservices/
├── k8s/                          # ← ALL K8S SETUP FILES HERE
│   ├── README.md                 # Full documentation
│   ├── QUICK-START.md            # 5-minute setup
│   ├── TROUBLESHOOTING.md        # Debug guide
│   ├── .env.example              # Configuration reference
│   ├── setup.sh                  # Main setup script ✨
│   ├── teardown.sh               # Cleanup script
│   ├── status.sh                 # Status check
│   ├── port-forward.sh           # Port forwarding helper
│   ├── kind-config.yaml          # Kind cluster config
│   ├── 01-infrastructure.yaml    # DB, RabbitMQ, etc.
│   ├── 02-eureka-gateway.yaml    # Eureka & Gateway
│   └── 03-microservices.yaml     # All microservices
├── Makefile                      # Convenience commands
├── pom.xml                       # ← UPDATED: Jib plugin added
├── api-gateway/
│   ├── pom.xml                   # ← UPDATED: Jib plugin
│   └── src/...
├── eureka/
│   ├── pom.xml                   # ← UPDATED: Jib plugin
│   └── src/...
├── customer/
│   ├── pom.xml                   # ← UPDATED: Jib plugin
│   └── src/...
├── catalog/
│   ├── pom.xml                   # ← UPDATED: Jib plugin
│   └── src/...
├── order/
│   ├── pom.xml                   # ← UPDATED: Jib plugin
│   └── src/...
├── payment/
│   ├── pom.xml                   # ← UPDATED: Jib plugin
│   └── src/...
├── shipping/
│   ├── pom.xml                   # ← UPDATED: Jib plugin
│   └── src/...
├── inventory/
│   ├── pom.xml                   # ← UPDATED: Jib plugin
│   └── src/...
├── notification/
│   ├── pom.xml                   # ← UPDATED: Jib plugin
│   └── src/...
└── shared/
    ├── pom.xml
    └── src/...
```

## 🎯 What Was Done

### 1. **Jib Maven Plugin Setup** ✅
- Added to root `pom.xml` (pluginManagement)
- Added to all 9 microservice pom.xml files
- Configures automatic Docker image building
- Images push to `localhost:5000/<service>:1.0`

### 2. **Kubernetes Manifests** ✅
- **kind-config.yaml** - 3-node cluster configuration
- **01-infrastructure.yaml** - PostgreSQL, RabbitMQ, Zipkin, WireMock
- **02-eureka-gateway.yaml** - Eureka service registry + API Gateway
- **03-microservices.yaml** - All 9 microservices with configs

### 3. **Automation Scripts** ✅
- **setup.sh** - Complete automated setup (~25-30 min first time)
- **teardown.sh** - Clean removal of cluster and registry
- **status.sh** - Comprehensive status check
- **port-forward.sh** - Helper for accessing services

### 4. **Documentation** ✅
- **QUICK-START.md** - Fast 5-minute guide
- **README.md** - Full reference documentation
- **TROUBLESHOOTING.md** - Debug common issues
- **.env.example** - Configuration reference

### 5. **Convenience Tools** ✅
- **Makefile** - Common commands at root level
- **K8S_SETUP_SUMMARY.md** - This setup overview

## 🚀 Getting Started (3 Steps)

### Step 1: Read Quick Start (2 min)
```bash
cat k8s/QUICK-START.md
```

### Step 2: Run Setup (25-30 min first time)
```bash
cd k8s
./setup.sh
```

### Step 3: Access Services (5 min)
```bash
# Open these in separate terminals:
kubectl port-forward -n microservices svc/api-gateway 8080:80
kubectl port-forward -n microservices svc/eureka 8761:8761
kubectl port-forward -n microservices svc/rabbitmq 15672:15672
```

Then:
- API Gateway: http://localhost:8080
- Eureka: http://localhost:8761
- RabbitMQ: http://localhost:15672 (guest:guest)

## 📊 Services & Endpoints

### Microservices (all via API Gateway on port 8080)
```
GET/POST /api/customers/*     → Customer Service
GET/POST /api/catalog/*       → Catalog Service
GET/POST /api/orders/*        → Order Service
GET/POST /api/payments/*      → Payment Service
GET/POST /api/shipping/*      → Shipping Service
GET/POST /api/inventory/*     → Inventory Service
GET/POST /api/notifications/* → Notification Service
```

### Infrastructure Access (via port-forward)
```
Eureka Dashboard    http://localhost:8761
RabbitMQ Management http://localhost:15672 (guest/guest)
Zipkin              http://localhost:9411
PostgreSQL          localhost:5432 (postgres/postgres)
```

## 🔧 Common Commands

### Setup & Teardown
```bash
make k8s-setup          # Full setup
make k8s-teardown       # Full teardown
```

### Status & Logs
```bash
make k8s-status         # Check status
make k8s-logs-customer  # View customer service logs
make k8s-restart-order  # Restart order service
```

### Building
```bash
make build              # Build all locally
make build-customer     # Build specific service
mvn clean compile       # Fast compile
```

### Port Forwarding
```bash
./k8s/port-forward.sh   # Show all port-forward commands
```

## 📋 Files Created

### K8s Configuration Files (12 files)
| File | Purpose |
|------|---------|
| kind-config.yaml | Kind cluster 3-node config |
| 01-infrastructure.yaml | PostgreSQL, RabbitMQ, Zipkin, WireMock |
| 02-eureka-gateway.yaml | Eureka & API Gateway services |
| 03-microservices.yaml | All 9 microservices deployments |
| setup.sh | Automated setup script |
| teardown.sh | Cluster cleanup script |
| status.sh | Status verification script |
| port-forward.sh | Port forwarding helper |
| README.md | Complete reference guide |
| QUICK-START.md | Fast setup guide |
| TROUBLESHOOTING.md | Debug guide |
| .env.example | Configuration reference |

### Modified Files (10 files)
| File | Change |
|------|--------|
| pom.xml | Added Jib pluginManagement |
| api-gateway/pom.xml | Added Jib plugin |
| eureka/pom.xml | Added Jib plugin |
| customer/pom.xml | Added Jib plugin |
| catalog/pom.xml | Added Jib plugin |
| order/pom.xml | Added Jib plugin |
| payment/pom.xml | Added Jib plugin |
| shipping/pom.xml | Added Jib plugin |
| inventory/pom.xml | Added Jib plugin |
| notification/pom.xml | Added Jib plugin |

### New Files at Root
| File | Purpose |
|------|---------|
| Makefile | Convenience commands |
| K8S_SETUP_SUMMARY.md | Setup overview |

## 🎯 Architecture

### Cluster Topology
```
Local Development Machine
├── Docker Desktop
│   ├── Local Registry (localhost:5000)
│   └── Kind Cluster
│       ├── Control Plane Node
│       ├── Worker Node 1
│       └── Worker Node 2
│
└── Port Forwarding
    ├── :8080 → api-gateway
    ├── :8761 → eureka
    ├── :15672 → rabbitmq
    ├── :9411 → zipkin
    └── :5432 → postgres
```

### Service Mesh
```
Client Requests
    ↓
API Gateway (port 8080)
    ↓
Service Discovery (Eureka)
    ↓
Microservices (customer, catalog, order, payment, shipping, inventory, notification)
    ↓
Data Storage
├── PostgreSQL (shared)
├── RabbitMQ (message broker)
├── Zipkin (tracing)
└── WireMock (API mocking)
```

## ⚙️ Configuration

### Database Credentials
- **User**: postgres
- **Password**: postgres
- **Databases**: catalog, customer, inventory, order_db, payment, shipping

### RabbitMQ Credentials
- **User**: guest
- **Password**: guest
- **AMQP Port**: 5672
- **Management Port**: 15672

### Service Registry
- **URL**: http://eureka:8761/eureka/
- **Dashboard**: http://localhost:8761 (via port-forward)

## 📈 Scaling Services

### Increase Replicas
```bash
kubectl scale deployment/customer --replicas=5 -n microservices
```

### Adjust Resource Limits
Edit `k8s/03-microservices.yaml` and adjust:
```yaml
resources:
  requests:
    memory: "512Mi"
    cpu: "250m"
  limits:
    memory: "1Gi"
    cpu: "500m"
```

Then apply:
```bash
kubectl apply -f k8s/03-microservices.yaml
```

## 🔍 Monitoring & Debugging

### View Pod Logs
```bash
kubectl logs -n microservices <pod-name> -f
```

### Execute Command in Pod
```bash
kubectl exec -it -n microservices <pod-name> -- bash
```

### Check Pod Status
```bash
kubectl describe pod -n microservices <pod-name>
```

### View Events
```bash
kubectl get events -n microservices --sort-by='.lastTimestamp'
```

## 📱 Health Checks

Every service has health endpoints:
```bash
curl http://localhost:8080/actuator/health
curl http://localhost:8080/actuator/health/liveness
curl http://localhost:8080/actuator/health/readiness
curl http://localhost:8080/actuator/metrics
curl http://localhost:8080/actuator/prometheus
```

## 🆘 Need Help?

1. **Fast Setup Issues**: See [QUICK-START.md](k8s/QUICK-START.md)
2. **Detailed Setup**: See [README.md](k8s/README.md)
3. **Debug Problems**: See [TROUBLESHOOTING.md](k8s/TROUBLESHOOTING.md)
4. **Check Status**: Run `./k8s/status.sh`
5. **View Logs**: Run `kubectl logs -n microservices <pod>`

## ✅ Prerequisites Checklist

- [ ] Docker Desktop installed
- [ ] kind installed (`brew install kind`)
- [ ] kubectl installed (`brew install kubectl`)
- [ ] Java 21 installed
- [ ] Maven 3.8+ installed
- [ ] 8 GB RAM available
- [ ] 30 GB disk space available
- [ ] Internet connection (for initial downloads)

## ⏱️ Timing

| Phase | Duration | Notes |
|-------|----------|-------|
| Prerequisite Check | 1 min | One-time |
| Registry Setup | 1 min | One-time |
| Cluster Creation | 3-5 min | One-time |
| Image Building | 15-20 min | First time only (cached after) |
| Infrastructure Deployment | 2-3 min | One-time |
| Services Deployment | 2-3 min | One-time |
| **TOTAL FIRST TIME** | **25-35 min** | Depends on bandwidth/hardware |
| **Subsequent** | **5-10 min** | Cached images |

## 🎓 Learning Path

1. Read [QUICK-START.md](k8s/QUICK-START.md) (5 min)
2. Run `./k8s/setup.sh` (25-30 min)
3. Explore Eureka dashboard (5 min)
4. Try API calls via gateway (10 min)
5. Check logs with `kubectl logs` (5 min)
6. Read [README.md](k8s/README.md) for deeper understanding (20 min)
7. Explore [TROUBLESHOOTING.md](k8s/TROUBLESHOOTING.md) (reference)

**Total Learning Time: 1.5-2 hours**

## 📞 Support Commands

```bash
# Quick status
./k8s/status.sh

# Port forwarding helper
./k8s/port-forward.sh

# Build locally (for testing)
mvn clean compile

# Full rebuild
mvn clean install -DskipTests

# Check Makefile options
make help
```

---

## 🎉 Ready to Go!

Everything is set up and ready. Start with:

```bash
cd k8s
./setup.sh
```

Then follow the printed instructions to access your services.

**Enjoy your microservices cluster!** 🚀

