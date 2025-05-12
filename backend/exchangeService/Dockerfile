
# Stage 1: Build the entire backend project
FROM maven:3.8.7-eclipse-temurin-17 AS project-builder
WORKDIR /usr/src/app
COPY . . 
RUN mvn clean install -DskipTests

# RUN echo | openssl s_client -connect api.collectapi.com:443 2>/dev/null | sed -ne '/-BEGIN CERTIFICATE-/,/-END CERTIFICATE-/p' > /tmp/collectapi.crt
# RUN keytool -import -noprompt -trustcacerts -alias collectapi -file /tmp/collectapi.crt -keystore $JAVA_HOME/lib/security/cacerts -storepass changeit
# Stage 2: Create the final application image for exchange-service
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=project-builder /usr/src/app/exchangeService/target/exchangeService-*.jar app.jar

# collectapi sertifikasını ekleme (önceki Dockerfile'dan alındı)
# Bu komutlar maven build image'ında değil, final image'da çalışmalı
# RUN echo | openssl s_client -connect api.collectapi.com:443 2>/dev/null | sed -ne '/-BEGIN CERTIFICATE-/,/-END CERTIFICATE-/p' > /tmp/collectapi.crt \
#    && keytool -import -noprompt -trustcacerts -alias collectapi -file /tmp/collectapi.crt -keystore $JAVA_HOME/lib/security/cacerts -storepass changeit \
#    && rm /tmp/collectapi.crt
# NOT: Yukarıdaki sertifika ekleme adımı eclipse-temurin:17-jre-alpine imajında `keytool` ve `openssl` olmayabilir.
# Eğer bu gerekiyorsa, base imajı `eclipse-temurin:17-jdk-alpine` olarak değiştirmek veya bu araçları kurmak gerekebilir.
# Ya da sertifikayı build aşamasında değil, runtime'da volume mount ile sağlamak daha iyi bir çözüm olabilir.
# Şimdilik bu adımı yorum satırı yapıyorum, gerekirse tekrar aktif ederiz.

EXPOSE 8082
ENTRYPOINT ["java", "-jar", "/app/app.jar"]