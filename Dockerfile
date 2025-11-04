FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Copy the JAR that was built locally
COPY build/libs/*.jar app.jar

EXPOSE 8085

ENTRYPOINT ["java", "-jar", "app.jar"]