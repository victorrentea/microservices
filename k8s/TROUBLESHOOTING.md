# Kubernetes Microservices - Troubleshooting Guide

## Common Issues and Solutions

### 1. Cluster Creation Issues

#### Error: "kind not found"
```bash
# Solution: Install kind
brew install kind
```

#### Error: "Docker not running"
```bash
# Solution: Start Docker Desktop
# Or check if docker daemon is running:
docker ps
```

#### Error: "Insufficient disk space"
```bash
# Solution: Clear old images and containers
docker system prune -a --volumes

# Or increase Docker Desktop resources:
# Preferences -> Resources -> Disk image size
```

### 2. Image Building Issues

#### Error: "Failed to push image to localhost:5000"
```bash
# Check if registry is running:
docker ps | grep local-registry

# If not running, restart it:
docker run -d --name local-registry -p 5000:5000 registry:2

# Verify connectivity:
curl http://localhost:5000/v2/
```

#### Error: "jib-maven-plugin not found"
```bash
# Solution: The plugin needs to be in pluginManagement or plugins section
# Check that pom.xml has the jib configuration
mvn help:describe -Dplugin=com.google.cloud.tools:jib-maven-plugin

# Or force download:
mvn clean compile
```

#### Error: "OutOfMemory during build"
```bash
# Increase Maven heap:
export MAVEN_OPTS="-Xmx2048m"

# Or edit ~/.mavenrc:
echo "export MAVEN_OPTS='-Xmx2048m'" >> ~/.mavenrc
```

### 3. Pod Startup Issues

#### Pods stuck in "Pending"
```bash
# Check events:
kubectl describe pod <pod-name> -n microservices

# Check node resources:
kubectl top nodes
kubectl describe nodes

# Check PVC status:
kubectl get pvc -n microservices
```

#### Pods in "CrashLoopBackOff"
```bash
# Check logs:
kubectl logs <pod-name> -n microservices --previous

# Check all containers:
kubectl logs <pod-name> -n microservices -c <container-name>

# Describe pod for events:
kubectl describe pod <pod-name> -n microservices
```

#### Pods in "ImagePullBackOff"
```bash
# Verify image exists in registry:
curl http://localhost:5000/v2/_catalog

# Check pull policy:
kubectl get pod <pod-name> -n microservices -o yaml | grep -A 2 imagePullPolicy

# Rebuild and push image:
cd <service-dir>
mvn clean compile jib:build \
  -DsendCredentialsOverHttp=true \
  -Djib.to.image="localhost:5000/<service>:1.0"

# Restart pod:
kubectl delete pod <pod-name> -n microservices
```

### 4. Database Connection Issues

#### Error: "Connection refused"
```bash
# Verify postgres pod is running:
kubectl get pods -n microservices -l app=postgres

# Check postgres logs:
kubectl logs -n microservices -l app=postgres

# Test connection:
kubectl run -it --rm --image=postgres:11 \
  --restart=Never \
  -n microservices \
  postgres-test -- \
  psql -h postgres -U postgres -c "SELECT 1"

# If that works, issue is with service discovery
# Check service:
kubectl get svc -n microservices postgres
```

#### Error: "Database does not exist"
```bash
# Check init container logs:
kubectl logs <service-pod> -n microservices -c wait-for-postgres

# Manually run init script:
kubectl exec -it -n microservices <postgres-pod> -- \
  psql -U postgres -c "CREATE DATABASE catalog;"

# Check ConfigMap:
kubectl get configmap postgres-init -n microservices -o yaml
```

### 5. RabbitMQ Issues

#### Error: "Connection to RabbitMQ refused"
```bash
# Verify RabbitMQ is running:
kubectl get pods -n microservices -l app=rabbitmq

# Check RabbitMQ logs:
kubectl logs -n microservices -l app=rabbitmq

# Test connection:
kubectl run -it --rm --image=rabbitmq:3-management \
  --restart=Never \
  -n microservices \
  rabbitmq-test -- \
  nc -zv rabbitmq 5672
```

#### Access RabbitMQ Management UI
```bash
# Port forward:
kubectl port-forward -n microservices svc/rabbitmq 15672:15672

# Access: http://localhost:15672
# Login: guest / guest

# Or describe service:
kubectl get svc -n microservices rabbitmq -o wide
```

### 6. Service Discovery Issues

#### Services can't find each other
```bash
# Verify DNS:
kubectl run -it --rm --image=busybox:1.35 \
  --restart=Never \
  -n microservices \
  dns-test -- \
  nslookup eureka

# Check service endpoints:
kubectl get endpoints -n microservices

# Check CoreDNS:
kubectl logs -n kube-system -l k8s-app=kube-dns
```

#### Eureka shows no services
```bash
# Port forward to Eureka:
kubectl port-forward -n microservices svc/eureka 8761:8761

# Access dashboard: http://localhost:8761

# Check Eureka logs:
kubectl logs -n microservices -l app=eureka -f

# Verify services have Eureka client config:
kubectl get configmap <service>-config -n microservices -o yaml
```

### 7. Network Issues

