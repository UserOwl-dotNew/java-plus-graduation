FROM eclipse-temurin:21-jdk-alpine
WORKDIR /app
COPY target/gateway-server-*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]