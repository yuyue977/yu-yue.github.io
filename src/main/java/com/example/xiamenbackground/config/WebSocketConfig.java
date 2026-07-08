package com.example.xiamenbackground.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import com.example.xiamenbackground.websocket.DataWebSocketHandler;
import org.springframework.beans.factory.annotation.Autowired;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    @Autowired
    private DataWebSocketHandler dataWebSocketHandler;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(dataWebSocketHandler, "/ws/data")
                .setAllowedOrigins("*");
    }
}
