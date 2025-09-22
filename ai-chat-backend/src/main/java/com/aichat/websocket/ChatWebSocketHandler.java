package com.aichat.websocket;

import com.aichat.dto.ChatRequest;
import com.aichat.dto.ChatResponse;
import com.aichat.service.AIService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * WebSocket聊天处理器
 * 提供实时聊天功能，支持多用户在线、消息推送等
 */
@Component
public class ChatWebSocketHandler implements WebSocketHandler {

    private static final Logger logger = LoggerFactory.getLogger(ChatWebSocketHandler.class);

    @Autowired
    private AIService aiService;

    private final ObjectMapper objectMapper = new ObjectMapper();
    
    // 存储所有活跃的WebSocket连接
    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();
    
    // 存储用户ID到会话ID的映射
    private final Map<Long, String> userSessions = new ConcurrentHashMap<>();
    
    // 存储在线用户
    private final CopyOnWriteArraySet<Long> onlineUsers = new CopyOnWriteArraySet<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String sessionId = session.getId();
        sessions.put(sessionId, session);
        
        logger.info("WebSocket连接建立: sessionId={}", sessionId);
        
        // 发送欢迎消息
        Map<String, Object> welcomeMessage = new HashMap<>();
        welcomeMessage.put("type", "system");
        welcomeMessage.put("message", "欢迎使用AI智能聊天系统！");
        welcomeMessage.put("timestamp", LocalDateTime.now().toString());
        welcomeMessage.put("sessionId", sessionId);
        sendMessage(session, welcomeMessage);
        
