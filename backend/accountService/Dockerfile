
FROM maven:3.8.7-eclipse-temurin-17 AS project-builder
WORKDIR /usr/src/app

COPY . .  

RUN mvn clean install -DskipTests


FROM eclipse-temurin:17-jre-alpine
WORKDIR /app


COPY --from=project-builder /usr/src/app/accountService/target/accountService-*.jar app.jar

EXPOSE 8081
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
