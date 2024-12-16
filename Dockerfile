FROM eclipse-temurin:17-jdk-alpine
RUN apk add --no-cache mysql-client
WORKDIR /dayzwiki
COPY /target/dayz-wiki-0.0.1-SNAPSHOT.jar /dayzwiki/dayzwiki.jar
ENTRYPOINT ["java", "-jar", "dayzwiki.jar"]
EXPOSE 8080