#### Can't access API Gateway
```bash
# Port forward:
kubectl port-forward -n microservices svc/api-gateway 8080:80

# Test:
curl -v http://localhost:8080/api/customers

# Check Gateway logs:
kubectl logs -n microservices -l app=api-gateway -f

# Verify routes:
kubectl get configmap api-gateway-config -n microservices -o yaml
```

#### Network policies blocking traffic
```bash
# Check network policies:
kubectl get networkpolicy -n microservices

# If issues, temporarily remove:
kubectl delete networkpolicy --all -n microservices

# Verify connectivity:
kubectl run -it --rm --image=curlimages/curl \
  --restart=Never \
  -n microservices \
  curl-test -- \
  curl http://api-gateway:8080/actuator/health
```

### 8. Resource Issues

#### Pods evicted due to memory pressure
```bash
# Check resource usage:
kubectl top pods -n microservices

# Increase resource limits in manifests:
# Edit 03-microservices.yaml and adjust:
# resources:
#   requests:
#     memory: "512Mi"
#   limits:
#     memory: "1Gi"

# Apply changes:
kubectl apply -f k8s/03-microservices.yaml
```

#### CPU throttling
```bash
# Check metrics:
kubectl top pods -n microservices --containers

# Increase CPU limits:
kubectl set resources deployment/<name> \
  -n microservices \
  --limits=cpu=1000m,memory=2Gi \
  --requests=cpu=500m,memory=1Gi
```

### 9. Persistent Data Issues

#### Lost data after pod restart
```bash
# This is expected - these are stateless services
# Data is stored in PostgreSQL which persists

# For PostgreSQL data persistence:
# You need to add PersistentVolume/PersistentVolumeClaim

# Current setup:
# - Data lives only while pod is running
# - PostgreSQL databases are in pod's ephemeral storage
```

### 10. Monitoring and Debugging

#### Enable debug logging
```bash
# Update ConfigMap:
kubectl edit configmap <service>-config -n microservices

# Add/update logging level:
# logging:
#   level:
#     root: DEBUG
#     victor: DEBUG

# Restart service:
kubectl rollout restart deployment/<service> -n microservices
```

#### Trace requests with curl
```bash
# Port forward service:
kubectl port-forward -n microservices svc/<service> 8080:8080

# Trace request:
curl -v -H "X-Trace-Id: my-trace-123" \
  http://localhost:8080/path
```

#### Check Zipkin traces
```bash
# Port forward:
kubectl port-forward -n microservices svc/zipkin 9411:9411

# Access: http://localhost:9411
# Search for traces by service or trace ID
```

## Useful Commands

### General
```bash
# Watch pods
kubectl get pods -n microservices -w

# Watch events
kubectl get events -n microservices --sort-by='.lastTimestamp' -w

# Get detailed info
kubectl describe pod <pod> -n microservices

# Execute command in pod
kubectl exec -it <pod> -n microservices -- bash

# Copy files
kubectl cp microservices/<pod>:/path/to/file /local/path
```

### Debugging
```bash
# Get pod YAML
kubectl get pod <pod> -n microservices -o yaml

# Get pod logs with timestamps
kubectl logs <pod> -n microservices --timestamps=true

# Get previous logs (crashed pod)
kubectl logs <pod> -n microservices --previous

# Follow logs
kubectl logs <pod> -n microservices -f

# Get all logs from deployment
kubectl logs -n microservices -l app=<app> --all-containers=true
```

### Health Checks
```bash
# Port forward to service
kubectl port-forward -n microservices svc/<service> 8080:8080

# Check health
curl http://localhost:8080/actuator/health

# Check full health
curl http://localhost:8080/actuator/health/liveness
curl http://localhost:8080/actuator/health/readiness

# Check metrics
curl http://localhost:8080/actuator/metrics
```

### Cleanup
```bash
# Delete pod (will restart)
kubectl delete pod <pod> -n microservices

# Delete all pods in deployment (cascade restart)
kubectl delete pods -l app=<app> -n microservices

# Restart deployment
kubectl rollout restart deployment/<app> -n microservices

# Rollback deployment
kubectl rollout undo deployment/<app> -n microservices
```

## Getting Help

1. **Check pod logs first:**
   ```bash
   kubectl logs -n microservices <pod-name> -f
   ```

2. **Describe the pod:**
   ```bash
   kubectl describe pod <pod-name> -n microservices
   ```

3. **Check events:**
   ```bash
   kubectl get events -n microservices --sort-by='.lastTimestamp' | tail -20
   ```

4. **Check cluster health:**
   ```bash
   kubectl cluster-info
   kubectl get nodes -o wide
   ```

5. **Check resources:**
   ```bash
   kubectl top nodes
   kubectl top pods -n microservices
   ```

## Prevention Tips

1. Always check logs early
2. Use labels for easier filtering
3. Set resource requests/limits
4. Use liveness/readiness probes
5. Keep images small
6. Use health endpoints
7. Monitor with Prometheus/Grafana
8. Use distributed tracing (Zipkin)
9. Keep namespace clean (delete old pods)
10. Document your configurations

## Still Having Issues?

1. Check the main README.md
2. Review the manifest files (k8s/*.yaml)
3. Check Spring Boot logs in detail
4. Verify all prerequisites are installed
5. Try rebuilding the entire cluster:
   ```bash
   ./k8s/teardown.sh
   ./k8s/setup.sh
   ```

