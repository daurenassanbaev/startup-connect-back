FROM openjdk:17-jdk
WORKDIR /app
COPY target/user-service-0.0.1-SNAPSHOT.jar /app/user-service.jar
EXPOSE 8083
ENTRYPOINT ["java", "-jar", "user-service.jar"]
