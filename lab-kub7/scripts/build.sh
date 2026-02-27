#!/usr/bin/env bash
set -e

IMAGE_NAME="akka-persistence-sharding"
IMAGE_TAG="${1:-latest}"

echo "[build] mvn clean package"
mvn clean package -DskipTests

echo "[build] docker build ${IMAGE_NAME}:${IMAGE_TAG}"
docker build -t "${IMAGE_NAME}:${IMAGE_TAG}" .

echo "[ok] image built: ${IMAGE_NAME}:${IMAGE_TAG}"
