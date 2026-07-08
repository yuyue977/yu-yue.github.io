package com.example.xiamenbackground.websocket;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.concurrent.CopyOnWriteArrayList;

@Component
public class DataWebSocketHandler extends TextWebSocketHandler {

    private static final CopyOnWriteArrayList<WebSocketSession> sessions = new CopyOnWriteArrayList<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        sessions.add(session);
        System.out.println("=== WebSocket 连接建立, 当前连接数: " + sessions.size() + " ===");
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        sessions.remove(session);
        System.out.println("=== WebSocket 连接关闭, 当前连接数: " + sessions.size() + " ===");
    }

    public void broadcast(String message) {
        TextMessage textMessage = new TextMessage(message);
        for (WebSocketSession session : sessions) {
            try {
                if (session.isOpen()) {
                    session.sendMessage(textMessage);
                }
            } catch (IOException e) {
                System.err.println("=== WebSocket 发送失败: " + e.getMessage() + " ===");
            }
        }
    }
}
