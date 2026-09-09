# ETAPA 1: Compilación y Extracción (JDK + Maven)
FROM maven:3.9.6-eclipse-temurin-17 AS builder
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests
RUN mkdir -p target/extracted && cd target/extracted && jar -xf ../*.jar

# ETAPA 2: Imagen final ligera (JRE)
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=builder /app/target/extracted .

ENTRYPOINT ["java", "-cp", ".:BOOT-INF/classes:BOOT-INF/lib/*"]