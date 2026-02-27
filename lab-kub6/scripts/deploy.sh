#!/usr/bin/env bash
set -e

echo "[check] kubectl connectivity"
if ! kubectl cluster-info >/dev/null 2>&1; then
	echo "[error] Kubernetes cluster недоступен из текущего kubectl context"
	echo ""
	echo "[hint] Проверьте контекст:"
	kubectl config current-context 2>/dev/null || true
	echo ""
	echo "[hint] Варианты исправления:"
	echo "  - Minikube: minikube start && eval \$(minikube docker-env)"
	echo "  - kind: kind create cluster"
	echo "  - Удалённый кластер: kubectl config use-context <your-context>"
	echo ""
	echo "[hint] Проверка после исправления: kubectl cluster-info"
	exit 1
fi

echo "[ok] kubectl context: $(kubectl config current-context 2>/dev/null || echo unknown)"

echo "[apply] namespace"
kubectl apply -f k8s/namespace.yaml

echo "[apply] rbac"
kubectl apply -f k8s/rbac.yaml

echo "[apply] service"
kubectl apply -f k8s/service.yaml

echo "[apply] deployment"
kubectl apply -f k8s/deployment.yaml

echo "[wait] pods ready"
kubectl wait --for=condition=ready pod -l app=akka-sharding -n akka-sharding --timeout=180s

echo "[status]"
kubectl get pods -n akka-sharding -o wide
