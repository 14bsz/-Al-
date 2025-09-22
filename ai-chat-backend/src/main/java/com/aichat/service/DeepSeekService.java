package com.aichat.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;
import java.util.Map;

/**
 * DeepSeek LLM服务实现类
 * 集成DeepSeek-V3 Chat API
 */
@Service
public class DeepSeekService implements LLMService {

    private static final Logger logger = LoggerFactory.getLogger(DeepSeekService.class);

    private final WebClient webClient;
    private final String model;

    public DeepSeekService(@Value("${api.deepseek.base-url}") String baseUrl,
                          @Value("${api.deepseek.key}") String apiKey,
                          @Value("${api.deepseek.model}") String model) {
        this.model = model;
        this.webClient = WebClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader("Authorization", "Bearer " + apiKey)
                .defaultHeader("Content-Type", "application/json")
                .build();
        
        logger.info("DeepSeek服务初始化完成，模型: {}", model);
    }

    @Override
    public String chat(String userMessage, String systemPrompt) {
        return chatWithContext(userMessage, systemPrompt, null);
    }

    @Override
    public String chatWithContext(String userMessage, String systemPrompt, String conversationHistory) {
        try {
            // 构建请求消息列表
            List<Map<String, String>> messages = buildMessages(systemPrompt, conversationHistory, userMessage);
            
            // 构建请求体
            Map<String, Object> requestBody = new java.util.HashMap<>();
            requestBody.put("model", model);
            requestBody.put("messages", messages);
            requestBody.put("max_tokens", 2000);
            requestBody.put("temperature", 0.7);
            requestBody.put("stream", false);

            logger.debug("发送DeepSeek API请求: {}", requestBody);

            // 发送API请求
            Map<String, Object> response = webClient.post()
                    .uri("/chat/completions")
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .timeout(Duration.ofSeconds(30))
                    .block();

            // 解析响应
            String aiResponse = extractResponseContent(response);
            logger.debug("DeepSeek API响应: {}", aiResponse);
            
            return aiResponse;

        } catch (WebClientResponseException e) {
            logger.error("DeepSeek API调用失败，状态码: {}, 响应: {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("DeepSeek API调用失败: " + e.getMessage(), e);
        } catch (Exception e) {
            logger.error("DeepSeek服务异常", e);
            throw new RuntimeException("DeepSeek服务异常: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean isServiceAvailable() {
        try {
            // 发送简单的测试请求
            Map<String, Object> testRequest = new java.util.HashMap<>();
            testRequest.put("model", model);
            
            java.util.List<Map<String, String>> testMessages = new java.util.ArrayList<>();
            Map<String, String> testMessage = new java.util.HashMap<>();
            testMessage.put("role", "user");
            testMessage.put("content", "Hello");
            testMessages.add(testMessage);
            
            testRequest.put("messages", testMessages);
            testRequest.put("max_tokens", 10);

            webClient.post()
                    .uri("/chat/completions")
                    .bodyValue(testRequest)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .timeout(Duration.ofSeconds(10))
                    .block();

            return true;
        } catch (Exception e) {
            logger.warn("DeepSeek服务不可用: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 构建消息列表
     */
    private List<Map<String, String>> buildMessages(String systemPrompt, String conversationHistory, String userMessage) {
        List<Map<String, String>> messages = new java.util.ArrayList<>();
        
        // 添加系统提示词
        if (systemPrompt != null && !systemPrompt.trim().isEmpty()) {
            Map<String, String> systemMessage = new java.util.HashMap<>();
            systemMessage.put("role", "system");
            systemMessage.put("content", systemPrompt);
            messages.add(systemMessage);
        }
        
        // 添加对话历史（如果有）
        if (conversationHistory != null && !conversationHistory.trim().isEmpty()) {
            Map<String, String> historyMessage = new java.util.HashMap<>();
            historyMessage.put("role", "assistant");
            historyMessage.put("content", "对话历史: " + conversationHistory);
            messages.add(historyMessage);
        }
        
        // 添加用户消息
        Map<String, String> userMsg = new java.util.HashMap<>();
        userMsg.put("role", "user");
        userMsg.put("content", userMessage);
        messages.add(userMsg);
        
        return messages;
    }

    /**
     * 从API响应中提取内容
     */
    private String extractResponseContent(Map<String, Object> response) {
        try {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> choices = (List<Map<String, Object>>) response.get("choices");
            
            if (choices == null || choices.isEmpty()) {
                throw new RuntimeException("DeepSeek API响应格式错误：choices为空");
            }
            
            @SuppressWarnings("unchecked")
            Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
            
            if (message == null) {
                throw new RuntimeException("DeepSeek API响应格式错误：message为空");
            }
            
            String content = (String) message.get("content");
            
            if (content == null || content.trim().isEmpty()) {
                throw new RuntimeException("DeepSeek API响应内容为空");
            }
            
            return content.trim();
            
        } catch (ClassCastException e) {
            logger.error("DeepSeek API响应格式解析错误", e);
            throw new RuntimeException("DeepSeek API响应格式错误", e);
        }
    }
}