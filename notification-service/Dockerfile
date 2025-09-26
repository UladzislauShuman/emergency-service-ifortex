FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app

COPY pom.xml .
COPY notification-service/pom.xml notification-service/
RUN mvn -B -f notification-service/pom.xml dependency:go-offline
COPY notification-service/src notification-service/src
RUN mvn -B -f notification-service/pom.xml clean package -DskipTests

FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
COPY --from=build /app/notification-service/target/*.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]