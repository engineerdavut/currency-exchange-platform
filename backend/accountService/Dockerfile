# Stage 1: Build the entire backend project to make parent POM and common-utils available
FROM maven:3.8.7-eclipse-temurin-17 AS project-builder
WORKDIR /usr/src/app

# Copy the entire backend project content (parent POM and all modules)
COPY . .  

# Build the entire multi-module project.
# Bu, common-utils'ı install edecek ve accountService'i package yapacaktır.
# -pl (projects) ve -am (also-make-dependents) ile sadece ilgili modülü ve bağımlılıklarını build etmeyi deneyebilirsiniz,
# ama tüm projeyi build etmek genellikle daha güvenilirdir.
RUN mvn clean install -DskipTests

# Stage 2: Create the final application image for account-service
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Copy only the built JAR for account-service from the project-builder stage
COPY --from=project-builder /usr/src/app/accountService/target/accountService-*.jar app.jar

EXPOSE 8081
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
