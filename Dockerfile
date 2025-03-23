FROM eclipse-temurin:19-jdk-alpine
LABEL authors="Greg"
EXPOSE 43594/tcp

RUN mkdir /app
WORKDIR /app/

# Copy JAR file
COPY ./game/build/libs/void-server-*.jar /app/void-server.jar

# Copy configuration and cache files
COPY ./data/ /app/data/

CMD ["java", "-jar", "void-server.jar"]