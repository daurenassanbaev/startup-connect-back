package com.daurenassanbaev.websocket.mapper;

import com.daurenassanbaev.websocket.chat.ChatMessage;
import com.daurenassanbaev.websocket.chat.ChatMessageDto;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class ChatMessageMapper implements Mapper<ChatMessageDto, ChatMessage> {

    public void copy(ChatMessageDto dto, ChatMessage chatMessage) {
        chatMessage.setContent(dto.getContent());
        chatMessage.setRecipientId(dto.getRecipientId());
        chatMessage.setTimestamp(LocalDateTime.now());
    }

    @Override
    public ChatMessage map(ChatMessageDto object) {
        ChatMessage chatMessage = new ChatMessage();
        copy(object, chatMessage);
        return chatMessage;
    }
}

