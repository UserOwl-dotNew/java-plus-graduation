FROM eclipse-temurin:21-jdk-alpine
WORKDIR /app
COPY config-server/target/*.jar app.jar
COPY config-repo ./config-repo
ENTRYPOINT ["java", "-jar", "app.jar"]