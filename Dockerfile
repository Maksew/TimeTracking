# Build stage
FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /app
COPY . .
RUN ./mvnw clean package -DskipTests

# Run stage
FROM eclipse-temurin:21-jdk-alpine AS runner

WORKDIR /app
COPY --from=builder /app/backend/target/backend-0.0.1-SNAPSHOT.jar app.jar

ENV SPRING_PROFILES_ACTIVE=deploy

# Force les paramètres PostgreSQL avec -D directement au lancement
CMD ["java", \
     "-Dspring.profiles.active=deploy", \
     "-Dspring.datasource.url=jdbc:postgresql://db.pwyzzsvgwicqstwfebyb.supabase.co:5432/postgres", \
     "-Dspring.datasource.username=${DB_USERNAME}", \
     "-Dspring.datasource.password=${DB_PASSWORD}", \
     "-Dspring.datasource.driver-class-name=org.postgresql.Driver", \
     "-Dspring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect", \
     "-Dspring.h2.console.enabled=false", \
     "-Dspring.autoconfigure.exclude=org.springframework.boot.autoconfigure.h2.H2ConsoleAutoConfiguration", \
     "-Dserver.port=${SERVER_PORT}", \
     "-jar", "app.jar"]