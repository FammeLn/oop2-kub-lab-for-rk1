# QUICKSTART: lab-kub7

## 1) Поднять Kubernetes и проверить доступ
```bash
# Вариант A: Minikube
minikube start
kubectl config use-context minikube

# Вариант B: kind
# kind create cluster

kubectl cluster-info
```

## 2) Перейти в папку проекта
```bash
cd lab-kub7
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
kubectl get pods -n akka-persistence
kubectl logs -n akka-persistence -l app=akka-persistence --tail=100
```

Если после изменений в `pom.xml`/коде нужно гарантированно запустить новый образ:
```bash
# 1) Собрать образ с уникальным тегом в docker daemon Minikube
eval $(minikube docker-env)
./scripts/build.sh fix-leveldb

# 2) Обновить image у контейнера akka-persistence
kubectl -n akka-persistence set image deployment/akka-persistence-deployment \
    akka-persistence=akka-persistence-sharding:fix-leveldb

# 3) Дождаться rollout и проверить свежие логи
kubectl rollout status deployment/akka-persistence-deployment -n akka-persistence
kubectl get pods -n akka-persistence -o wide
kubectl logs -n akka-persistence -l app=akka-persistence --since=2m
```

## 7) Эксперимент: восстановление состояния после рестарта Pod
```bash
# 1) Убедитесь, что в логах были события и recovery
kubectl logs -n akka-persistence -l app=akka-persistence --tail=200 | grep -Ei "Recovery completed|Deposit applied|Withdraw applied|Duplicate"

# 2) Перезапустите Pod
kubectl rollout restart deployment/akka-persistence-deployment -n akka-persistence
kubectl rollout status deployment/akka-persistence-deployment -n akka-persistence

# 3) Проверьте, что после старта есть recovery с восстановленным балансом
kubectl logs -n akka-persistence -l app=akka-persistence --since=2m | grep -Ei "Recovery completed|Duplicate"
```

Ожидаемое поведение:
- после первого запуска применяются `tx-1`, `tx-2`, `tx-3`;
- после рестарта состояние восстанавливается из журнала;
- те же команды с теми же `txId` не применяются повторно (идемпотентность).

## 8) Масштабирование
```bash
# В этой версии используется локальный LevelDB + PVC, поэтому оставляйте 1 реплику
kubectl scale deployment/akka-persistence-deployment --replicas=1 -n akka-persistence
```

## 9) Очистка
```bash
./scripts/cleanup.sh
```

## Примечания
- Для полноценных multi-node migration сценариев нужен общий journal (например JDBC/Cassandra),
  а не локальный LevelDB.
- Если используете внешний registry, замените `image` в `k8s/deployment.yaml`.
