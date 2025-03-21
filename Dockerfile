FROM eclipse-temurin:19-jdk-alpine
LABEL authors="Greg"
EXPOSE 43594/tcp

RUN mkdir /app
WORKDIR /app/

# Copy JAR file
COPY ./game/build/libs/void-server-*.jar /app/void-server.jar

# Copy configuration and cache files
COPY ./data/map/ /app/data/map/
COPY ./data/spawns/ /app/data/spawns/
COPY data/client/ /app/data/definitions/
COPY ./data/cache/ /app/data/cache/

CMD ["java", "-jar", "void-server.jar"]