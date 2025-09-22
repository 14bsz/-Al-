package com.aichat.controller;

import com.aichat.dto.ChatRequest;
import com.aichat.dto.ChatResponse;
import com.aichat.service.AIService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.util.HashMap;
import java.util.Map;

/**
 * WebSocket聊天控制器
 * 处理STOMP消息
 */
@Controller
public class WebSocketChatController {

    private static final Logger logger = LoggerFactory.getLogger(WebSocketChatController.class);

    @Autowired
    private AIService aiService;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    /**
     * 处理发送消息
     */
    @MessageMapping("/chat.sendMessage")
    @SendTo("/topic/messages")
    public Map<String, Object> sendMessage(Map<String, Object> message) {
        logger.info("收到WebSocket消息: {}", message);
        
        try {
            // 验证消息参数
            if (message == null) {
                throw new IllegalArgumentException("消息不能为空");
            }
            
            Object userIdObj = message.get("userId");
            Object contentObj = message.get("content");
            
            if (userIdObj == null) {
                throw new IllegalArgumentException("用户ID不能为空");
            }
            
            if (contentObj == null) {
                throw new IllegalArgumentException("消息内容不能为空");
            }
            
            // 构建聊天请求
            ChatRequest request = new ChatRequest();
            request.setUserId(Long.valueOf(userIdObj.toString()));
            request.setMessage(contentObj.toString());
            request.setMessageType(message.getOrDefault("messageType", "text").toString());
            request.setCharacterId(1L); // 默认角色ID
            
            // 发送给AI服务处理
            ChatResponse response = aiService.sendMessage(request);
            
            // 构建响应消息
            Map<String, Object> responseMessage = new HashMap<>();
            responseMessage.put("type", "chat_message");
            responseMessage.put("content", response != null && response.getMessage() != null ? response.getMessage() : "AI响应为空");
            responseMessage.put("userId", "ai");
            responseMessage.put("messageType", "text");
            responseMessage.put("timestamp", response != null && response.getTimestamp() != null ? response.getTimestamp().toString() : java.time.LocalDateTime.now().toString());
            responseMessage.put("model", response != null && response.getModel() != null ? response.getModel() : "unknown");
            
            return responseMessage;
            
        } catch (Exception e) {
            logger.error("处理消息失败", e);
            Map<String, Object> errorMessage = new HashMap<>();
            errorMessage.put("type", "error");
            errorMessage.put("content", "抱歉，消息处理失败，请稍后重试。错误信息: " + e.getMessage());
            errorMessage.put("userId", "system");
            errorMessage.put("timestamp", java.time.LocalDateTime.now().toString());
            return errorMessage;
        }
    }

    /**
     * 处理输入状态
     */
    @MessageMapping("/chat.typing")
    @SendTo("/topic/messages")
    public Map<String, Object> handleTyping(Map<String, Object> message) {
        logger.info("收到输入状态: {}", message);
        
        Map<String, Object> typingMessage = new HashMap<>();
        typingMessage.put("type", "typing_status");
        typingMessage.put("userId", message.get("userId"));
        typingMessage.put("isTyping", message.get("isTyping"));
        
        return typingMessage;
    }

    /**
     * 处理加入房间
     */
    @MessageMapping("/chat.joinRoom")
    @SendTo("/topic/notifications")
    public Map<String, Object> joinRoom(Map<String, Object> message) {
        logger.info("用户加入房间: {}", message);
        
        Map<String, Object> joinMessage = new HashMap<>();
        joinMessage.put("type", "user_join");
        joinMessage.put("userId", message.get("userId"));
        joinMessage.put("roomId", message.get("roomId"));
        joinMessage.put("message", "用户加入了聊天");
        
        return joinMessage;
    }

    /**
     * 处理离开房间
     */
    @MessageMapping("/chat.leaveRoom")
    @SendTo("/topic/notifications")
    public Map<String, Object> leaveRoom(Map<String, Object> message) {
        logger.info("用户离开房间: {}", message);
        
        Map<String, Object> leaveMessage = new HashMap<>();
        leaveMessage.put("type", "user_leave");
        leaveMessage.put("userId", message.get("userId"));
        leaveMessage.put("roomId", message.get("roomId"));
        leaveMessage.put("message", "用户离开了聊天");
        
        return leaveMessage;
    }
}