FROM openjdk:17-jdk
WORKDIR /app
COPY target/ratings-service-0.0.1-SNAPSHOT.jar /app/ratings-service.jar
EXPOSE 8085
ENTRYPOINT ["java", "-jar", "ratings-service.jar"]
