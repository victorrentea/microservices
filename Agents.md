# AGENTS.md

## Purpose
Quick handoff for future agents working in this repo.
This document summarizes the **current practical source of truth** for local development and Kubernetes usage.

---

## Repo shape
Multi-module Maven project rooted at `pom.xml`.

Modules:
- `shared`
- `api-gateway`
- `eureka`
- `customer`
- `catalog`
- `inventory`
- `notification`
- `order`
- `payment`
- `shipping`

Infrastructure/manifests/scripts live under `k8s/`.

---

## Current local runtime model
### Source of truth
Use **Docker Desktop Kubernetes** as the current local K8s target.

Important:
- `kubectl` context should normally be `docker-desktop`
- local deploy flow is driven by:
  - `k8s/setup.sh`
  - `k8s/rebuild-redeploy.sh`
  - `k8s/helm-deploy.sh`
  - `k8s/01-infrastructure.yaml`
  - `k8s/02-eureka-gateway.yaml`
  - `k8s/charts/microservice/` — Helm chart for all 7 microservices

### No Docker push needed locally
For Docker Desktop Kubernetes, **do not push images to Docker Hub for local deploy**.
Docker Desktop Kubernetes shares the local Docker daemon.

This only works because manifests now use:
- `imagePullPolicy: IfNotPresent`

If someone changes it back to `Always`, local deploys will start pulling from Docker Hub again.

---

## Build/runtime stack
From `pom.xml`:
- Java: **21**
- Spring Boot: **3.1.4**
- Spring Cloud BOM: **2022.0.4**

Common root dependencies:
- `spring-boot-starter-actuator`
- `micrometer-registry-prometheus`
- `lombok`
- `spring-boot-starter-test`

Shared module (`shared/pom.xml`) provides common app dependencies such as:
- `spring-boot-starter-web`
- `spring-cloud-starter-netflix-eureka-client`
- `spring-boot-starter-validation`
- `spring-cloud-starter-openfeign`
- `springdoc-openapi-starter-webmvc-ui`
- `spring-boot-devtools` (runtime)

Other modules add service-specific deps, especially:
- Spring Data JPA
- PostgreSQL driver
- Spring AMQP / Spring Cloud Stream / Rabbit binder
- Resilience4j (at least in `customer`)

---

## Kubernetes model
### Namespace
Everything is deployed in namespace:
- `microservices`

### Manifest order
Apply in this order:
1. `k8s/01-infrastructure.yaml` (Namespace, Postgres, RabbitMQ, WireMock, LGTM)
2. `k8s/02-eureka-gateway.yaml` (Eureka, API Gateway)
3. `./k8s/helm-deploy.sh` — deploys all 7 microservices via Helm

### Core infrastructure
`k8s/01-infrastructure.yaml` provides at least:
- `postgres`
- `rabbitmq`
- `wiremock`

`k8s/01-infrastructure.yaml` also provides:
- `lgtm` — Grafana LGTM all-in-one (Grafana + Loki + Tempo + Prometheus)
  - Grafana UI: `http://localhost:3000` (LoadBalancer, no auth in dev)
  - OTLP HTTP: `http://lgtm:4318` (cluster-internal, used by OTel agents)
  - OTLP gRPC: `lgtm:4317`

The OTel agent downloader is **inline in the Helm chart** (`initContainer` in `deployment.yaml`), not a separate manifest.

---

## Helm Charts — Microservice Deployments

The 7 microservices (`customer`, `catalog`, `order`, `payment`, `shipping`, `inventory`, `notification`) are deployed via a **shared Helm chart** that eliminates repetition.

```
k8s/charts/
  microservice/          ← single reusable chart (templates only once)
    Chart.yaml
    values.yaml          ← shared defaults: OTel, Eureka URL, port, image, HPA off
    templates/
      configmap.yaml
      deployment.yaml    ← includes OTel initContainer, conditional postgres/rabbitmq wait
      service.yaml
      hpa.yaml
  values/
    customer.yaml        ← 1 line  (datasourceHost)
    catalog.yaml         ← 8 lines (replicas, DB, rabbit, HPA enabled)
    order.yaml           ← 3 lines
    payment.yaml         ← 3 lines
    shipping.yaml        ← 3 lines
    inventory.yaml       ← 2 lines
    notification.yaml    ← 1 line
```

### Deploy microservices
```bash
./k8s/helm-deploy.sh              # all 7 services
./k8s/helm-deploy.sh order catalog  # selected services only
```

### Check deployed releases
```bash
helm list -n microservices
```

### To upgrade the OTel agent version
Change the version in the `curl` command inside `initContainers` in `k8s/charts/microservice/templates/deployment.yaml`.

---

## Observability — OTel Zero-code Instrumentation

Every microservice pod uses the **OpenTelemetry Java Agent v2.25.0** for zero-code instrumentation.

