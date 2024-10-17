# **StartupConnect**

## **Description**
**StartupConnect** is a platform that connects startup founders and investors. Startup founders can showcase their ideas, and investors can evaluate them. After the evaluation, both parties have the opportunity to communicate through an integrated chat.

## **Key Technologies**
- **Java**
- **Spring Boot**
- **PostgreSQL**
- **Kafka**
- **MongoDB**
- **Amazon S3**
- **Liquibase**
- **WebSocket** (for online chat)
- **Google SMTP** (for sending email notifications)
- **KeyCloak** (for authentication and authorization)

## **Project Structure**
- **user-service**: Manages user registration and profiles.
- **comment-service**: Allows users to leave comments.
- **notification-service**: Notifies users about new evaluations and comments.
- **chat-service**: Enables communication between startup founders and investors.
- **apigw**: API Gateway for routing requests.
- **eureka-server**: Service for discovering other microservices.
- **amazon-s3-service**: Handles file storage in S3.
