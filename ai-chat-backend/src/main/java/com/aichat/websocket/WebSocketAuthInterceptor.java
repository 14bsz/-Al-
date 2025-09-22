package com.aichat.websocket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

/**
 * WebSocket认证拦截器
 * 在WebSocket握手阶段进行身份验证
 */
@Component
public class WebSocketAuthInterceptor implements HandshakeInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(WebSocketAuthInterceptor.class);

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {
        
        logger.info("WebSocket握手前验证: {}", request.getURI());
        
        // 获取查询参数
        String query = request.getURI().getQuery();
        if (query != null) {
            // 解析用户ID和token
            String[] params = query.split("&");
            for (String param : params) {
                String[] keyValue = param.split("=");
                if (keyValue.length == 2) {
                    String key = keyValue[0];
                    String value = keyValue[1];
                    
                    if ("userId".equals(key)) {
                        attributes.put("userId", value);
                        logger.info("WebSocket用户ID: {}", value);
                    } else if ("token".equals(key)) {
                        attributes.put("token", value);
                        logger.info("WebSocket认证token已获取");
                    }
                }
            }
        }
        
        // 简单验证逻辑（实际项目中应该验证JWT token）
        // 这里暂时允许所有连接
        return true;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                               WebSocketHandler wsHandler, Exception exception) {
        if (exception != null) {
            logger.error("WebSocket握手失败", exception);
        } else {
            logger.info("WebSocket握手成功: {}", request.getURI());
        }
    }
}