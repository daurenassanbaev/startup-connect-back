package com.daurenassanbaev.websocket.config;

import com.daurenassanbaev.websocket.chat.ChatMessage;
import com.daurenassanbaev.websocket.chat.ChatMessageDto;
import com.daurenassanbaev.websocket.chat.ChatMessageService;
import com.daurenassanbaev.websocket.mapper.ChatMessageMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

@RequiredArgsConstructor
public class ChatHandler extends TextWebSocketHandler {
    private final ConcurrentHashMap<String, WebSocketSession> sessionMap = new ConcurrentHashMap<>();
    private final ChatMessageService chatMessageService;
    private final JwtDecoder jwtDecoder;
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        JwtAuthenticationToken exp =  (JwtAuthenticationToken) session.getPrincipal();
        String token = exp.getToken().getTokenValue();
        Jwt jwt = jwtDecoder.decode(token);
        session.getAttributes().put("jwt", jwt);
        sessionMap.put(jwt.getSubject(), session);
        System.out.println(jwt.getSubject());
    }


    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) throws IOException {
        String body = message.getPayload();
        ObjectMapper objectMapper = new ObjectMapper();
        ChatMessageDto chatMessageDto = objectMapper.readValue(body, ChatMessageDto.class);
        ChatMessageMapper chatMessageMapper = new ChatMessageMapper();
        ChatMessage chatMessage = chatMessageMapper.map(chatMessageDto);
        Jwt jwt = (Jwt) session.getAttributes().get("jwt");
        chatMessageService.save(chatMessage, jwt);
        String receiver = chatMessage.getRecipientId();
        WebSocketSession socketSession = sessionMap.get(receiver);
        if (socketSession.isOpen() && socketSession != null) {
            socketSession.sendMessage(new TextMessage(message.getPayload()));
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        String token = ((JwtAuthenticationToken) session.getPrincipal()).getToken().getTokenValue();
        Jwt jwt = jwtDecoder.decode(token);
        sessionMap.remove(jwt.getSubject());
    }
}


