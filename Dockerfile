# Stage 1: Build with Maven
FROM maven:3.9.6-eclipse-temurin-20 AS build
COPY . .
RUN mvn clean package -DskipTests

# Stage 2: Run with JDK 20
FROM eclipse-temurin:20-jdk-jammy
COPY --from=build /target/*.jar app.jar
EXPOSE 10000
ENTRYPOINT ["java", "-jar", "app.jar"]