#!/usr/bin/env bash
set -e
mvn clean package -DskipTests
java -jar target/lab-kub7-1.0-SNAPSHOT.jar