        // 广播在线用户数量
        broadcastOnlineCount();
    }

    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
        String sessionId = session.getId();
        String payload = message.getPayload().toString();
        
        logger.info("收到WebSocket消息: sessionId={}, payload={}", sessionId, payload);
        
        try {
            Map<String, Object> messageData = objectMapper.readValue(payload, Map.class);
            String messageType = (String) messageData.get("type");
            
            switch (messageType) {
                case "chat":
                    handleChatMessage(session, messageData);
                    break;
                case "join":
                    handleUserJoin(session, messageData);
                    break;
                case "leave":
                    handleUserLeave(session, messageData);
                    break;
                case "typing":
                    handleTypingIndicator(session, messageData);
                    break;
                case "heartbeat":
                    handleHeartbeat(session);
                    break;
                default:
                    logger.warn("未知消息类型: {}", messageType);
            }
            
        } catch (Exception e) {
            logger.error("处理WebSocket消息失败", e);
            sendErrorMessage(session, "消息处理失败: " + e.getMessage());
        }
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        String sessionId = session.getId();
        logger.error("WebSocket传输错误: sessionId={}", sessionId, exception);
        
        // 清理会话
        cleanupSession(session);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {
        String sessionId = session.getId();
        logger.info("WebSocket连接关闭: sessionId={}, status={}", sessionId, closeStatus);
        
        // 清理会话
        cleanupSession(session);
        
        // 广播在线用户数量
        broadcastOnlineCount();
    }

    @Override
    public boolean supportsPartialMessages() {
        return false;
    }

    /**
     * 处理聊天消息
     */
    private void handleChatMessage(WebSocketSession session, Map<String, Object> messageData) {
        try {
            // 构建聊天请求
            ChatRequest request = new ChatRequest();
            request.setUserId(((Number) messageData.get("userId")).longValue());
            request.setCharacterId(((Number) messageData.get("characterId")).longValue());
            request.setMessage((String) messageData.get("message"));
            request.setMessageType((String) messageData.getOrDefault("messageType", "text"));
            
            // 发送消息给AI服务
            ChatResponse response = aiService.sendMessage(request);
            
            // 构建响应消息
            Map<String, Object> responseMessage = new HashMap<>();
            responseMessage.put("type", "chat_response");
            responseMessage.put("userId", request.getUserId());
            responseMessage.put("characterId", request.getCharacterId());
            responseMessage.put("message", response.getMessage());
            responseMessage.put("timestamp", response.getTimestamp().toString());
            responseMessage.put("model", response.getModel());
            responseMessage.put("tokensUsed", response.getTokensUsed());
            
            // 发送响应给用户
            sendMessage(session, responseMessage);
            
            // 可选：广播给其他用户（如果是群聊）
            // broadcastToOthers(session, responseMessage);
            
        } catch (Exception e) {
            logger.error("处理聊天消息失败", e);
            sendErrorMessage(session, "聊天消息处理失败");
        }
    }

    /**
     * 处理用户加入
     */
    private void handleUserJoin(WebSocketSession session, Map<String, Object> messageData) {
        Long userId = ((Number) messageData.get("userId")).longValue();
        String sessionId = session.getId();
        
        // 记录用户会话映射
        userSessions.put(userId, sessionId);
        onlineUsers.add(userId);
        
        logger.info("用户加入聊天: userId={}, sessionId={}", userId, sessionId);
        
        // 发送加入成功消息
        Map<String, Object> joinMessage = new HashMap<>();
        joinMessage.put("type", "join_success");
        joinMessage.put("userId", userId);
        joinMessage.put("message", "成功加入聊天室");
        joinMessage.put("onlineCount", onlineUsers.size());
        sendMessage(session, joinMessage);
        
        // 广播用户加入消息
        Map<String, Object> userJoinedMessage = new HashMap<>();
        userJoinedMessage.put("type", "user_joined");
        userJoinedMessage.put("userId", userId);
        userJoinedMessage.put("message", "用户 " + userId + " 加入了聊天");
        userJoinedMessage.put("onlineCount", onlineUsers.size());
        broadcastToOthers(session, userJoinedMessage);
    }

    /**
     * 处理用户离开
     */
    private void handleUserLeave(WebSocketSession session, Map<String, Object> messageData) {
        Long userId = ((Number) messageData.get("userId")).longValue();
        
        // 清理用户数据
        userSessions.remove(userId);
        onlineUsers.remove(userId);
        
        logger.info("用户离开聊天: userId={}", userId);
        
        // 广播用户离开消息
        Map<String, Object> userLeftMessage = new HashMap<>();
        userLeftMessage.put("type", "user_left");
        userLeftMessage.put("userId", userId);
        userLeftMessage.put("message", "用户 " + userId + " 离开了聊天");
        userLeftMessage.put("onlineCount", onlineUsers.size());
        broadcastToOthers(session, userLeftMessage);
    }

    /**
     * 处理打字指示器
     */
    private void handleTypingIndicator(WebSocketSession session, Map<String, Object> messageData) {
        Long userId = ((Number) messageData.get("userId")).longValue();
        boolean isTyping = (Boolean) messageData.getOrDefault("isTyping", false);
        
        // 广播打字状态给其他用户
        Map<String, Object> typingMessage = new HashMap<>();
        typingMessage.put("type", "typing_indicator");
        typingMessage.put("userId", userId);
        typingMessage.put("isTyping", isTyping);
        broadcastToOthers(session, typingMessage);
    }

    /**
     * 处理心跳消息
     */
    private void handleHeartbeat(WebSocketSession session) {
        Map<String, Object> heartbeatResponse = new HashMap<>();
        heartbeatResponse.put("type", "heartbeat_response");
        heartbeatResponse.put("timestamp", LocalDateTime.now().toString());
        sendMessage(session, heartbeatResponse);
    }

    /**
     * 发送消息给指定会话
     */
    private void sendMessage(WebSocketSession session, Object message) {
        try {
            if (session.isOpen()) {
                String jsonMessage = objectMapper.writeValueAsString(message);
                session.sendMessage(new TextMessage(jsonMessage));
            }
        } catch (IOException e) {
            logger.error("发送WebSocket消息失败", e);
        }
    }

    /**
     * 发送错误消息
     */
    private void sendErrorMessage(WebSocketSession session, String errorMessage) {
        Map<String, Object> errorMsg = new HashMap<>();
        errorMsg.put("type", "error");
        errorMsg.put("message", errorMessage);
        errorMsg.put("timestamp", LocalDateTime.now().toString());
        sendMessage(session, errorMsg);
    }

    /**
     * 广播消息给所有其他用户
     */
    private void broadcastToOthers(WebSocketSession excludeSession, Object message) {
        sessions.values().parallelStream()
            .filter(session -> !session.getId().equals(excludeSession.getId()))
            .filter(WebSocketSession::isOpen)
            .forEach(session -> sendMessage(session, message));
    }

    /**
     * 广播消息给所有用户
     */
    private void broadcastToAll(Object message) {
        sessions.values().parallelStream()
            .filter(WebSocketSession::isOpen)
            .forEach(session -> sendMessage(session, message));
    }

    /**
     * 广播在线用户数量
     */
    private void broadcastOnlineCount() {
        Map<String, Object> message = new HashMap<>();
        message.put("type", "online_count");
        message.put("count", onlineUsers.size());
        message.put("timestamp", LocalDateTime.now().toString());
        broadcastToAll(message);
    }

    /**
     * 清理会话数据
     */
    private void cleanupSession(WebSocketSession session) {
        String sessionId = session.getId();
        sessions.remove(sessionId);
        
        // 查找并移除用户映射
        userSessions.entrySet().removeIf(entry -> entry.getValue().equals(sessionId));
        
        // 移除在线用户（通过会话ID反查用户ID）
        Long userId = userSessions.entrySet().stream()
            .filter(entry -> entry.getValue().equals(sessionId))
            .map(Map.Entry::getKey)
            .findFirst()
            .orElse(null);
            
        if (userId != null) {
            onlineUsers.remove(userId);
            userSessions.remove(userId);
        }
    }

    /**
     * 发送消息给指定用户
     */
    public void sendMessageToUser(Long userId, Object message) {
        String sessionId = userSessions.get(userId);
        if (sessionId != null) {
            WebSocketSession session = sessions.get(sessionId);
            if (session != null && session.isOpen()) {
                sendMessage(session, message);
            }
        }
    }

    /**
     * 获取在线用户数量
     */
    public int getOnlineUserCount() {
        return onlineUsers.size();
    }

    /**
     * 检查用户是否在线
     */
    public boolean isUserOnline(Long userId) {
        return onlineUsers.contains(userId);
    }

    /**
     * 发送系统通知
     */
    public void sendSystemNotification(String message) {
        Map<String, Object> notification = new HashMap<>();
        notification.put("type", "system_notification");
        notification.put("message", message);
        notification.put("timestamp", LocalDateTime.now().toString());
        broadcastToAll(notification);
    }
}