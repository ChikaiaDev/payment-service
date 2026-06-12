FROM maven:3.9.5-eclipse-temurin-21 AS build
WORKDIR /app

COPY pom.xml ./pom.xml

COPY payment-service/pom.xml ./payment-service/pom.xml
COPY notification-service/pom.xml ./notification-service/pom.xml
COPY reporting-service/pom.xml ./reporting-service/pom.xml

RUN mvn dependency:go-offline -q

COPY reporting-service/src ./reporting-service/src
RUN mvn package -pl reporting-service -DskipTests -q

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
COPY --from=build /app/reporting-service/target/*.jar app.jar
USER appuser
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]