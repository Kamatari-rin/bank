FROM openjdk:21-jdk-slim
WORKDIR /app
COPY build/libs/transfer-service-1.0-SNAPSHOT.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]