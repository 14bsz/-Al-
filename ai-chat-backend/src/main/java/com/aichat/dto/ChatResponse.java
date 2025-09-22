package com.aichat.dto;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 聊天响应DTO
 * 用于向前端返回AI角色的回复数据
 */
public class ChatResponse {

    private Long characterId;
    
    private String characterName;
    
    private String message;
    
    private String audioUrl;
    
    private LocalDateTime timestamp;
    
    private String sessionId;
    
    // 响应状态
    private ResponseStatus status;
    
    // 错误信息（如果有）
    private String errorMessage;
    
    // 元数据（如情感分析结果、技能状态等）
    private Map<String, Object> metadata;

    // AI模型信息
    private String model;

    // 使用的token数量
    private Integer tokensUsed;

    // 响应状态枚举
    public enum ResponseStatus {
        SUCCESS,    // 成功
        ERROR,      // 错误
        PROCESSING  // 处理中
    }

    // 无参构造函数
    public ChatResponse() {
        this.timestamp = LocalDateTime.now();
        this.status = ResponseStatus.SUCCESS;
    }

    // 基础构造函数
    public ChatResponse(Long characterId, String message) {
        this();
        this.characterId = characterId;
        this.message = message;
    }

    // 完整构造函数
    public ChatResponse(Long characterId, String characterName, String message, 
                       String audioUrl, String sessionId, Map<String, Object> metadata) {
        this();
        this.characterId = characterId;
        this.characterName = characterName;
        this.message = message;
        this.audioUrl = audioUrl;
        this.sessionId = sessionId;
        this.metadata = metadata;
    }

    // 错误响应构造函数
    public ChatResponse(ResponseStatus status, String errorMessage) {
        this();
        this.status = status;
        this.errorMessage = errorMessage;
    }

    // 静态工厂方法 - 成功响应
    public static ChatResponse success(Long characterId, String characterName, String message) {
        return new ChatResponse(characterId, characterName, message, null, null, null);
    }

    // 静态工厂方法 - 成功响应（带音频URL和会话ID）
    public static ChatResponse success(Long characterId, String characterName, String message, 
                                     String audioUrl, String sessionId) {
        return new ChatResponse(characterId, characterName, message, audioUrl, sessionId, null);
    }

    // 静态工厂方法 - 错误响应
    public static ChatResponse error(String errorMessage) {
        return new ChatResponse(ResponseStatus.ERROR, errorMessage);
    }

    // 静态工厂方法 - 处理中响应
    public static ChatResponse processing(String sessionId) {
        ChatResponse response = new ChatResponse();
        response.setStatus(ResponseStatus.PROCESSING);
        response.setSessionId(sessionId);
        return response;
    }

    // Getter和Setter方法
    public Long getCharacterId() {
        return characterId;
    }

    public void setCharacterId(Long characterId) {
        this.characterId = characterId;
    }

    public String getCharacterName() {
        return characterName;
    }

    public void setCharacterName(String characterName) {
        this.characterName = characterName;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getAudioUrl() {
        return audioUrl;
    }

    public void setAudioUrl(String audioUrl) {
        this.audioUrl = audioUrl;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public ResponseStatus getStatus() {
        return status;
    }

    public void setStatus(ResponseStatus status) {
        this.status = status;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }

    // 添加元数据的便捷方法
    public void addMetadata(String key, Object value) {
        if (this.metadata == null) {
            this.metadata = new java.util.HashMap<>();
        }
        this.metadata.put(key, value);
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public Integer getTokensUsed() {
        return tokensUsed;
    }

    public void setTokensUsed(Integer tokensUsed) {
        this.tokensUsed = tokensUsed;
    }

    @Override
    public String toString() {
        return "ChatResponse{" +
                "characterId=" + characterId +
                ", characterName='" + characterName + '\'' +
                ", message='" + message + '\'' +
                ", audioUrl='" + audioUrl + '\'' +
                ", timestamp=" + timestamp +
                ", sessionId='" + sessionId + '\'' +
                ", status=" + status +
                ", errorMessage='" + errorMessage + '\'' +
                ", model='" + model + '\'' +
                ", tokensUsed=" + tokensUsed +
                '}';
    }
}