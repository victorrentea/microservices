# 🚀 GETTING STARTED WITH KUBERNETES MICROSERVICES

Welcome! Your complete Kubernetes setup is ready. Follow these steps to get everything running.

## ⏱️ Time Required

- **First Time**: 25-35 minutes
- **Subsequent**: 5-10 minutes
- **Current Time**: ~30 minutes for this entire process

## 📋 3-Step Quick Start

### Step 1️⃣ : Validate Your System (2 minutes)

```bash
cd /Users/victorrentea/workspace/microservices
./k8s/validate.sh
```

This checks:
- ✅ All required tools installed (kind, kubectl, docker, java, maven)
- ✅ Sufficient system resources (8 GB RAM, 30 GB disk)
- ✅ Project files in place
- ✅ Docker running

**What to fix if validation fails:**
- Missing tools? Use `brew install <tool>`
- Docker not running? Open Docker Desktop
- Low resources? Close other apps or increase Docker Desktop resources
- See [TROUBLESHOOTING.md](k8s/TROUBLESHOOTING.md) for help

### Step 2️⃣ : Run Automated Setup (25-30 minutes)

```bash
cd /Users/victorrentea/workspace/microservices/k8s
./setup.sh
```

This will automatically:
1. Create a local Docker registry
2. Create a Kubernetes cluster with 3 nodes
3. Build all 9 microservices (this takes ~15-20 min)
4. Deploy infrastructure (PostgreSQL, RabbitMQ, Zipkin)
5. Deploy all microservices
6. Print connection information

**What to expect:**
- Several "Building..." messages
- "Waiting for..." messages during deployment
- Final printout with connection commands
- **Total time: 25-35 minutes first time**

### Step 3️⃣ : Access Your Services (5 minutes)

Once `./setup.sh` completes, run this in a separate terminal:

```bash
./k8s/port-forward.sh
```

This prints all port-forward commands. Run them in separate terminals:

```bash
# Terminal 1: API Gateway
kubectl port-forward -n microservices svc/api-gateway 8080:80

# Terminal 2: Eureka Dashboard
kubectl port-forward -n microservices svc/eureka 8761:8761

# Terminal 3: RabbitMQ Management
kubectl port-forward -n microservices svc/rabbitmq 15672:15672

# Terminal 4: Zipkin Tracing
kubectl port-forward -n microservices svc/zipkin 9411:9411

# Terminal 5: PostgreSQL Database
kubectl port-forward -n microservices svc/postgres 5432:5432
```

Then access:
- **API Gateway**: http://localhost:8080
- **Eureka**: http://localhost:8761
- **RabbitMQ**: http://localhost:15672 (guest:guest)
- **Zipkin**: http://localhost:9411
- **PostgreSQL**: localhost:5432

## 🧪 Test It Works

### Quick Health Check
```bash
curl http://localhost:8080/actuator/health
```

Expected: `{"status":"UP"}`

### View Eureka Dashboard
1. Port-forward to Eureka (see above)
2. Open http://localhost:8761
3. Should see all 9 services registered with status UP

### Make API Call
```bash
curl -X GET http://localhost:8080/api/customers
```

### View Traces in Zipkin
1. Port-forward to Zipkin
2. Open http://localhost:9411
3. After making API calls, search for recent traces

## 📁 What's Running

### 9 Microservices
- api-gateway (entry point)
- eureka (service registry)
- customer (2 replicas)
- catalog (2 replicas)
- order, payment, shipping, inventory, notification

### Infrastructure
- PostgreSQL (databases for services)
- RabbitMQ (message broker)
- Zipkin (distributed tracing)
- WireMock (API mocking)

### All in Kubernetes
- 3-node cluster running in Docker
- Services communicate via Kubernetes networking
- Data persists in containers (ephemeral - lost on restart)

## 🛠️ Common Tasks

### View Service Logs
```bash
kubectl logs -n microservices -l app=api-gateway -f
```

### Restart a Service
```bash
kubectl rollout restart deployment/customer -n microservices
```

### Check Cluster Status
```bash
./k8s/status.sh
```

### Scale a Service
```bash
kubectl scale deployment/customer --replicas=5 -n microservices
```

### Access PostgreSQL
```bash
# Already port-forwarded to localhost:5432
psql -h localhost -U postgres -d customer
# Password: postgres
```

## 📚 Documentation

- **[QUICK-START.md](k8s/QUICK-START.md)** - Fast reference
- **[README.md](k8s/README.md)** - Full documentation
- **[TROUBLESHOOTING.md](k8s/TROUBLESHOOTING.md)** - Debug guide
- **[K8S_SETUP_SUMMARY.md](K8S_SETUP_SUMMARY.md)** - Setup overview
- **[INDEX.md](INDEX.md)** - Complete index

