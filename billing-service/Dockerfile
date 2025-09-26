FROM maven:3.9.6-eclipse-temurin-17 AS build

WORKDIR /app

COPY pom.xml .
COPY billing-service/pom.xml billing-service/
RUN mvn -f billing-service/pom.xml dependency:go-offline
COPY billing-service/src billing-service/src

RUN mvn -f billing-service/pom.xml clean package -DskipTests

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/billing-service/target/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]