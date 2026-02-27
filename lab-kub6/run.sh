#!/usr/bin/env bash
set -e
cd "$(dirname "$0")"
mvn clean compile
mvn exec:java
