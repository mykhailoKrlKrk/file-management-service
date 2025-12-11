FROM bellsoft/liberica-openjdk-debian:21 AS builder
WORKDIR /app

COPY mvnw pom.xml ./
COPY .mvn .mvn
RUN chmod +x mvnw

COPY src src
RUN ./mvnw -q clean package -DskipTests -Dcheckstyle.skip=true
FROM bellsoft/hardened-liberica-runtime-container:jdk-21-cds-glibc

WORKDIR /app
COPY --from=builder /app/target/*.jar /app/app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
