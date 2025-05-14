
FROM maven:3.8.7-eclipse-temurin-17 AS project-builder
WORKDIR /usr/src/app
COPY . . 
RUN mvn clean install -DskipTests

FROM openjdk:17-jdk-slim
WORKDIR /app
COPY --from=project-builder /usr/src/app/apiGateway/target/apiGateway-*.jar app.jar
EXPOSE 8090
ENTRYPOINT ["java", "-jar", "/app/app.jar"]

