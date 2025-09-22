package com.aichat.entity;

import javax.persistence.*;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * 升级版用户实体类
 * 支持完整的用户管理、角色权限、个人偏好等功能
 */
@Entity
@Table(name = "users_v2", indexes = {
    @Index(name = "idx_user_email", columnList = "email"),
    @Index(name = "idx_user_username", columnList = "username"),
    @Index(name = "idx_user_status", columnList = "status")
})
@EntityListeners(AuditingEntityListener.class)
public class UserUpgrade {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 50, message = "用户名长度必须在3-50个字符之间")
    @Column(unique = true, nullable = false, length = 50)
    private String username;

    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    @Column(unique = true, nullable = false, length = 100)
    private String email;

    @NotBlank(message = "密码不能为空")
    @Column(nullable = false)
    private String password;

    @Size(max = 100, message = "昵称长度不能超过100个字符")
    @Column(length = 100)
    private String nickname;

    @Column(name = "avatar_url", length = 500)
    private String avatarUrl;

    @Size(max = 500, message = "个人简介长度不能超过500个字符")
    @Column(length = 500)
    private String bio;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserStatus status = UserStatus.ACTIVE;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role = UserRole.USER;

    @Column(name = "email_verified")
    private Boolean emailVerified = false;

    @Column(name = "phone_number", length = 20)
    private String phoneNumber;

    @Column(name = "phone_verified")
    private Boolean phoneVerified = false;

    @Column(name = "two_factor_enabled")
    private Boolean twoFactorEnabled = false;

    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    @Column(name = "last_login_ip", length = 45)
    private String lastLoginIp;

    @Column(name = "login_count")
    private Long loginCount = 0L;

    @Column(name = "failed_login_attempts")
    private Integer failedLoginAttempts = 0;

    @Column(name = "locked_until")
    private LocalDateTime lockedUntil;

    // 用户偏好设置
    @Column(name = "preferred_language", length = 10)
    private String preferredLanguage = "zh-CN";

    @Column(name = "theme_preference", length = 20)
    private String themePreference = "auto";

    @Column(name = "notification_enabled")
    private Boolean notificationEnabled = true;

    @Column(name = "email_notification_enabled")
    private Boolean emailNotificationEnabled = true;

    @Column(name = "sound_enabled")
    private Boolean soundEnabled = true;

    // AI相关偏好
    @Column(name = "ai_model_preference", length = 50)
    private String aiModelPreference = "deepseek-chat";

    @Column(name = "conversation_style", length = 20)
    private String conversationStyle = "balanced";

    @Column(name = "max_context_length")
    private Integer maxContextLength = 4000;

    // 统计信息
    @Column(name = "total_conversations")
    private Long totalConversations = 0L;

    @Column(name = "total_messages")
    private Long totalMessages = 0L;

    @Column(name = "total_characters_created")
    private Long totalCharactersCreated = 0L;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // 构造函数
    public UserUpgrade() {}

    public UserUpgrade(String username, String email, String password) {
        this.username = username;
        this.email = email;
        this.password = password;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public UserStatus getStatus() {
        return status;
    }

    public void setStatus(UserStatus status) {
        this.status = status;
    }

    public UserRole getRole() {
        return role;
    }

    public void setRole(UserRole role) {
        this.role = role;
    }

    public Boolean getEmailVerified() {
        return emailVerified;
    }

    public void setEmailVerified(Boolean emailVerified) {
        this.emailVerified = emailVerified;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public Boolean getPhoneVerified() {
        return phoneVerified;
    }

    public void setPhoneVerified(Boolean phoneVerified) {
        this.phoneVerified = phoneVerified;
    }

    public Boolean getTwoFactorEnabled() {
        return twoFactorEnabled;
    }

    public void setTwoFactorEnabled(Boolean twoFactorEnabled) {
        this.twoFactorEnabled = twoFactorEnabled;
    }

    public LocalDateTime getLastLoginAt() {
        return lastLoginAt;
    }

    public void setLastLoginAt(LocalDateTime lastLoginAt) {
        this.lastLoginAt = lastLoginAt;
    }

    public String getLastLoginIp() {
        return lastLoginIp;
    }

    public void setLastLoginIp(String lastLoginIp) {
        this.lastLoginIp = lastLoginIp;
    }

    public Long getLoginCount() {
        return loginCount;
    }

    public void setLoginCount(Long loginCount) {
        this.loginCount = loginCount;
    }

    public Integer getFailedLoginAttempts() {
        return failedLoginAttempts;
    }

    public void setFailedLoginAttempts(Integer failedLoginAttempts) {
        this.failedLoginAttempts = failedLoginAttempts;
    }

    public LocalDateTime getLockedUntil() {
        return lockedUntil;
    }

    public void setLockedUntil(LocalDateTime lockedUntil) {
        this.lockedUntil = lockedUntil;
    }

    public String getPreferredLanguage() {
        return preferredLanguage;
    }

    public void setPreferredLanguage(String preferredLanguage) {
        this.preferredLanguage = preferredLanguage;
    }

    public String getThemePreference() {
        return themePreference;
    }

    public void setThemePreference(String themePreference) {
        this.themePreference = themePreference;
    }

    public Boolean getNotificationEnabled() {
        return notificationEnabled;
    }

    public void setNotificationEnabled(Boolean notificationEnabled) {
        this.notificationEnabled = notificationEnabled;
    }

    public Boolean getEmailNotificationEnabled() {
        return emailNotificationEnabled;
    }

    public void setEmailNotificationEnabled(Boolean emailNotificationEnabled) {
        this.emailNotificationEnabled = emailNotificationEnabled;
    }

    public Boolean getSoundEnabled() {
        return soundEnabled;
    }

    public void setSoundEnabled(Boolean soundEnabled) {
        this.soundEnabled = soundEnabled;
    }

    public String getAiModelPreference() {
        return aiModelPreference;
    }

    public void setAiModelPreference(String aiModelPreference) {
        this.aiModelPreference = aiModelPreference;
    }

    public String getConversationStyle() {
        return conversationStyle;
    }

    public void setConversationStyle(String conversationStyle) {
        this.conversationStyle = conversationStyle;
    }

    public Integer getMaxContextLength() {
        return maxContextLength;
    }

    public void setMaxContextLength(Integer maxContextLength) {
        this.maxContextLength = maxContextLength;
    }

    public Long getTotalConversations() {
        return totalConversations;
    }

    public void setTotalConversations(Long totalConversations) {
        this.totalConversations = totalConversations;
    }

    public Long getTotalMessages() {
        return totalMessages;
    }

    public void setTotalMessages(Long totalMessages) {
        this.totalMessages = totalMessages;
    }

    public Long getTotalCharactersCreated() {
        return totalCharactersCreated;
    }

    public void setTotalCharactersCreated(Long totalCharactersCreated) {
        this.totalCharactersCreated = totalCharactersCreated;
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

    // 业务方法
    public boolean isAccountLocked() {
        return lockedUntil != null && lockedUntil.isAfter(LocalDateTime.now());
    }

    public boolean isActive() {
        return status == UserStatus.ACTIVE;
    }

    public void incrementLoginCount() {
        this.loginCount++;
        this.lastLoginAt = LocalDateTime.now();
        this.failedLoginAttempts = 0;
    }

    public void incrementFailedLoginAttempts() {
        this.failedLoginAttempts++;
        if (this.failedLoginAttempts >= 5) {
            this.lockedUntil = LocalDateTime.now().plusMinutes(30);
        }
    }

    public void resetFailedLoginAttempts() {
        this.failedLoginAttempts = 0;
        this.lockedUntil = null;
    }

    // 用户状态枚举
    public enum UserStatus {
        ACTIVE,
        INACTIVE,
        SUSPENDED,
        DELETED
    }

    // 用户角色枚举
    public enum UserRole {
        USER,
        PREMIUM,
        MODERATOR,
        ADMIN,
        SUPER_ADMIN
    }

    @Override
    public String toString() {
        return "UserUpgrade{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", nickname='" + nickname + '\'' +
                ", status=" + status +
                ", role=" + role +
                ", createdAt=" + createdAt +
                '}';
    }
}