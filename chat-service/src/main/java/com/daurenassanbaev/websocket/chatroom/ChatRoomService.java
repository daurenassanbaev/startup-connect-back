package com.daurenassanbaev.websocket.chatroom;

import com.daurenassanbaev.websocket.chatroom.repository.ChatRoomRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatRoomService {
    private final ChatRoomRepository chatRoomRepository;

    public Optional<String> getChatRoomId(String senderId, String recipientId, boolean createNewRoomIfNotExists) {
        log.info("Retrieving chat room ID for senderId: {} and recipientId: {}", senderId, recipientId);

        return chatRoomRepository.findBySenderIdAndRecipientId(senderId, recipientId)
                .map(chatRoom -> {
                    log.info("Chat room found with ID: {}", chatRoom.getChatId());
                    return chatRoom.getChatId();
                })
                .or(() -> {
                    if (createNewRoomIfNotExists) {
                        var chatId = createChatId(senderId, recipientId);
                        log.info("No chat room found, created new chat room with ID: {}", chatId);
                        return Optional.of(chatId);
                    }
                    log.warn("No chat room found for senderId: {} and recipientId: {}", senderId, recipientId);
                    return Optional.empty();
                });
    }

    private String createChatId(String senderId, String recipientId) {
        var chatId = String.format("%s_%s", senderId, recipientId);
        log.info("Creating new chat ID: {}", chatId);

        ChatRoom senderRecipient = ChatRoom.builder()
                .chatId(chatId)
                .senderId(senderId)
                .recipientId(recipientId)
                .build();
        ChatRoom recipientSender = ChatRoom.builder()
                .chatId(chatId)
                .senderId(recipientId)
                .recipientId(senderId)
                .build();

        chatRoomRepository.save(senderRecipient);
        log.info("Saved chat room for sender: {} and recipient: {}", senderId, recipientId);

        chatRoomRepository.save(recipientSender);
        log.info("Saved chat room for recipient: {} and sender: {}", recipientId, senderId);

        return chatId;
    }
}
