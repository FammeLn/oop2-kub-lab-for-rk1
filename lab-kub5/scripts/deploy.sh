#!/bin/bash

################################################################################
# СКРИПТ ДЕПЛОЯ В KUBERNETES
################################################################################
#
# Этот скрипт:
# 1. Создаёт namespace
# 2. Применяет RBAC конфигурацию
# 3. Создаёт Service
# 4. Разворачивает Deployment
#
################################################################################

set -e  # Остановка при ошибке

echo "════════════════════════════════════════════════════════════════"
echo "🚀 ДЕПЛОЙ AKKA CLUSTER В KUBERNETES"
echo "════════════════════════════════════════════════════════════════"

# Проверка kubectl
if ! command -v kubectl &> /dev/null; then
    echo "❌ kubectl не найден! Установите kubectl."
    exit 1
fi

echo "✅ kubectl: $(kubectl version --client --short 2>/dev/null || kubectl version --client)"

# Проверка подключения к кластеру
echo ""
echo "🔍 Проверка подключения к Kubernetes кластеру..."
if ! kubectl cluster-info &> /dev/null; then
    echo "❌ Не удалось подключиться к кластеру!"
    echo "   Проверьте ~/.kube/config или запустите minikube/kind"
    exit 1
fi

echo "✅ Подключение к кластеру установлено"
kubectl cluster-info | head -n 1

# Шаг 1: Создание namespace
echo ""
echo "📦 Шаг 1: Создание namespace akka-cluster..."
kubectl apply -f k8s/namespace.yaml

# Шаг 2: Применение RBAC
echo ""
echo "🔐 Шаг 2: Применение RBAC конфигурации..."
kubectl apply -f k8s/rbac.yaml

# Проверка ServiceAccount
echo "   ✅ ServiceAccount создан:"
kubectl get serviceaccount akka-cluster-sa -n akka-cluster

# Шаг 3: Создание Service
echo ""
echo "🌐 Шаг 3: Создание Kubernetes Service..."
kubectl apply -f k8s/service.yaml

# Проверка Service
echo "   ✅ Service создан:"
kubectl get service akka-cluster-service -n akka-cluster

# Шаг 4: Разворачивание Deployment
echo ""
echo "🚢 Шаг 4: Разворачивание Deployment..."
kubectl apply -f k8s/deployment.yaml

# Проверка Deployment
echo "   ✅ Deployment создан:"
kubectl get deployment akka-cluster-deployment -n akka-cluster

# Ожидание готовности Pod'ов
echo ""
echo "⏳ Ожидание запуска Pod'ов..."
kubectl wait --for=condition=ready pod \
    -l app=akka-cluster \
    -n akka-cluster \
    --timeout=120s || echo "⚠️  Тайм-аут ожидания. Проверьте статус вручную."

# Статус
echo ""
echo "════════════════════════════════════════════════════════════════"
echo "📊 СТАТУС КЛАСТЕРА"
echo "════════════════════════════════════════════════════════════════"

echo ""
echo "Pod'ы:"
kubectl get pods -n akka-cluster -o wide

echo ""
echo "Service:"
kubectl get service -n akka-cluster

echo ""
echo "Deployment:"
kubectl get deployment -n akka-cluster

echo ""
echo "════════════════════════════════════════════════════════════════"
echo "✅ ДЕПЛОЙ ЗАВЕРШЁН"
echo "════════════════════════════════════════════════════════════════"

echo ""
echo "📋 ПОЛЕЗНЫЕ КОМАНДЫ:"
echo ""
echo "  Логи Pod'а:"
echo "    kubectl logs -f <pod-name> -n akka-cluster"
echo ""
echo "  Логи всех Pod'ов:"
echo "    kubectl logs -f -l app=akka-cluster -n akka-cluster"
echo ""
echo "  Масштабирование:"
echo "    kubectl scale deployment/akka-cluster-deployment --replicas=5 -n akka-cluster"
echo ""
echo "  Проверка событий:"
echo "    kubectl get events -n akka-cluster --sort-by='.lastTimestamp'"
echo ""
echo "  Исполнить команду в Pod'е:"
echo "    kubectl exec -it <pod-name> -n akka-cluster -- sh"
echo ""
echo "  Удаление:"
echo "    ./scripts/cleanup.sh"
echo ""
