# lab-kub7 — Akka Persistence + Cluster Sharding

Лабораторная работа №7: persistent entity в Akka Typed с Event Sourcing и запуском в Kubernetes.

## Что реализовано
- `Command / Event / State` модель для `AccountEntity`
- `EventSourcedBehavior` с журналом событий
- Автоматический recovery после перезапуска Pod
- Cluster Sharding для entity
- Kubernetes deployment с `PersistentVolumeClaim`

## Ключевые файлы
- `src/main/java/com/akka/kublab7/AccountEntity.java`
- `src/main/java/com/akka/kublab7/Main.java`
- `src/main/resources/application.conf`
- `k8s/deployment.yaml`

## Быстрый старт
См. `QUICKSTART.md`.
