FROM openjdk:17-jdk
WORKDIR /app
COPY target/comment-service-0.0.1-SNAPSHOT.jar /app/comment-service.jar
EXPOSE 8086
ENTRYPOINT ["java", "-jar", "comment-service.jar"]
