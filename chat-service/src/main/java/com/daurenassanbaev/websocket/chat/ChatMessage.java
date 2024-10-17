package com.daurenassanbaev.websocket.chat;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.Date;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Document
public class ChatMessage {
    @Id
    private String id;

    @NotBlank(message = "Chat ID cannot be blank")
    private String chatId;

    @NotBlank(message = "Sender ID cannot be blank")
    private String senderId;

    @NotBlank(message = "Recipient ID cannot be blank")
    private String recipientId;

    @NotBlank(message = "Content cannot be blank")
    @Size(max = 500, message = "Content must be at most 500 characters long")
    private String content;
    private LocalDateTime timestamp;
}
