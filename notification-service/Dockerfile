FROM openjdk:17-jdk
WORKDIR /app
COPY target/notification-service-0.0.1-SNAPSHOT.jar /app/notification-service.jar
EXPOSE 8777
ENTRYPOINT ["java", "-jar", "notification-service.jar"]
