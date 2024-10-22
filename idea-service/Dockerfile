FROM openjdk:17-jdk
WORKDIR /app
COPY target/idea-service-0.0.1-SNAPSHOT.jar /app/idea-service.jar
EXPOSE 8082
ENTRYPOINT ["java", "-jar", "idea-service.jar"]