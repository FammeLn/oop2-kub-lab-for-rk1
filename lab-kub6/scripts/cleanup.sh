#!/usr/bin/env bash
set -e

kubectl delete -f k8s/deployment.yaml --ignore-not-found=true
kubectl delete -f k8s/service.yaml --ignore-not-found=true
kubectl delete -f k8s/rbac.yaml --ignore-not-found=true
kubectl delete -f k8s/namespace.yaml --ignore-not-found=true

echo "[ok] resources removed"
