FROM maven:3.9.9-eclipse-temurin-21 AS build

WORKDIR /build

COPY pom.xml .
COPY src ./src

RUN mvn -B -DskipTests package


FROM eclipse-temurin:21-jre-jammy

WORKDIR /app

RUN useradd --system --create-home appuser

COPY --from=build /build/target/*.jar app.jar

USER appuser

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]