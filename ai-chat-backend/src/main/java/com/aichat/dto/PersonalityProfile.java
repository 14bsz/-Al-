package com.aichat.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 个性化档案DTO
 * 用于用户个性分析和个性化推荐
 */
public class PersonalityProfile {

    private Long userId;
    private String personalityType; // MBTI类型
    private Map<String, Double> traits; // 五大人格特质
    private List<String> interests;
    private List<String> preferences;
    private String communicationStyle;
    private String learningStyle;
    private Map<String, Object> behaviorPatterns;
    private Double extroversionScore;
    private Double opennessScore;
    private Double conscientiousnessScore;
    private Double agreeablenessScore;
    private Double neuroticismScore;
    private String dominantTrait;
    private List<String> recommendedCharacterTypes;
    private Map<String, Object> conversationPreferences;
    private LocalDateTime lastUpdated;
    private Double confidenceScore;

    public PersonalityProfile() {
        this.lastUpdated = LocalDateTime.now();
    }

    public PersonalityProfile(Long userId, String personalityType, Map<String, Double> traits) {
        this();
        this.userId = userId;
        this.personalityType = personalityType;
        this.traits = traits;
    }

    // Getters and Setters
    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getPersonalityType() {
        return personalityType;
    }

    public void setPersonalityType(String personalityType) {
        this.personalityType = personalityType;
    }

    public Map<String, Double> getTraits() {
        return traits;
    }

    public void setTraits(Map<String, Double> traits) {
        this.traits = traits;
    }

    public List<String> getInterests() {
        return interests;
    }

    public void setInterests(List<String> interests) {
        this.interests = interests;
    }

    public List<String> getPreferences() {
        return preferences;
    }

    public void setPreferences(List<String> preferences) {
        this.preferences = preferences;
    }

    public String getCommunicationStyle() {
        return communicationStyle;
    }

    public void setCommunicationStyle(String communicationStyle) {
        this.communicationStyle = communicationStyle;
    }

    public String getLearningStyle() {
        return learningStyle;
    }

    public void setLearningStyle(String learningStyle) {
        this.learningStyle = learningStyle;
    }

    public Map<String, Object> getBehaviorPatterns() {
        return behaviorPatterns;
    }

    public void setBehaviorPatterns(Map<String, Object> behaviorPatterns) {
        this.behaviorPatterns = behaviorPatterns;
    }

    public Double getExtroversionScore() {
        return extroversionScore;
    }

    public void setExtroversionScore(Double extroversionScore) {
        this.extroversionScore = extroversionScore;
    }

    public Double getOpennessScore() {
        return opennessScore;
    }

    public void setOpennessScore(Double opennessScore) {
        this.opennessScore = opennessScore;
    }

    public Double getConscientiousnessScore() {
        return conscientiousnessScore;
    }

    public void setConscientiousnessScore(Double conscientiousnessScore) {
        this.conscientiousnessScore = conscientiousnessScore;
    }

    public Double getAgreeablenessScore() {
        return agreeablenessScore;
    }

    public void setAgreeablenessScore(Double agreeablenessScore) {
        this.agreeablenessScore = agreeablenessScore;
    }

    public Double getNeuroticismScore() {
        return neuroticismScore;
    }

    public void setNeuroticismScore(Double neuroticismScore) {
        this.neuroticismScore = neuroticismScore;
    }

    public String getDominantTrait() {
        return dominantTrait;
    }

    public void setDominantTrait(String dominantTrait) {
        this.dominantTrait = dominantTrait;
    }

    public List<String> getRecommendedCharacterTypes() {
        return recommendedCharacterTypes;
    }

    public void setRecommendedCharacterTypes(List<String> recommendedCharacterTypes) {
        this.recommendedCharacterTypes = recommendedCharacterTypes;
    }

    public Map<String, Object> getConversationPreferences() {
        return conversationPreferences;
    }

    public void setConversationPreferences(Map<String, Object> conversationPreferences) {
        this.conversationPreferences = conversationPreferences;
    }

    public LocalDateTime getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(LocalDateTime lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public Double getConfidenceScore() {
        return confidenceScore;
    }

    public void setConfidenceScore(Double confidenceScore) {
        this.confidenceScore = confidenceScore;
    }

    @Override
    public String toString() {
        return "PersonalityProfile{" +
                "userId=" + userId +
                ", personalityType='" + personalityType + '\'' +
                ", communicationStyle='" + communicationStyle + '\'' +
                ", dominantTrait='" + dominantTrait + '\'' +
                ", confidenceScore=" + confidenceScore +
                ", lastUpdated=" + lastUpdated +
                '}';
    }
}