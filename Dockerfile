FROM openjdk:21
ARG JAR_FILE=target/mybopi-0.0.1-SNAPSHOT.jar
COPY ${JAR_FILE} app_MYBOPI.jar
COPY src/main/resources/templates /app/src/main/resources/templates
COPY images /app/images
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app_MYBOPI.jar"]