## ⚠️ Troubleshooting

### Pods stuck in "Pending"
```bash
kubectl describe pod <pod-name> -n microservices
```

### Can't connect to a service
```bash
# Port-forward might be missing
./k8s/port-forward.sh
```

### ImagePullBackOff error
```bash
# Registry might not be running
docker ps | grep local-registry
```

### OutOfMemory errors
```bash
# Increase Docker resources in Docker Desktop Preferences
# Or scale down replicas
kubectl scale deployment/customer --replicas=1 -n microservices
```

**For more help**: See [TROUBLESHOOTING.md](k8s/TROUBLESHOOTING.md)

## 🧹 Cleanup

### Stop Everything (keep data in Docker)
```bash
kubectl delete deployment --all -n microservices
```

### Full Teardown (remove cluster and registry)
```bash
./k8s/teardown.sh
```

## 🎓 Next Steps

### Learn the System
1. Browse Eureka dashboard
2. Make API calls via API Gateway
3. Check logs with kubectl
4. Explore Zipkin traces

### Make Changes
1. Edit code in a service
2. Rebuild: `mvn -pl <service> clean compile`
3. Build image: `mvn -pl <service> jib:build -DsendCredentialsOverHttp=true`
4. Restart pod: `kubectl delete pod <pod> -n microservices`

### Scale for Load
1. Increase replicas: `kubectl scale deployment/<service> --replicas=3`
2. Test load balancing: `curl http://localhost:8080/api/customers`

### Monitor Performance
1. Check logs: `kubectl logs -n microservices -l app=<service> -f`
2. View metrics: `curl http://localhost:8080/actuator/prometheus`
3. Check traces: Open Zipkin dashboard

## 💡 Pro Tips

1. **Keep terminals open** - You need 5 terminal windows for port-forwarding
2. **Check logs first** - Most issues show in `kubectl logs`
3. **Use Makefile** - `make help` for convenient commands
4. **Watch pods** - `kubectl get pods -n microservices -w` shows live updates
5. **Describe pods** - `kubectl describe pod <name> -n microservices` shows events

## ❓ FAQ

**Q: Do I need to rebuild images every time?**  
A: No. Only when you change code. Then just rebuild and restart the pod.

**Q: Where's my data stored?**  
A: In PostgreSQL containers. Data is lost if the pod restarts (this is development setup).

**Q: Can I scale services?**  
A: Yes! `kubectl scale deployment/<service> --replicas=3`

**Q: How do I debug pod issues?**  
A: Check logs: `kubectl logs <pod> -n microservices`

**Q: How much disk space does this need?**  
A: ~30 GB (includes Docker images, build artifacts)

**Q: Can I use a different registry?**  
A: Yes, edit Jib config in pom.xml files.

## 🚨 If Something Goes Wrong

1. **Check logs first**:
   ```bash
   ./k8s/status.sh
   kubectl logs -n microservices <pod-name>
   ```

2. **Check events**:
   ```bash
   kubectl get events -n microservices --sort-by='.lastTimestamp'
   ```

3. **Nuclear option** (full reset):
   ```bash
   ./k8s/teardown.sh
   ./k8s/setup.sh
   ```

4. **See troubleshooting guide**:
   ```bash
   cat k8s/TROUBLESHOOTING.md
   ```

## 📞 Support Resources

- **Official Docs**:
  - [Kind Documentation](https://kind.sigs.k8s.io/)
  - [kubectl Cheat Sheet](https://kubernetes.io/docs/reference/kubectl/cheatsheet/)
  - [Jib Documentation](https://github.com/GoogleContainerTools/jib)

- **Local Resources**:
  - [QUICK-START.md](k8s/QUICK-START.md) - Fast reference
  - [README.md](k8s/README.md) - Full guide
  - [TROUBLESHOOTING.md](k8s/TROUBLESHOOTING.md) - Problem solving

## ✅ Success Checklist

After setup, you should have:
- [ ] Docker registry running (`docker ps | grep local-registry`)
- [ ] Kind cluster created (`kind get clusters | grep microservices`)
- [ ] All 9 services running (`kubectl get pods -n microservices`)
- [ ] Can access API Gateway (http://localhost:8080)
- [ ] Can see Eureka dashboard (http://localhost:8761)
- [ ] Can connect to RabbitMQ (http://localhost:15672)

## 🎉 Ready!

Everything is set up! Now run:

```bash
cd k8s
./validate.sh        # Verify system (2 min)
./setup.sh          # Run setup (25-30 min)
./port-forward.sh   # Print port-forward commands
```

Then follow the instructions on screen. Enjoy your microservices! 🚀

---

**Questions?** See the documentation files or check [TROUBLESHOOTING.md](k8s/TROUBLESHOOTING.md)