Strategy:
- No code changes in any microservice
- Each Deployment has an `initContainer` (`otel-agent-downloader`) that downloads the agent JAR from GitHub into an `emptyDir` volume
- The app container mounts that same volume and activates the agent via `JAVA_TOOL_OPTIONS`
- All signals (traces, metrics, logs) are sent via OTLP/HTTP to `http://lgtm:4318`

Env vars set in every microservice pod:
```
JAVA_TOOL_OPTIONS=-javaagent:/otel-agent/opentelemetry-javaagent.jar
OTEL_EXPORTER_OTLP_ENDPOINT=http://lgtm:4318
OTEL_EXPORTER_OTLP_PROTOCOL=http/protobuf
OTEL_SERVICE_NAME=<service-name>
OTEL_RESOURCE_ATTRIBUTES=deployment.environment=kubernetes
OTEL_LOGS_EXPORTER=otlp
OTEL_METRICS_EXPORTER=otlp
OTEL_TRACES_EXPORTER=otlp
```

To upgrade the agent: change the version in the `curl` URL in the `otel-agent-downloader` initContainer in `k8s/charts/microservice/templates/deployment.yaml`.

### Service discovery
- `eureka` runs inside K8s as a ClusterIP service
- microservices register to Eureka using `http://eureka:8761/eureka/`

### API entrypoint
In `k8s/02-eureka-gateway.yaml`:
- `api-gateway` service type is `LoadBalancer`
- public port is **80**
- pod listens on **8080**

In Docker Desktop, `http://localhost` should reach the API gateway.

---

## Naming conventions
### Docker images
Current local/K8s image naming convention:
- `victorrentea/victor:<service>-1.0`

Examples:
- `victorrentea/victor:eureka-1.0`
- `victorrentea/victor:api-gateway-1.0`
- `victorrentea/victor:order-1.0`

### K8s resource naming
Typical naming pattern per service:
- Deployment: `<service>`
- Service: `<service>`
- ConfigMap: `<service>-config`
- Label: `app: <service>`

Examples:
- deployment `order`
- service `order`
- configmap `order-config`

### Gateway route naming
Current gateway config includes **two route styles**:
1. legacy/direct paths used by shell test flow:
   - `/order`
   - `/payment/**`
   - `/shipping/**`
   - `/customer/**`
   - `/catalog/**`
   - `/inventory/**`
   - `/notification/**`
2. `/api/*` prefixed routes for external-style APIs

This dual mapping exists because older scripts/tests use direct paths, while other docs/code used `/api/...`.

---

## Local deploy scripts
### Full setup
`k8s/setup.sh`

What it does:
- checks `kubectl`, `docker`, `mvn`
- warns if current context is not `docker-desktop`
- runs full Maven package for all modules
- builds Docker images locally
- applies all manifests
- waits for infra and deployments

### Rebuild/redeploy selected services
`k8s/rebuild-redeploy.sh`

Usage:
- no args => rebuild all services
- args => rebuild only selected services

It does:
- Maven package per selected service
- local `docker build`
- for the 7 Helm-managed microservices: `helm upgrade --install`
- for eureka/api-gateway: `kubectl rollout restart`

### Teardown
`k8s/teardown.sh`

Current simplified behavior:
- deletes namespace `microservices`

---

## Logging and debugging
### Basic status
Useful commands:
- `kubectl get pods -n microservices`
- `kubectl get svc -n microservices`
- `kubectl get deployments -n microservices`
- `kubectl describe pod <pod-name> -n microservices`

### Logs
Use either deployment or pod logs:
- `kubectl logs -n microservices deployment/<service>`
- `kubectl logs -n microservices <pod-name>`
- `kubectl logs -n microservices deployment/<service> --previous`

When debugging crashes, `--previous` is often essential.

### Rollouts
- `kubectl rollout restart deployment/<service> -n microservices`
- `kubectl rollout status deployment/<service> -n microservices`

### ConfigMaps (Helm-managed services)
ConfigMaps for the 7 microservices are now managed by Helm.
To update config for a Helm service, edit `k8s/charts/values/<service>.yaml` or `k8s/charts/microservice/values.yaml`, then:
```bash
./k8s/helm-deploy.sh <service>
```
No manual `kubectl delete configmap` needed — Helm handles it.

---

## Shared app config conventions
From `shared/src/main/resources/config/application.yaml`:
- actuator endpoints are widely exposed
- health details are shown
- prometheus endpoint is enabled
- OpenFeign logging is `full`
- Eureka refresh intervals are very short for local demos
- logging pattern expects OTel-style MDC keys:
  - `trace_id`
  - `span_id`

Zipkin integration was removed from shared config and K8s manifests.
Old docs may still mention Zipkin.

---

## Lombok conventions
From `lombok.config`:
- `lombok.accessors.chain=true`
- `lombok.accessors.fluent=true`

