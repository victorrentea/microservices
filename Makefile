.PHONY: help k8s-setup k8s-teardown k8s-status k8s-logs k8s-port-forward build clean

help:
	@echo "Microservices K8s Management"
	@echo ""
	@echo "Usage: make [command]"
	@echo ""
	@echo "Commands:"
	@echo "  k8s-setup        - Setup entire K8s cluster with all services"
	@echo "  k8s-teardown     - Teardown K8s cluster"
	@echo "  k8s-status       - Check status of services"
	@echo "  k8s-logs-<svc>   - View logs for a service (e.g., k8s-logs-api-gateway)"
	@echo "  k8s-port-forward - Display port-forward commands"
	@echo "  build            - Build all microservices locally"
	@echo "  build-<svc>      - Build specific service (e.g., build-customer)"
	@echo "  clean            - Clean all build artifacts"
	@echo ""

k8s-setup:
	@./k8s/setup.sh

k8s-teardown:
	@./k8s/teardown.sh

k8s-status:
	@./k8s/status.sh

k8s-port-forward:
	@./k8s/port-forward.sh

k8s-logs-%:
	@kubectl logs -n microservices -l app=$* -f --all-containers=true

build:
	mvn clean install -DskipTests=true

build-%:
	mvn -pl $* clean install -DskipTests=true

clean:
	mvn clean

.PHONY: docker-login docker-ps docker-ps-registry

docker-login:
	@docker login localhost:5000

docker-ps:
	@docker ps | grep -E "microservices|registry|postgres|rabbit"

docker-ps-registry:
	@docker ps | grep local-registry

.PHONY: k8s-get-pods k8s-get-svc k8s-get-events k8s-describe-%

k8s-get-pods:
	@kubectl get pods -n microservices -o wide

k8s-get-svc:
	@kubectl get svc -n microservices

k8s-get-events:
	@kubectl get events -n microservices --sort-by='.lastTimestamp'

k8s-describe-%:
	@kubectl describe pod $* -n microservices

.PHONY: k8s-restart-%

k8s-restart-%:
	@kubectl rollout restart deployment/$* -n microservices

.PHONY: mvn-compile mvn-test mvn-package

mvn-compile:
	mvn clean compile -DskipTests=true

mvn-test:
	mvn test

mvn-package:
	mvn clean package -DskipTests=true -DskipDockerBuild=true

