package com.daurenassanbaev.websocket.chat;

import com.daurenassanbaev.websocket.chatroom.ChatRoomService;
import com.daurenassanbaev.websocket.exception.ChatMessageValidationException;
import com.daurenassanbaev.websocket.exception.ChatRoomNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatMessageService {
    private final ChatMessageRepository repository;
    private final ChatRoomService chatRoomService;

    public ChatMessage save(ChatMessage chatMessage, @AuthenticationPrincipal Jwt jwt) {

        chatMessage.setSenderId(jwt.getSubject());
        log.info("Saving chat message from senderId: {}", chatMessage.getSenderId());

        var chatId = chatRoomService.getChatRoomId(chatMessage.getSenderId(), chatMessage.getRecipientId(), true)
                .orElseThrow(() -> new ChatRoomNotFoundException("Chat room not found for senderId: " + chatMessage.getSenderId() + " and recipientId: " + chatMessage.getRecipientId()));

        chatMessage.setChatId(chatId);
        repository.save(chatMessage);
        log.info("Chat message saved: {}", chatMessage);

        return chatMessage;
    }

    public List<ChatMessage> findChatMessages(String senderId, String recipientId) {
        log.info("Retrieving chat messages between senderId: {} and recipientId: {}", senderId, recipientId);

        var chatId = chatRoomService.getChatRoomId(senderId, recipientId, false);
        if (chatId.isEmpty()) {
            log.warn("No chat room found between senderId: {} and recipientId: {}", senderId, recipientId);
        }

        return chatId.map(repository::findByChatId).orElse(new ArrayList<>());
    }
}
