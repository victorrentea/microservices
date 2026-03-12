# AGENTS Guide: Helm/Kubernetes Build-Deploy Practices

This document captures reusable build/deploy chart practices extracted from this repository.
It is language-agnostic and intended to be reused in other repos (including Java services).

## Scope

- Kubernetes packaging and deployment with Helm
- CI-to-Helm deployment contract
- Environment layering, safety flags, and chart structure
- Secret/config integration patterns around deployment

## Reusable Best Practices

### 1) Chart topology: service charts + umbrella chart

- Keep each deployable workload in its own chart (e.g. API, consumer/worker).
- Use an umbrella/stack chart to orchestrate multi-service deployment as one release.
- Keep chart concerns separated:
  - service-level resources in workload charts
  - environment composition in stack chart values

Repository pattern observed:
- `build.fd/charts/skeleton-api`
- `build.fd/charts/skeleton-consumer`
- `build.fd/charts/skeleton-stack`

### 2) Values layering per environment

- Maintain a base `values.yaml` and add env-specific overlays (`values-production.yaml`, etc.).
- Avoid duplicating templates per environment; use values overlays instead.
- Pass runtime deployment metadata from CI via `--set` (not hardcoded in templates).

Repository pattern observed:
- Base values in stack chart + production override file
- CI injects image tag, build number, and app environment at deploy time

### 3) CI-to-Helm input contract (must be explicit)

Standardize these deployment inputs:
- chart path
- namespace
- values files
- immutable image tag
- build identifier
- target environment

Repository pattern observed:
- `--set "global.image.tag=..."`
- `--set "global.appBuildNumber=..."`
- `--set "global.appEnv=..."`

Recommendation for reuse:
- Keep these keys stable across repos to simplify platform automation.

### 4) Safe deployment flags

Use Helm with rollback and readiness guarantees:

```bash
helm upgrade --install --atomic --wait ...
```

- `--atomic` rolls back failed releases automatically
- `--wait` blocks CI until resources become ready or fail

Repository pattern observed:
- deploy step uses `upgrade --install --atomic --wait`

### 5) Dependency hygiene before deploy

- Run chart dependency resolution in CI before deployment.
- Fail early if dependencies are broken or missing.

Repository pattern observed:
- `helm dependency update build.fd/charts/skeleton-stack`

### 6) Immutable image deployment

- Build/publish workload images first, then deploy charts referencing immutable tags.
- Do not bake mutable tags into chart defaults.
- Keep rollout trigger value (`appBuildNumber`) separate from image tag when needed (useful for forcing re-deploys in staging with the same image).

Repository pattern observed:
- images built/pushed before Helm runs
- deploy injects tag/build metadata via `--set`

### 7) Namespace/environment isolation

- Deploy each environment to a deterministic namespace.
- Keep namespace naming convention stable and derivable from environment name.

Repository pattern observed:
- distinct namespace per stage and production

### 8) Secrets/config externalization

- Keep sensitive values out of chart values/templates as raw literals.
- Reference external secret/config objects from templates.
- Provision those objects before Helm release (via platform tooling like Terraform/ESO).

Repository pattern observed:
- service chart templates include External Secrets Operator manifests (`*-eso.yaml`)
- Terraform is applied before Helm in the pipeline

### 9) Observability as charted resources

- Package observability resources (e.g. `ServiceMonitor`) with workloads so monitoring stays versioned with the deployable unit.
- Gate observability resources behind chart values to enable/disable per environment.

Repository pattern observed:
- `ServiceMonitor` templates present in each service chart

## Canonical Deployment Command Pattern

```bash
helm dependency update <stack-chart-path>

helm upgrade --install --atomic --wait -n <namespace> \
  -f <stack-chart>/values.yaml \
  -f <stack-chart>/values-<env>.yaml \
  --set "global.image.tag=<immutable-tag>" \
  --set "global.appBuildNumber=<build-id>" \
  --set "global.appEnv=<env>" \
  <release-name> <stack-chart-path>
```

## Minimal Contract to Reuse in Another Repo (e.g. Java)

Chart structure:
- `charts/<service-api>`
- `charts/<service-worker>` (if async workload exists)
- `charts/<service-stack>` umbrella

Stable global values:
- `global.image.tag`
- `global.appBuildNumber`
- `global.appEnv`

CI step order:
1. build and test
2. build and push images
3. provision infra/config dependencies (e.g. Terraform)
4. `helm dependency update`
5. `helm upgrade --install --atomic --wait`
