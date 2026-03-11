# Minikube quick start

Manifestele din `k8s/02-eureka-gateway.yaml` și `k8s/03-microservices.yaml` folosesc acum imaginile publice din Docker Hub:

- `victorrentea/victor:eureka-1.0`
- `victorrentea/victor:api-gateway-1.0`
- `victorrentea/victor:customer-1.0`
- `victorrentea/victor:catalog-1.0`
- `victorrentea/victor:order-1.0`
- `victorrentea/victor:payment-1.0`
- `victorrentea/victor:shipping-1.0`
- `victorrentea/victor:inventory-1.0`
- `victorrentea/victor:notification-1.0`

## Pornire

```zsh
cd /Users/victorrentea/workspace/microservices/k8s
chmod +x minikube-up.sh minikube-down.sh
./minikube-up.sh
```

## Verificare

```zsh
kubectl get pods -n microservices
kubectl get svc -n microservices
```

## Acces servicii

### Varianta stabilă: port-forward

```zsh
kubectl port-forward -n microservices svc/api-gateway 8080:80
kubectl port-forward -n microservices svc/eureka 8761:8761
kubectl port-forward -n microservices svc/rabbitmq 15672:15672
kubectl port-forward -n microservices svc/zipkin 9411:9411
```

### Varianta Minikube service

```zsh
minikube service api-gateway -n microservices --url --profile=microservices
```

## Oprire

```zsh
./minikube-down.sh
```

## Rebuild + redeploy (single command)

```zsh
cd /Users/victorrentea/workspace/microservices/k8s
./rebuild-redeploy.sh
```

### Useful variants

```zsh
# doar un serviciu
./rebuild-redeploy.sh --services customer

# fără push (imagine locală), doar restart
./rebuild-redeploy.sh --services customer --skip-push --skip-apply

# vezi ce ar rula, fără execuție
./rebuild-redeploy.sh --services customer --dry-run
```
