FROM maven:3.8.6-openjdk-21-slim as build
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

FROM openjdk:21-slim
COPY --from=build /app/backend/target/backend-0.0.1-SNAPSHOT.jar app.jar
ENV PORT=8989
EXPOSE 8989
CMD ["java", "-Dserver.port=${PORT}", "-jar", "/app.jar"]