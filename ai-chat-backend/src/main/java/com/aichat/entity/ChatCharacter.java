package com.aichat.entity;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;

/**
 * AI角色实体类
 * 存储AI角色的基本信息、系统提示词、技能配置等
 */
@Entity
@Table(name = "chat_characters")
public class ChatCharacter {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "角色名称不能为空")
    @Size(max = 100, message = "角色名称长度不能超过100个字符")
    @Column(nullable = false, length = 100)
    private String name;

    @Size(max = 500, message = "角色描述长度不能超过500个字符")
    @Column(length = 500)
    private String description;

    @NotBlank(message = "系统提示词不能为空")
    @Column(name = "system_prompt", nullable = false, columnDefinition = "TEXT")
    private String systemPrompt;

    @Column(name = "background_prompt", columnDefinition = "TEXT")
    private String backgroundPrompt;

    @Column(name = "avatar_url", length = 255)
    private String avatarUrl;

    @Size(max = 200, message = "性格特征描述长度不能超过200个字符")
    @Column(length = 200)
    private String personality;

    @Column(name = "skills", columnDefinition = "TEXT")
    private String skills;

    @Column(name = "personality_config", columnDefinition = "TEXT")
    private String personalityConfig;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // 无参构造函数
    public ChatCharacter() {
    }

    // 有参构造函数
    public ChatCharacter(String name, String description, String systemPrompt) {
        this.name = name;
        this.description = description;
        this.systemPrompt = systemPrompt;
        this.isActive = true;
    }

    // 完整构造函数
    public ChatCharacter(String name, String description, String systemPrompt, 
                        String avatarUrl, String personality, String skills) {
        this(name, description, systemPrompt);
        this.avatarUrl = avatarUrl;
        this.personality = personality;
        this.skills = skills;
    }

    // 带背景提示词的构造函数
    public ChatCharacter(String name, String description, String systemPrompt, String backgroundPrompt,
                        String avatarUrl, String personality, String skills) {
        this(name, description, systemPrompt, avatarUrl, personality, skills);
        this.backgroundPrompt = backgroundPrompt;
    }

    // Getter和Setter方法
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

    public String getSystemPrompt() {
        return systemPrompt;
    }

    public void setSystemPrompt(String systemPrompt) {
        this.systemPrompt = systemPrompt;
    }

    public String getBackgroundPrompt() {
        return backgroundPrompt;
    }

    public void setBackgroundPrompt(String backgroundPrompt) {
        this.backgroundPrompt = backgroundPrompt;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public String getPersonality() {
        return personality;
    }

    public void setPersonality(String personality) {
        this.personality = personality;
    }

    public String getSkills() {
        return skills;
    }

    public void setSkills(String skills) {
        this.skills = skills;
    }

    public String getPersonalityConfig() {
        return personalityConfig;
    }

    public void setPersonalityConfig(String personalityConfig) {
        this.personalityConfig = personalityConfig;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
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

    @Override
    public String toString() {
        return "ChatCharacter{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", personality='" + personality + '\'' +
                ", isActive=" + isActive +
                '}';
    }
}