# Build React frontend on Node, then bundle it inside Spring Boot JAR.
FROM node:22-alpine AS frontend-build
WORKDIR /frontend
COPY frontend/package*.json ./
RUN npm install --no-audit --no-fund
COPY frontend ./
RUN npm run build

FROM maven:3.9-eclipse-temurin-21 AS backend-build
WORKDIR /workspace
COPY pom.xml ./
RUN mvn -B -ntp dependency:go-offline
COPY src ./src
RUN rm -rf src/main/resources/static && mkdir -p src/main/resources/static
COPY --from=frontend-build /frontend/dist/ src/main/resources/static/
RUN mvn -B -ntp -DskipTests package

FROM eclipse-temurin:21-jre
WORKDIR /app
RUN useradd -r -u 10001 -m appuser
COPY --from=backend-build /workspace/target/sinostock-erp-0.0.1-SNAPSHOT.jar app.jar
USER 10001
EXPOSE 8080
ENTRYPOINT ["java","-XX:MaxRAMPercentage=70.0","-XX:ActiveProcessorCount=2","-jar","/app/app.jar"]
