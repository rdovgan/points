FROM eclipse-temurin:21-jre

WORKDIR /app

COPY local-experiences-1.0-SNAPSHOT.jar app.jar

# Environment variables for database connection
ENV DB_HOST=${DB_HOST}
ENV DB_PORT=${DB_PORT:-5432}
ENV DB_NAME=${DB_NAME:-postgres}
ENV DB_USERNAME=${DB_USERNAME}
ENV DB_PASSWORD=${DB_PASSWORD}

EXPOSE 8080

# JVM optimization for production
ENTRYPOINT ["java", \
    "-Dspring.profiles.active=prod", \
    "-Xms512m", \
    "-Xmx1024m", \
    "-XX:+UseG1GC", \
    "-jar", \
    "/app/app.jar"]
