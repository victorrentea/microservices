FROM eclipse-temurin:21-jre
WORKDIR /app
COPY target/*.jar app.jar
ENTRYPOINT ["java", "-XX:+UseG1GC", "-jar", "/app/app.jar"]

