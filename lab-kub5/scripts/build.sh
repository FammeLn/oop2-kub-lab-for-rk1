#!/bin/bash

################################################################################
# СКРИПТ СБОРКИ DOCKER ОБРАЗА
################################################################################
#
# Этот скрипт:
# 1. Компилирует Java приложение
# 2. Создаёт Docker образ
# 3. (Опционально) Пушит образ в registry
#
################################################################################

set -e  # Остановка при ошибке

echo "════════════════════════════════════════════════════════════════"
echo "🔨 СБОРКА AKKA CLUSTER ОБРАЗА"
echo "════════════════════════════════════════════════════════════════"

# Переменные
IMAGE_NAME="akka-cluster-bootstrap"
IMAGE_TAG=${1:-"latest"}
FULL_IMAGE_NAME="${IMAGE_NAME}:${IMAGE_TAG}"

# Шаг 1: Проверка наличия Maven
echo ""
echo "📋 Проверка окружения..."
if ! command -v mvn &> /dev/null; then
    echo "❌ Maven не найден! Установите Maven."
    exit 1
fi

if ! command -v docker &> /dev/null; then
    echo "❌ Docker не найден! Установите Docker."
    exit 1
fi

echo "✅ Maven: $(mvn -version | head -n 1)"
echo "✅ Docker: $(docker --version)"

# Шаг 2: Сборка приложения (опционально, Dockerfile сам собирает)
echo ""
echo "📦 Опциональная проверка компиляции..."
mvn clean compile

# Шаг 3: Сборка Docker образа
echo ""
echo "🐳 Сборка Docker образа: ${FULL_IMAGE_NAME}"
docker build -t "${FULL_IMAGE_NAME}" .

# Проверка размера образа
IMAGE_SIZE=$(docker images "${FULL_IMAGE_NAME}" --format "{{.Size}}")
echo ""
echo "✅ Образ успешно собран!"
echo "📊 Размер образа: ${IMAGE_SIZE}"

# Шаг 4: (Опционально) Push в registry
read -p "🚀 Запушить образ в registry? (y/n): " -n 1 -r
echo
if [[ $REPLY =~ ^[Yy]$ ]]; then
    read -p "📝 Введите адрес registry (например, dockerhub_username): " REGISTRY
    
    REGISTRY_IMAGE="${REGISTRY}/${FULL_IMAGE_NAME}"
    
    echo "🔖 Тегирование: ${REGISTRY_IMAGE}"
    docker tag "${FULL_IMAGE_NAME}" "${REGISTRY_IMAGE}"
    
    echo "📤 Push в registry..."
    docker push "${REGISTRY_IMAGE}"
    
    echo "✅ Образ загружен: ${REGISTRY_IMAGE}"
    echo ""
    echo "⚠️  Не забудьте обновить image в k8s/deployment.yaml:"
    echo "    image: ${REGISTRY_IMAGE}"
else
    echo ""
    echo "ℹ️  Образ доступен локально как: ${FULL_IMAGE_NAME}"
    echo ""
    echo "⚠️  Для использования в Kubernetes:"
    echo "   1. Если используете Minikube:"
    echo "      eval \$(minikube docker-env)"
    echo "      ./scripts/build.sh"
    echo ""
    echo "   2. Если используете удалённый кластер:"
    echo "      Запушите образ в registry и обновите deployment.yaml"
fi

echo ""
echo "════════════════════════════════════════════════════════════════"
echo "✅ СБОРКА ЗАВЕРШЕНА"
echo "════════════════════════════════════════════════════════════════"
