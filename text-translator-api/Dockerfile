# Estágio de Build
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

# Estágio de Execução
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Labels de Metadados
LABEL maintainer="Thomaz Collet" \
      version="1.0" \
      description="Backend API para o Text Translator com Cache em Redis"

COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]