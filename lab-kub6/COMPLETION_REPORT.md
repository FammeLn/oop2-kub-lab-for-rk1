# COMPLETION REPORT — lab-kub6

## Выполнено
- Создан проект `lab-kub6` на Java 17 + Akka 2.8.5.
- Подключен `akka-cluster-sharding-typed_2.13`.
- Реализован entity-актор `AccountActor` с распределённым состоянием баланса.
- Инициализирован Cluster Sharding и отправка сообщений через `EntityRef`.
- Настроены Akka Management + Cluster Bootstrap + Kubernetes API discovery.
- Добавлены Kubernetes manifests: namespace, rbac, service, deployment.
- Добавлены скрипты: build/deploy/cleanup.
- Добавлена документация: README, QUICKSTART, LECTURE_NOTES.

## Проверка
- Структура проекта сформирована.
- Конфигурация и код готовы к `mvn clean compile` и контейнерному запуску.
