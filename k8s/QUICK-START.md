# Quick Start Guide - K8s Microservices

## 5-Minute Setup

### Prerequisites Check (1 minute)

```bash
# Verify all tools are installed
kind --version
kubectl version --client
docker --version
java -version
mvn --version
```

### Full Automated Setup (15-30 minutes)

```bash
cd k8s
./setup.sh
```

This will:
1. Create local Docker registry
2. Create kind cluster
3. Build all microservices images
4. Deploy infrastructure (DB, RabbitMQ, etc.)
5. Deploy all microservices

**First time: ~25-30 minutes** (depends on internet and build)  
**Subsequent: ~5-10 minutes** (cached images)

## Access Services (After Setup)

### Option 1: Using the helper script
```bash
./k8s/port-forward.sh  # Shows all commands needed
```

### Option 2: Manual port forwarding

Open 5 terminals and run:

**Terminal 1: API Gateway**
```bash
kubectl port-forward -n microservices svc/api-gateway 8080:80
# Access: http://localhost:8080
```

**Terminal 2: Eureka Dashboard**
```bash
kubectl port-forward -n microservices svc/eureka 8761:8761
# Access: http://localhost:8761
```

**Terminal 3: RabbitMQ Management**
```bash
kubectl port-forward -n microservices svc/rabbitmq 15672:15672
# Access: http://localhost:15672 (guest:guest)
```

**Terminal 4: PostgreSQL**
```bash
kubectl port-forward -n microservices svc/postgres 5432:5432
# Connect: psql -h localhost -U postgres
```

## Test the Services

### 1. Check Service Status
```bash
./k8s/status.sh
```

Or manually:
```bash
kubectl get pods -n microservices
kubectl get svc -n microservices
```

### 2. Test API Gateway

```bash
# Health check
curl http://localhost:8080/actuator/health

# Customer service via gateway
curl http://localhost:8080/api/customers

# View available routes
curl http://localhost:8080/actuator/gateway/routes
```

### 3. View Eureka Dashboard
- Open http://localhost:8761 in browser
- Should see all services registered

### 4. Check RabbitMQ
- Open http://localhost:15672 in browser
- Login: guest / guest
- Check connections and queues


## Common Tasks

### View Service Logs
```bash
# Stream logs
kubectl logs -n microservices -l app=api-gateway -f

# View all containers
kubectl logs -n microservices -l app=order --all-containers=true
```

### Restart a Service
```bash
kubectl rollout restart deployment/customer -n microservices
```

### Scale Service
```bash
kubectl scale deployment/customer --replicas=3 -n microservices
```

### Execute Command in Pod
```bash
kubectl exec -it <pod-name> -n microservices -- bash
```

### Check Database
```bash
# Port forward already running? Connect:
psql -h localhost -U postgres

# Inside psql:
\l                    # List databases
\c customer           # Connect to database
\dt                   # List tables
SELECT * FROM users;  # Query data
```

## Cleanup

### Stop Everything (Keep data)
```bash
kubectl delete deployment --all -n microservices
```

### Full Teardown
```bash
./k8s/teardown.sh
```

This removes:
- Kind cluster
- All pods and services
- Local Docker registry

## Common Errors

### "kubectl not found"
```bash
brew install kubectl
```

### "Docker not running"
Start Docker Desktop

### "ImagePullBackOff"
```bash
# Registry not running, restart:
docker run -d --name local-registry -p 5000:5000 registry:2
```

### "Pods stuck in Pending"
```bash
kubectl describe pod <pod-name> -n microservices
```

### "Connection refused"
```bash
# Service might not be running yet
./k8s/status.sh    # Check status
kubectl logs -n microservices <pod-name>  # Check logs
```

See [TROUBLESHOOTING.md](TROUBLESHOOTING.md) for more issues.

## What's Running

### Microservices
- **api-gateway** - Main entry point (port 8080)
- **eureka** - Service registry (port 8761)
- **customer** - Customer service (2 replicas)
- **catalog** - Catalog service (2 replicas)
- **order** - Order service
- **payment** - Payment service
- **shipping** - Shipping service
- **inventory** - Inventory service
- **notification** - Notification service

### Infrastructure
- **postgres** - Database
- **rabbitmq** - Message broker
- **wiremock** - API mocking

## Next Steps

1. **Make API calls to test**
   ```bash
   curl -X POST http://localhost:8080/api/customers \
     -H "Content-Type: application/json" \
     -d '{"name":"John","email":"john@example.com"}'
   ```

2. **View Eureka Dashboard** at http://localhost:8761

3. **Monitor with Prometheus**
   - Port forward to services
   - View metrics at `/actuator/prometheus`

4. **Make code changes**
   - Edit code
   - Rebuild: `mvn clean compile jib:build -DsendCredentialsOverHttp=true`
   - Restart pod: `kubectl delete pod <pod-name> -n microservices`

## Useful Commands

```bash
# Watch pods
kubectl get pods -n microservices -w

# Get pod details
kubectl describe pod <pod-name> -n microservices

# View recent events
kubectl get events -n microservices --sort-by='.lastTimestamp' | tail -20

# Check resource usage
kubectl top pods -n microservices

# Execute in pod
kubectl exec -it <pod-name> -n microservices -- bash

# View ConfigMap
kubectl get configmap <name> -n microservices -o yaml

# Use Makefile
make k8s-status
make k8s-logs-customer
make k8s-restart-order
```

## Health Endpoints

Every service exposes health endpoints:

```bash
# Basic health
curl http://localhost:8080/actuator/health

# Liveness probe
curl http://localhost:8080/actuator/health/liveness

# Readiness probe
curl http://localhost:8080/actuator/health/readiness

# Metrics
curl http://localhost:8080/actuator/metrics

# Prometheus format
curl http://localhost:8080/actuator/prometheus
```

## Performance Tips

1. **Don't rebuild images constantly**
   - Use `mvn compile` for local testing
   - Only rebuild when deploying

2. **Use Makefile**
   - `make k8s-setup`
   - `make k8s-logs-<service>`

3. **Check logs early**
   - Always first check: `kubectl logs <pod> -n microservices`

4. **Monitor resources**
   - Run `kubectl top pods -n microservices`
   - Increase limits if needed

5. **Keep registry clean**
   - Run `docker system prune -a` if space issues

## Need Help?

1. Check [README.md](README.md) - Full documentation
2. Check [TROUBLESHOOTING.md](TROUBLESHOOTING.md) - Common issues
3. Check logs: `kubectl logs -n microservices <pod>`
4. Check events: `kubectl get events -n microservices`

---

**Estimated total setup time: 15-30 minutes**  
**Estimated disk space: 5-10 GB**  
**Estimated RAM needed: 8 GB**

