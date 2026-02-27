# 🚀 Быстрый старт - Лабораторная работа №5

## Minimal Viable Setup

Самый быстрый способ запустить Akka Cluster в Kubernetes.

---

## ⚡ 3 шага до работающего кластера

### 1️⃣ Подготовка окружения

```bash
# Проверка инструментов
java -version        # Должна быть 17+
mvn -version         # Должна быть 3.6+
docker --version     # Любая версия
kubectl version      # Любая версия

# Если используете Minikube
minikube start
eval $(minikube docker-env)

# Если используете kind
kind create cluster
```

### 2️⃣ Сборка образа

```bash
cd lab-kub5
./scripts/build.sh
```

**Что делает скрипт:**
- Компилирует Java приложение
- Создаёт Docker образ `akka-cluster-bootstrap:latest`
- (Опционально) Пушит в registry

### 3️⃣ Деплой в Kubernetes

```bash
./scripts/deploy.sh
```

**Что делает скрипт:**
- Создаёт namespace `akka-cluster`
- Применяет RBAC (ServiceAccount, Role, RoleBinding)
- Создаёт Kubernetes Service
- Разворачивает Deployment с 3 репликами

---

## 📊 Проверка работы

### Смотрим Pod'ы

```bash
kubectl get pods -n akka-cluster
```

Ожидаемый результат:
```
NAME                                      READY   STATUS    RESTARTS   AGE
akka-cluster-deployment-7d4f8c9b-abc12   1/1     Running   0          30s
akka-cluster-deployment-7d4f8c9b-def34   1/1     Running   0          30s
akka-cluster-deployment-7d4f8c9b-ghi56   1/1     Running   0          30s
```

### Смотрим логи

```bash
# Логи одного Pod'а
kubectl logs -f akka-cluster-deployment-7d4f8c9b-abc12 -n akka-cluster

# Логи всех Pod'ов
kubectl logs -f -l app=akka-cluster -n akka-cluster --max-log-requests=10
```

### Что вы увидите в логах

```
🚀 ЗАПУСК AKKA CLUSTER NODE
✅ ActorSystem создана: ClusterSystem
✅ Akka Management запущен
✅ Cluster Bootstrap запущен

✅ Узел присоединился к кластеру: akka://ClusterSystem@10.1.0.10:25520
✅ Узел присоединился к кластеру: akka://ClusterSystem@10.1.0.11:25520
✅ Узел присоединился к кластеру: akka://ClusterSystem@10.1.0.12:25520

👑 Новый лидер кластера: akka://ClusterSystem@10.1.0.10:25520
📊 Членов в кластере: 3
```

---

## 🧪 Простые эксперименты

### Масштабирование

```bash
# Увеличить до 5 узлов
kubectl scale deployment/akka-cluster-deployment --replicas=5 -n akka-cluster

# Уменьшить до 2 узлов
kubectl scale deployment/akka-cluster-deployment --replicas=2 -n akka-cluster

# Смотрим изменения
kubectl get pods -n akka-cluster -w
```

### Убийство Pod'а (тест resilience)

```bash
# Удаляем любой Pod
kubectl delete pod akka-cluster-deployment-7d4f8c9b-abc12 -n akka-cluster

# Наблюдаем восстановление
kubectl get pods -n akka-cluster -w
```

**Что произойдёт:**
- Kubernetes автоматически создаст новый Pod
- Новый Pod присоединится к кластеру
- Кластер продолжит работать

### Проверка Management endpoint

```bash
# Пробрасываем порт
kubectl port-forward akka-cluster-deployment-7d4f8c9b-abc12 8558:8558 -n akka-cluster

# В другом терминале проверяем
curl http://localhost:8558/alive    # Должен вернуть 200 OK
curl http://localhost:8558/ready    # Должен вернуть 200 OK
```

---

## 🧹 Очистка

Удалить все ресурсы:

```bash
./scripts/cleanup.sh
```

Или вручную:

```bash
kubectl delete namespace akka-cluster
```

---

## 🔧 Структура проекта

```
lab-kub5/
├── src/main/java/com/akka/kublab5/
│   ├── ClusterNode.java      # Узел кластера с мониторингом событий
│   └── Main.java              # Запуск Management и Bootstrap
│
├── src/main/resources/
│   ├── application.conf       # Конфигурация Akka (Management, Bootstrap)
│   └── logback.xml            # Логирование
│
├── k8s/
│   ├── namespace.yaml         # Namespace akka-cluster
│   ├── rbac.yaml              # ServiceAccount + Role для K8s API
│   ├── service.yaml           # Headless Service для discovery
│   └── deployment.yaml        # Deployment с 3 репликами
│
├── scripts/
│   ├── build.sh               # Сборка Docker образа
│   ├── deploy.sh              # Деплой в K8s
│   └── cleanup.sh             # Удаление ресурсов
│
├── Dockerfile                 # Multi-stage build образа
└── pom.xml                    # Maven: Akka + Management + Bootstrap
```

---

## 🎯 Ключевые файлы

### Main.java — Запуск приложения

```java
// 1. Создание ActorSystem
ActorSystem<ClusterNode.Command> system = 
    ActorSystem.create(ClusterNode.create(), "ClusterSystem");

// 2. Запуск Akka Management (HTTP :8558)
AkkaManagement.get(system).start();

// 3. Запуск Cluster Bootstrap
ClusterBootstrap.get(system).start();
```

