# QUICKSTART: lab-kub6

## 1) Поднять Kubernetes и проверить доступ
```bash
# Вариант A: Minikube
minikube start
kubectl config use-context minikube

# Вариант B: kind
# kind create cluster

kubectl cluster-info
```

## 2) Сборка
```bash
cd lab-kub6
# Опционально: локально проверить, что проект компилируется
mvn clean package -DskipTests
```

## 3) Права на скрипты (один раз)
```bash
chmod +x scripts/*.sh run.sh
```

## 4) Сборка Docker образа
```bash
# Для Minikube обязательно перед сборкой образа
eval $(minikube docker-env)

./scripts/build.sh
```

## 5) Деплой в Kubernetes
```bash
./scripts/deploy.sh
```

## 6) Проверка
```bash
kubectl get pods -n akka-sharding
kubectl logs -n akka-sharding -l app=akka-sharding --tail=100
kubectl logs -f -n akka-sharding -l app=akka-sharding --since=1m --all-containers=true | grep -Ei "NotSerializable|serialization|ERROR|WARN"
```

## 7) Масштабирование
```bash
kubectl scale deployment/akka-sharding-deployment --replicas=5 -n akka-sharding
```

## 8) Очистка
```bash
./scripts/cleanup.sh
```

## Примечания
- Если используете внешний registry, замените `image` в `k8s/deployment.yaml`.
