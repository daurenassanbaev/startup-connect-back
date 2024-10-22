FROM openjdk:17-jdk
WORKDIR /app
COPY target/websocket-0.0.1-SNAPSHOT.jar /app/websocket.jar
EXPOSE 8088
ENTRYPOINT ["java", "-jar", "websocket.jar"]