### application.conf — Конфигурация

```hocon
akka {
  # Remoting для связи между узлами
  remote.artery.canonical {
    hostname = ${HOSTNAME}    # IP Pod'а из переменной окружения
    port = 25520
  }
  
  # Management endpoint
  management.http {
    hostname = "0.0.0.0"
    port = 8558
  }
  
  # Bootstrap конфигурация
  management.cluster.bootstrap {
    contact-point-discovery {
      discovery-method = kubernetes-api    # Используем K8s API
    }
  }
  
  # Discovery через Kubernetes API
  discovery.kubernetes-api {
    pod-label-selector = "app=akka-cluster"    # Фильтр Pod'ов
  }
}
```

### deployment.yaml — Kubernetes конфигурация

```yaml
spec:
  replicas: 3                                # Количество узлов
  serviceAccountName: akka-cluster-sa        # Для доступа к K8s API
  
  containers:
    - name: akka-cluster
      image: akka-cluster-bootstrap:latest
      ports:
        - name: remoting
          containerPort: 25520               # Akka Remoting
        - name: management
          containerPort: 8558                # Akka Management
      
      # Health checks
      livenessProbe:
        httpGet:
          path: /alive
          port: management
      
      readinessProbe:
        httpGet:
          path: /ready
          port: management
```

---

## ❓ Часто задаваемые вопросы

### Q: Нужен ли мне настоящий Kubernetes кластер?

**A:** Нет! Можно использовать:
- **Minikube** — локальный K8s (рекомендуется для начала)
- **kind** — K8s в Docker контейнерах
- **Docker Desktop** — встроенный K8s
- **k3s** — лёгкий K8s

### Q: Как это работает без seed-nodes?

**A:** Akka Management:
1. Обращается к Kubernetes API
2. Получает список Pod'ов (по label `app=akka-cluster`)
3. Связывается с ними через HTTP (:8558)
4. Согласовывает формирование кластера
5. Автоматически!

### Q: Что такое RBAC и зачем он нужен?

**A:** RBAC (Role-Based Access Control) — это права доступа.

Pod'ам нужны права на чтение информации о других Pod'ах:
```yaml
rules:
  - apiGroups: [""]
    resources: ["pods"]
    verbs: ["get", "list", "watch"]
```

Без этих прав Bootstrap не сможет найти другие узлы!

### Q: Что если Pod перезапустится?

**A:** Kubernetes автоматически:
1. Создаст новый Pod
2. Присвоит новый IP
3. Pod запустится и через Bootstrap присоединится к кластеру
4. Кластер обновит membership

Это демонстрирует resilience!

### Q: Можно ли запустить локально без K8s?

**A:** Да, но нужно изменить конфигурацию discovery:
```hocon
akka.discovery.method = config  # Вместо kubernetes-api
```

И указать статический список узлов.

---

## 🎓 Что дальше?

После базового запуска:

1. **Изучите логи** — поймите процесс bootstrap
2. **Эксперименты** — масштабирование, убийство Pod'ов
3. **README.md** — полное описание архитектуры
4. **LECTURE_NOTES.md** — теоретическая часть
5. **Контрольные вопросы** — проверьте понимание

---

## 📋 Чеклист успешного запуска

- [ ] Minikube/kind запущен и работает
- [ ] `kubectl cluster-info` показывает подключение
- [ ] Docker образ собран (`docker images | grep akka-cluster`)
- [ ] Namespace создан (`kubectl get ns akka-cluster`)
- [ ] RBAC применён (`kubectl get sa -n akka-cluster`)
- [ ] Pod'ы запущены (`kubectl get pods -n akka-cluster`)
- [ ] Pod'ы в статусе `Running` и `READY 1/1`
- [ ] В логах видны сообщения о присоединении к кластеру
- [ ] Management endpoint отвечает (`curl localhost:8558/alive`)

---

## 🆘 Проблемы?

### ImagePullBackOff

```bash
# Для Minikube
eval $(minikube docker-env)
./scripts/build.sh
```

### CrashLoopBackOff

```bash
# Смотрим логи
kubectl logs <pod-name> -n akka-cluster
kubectl describe pod <pod-name> -n akka-cluster
```

### Pod'ы не формируют кластер

```bash
# Проверяем RBAC
kubectl get serviceaccount akka-cluster-sa -n akka-cluster
kubectl get role akka-cluster-role -n akka-cluster

# Проверяем labels
kubectl get pods -n akka-cluster --show-labels
```

---

## 🎉 Успешный результат

Если всё работает, вы увидите:

```
📊 ПЕРИОДИЧЕСКИЙ ОТЧЁТ О КЛАСТЕРЕ
═══════════════════════════════════════════════════════════════
⏰ Время: 14:30:45
👥 Членов в кластере: 3
👑 Лидер: akka://ClusterSystem@10.1.0.10:25520
🏠 Мой адрес: akka://ClusterSystem@10.1.0.11:25520
📡 Статус: Up
═══════════════════════════════════════════════════════════════
```

**Поздравляем! Кластер работает! 🎊**

---

**Создано:** 27 февраля 2026  
**Курс:** OOP2  
**Тема:** Akka Cluster Bootstrap в Kubernetes
