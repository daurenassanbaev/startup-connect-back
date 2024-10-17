package com.daurenassanbaev.websocket.config;

import com.daurenassanbaev.websocket.chat.ChatMessageService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.web.socket.config.annotation.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;


@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {
    private final ChatMessageService chatMessageService;
    private final JwtDecoder jwtDecoder;
    public WebSocketConfig(ChatMessageService chatMessageService, JwtDecoder jwtDecoder) {
        this.chatMessageService = chatMessageService;
        this.jwtDecoder = jwtDecoder;
    }
    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(chatHandler(), "/chat");
    }
    @Bean
    public TextWebSocketHandler chatHandler() {
        return new ChatHandler(chatMessageService, jwtDecoder);
    }
}
