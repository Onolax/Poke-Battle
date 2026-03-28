FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /workspace
COPY pom.xml .
COPY api-gateway/pom.xml api-gateway/
COPY user-service/pom.xml user-service/
COPY game-data-service/pom.xml game-data-service/
COPY lobby-service/pom.xml lobby-service/
COPY battle-service/pom.xml battle-service/
COPY rating-service/pom.xml rating-service/
RUN mvn dependency:go-offline -pl game-data-service -am -q
COPY game-data-service/src game-data-service/src
RUN mvn package -pl game-data-service -am -DskipTests -q
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /workspace/game-data-service/target/*.jar app.jar
EXPOSE 8082
ENTRYPOINT ["java", "-jar", "app.jar"]
