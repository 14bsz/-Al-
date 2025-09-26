package com.aichat.entity;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

/**
 * AI角色实体类
 * 存储AI角色的基本信息和配置
 */
@Entity
@Table(name = "ai_characters")
public class AICharacter {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true)
    private String name;
    
    @Column(length = 1000)
    private String description;
    
    @Column(name = "avatar_url")
    private String avatarUrl;
    
    @Column(name = "character_type")
    private String characterType; // 历史人物、虚拟角色、专业导师等
    
    @Column(name = "personality_traits", length = 2000)
    private String personalityTraits; // JSON格式存储性格特征
    
    @Column(name = "background_story", length = 5000)
    private String backgroundStory;
    
    @Column(name = "speaking_style", length = 1000)
    private String speakingStyle;
    
    @Column(name = "knowledge_domains", length = 1000)
    private String knowledgeDomains; // JSON格式存储知识领域
    
    @Column(name = "available_skills", length = 1000)
    private String availableSkills; // JSON格式存储可用技能
    
    @Column(name = "voice_config", length = 500)
    private String voiceConfig; // 语音配置（音色、语速等）
    
    @Column(name = "is_active")
    private Boolean isActive = true;
    
    @Column(name = "popularity_score")
    private Integer popularityScore = 0;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    // Constructors
    public AICharacter() {}
    
    public AICharacter(String name, String description, String characterType) {
        this.name = name;
        this.description = description;
        this.characterType = characterType;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getAvatarUrl() {
        return avatarUrl;
    }
    
    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }
    
    public String getCharacterType() {
        return characterType;
    }
    
    public void setCharacterType(String characterType) {
        this.characterType = characterType;
    }
    
    public String getPersonalityTraits() {
        return personalityTraits;
    }
    
    public void setPersonalityTraits(String personalityTraits) {
        this.personalityTraits = personalityTraits;
    }
    
    public String getBackgroundStory() {
        return backgroundStory;
    }
    
    public void setBackgroundStory(String backgroundStory) {
        this.backgroundStory = backgroundStory;
    }
    
    public String getSpeakingStyle() {
        return speakingStyle;
    }
    
    public void setSpeakingStyle(String speakingStyle) {
        this.speakingStyle = speakingStyle;
    }
    
    public String getKnowledgeDomains() {
        return knowledgeDomains;
    }
    
    public void setKnowledgeDomains(String knowledgeDomains) {
        this.knowledgeDomains = knowledgeDomains;
    }
    
    public String getAvailableSkills() {
        return availableSkills;
    }
    
    public void setAvailableSkills(String availableSkills) {
        this.availableSkills = availableSkills;
    }
    
    public String getVoiceConfig() {
        return voiceConfig;
    }
    
    public void setVoiceConfig(String voiceConfig) {
        this.voiceConfig = voiceConfig;
    }
    
    public Boolean getIsActive() {
        return isActive;
    }
    
    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }
    
    public Integer getPopularityScore() {
        return popularityScore;
    }
    
    public void setPopularityScore(Integer popularityScore) {
        this.popularityScore = popularityScore;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}