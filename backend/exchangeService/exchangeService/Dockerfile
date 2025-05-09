FROM maven:3.8.7-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline
COPY src ./src
RUN mvn package -DskipTests

RUN echo | openssl s_client -connect api.collectapi.com:443 2>/dev/null | sed -ne '/-BEGIN CERTIFICATE-/,/-END CERTIFICATE-/p' > /tmp/collectapi.crt
RUN keytool -import -noprompt -trustcacerts -alias collectapi -file /tmp/collectapi.crt -keystore $JAVA_HOME/lib/security/cacerts -storepass changeit

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8082
ENTRYPOINT ["java", "-jar", "app.jar", "--server.port=8082"]
