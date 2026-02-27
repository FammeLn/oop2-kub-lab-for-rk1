# =====================================================================
# DOCKERFILE ДЛЯ AKKA CLUSTER ПРИЛОЖЕНИЯ
# =====================================================================
#
# Этот Dockerfile создаёт образ с:
# - Java 17
# - Akka Cluster приложением
# - Всеми необходимыми зависимостями
#
# Используем multi-stage build для уменьшения размера образа
#
# =====================================================================

# =====================================================================
# STAGE 1: BUILD
# =====================================================================
#
# Стадия сборки - компиляция и создание JAR
#
FROM maven:3.9-eclipse-temurin-17 AS build

WORKDIR /app

# Копируем pom.xml и загружаем зависимости (для кэширования слоёв)
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Копируем исходный код
COPY src ./src

# Сборка приложения (создание fat JAR с зависимостями)
RUN mvn clean package -DskipTests

# =====================================================================
# STAGE 2: RUNTIME
# =====================================================================
#
# Стадия выполнения - только JRE и приложение
#
FROM eclipse-temurin:17-jre-alpine

# Метаданные образа
LABEL maintainer="oop2-course"
LABEL description="Akka Cluster Bootstrap in Kubernetes - Lab 5"
LABEL version="1.0"

# Создаём пользователя для запуска приложения (безопасность)
RUN addgroup -S akka && adduser -S akka -G akka

# Рабочая директория
WORKDIR /app

# Копируем JAR из стадии сборки
COPY --from=build /app/target/lab-kub5-1.0-SNAPSHOT.jar /app/application.jar

# Меняем владельца файлов
RUN chown -R akka:akka /app

# Переключаемся на непривилегированного пользователя
USER akka

# Порты:
# - 25520: Akka Remoting (связь между узлами)
# - 8558: Akka Management HTTP endpoint
EXPOSE 25520 8558

# Переменные окружения по умолчанию
ENV JAVA_OPTS="-Xms512m -Xmx512m -XX:+UseG1GC -XX:MaxGCPauseMillis=200"

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8558/alive || exit 1

# Точка входа
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/application.jar"]
