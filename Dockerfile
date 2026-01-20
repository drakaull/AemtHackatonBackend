# ---- Build stage (Java 25) ----
FROM eclipse-temurin:25-jdk AS build

WORKDIR /app

# Installer curl (mvnw peut en avoir besoin selon l’image)
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

# Copier le wrapper + pom pour profiter du cache
COPY mvnw pom.xml ./
COPY .mvn .mvn

# Pré-téléchargement deps (accélère les builds suivants)
RUN chmod +x mvnw && ./mvnw -q -DskipTests dependency:go-offline

# Copier le code
COPY src src

# Build
RUN ./mvnw -q -DskipTests package

# ---- Run stage ----
FROM eclipse-temurin:25-jre

WORKDIR /app

# Copier le jar
COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar"]
