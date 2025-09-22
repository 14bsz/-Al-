package com.aichat.config;

import com.aichat.websocket.ChatWebSocketHandler;
import com.aichat.websocket.WebSocketAuthInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.*;

/**
 * WebSocket配置
 * 支持实时聊天、消息推送、在线状态等功能
 */
@Configuration
@EnableWebSocket
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketConfigurer, WebSocketMessageBrokerConfigurer {

    private final ChatWebSocketHandler chatWebSocketHandler;
    private final WebSocketAuthInterceptor webSocketAuthInterceptor;

    public WebSocketConfig(ChatWebSocketHandler chatWebSocketHandler,
                          WebSocketAuthInterceptor webSocketAuthInterceptor) {
        this.chatWebSocketHandler = chatWebSocketHandler;
        this.webSocketAuthInterceptor = webSocketAuthInterceptor;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(chatWebSocketHandler, "/ws/chat")
                .addInterceptors(webSocketAuthInterceptor)
                .setAllowedOrigins("*")
                .withSockJS();
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // 启用简单消息代理
        config.enableSimpleBroker("/topic", "/queue", "/user");
        
        // 设置应用程序目的地前缀
        config.setApplicationDestinationPrefixes("/app");
        
        // 设置用户目的地前缀
        config.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*")
                .withSockJS()
                .setSessionCookieNeeded(false);
        
        // 添加不使用SockJS的端点，用于原生WebSocket连接
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*");
    }
}