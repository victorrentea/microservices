# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

> Also read `AGENTS.md` for the current operational source of truth before making substantive changes.

## Build & Test

```bash
# Build all (skip tests)
mvn clean install -DskipTests=true

# Build single service
mvn -pl order clean install -DskipTests=true

# Run all tests
mvn test

# Run specific test
mvn -pl customer test -Dtest=BulkheadTest

# End-to-end flow test (requires running stack)
./tests.sh
```

## Local Kubernetes Stack

**Runtime target: Docker Desktop Kubernetes** (not minikube/kind).

```bash
# Full setup (first run ~25 min, subsequent ~5 min)
./k8s/setup.sh

# Rebuild & redeploy selected services
./k8s/rebuild-redeploy.sh inventory order api-gateway

# Tear down
./k8s/teardown.sh
```

Manifests apply in this order (matters for dependencies):
1. `k8s/01-infrastructure.yaml` — Postgres, RabbitMQ, WireMock
2. `k8s/02-eureka-gateway.yaml` — Eureka, API Gateway (LoadBalancer on port 80)
3. `k8s/03-microservices.yaml` — All 9 microservices

Namespace: `microservices`. Always verify context with `kubectl config current-context` (should be `docker-desktop`).

No `docker push` needed — Docker Desktop K8s shares the local daemon. Manifests use `imagePullPolicy: IfNotPresent`.

Docker image naming: `victorrentea/victor:<service>-1.0`

## Architecture

Multi-module Maven monorepo (Java 21, Spring Boot 3.1.4, Spring Cloud 2022.0.4). Modules: `shared`, `api-gateway`, `eureka`, `customer`, `catalog`, `order`, `payment`, `shipping`, `inventory`, `notification`.

**`shared` module** provides common dependencies to all services: web, Eureka client, Feign, OpenAPI. Base config in `shared/src/main/resources/config/application.yaml` (Actuator, Prometheus, Feign logging=FULL, short Eureka refresh for demos).

**Service communication:**
- Sync: OpenFeign clients via Eureka service discovery
- Async: Spring Cloud Stream + RabbitMQ (order→payment→shipping flow)

**Order lifecycle:** `AWAITING_PAYMENT` → `PAYMENT_APPROVED` → `SHIPPING_IN_PROGRESS` → `SHIPPING_COMPLETED`

**Containerization:** Jib Maven plugin (no Dockerfile needed). Base image: `eclipse-temurin:21-jre`.

**API Gateway routes:** Two styles coexist — legacy direct paths (`/order`, `/payment/**`, etc.) and `/api/*` prefixed routes.

## Lombok Conventions

`lombok.config` sets `fluent=true` and `chain=true` repo-wide:
- Getters: `entity.id()`, `entity.status()` — **not** `getId()`
- Setters: `entity.items(20)` — **not** `setItems(20)`

Do not introduce JavaBean-style accessors unless intentionally required.

## Known Operational Pitfalls

- **RabbitMQ config:** Services using AMQP must have `spring.rabbitmq.host: rabbitmq` in their ConfigMap. Default is `localhost:5672` which fails in K8s.
- **ConfigMap changes:** Delete → re-apply → rollout restart. Changes don't propagate automatically.
- **Inventory duplicate seed data:** `InventoryApp` has idempotency fix. If reservation fails with `NonUniqueResultException`, inspect `StockRepo`.
- **Seeded test data:** `catalog` seeds product `1` (price 1000); `inventory` seeds stock for product `1`. Test flows use `productId=1`.
- **Zipkin is removed** — old docs/scripts may still reference it; ignore those references.

## Debugging K8s

```bash
kubectl get pods -n microservices
kubectl logs -n microservices deployment/order
kubectl logs -n microservices deployment/order --previous   # essential for crash loops
kubectl rollout restart deployment/order -n microservices
kubectl rollout status deployment/order -n microservices
```

When debugging, always inspect the **applied** ConfigMap in the cluster, not just the YAML in git.
