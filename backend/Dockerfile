FROM maven:3.9.11-eclipse-temurin-25 AS build
WORKDIR /app
COPY pom.xml ./
COPY src ./src
RUN mvn --batch-mode verify

FROM eclipse-temurin:25-jre
WORKDIR /app
RUN groupadd --system events && useradd --system --gid events events && mkdir data && chown events:events data
COPY --from=build /app/target/eventos-1.0.0.jar app.jar
USER events
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
