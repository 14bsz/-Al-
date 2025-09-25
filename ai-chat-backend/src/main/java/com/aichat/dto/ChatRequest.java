package com.aichat.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 聊天请求DTO
 * 用于接收前端发送的聊天请求数据
 */
public class ChatRequest {

    @NotNull(message = "用户ID不能为空")
    private Long userId;

    @NotNull(message = "角色ID不能为空")
    private Long characterId;

    @NotBlank(message = "消息内容不能为空")
    private String message;

    private String sessionId;

    // 语言设置（用于多语言技能）
    private String language;

    // 消息类型（用于WebSocket消息分类）
    private String messageType;

    // 是否需要语音回复
    private boolean needVoiceResponse = false;

    // AI技能类型
    private String skillType;

    // 上下文信息
    private String context;

    // 音频文件路径（用于语音聊天）
    private String audioPath;

    // 无参构造函数
    public ChatRequest() {
    }

    // 基础构造函数
    public ChatRequest(Long userId, Long characterId, String message) {
        this.userId = userId;
        this.characterId = characterId;
        this.message = message;
    }

    // 完整构造函数
    public ChatRequest(Long userId, Long characterId, String message, String sessionId, String language) {
        this.userId = userId;
        this.characterId = characterId;
        this.message = message;
        this.sessionId = sessionId;
        this.language = language;
    }

    // Getter和Setter方法
    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getCharacterId() {
        return characterId;
    }

    public void setCharacterId(Long characterId) {
        this.characterId = characterId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getMessageType() {
        return messageType;
    }

    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }

    public boolean isNeedVoiceResponse() {
        return needVoiceResponse;
    }

    public void setNeedVoiceResponse(boolean needVoiceResponse) {
        this.needVoiceResponse = needVoiceResponse;
    }

    public String getSkillType() {
        return skillType;
    }

    public void setSkillType(String skillType) {
        this.skillType = skillType;
    }

    public String getContext() {
        return context;
    }

    public void setContext(String context) {
        this.context = context;
    }

    public String getAudioPath() {
        return audioPath;
    }

    public void setAudioPath(String audioPath) {
        this.audioPath = audioPath;
    }

    /**
     * 验证请求参数的有效性
     */
    public boolean isValid() {
        return userId != null && characterId != null && 
               (message != null && !message.trim().isEmpty() || audioPath != null);
    }

    /**
     * 是否为语音请求
     */
    public boolean isVoiceRequest() {
        return audioPath != null && !audioPath.trim().isEmpty();
    }

    @Override
    public String toString() {
        return "ChatRequest{" +
                "userId=" + userId +
                ", characterId=" + characterId +
                ", message='" + message + '\'' +
                ", sessionId='" + sessionId + '\'' +
                ", language='" + language + '\'' +
                ", messageType='" + messageType + '\'' +
                '}';
    }
}