Meaning:
- getters are fluent: `id()`, `items()`, `status()`
- setters are fluent/chained: `items(20)`, `customerId("x")`
- **do not assume JavaBean-style** accessors like `getId()` / `setItems(...)`

This matters a lot when editing entities.
If you see handwritten `getX()` / `setX()` methods in model classes, treat them as suspicious unless intentionally added.

---

## Operational pitfalls already discovered
### 1. K8s context confusion
There were previously multiple local K8s contexts/profiles (`minikube`, custom profile, Docker Desktop).
This caused edits/restarts to happen in one cluster while IntelliJ was showing another.

Current recommendation:
- use `docker-desktop`
- verify with: `kubectl config current-context`

### 2. Old docs drift
Some repo docs/scripts historically referenced:
- kind
- minikube
- local registry / Jib push flow

Current working flow is Docker Desktop + local Docker images.
Treat older minikube/kind references as stale unless revalidated.

### 3. RabbitMQ defaults to localhost if not configured
Several services failed until their ConfigMaps explicitly had:
```yaml
spring:
  rabbitmq:
    host: rabbitmq
    port: 5672
    username: guest
    password: guest
```

If a service uses AMQP / Spring Cloud Stream and logs show attempts to connect to `localhost:5672`, check its ConfigMap first.

### 4. Inventory duplicate seed data
`inventory` hit `NonUniqueResultException` because multiple `Stock` rows existed for the same product after repeated starts.
A startup cleanup/idempotent seeding fix was added in `InventoryApp` + `StockRepo`.
If reservation starts failing again with duplicate rows, inspect that code first.

### 5. Gateway route mismatches break tests fast
The shell-based epic flow assumes gateway routes like:
- `POST /order`
- `PUT /payment/{id}/status`
- `PUT /shipping/{id}/status`
- `GET /order/{id}`

If these return 404, inspect the **applied** `api-gateway-config` in the cluster, not just the YAML file in git.

---

## Domain/test assumptions already confirmed
### Seeded data
At startup:
- `catalog` seeds product `1` with price `1000`
- `inventory` seeds stock for product `1`

That is why test flows usually use:
- `productId = 1`

### Order lifecycle
From `order` module:
- initial status: `AWAITING_PAYMENT`
- after payment success: `PAYMENT_APPROVED`
- shipment requested: `SHIPPING_IN_PROGRESS`
- after shipping callback success: `SHIPPING_COMPLETED`

### Async flow
Order placement is not purely synchronous:
- payment/shipping parts interact with messaging and callbacks
- tests should prefer **polling with timeout** over fixed sleeps wherever possible

---

## Epic test notes
`tests.sh` exists as an end-to-end black-box script.
It currently targets the gateway at:
- `BASE_URL=${BASE_URL:-http://localhost}`

Useful assumptions for future work on this script:
- it should be autonomous (no manual orderId input)
- it should fail with diagnostics
- it should poll order status rather than relying only on `sleep`
- it depends on the gateway direct routes (`/order`, `/payment/...`, `/shipping/...`)

If converting it later to Java, prefer a dedicated black-box/system-test style rather than embedding the flow in a single microservice’s unit tests.

---

## Useful commands
### Current pod state
```bash
kubectl get pods -n microservices
```

### Current services
```bash
kubectl get svc -n microservices
```

### Logs for one service
```bash
kubectl logs -n microservices deployment/order
kubectl logs -n microservices deployment/order --previous
```

### Restart one service
```bash
kubectl rollout restart deployment/order -n microservices
kubectl rollout status deployment/order -n microservices
```

### Full local deploy
```bash
./k8s/setup.sh
```

### Rebuild only selected services
```bash
./k8s/rebuild-redeploy.sh inventory order api-gateway
```

### Tear down local stack
```bash
./k8s/teardown.sh
```

---

## Recommended approach for future agents
When changing runtime behavior:
1. verify current `kubectl` context
2. inspect the **applied** ConfigMap/Deployment in cluster
3. if config changed, recreate ConfigMap if needed
4. rollout restart the affected deployment
5. verify logs and health before touching unrelated services

When changing entity/model code:
1. check `lombok.config`
2. prefer fluent accessor style
3. avoid introducing bean-style accessors unless intentionally required

When debugging local K8s failures:
1. check `kubectl get pods -n microservices`
2. inspect `--previous` logs
3. inspect RabbitMQ/Postgres config in the service ConfigMap
4. verify gateway routes and service names match manifest naming

---

## Files worth reading first
- `AGENTS.md`
- `pom.xml`
- `lombok.config`
- `shared/pom.xml`
- `shared/src/main/resources/config/application.yaml`
- `k8s/charts/microservice/values.yaml`
- `k8s/charts/values/<service>.yaml`
- `k8s/helm-deploy.sh`
- `k8s/setup.sh`
- `k8s/rebuild-redeploy.sh`
- `k8s/01-infrastructure.yaml`
- `k8s/02-eureka-gateway.yaml`
- `tests.sh`

