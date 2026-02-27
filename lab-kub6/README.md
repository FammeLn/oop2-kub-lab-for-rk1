# Лабораторная работа №6: Cluster Sharding на практике (Java + Kubernetes)

## Цель
Научиться:
- подключать Akka Cluster Sharding;
- создавать entity-актор;
- отправлять сообщения через sharding proxy (`EntityRef`);
- наблюдать распределение entity по кластеру.

## Что реализовано
- `AccountActor` как entity с состоянием баланса;
- команды `UpdateBalance` и `GetBalance`;
- инициализация Cluster Sharding через `ClusterSharding.init`;
- автоматический bootstrap кластера через Akka Management + Kubernetes API;
- Kubernetes манифесты для namespace, RBAC, service и deployment.

## Архитектура
Client → Sharding Proxy (`EntityRef`) → Shard → Entity (`AccountActor`)

## Структура проекта
- `src/main/java/com/akka/kublab6/AccountActor.java`
- `src/main/java/com/akka/kublab6/Main.java`
- `src/main/resources/application.conf`
- `k8s/*.yaml`
- `scripts/build.sh`, `scripts/deploy.sh`, `scripts/cleanup.sh`

## Запуск локальной сборки
```bash
cd lab-kub6
mvn clean compile
mvn package
```

## Docker + Kubernetes
```bash
cd lab-kub6
./scripts/build.sh
./scripts/deploy.sh
```

Проверка:
```bash
kubectl get pods -n akka-sharding
kubectl logs -f -l app=akka-sharding -n akka-sharding
```

## Эксперименты
1. Отправляйте сообщения разным `entityId` (`user-42`, `user-77`, ...).
2. Масштабируйте deployment:
   ```bash
   kubectl scale deployment/akka-sharding-deployment --replicas=5 -n akka-sharding
   ```
3. Удалите pod и наблюдайте восстановление:
   ```bash
   kubectl delete pod <pod-name> -n akka-sharding
   ```

## Контрольные вопросы
1. Чем entity отличается от сервиса?
2. Что такое shard?
3. Почему entity нельзя создавать вручную?
4. Что происходит при падении Pod’а?
5. Почему Cluster Sharding хорошо подходит Kubernetes?

## Краткий вывод
В лабораторной показано, как хранить распределённое состояние в entity-акторах и доверить размещение/восстановление Cluster Sharding в динамической Kubernetes-среде.
