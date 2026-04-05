# ===== STAGE 1: BUILD =====
FROM maven:3.9.12-eclipse-temurin-21 AS builder

WORKDIR /app

# copy dependency dulu (biar cache optimal)
COPY pom.xml .
RUN mvn dependency:go-offline

# copy source
COPY src ./src

# build jar
RUN mvn package -DskipTests


# ===== STAGE 2: RUNTIME =====
FROM eclipse-temurin:21-jre-jammy

WORKDIR /app

# copy hasil build
COPY --from=builder /app/target/*.jar app.jar

# JVM tuning (untuk VPS kecil)
ENV JAVA_OPTS="-Xms256m -Xmx512m"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]