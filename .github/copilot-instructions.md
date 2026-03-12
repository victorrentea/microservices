# GitHub Copilot Instructions

Always read `AGENTS.md` at the repository root before making changes in this workspace.
Treat `AGENTS.md` as the primary handoff and operational context file for this project.

Key repo rules to keep in mind:
- This is a Java 21 multi-module Maven monorepo using Spring Boot 3.1.4 and Spring Cloud 2022.0.4.
- The current local Kubernetes source of truth is Docker Desktop Kubernetes, not Minikube or kind.
- Local Kubernetes deploys do not require `docker push`; images are built locally and manifests use `imagePullPolicy: IfNotPresent`.
- Kubernetes namespace is `microservices`.
- Apply manifests in this order: `k8s/01-infrastructure.yaml`, `k8s/02-eureka-gateway.yaml`, `k8s/03-microservices.yaml`.
- `api-gateway` is the local entrypoint and should be reachable at `http://localhost` in Docker Desktop.
- Gateway supports both legacy direct routes (`/order`, `/payment/**`, `/shipping/**`, etc.) and `/api/*` routes.
- Lombok uses fluent + chained accessors repo-wide (`lombok.accessors.fluent=true`, `lombok.accessors.chain=true`), so prefer `id()`, `items()`, `items(value)` over `getId()` / `setItems(...)`.
- When debugging Kubernetes issues, inspect the applied ConfigMaps/Deployments in the cluster, not just the YAML files in git.
- If a service using RabbitMQ tries to connect to `localhost:5672`, fix its ConfigMap to point to `rabbitmq:5672`.

Before concluding substantial changes:
- run the relevant Maven build/tests
- validate Kubernetes changes with `kubectl` if applicable
- keep `AGENTS.md` updated if you learn new repo-specific operational details

