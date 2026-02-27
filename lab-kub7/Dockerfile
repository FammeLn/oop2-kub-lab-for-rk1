FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml ./
RUN mvn dependency:go-offline -B
COPY src ./src
RUN mvn clean package -DskipTests

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
RUN addgroup -S akka && adduser -S akka -G akka
COPY --from=build /app/target/lab-kub7-1.0-SNAPSHOT.jar /app/application.jar
RUN chown -R akka:akka /app
USER akka
EXPOSE 25520 8558
ENV JAVA_OPTS="-Xms384m -Xmx768m -XX:+UseG1GC"
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/application.jar"]
