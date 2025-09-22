package com.aichat.entity;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

/**
 * 对话历史实体类
 * 存储用户与AI角色的对话记录
 */
@Entity
@Table(name = "conversation_history")
public class ConversationHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "用户ID不能为空")
    @Column(name = "user_id", nullable = false)
    private Long userId;

    @NotNull(message = "角色ID不能为空")
    @Column(name = "character_id", nullable = false)
    private Long characterId;

    @Column(name = "session_id", length = 100)
    private String sessionId;

    @NotBlank(message = "用户消息不能为空")
    @Column(name = "user_message", nullable = false, columnDefinition = "TEXT")
    private String userMessage;

    @NotBlank(message = "AI回复不能为空")
    @Column(name = "ai_response", nullable = false, columnDefinition = "TEXT")
    private String aiResponse;

    @Column(name = "audio_url", length = 500)
    private String audioUrl;

    @Column(name = "language", length = 10)
    private String language;

    @Column(name = "emotion", length = 50)
    private String emotion;

    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;

    @Column(name = "metadata", columnDefinition = "TEXT")
    private String metadata;

    // 无参构造函数
    public ConversationHistory() {
        this.timestamp = LocalDateTime.now();
    }

    // 基础构造函数
    public ConversationHistory(Long userId, Long characterId, String userMessage, String aiResponse) {
        this();
        this.userId = userId;
        this.characterId = characterId;
        this.userMessage = userMessage;
        this.aiResponse = aiResponse;
        this.language = "zh-CN";
    }

    // 完整构造函数
    public ConversationHistory(Long userId, Long characterId, String sessionId,
                             String userMessage, String aiResponse, String audioUrl, String metadata) {
        this(userId, characterId, userMessage, aiResponse);
        this.sessionId = sessionId;
        this.audioUrl = audioUrl;
        this.metadata = metadata;
    }

    // 带语言参数的构造函数
    public ConversationHistory(Long userId, Long characterId, String sessionId,
                             String userMessage, String aiResponse, String audioUrl, 
                             String language, String metadata) {
        this(userId, characterId, sessionId, userMessage, aiResponse, audioUrl, metadata);
        this.language = language != null ? language : "zh-CN";
    }

    // JPA生命周期回调
    @PrePersist
    protected void onCreate() {
        timestamp = LocalDateTime.now();
    }

    // Getter和Setter方法
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getUserMessage() {
        return userMessage;
    }

    public void setUserMessage(String userMessage) {
        this.userMessage = userMessage;
    }

    public String getAiResponse() {
        return aiResponse;
    }

    public void setAiResponse(String aiResponse) {
        this.aiResponse = aiResponse;
    }

    public String getAudioUrl() {
        return audioUrl;
    }

    public void setAudioUrl(String audioUrl) {
        this.audioUrl = audioUrl;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language != null ? language : "zh-CN";
    }

    public String getEmotion() {
        return emotion;
    }

    public void setEmotion(String emotion) {
        this.emotion = emotion;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getMetadata() {
        return metadata;
    }

    public void setMetadata(String metadata) {
        this.metadata = metadata;
    }

    @Override
    public String toString() {
        return "ConversationHistory{" +
                "id=" + id +
                ", userId=" + userId +
                ", characterId=" + characterId +
                ", sessionId='" + sessionId + '\'' +
                ", userMessage='" + userMessage + '\'' +
                ", aiResponse='" + aiResponse + '\'' +
                ", language='" + language + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }
}