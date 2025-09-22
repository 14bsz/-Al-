package com.aichat.dto;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 情感分析结果DTO
 * 包含详细的情感识别和分析数据
 */
public class EmotionAnalysisResult {

    private String primaryEmotion;
    private Double confidence;
    private Map<String, Double> emotionScores;
    private String sentiment; // positive, negative, neutral
    private Double sentimentScore;
    private String mood;
    private Integer intensity; // 1-10
    private Map<String, Object> details;
    private LocalDateTime analyzedAt;

    public EmotionAnalysisResult() {
        this.analyzedAt = LocalDateTime.now();
    }

    public EmotionAnalysisResult(String primaryEmotion, Double confidence, 
                               Map<String, Double> emotionScores, String sentiment) {
        this();
        this.primaryEmotion = primaryEmotion;
        this.confidence = confidence;
        this.emotionScores = emotionScores;
        this.sentiment = sentiment;
    }

    // Getters and Setters
    public String getPrimaryEmotion() {
        return primaryEmotion;
    }

    public void setPrimaryEmotion(String primaryEmotion) {
        this.primaryEmotion = primaryEmotion;
    }

    public Double getConfidence() {
        return confidence;
    }

    public void setConfidence(Double confidence) {
        this.confidence = confidence;
    }

    public Map<String, Double> getEmotionScores() {
        return emotionScores;
    }

    public void setEmotionScores(Map<String, Double> emotionScores) {
        this.emotionScores = emotionScores;
    }

    public String getSentiment() {
        return sentiment;
    }

    public void setSentiment(String sentiment) {
        this.sentiment = sentiment;
    }

    public Double getSentimentScore() {
        return sentimentScore;
    }

    public void setSentimentScore(Double sentimentScore) {
        this.sentimentScore = sentimentScore;
    }

    public String getMood() {
        return mood;
    }

    public void setMood(String mood) {
        this.mood = mood;
    }

    public Integer getIntensity() {
        return intensity;
    }

    public void setIntensity(Integer intensity) {
        this.intensity = intensity;
    }

    public Map<String, Object> getDetails() {
        return details;
    }

    public void setDetails(Map<String, Object> details) {
        this.details = details;
    }

    public LocalDateTime getAnalyzedAt() {
        return analyzedAt;
    }

    public void setAnalyzedAt(LocalDateTime analyzedAt) {
        this.analyzedAt = analyzedAt;
    }

    @Override
    public String toString() {
        return "EmotionAnalysisResult{" +
                "primaryEmotion='" + primaryEmotion + '\'' +
                ", confidence=" + confidence +
                ", sentiment='" + sentiment + '\'' +
                ", sentimentScore=" + sentimentScore +
                ", mood='" + mood + '\'' +
                ", intensity=" + intensity +
                ", analyzedAt=" + analyzedAt +
                '}';
    }
}