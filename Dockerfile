FROM openjdk:17-jdk-slim

WORKDIR /app

COPY build/libs/backend-0.0.1-SNAPSHOT.jar /app/

ENTRYPOINT ["java", "-jar", "backend-0.0.1-SNAPSHOT.jar"]