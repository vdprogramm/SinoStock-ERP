# Java 21 multistage Docker image. The first stage compiles the application.
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /workspace
COPY pom.xml .
RUN mvn -B -ntp dependency:go-offline
COPY src ./src
RUN mvn -B -ntp -DskipTests package

# Small runtime image for Render free instances (512 MB memory).
FROM eclipse-temurin:21-jre
WORKDIR /app
RUN useradd -r -u 10001 -m appuser
COPY --from=build /workspace/target/sinostock-erp-0.0.1-SNAPSHOT.jar app.jar
USER 10001
EXPOSE 8080
ENTRYPOINT ["java","-XX:MaxRAMPercentage=70.0","-XX:ActiveProcessorCount=2","-jar","/app/app.jar"]
