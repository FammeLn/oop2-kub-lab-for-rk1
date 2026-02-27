@echo off
mvn clean package -DskipTests
java -jar target/lab-kub7-1.0-SNAPSHOT.jar
