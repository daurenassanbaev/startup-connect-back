package com.daurenassanbaev.notificationservice.listeners;

import com.daurenassanbaev.notificationservice.exceptions.InvalidMessageFormatException;
import com.daurenassanbaev.notificationservice.service.EmailSenderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@Slf4j
public class NotificationListener {
    @Autowired
    private EmailSenderService emailSenderService;

    @KafkaListener(topics = "notification-topic", groupId = "groupId", containerFactory = "factory")
    void listener(Map<String, String> data) {
         System.out.println("Received: " + data);
         String toEmail = data.get("email");
         String message = data.get("message");
         String subject = data.get("subject");
         if (toEmail != null && message != null && subject != null) {
             emailSenderService.sendEmail(toEmail, subject, message);
         } else {
             log.warn("Invalid message format: {}" ,data);
             throw new InvalidMessageFormatException("Invalid message format");
         }
    }
}
