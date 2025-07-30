FROM maven:3-eclipse-temurin-21 AS builder
WORKDIR /app

COPY pom.xml .
RUN mvn -B -ntp dependency:go-offline

COPY src ./src
RUN mvn -B -ntp package -DskipTests

FROM bellsoft/liberica-runtime-container:jre-21-slim-glibc
WORKDIR /app

ARG JAR_FILE=target/*.jar
COPY --from=builder /app/${JAR_FILE} /app/app.jar

ENTRYPOINT ["java","-jar","/app/app.jar"]
