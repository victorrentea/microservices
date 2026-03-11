# Kubernetes Setup for Microservices

This setup deploys all microservices to a local Kubernetes cluster using **kind** (Kubernetes in Docker).

## Prerequisites

- **Docker Desktop** (with Kubernetes disabled in settings)
- **kind**: `brew install kind`
- **kubectl**: `brew install kubectl`
- **Maven 3.8+**: For building images with Jib
- **Java 21**: Required by the project

## Quick Start

### 1. Full Setup (Automated)

Run the complete setup script which will:
- Check prerequisites
- Create a local Docker registry
- Create a kind cluster
- Build all microservices images using Jib
- Deploy infrastructure (Postgres, RabbitMQ, Zipkin, WireMock)
- Deploy all microservices

```bash
cd k8s
./setup.sh
```

**Estimated time:** 15-30 minutes (first run, depends on image builds)

### 2. Access Services

After setup completes, open port-forwards in separate terminals:

```bash
# API Gateway (port 8080)
kubectl port-forward -n microservices svc/api-gateway 8080:80

# Eureka Dashboard (port 8761)
kubectl port-forward -n microservices svc/eureka 8761:8761

# RabbitMQ Management (port 15672, login: guest:guest)
kubectl port-forward -n microservices svc/rabbitmq 15672:15672

# Zipkin (port 9411)
kubectl port-forward -n microservices svc/zipkin 9411:9411

# PostgreSQL (port 5432)
kubectl port-forward -n microservices svc/postgres 5432:5432
```

Or use the helper script:
```bash
./port-forward.sh
```

### 3. Verify Status

```bash
# Check pod status
./status.sh

# Or manually:
kubectl get pods -n microservices
kubectl get svc -n microservices
```

## Architecture

### Microservices

- **api-gateway** (port 8080) - Main entry point, routes requests to services
- **eureka** (port 8761) - Service registry
- **customer** - Customer service (2 replicas)
- **catalog** - Catalog service (2 replicas)
- **order** - Order service
- **payment** - Payment service
- **shipping** - Shipping service
- **inventory** - Inventory service
- **notification** - Notification service

### Infrastructure

- **postgres** - PostgreSQL database (one database per microservice)
- **rabbitmq** - Message broker (for async communication)
- **zipkin** - Distributed tracing
- **wiremock** - API mocking

## Files Structure

```
k8s/
├── kind-config.yaml          # Kind cluster configuration
├── 01-infrastructure.yaml     # Infrastructure services (DB, RabbitMQ, etc.)
├── 02-eureka-gateway.yaml     # Eureka and API Gateway
├── 03-microservices.yaml      # All microservices deployments
├── setup.sh                   # Main setup script
├── teardown.sh                # Cleanup script
├── status.sh                  # Check cluster status
└── port-forward.sh            # Port forwarding helper
```

## Configuration Details

### Database Access

- **Host:** postgres (within cluster) or localhost:5432 (via port-forward)
- **Username:** postgres
- **Password:** postgres
- **Databases:** catalog, customer, inventory, order_db, payment, shipping

### RabbitMQ

- **Host:** rabbitmq (within cluster) or localhost:15672 (management UI)
- **Username:** guest
- **Password:** guest
- **AMQP Port:** 5672

### Jib Configuration

The Jib Maven plugin is configured in the root `pom.xml`:
- Base image: `eclipse-temurin:21-jre`
- Registry: Local Docker registry on `localhost:5001`
- Target: Each service builds to `localhost:5001/<service-name>:1.0`

## Common Commands

### View Logs

```bash
# View logs for a specific service
kubectl logs -n microservices <pod-name>

# Stream logs
kubectl logs -n microservices <pod-name> -f

# View logs from all pods of a service
kubectl logs -n microservices -l app=customer
```

### SSH into a Pod

```bash
kubectl exec -it -n microservices <pod-name> -- /bin/bash
```

### Port Forward

```bash
kubectl port-forward -n microservices svc/<service-name> <local-port>:<remote-port>
```

### Restart a Service

```bash
kubectl rollout restart deployment/<service-name> -n microservices
```

### View Resource Usage

```bash
kubectl top pods -n microservices
kubectl top nodes
```

## Rebuilding Images

To rebuild a specific service:

```bash
cd <service-directory>
mvn clean compile jib:build \
  -DsendCredentialsOverHttp=true \
  -Djib.to.image="localhost:5000/<service-name>:1.0"
```

Then restart the deployment:
```bash
kubectl rollout restart deployment/<service-name> -n microservices
```

## Troubleshooting

### Pods not starting

```bash
# Check pod status and events
kubectl describe pod -n microservices <pod-name>

# Check init container logs
kubectl logs -n microservices <pod-name> -c wait-for-postgres
```

### Can't connect to local registry

```bash
# Check if registry is running
docker ps | grep local-registry

# If not running, manually start:
docker run -d --name local-registry -p 5000:5000 registry:2
```

### Cluster issues

```bash
# Get cluster info
kind get clusters
kubectl cluster-info --context kind-microservices

# Describe cluster
kubectl describe nodes
```

## Cleanup

To remove the cluster and registry:

```bash
cd k8s
./teardown.sh
```

This will:
- Delete the kind cluster
- Stop and remove the local Docker registry

## Performance Tips

1. **Resource limits:** Current setup uses reasonable defaults. Adjust in manifest files if needed.
2. **Replicas:** Customer and Catalog services run 2 replicas for testing load balancing.
3. **Init containers:** Wait for dependencies before starting services.
4. **Liveness/Readiness probes:** Automatically restart unhealthy pods.

## Monitoring

### Eureka Dashboard
- Visit: http://localhost:8761
- Shows all registered services and their health status

### RabbitMQ Management
- Visit: http://localhost:15672
- Login: guest / guest
- Monitor message queues and connections

### Zipkin
- Visit: http://localhost:9411
- View distributed traces across all services

## Next Steps

1. Create Ingress controller for better routing
2. Add Prometheus for metrics collection
3. Add Grafana for visualization
4. Set up persistent volumes for stateful services
5. Configure SSL/TLS
6. Add service mesh (Istio) for advanced networking

## References

- [Kind Documentation](https://kind.sigs.k8s.io/)
- [kubectl Cheat Sheet](https://kubernetes.io/docs/reference/kubectl/cheatsheet/)
- [Jib Documentation](https://github.com/GoogleContainerTools/jib)
- [Spring Boot on Kubernetes](https://spring.io/guides/gs/spring-boot-kubernetes/)

