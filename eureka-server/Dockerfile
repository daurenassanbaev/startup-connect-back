FROM openjdk:17-jdk
WORKDIR /app
COPY target/eureka-server-0.0.1-SNAPSHOT.jar /app/eureka-server.jar
CMD ["java", "-jar", "eureka-server.jar"]
EXPOSE 8761