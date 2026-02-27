@echo off
REM Скрипт для запуска лабораторной работы №5 в Windows

echo ===================================
echo Лабораторная работа №5: Actor Model
echo ===================================
echo.

cd %~dp0

REM Проверка наличия Maven
where mvn >nul 2>nul
if %ERRORLEVEL% NEQ 0 (
    echo ❌ Maven не установлен!
    echo Установите Maven: https://maven.apache.org/install.html
    exit /b 1
)

echo ✓ Maven найден
echo.

REM Сборка проекта
echo 📦 Сборка проекта...
call mvn clean compile

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo ❌ Ошибка сборки!
    exit /b 1
)

echo.
echo ✓ Проект успешно скомпилирован
echo.

REM Запуск основного приложения
echo 🚀 Запуск основного приложения...
echo ===================================
echo.
call mvn exec:java -Dexec.mainClass="com.akka.lab5.Main" -q

echo.
echo.
echo 📝 Хотите запустить дополнительные эксперименты?
echo Используйте команду:
echo   mvn exec:java -Dexec.mainClass="com.akka.lab5.ExperimentsMain"
echo.

pause
