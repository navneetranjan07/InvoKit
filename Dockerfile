# Stage 1: Build with Maven using a standard image
FROM maven:3-eclipse-temurin-21 AS build
COPY . .
# Maven will use the project's pom.xml settings to build for Java 20
RUN mvn clean package -DskipTests

# Stage 2: Run with verified JDK 20
FROM eclipse-temurin:20-jdk-jammy
COPY --from=build /target/*.jar app.jar
EXPOSE 10000
ENTRYPOINT ["java", "-jar", "app.jar"]