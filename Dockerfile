FROM maven:3.8.8-openjdk-17-slim AS build
WORKDIR /workspace

# copy only what Maven needs first for better caching
COPY pom.xml ./
COPY src ./src

RUN mvn -B -DskipTests package

FROM openjdk:17-jdk-slim
WORKDIR /app
EXPOSE 8080

# copy the built jar from the maven build stage
COPY --from=build /workspace/target/*.jar app.jar

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
