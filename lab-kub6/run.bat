@echo off
cd /d %~dp0
call mvn clean compile
call mvn exec:java
