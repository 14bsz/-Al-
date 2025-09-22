package com.aichat.service;

import java.util.Map;
import java.util.List;

/**
 * AI技能服务接口
 * 定义AI角色应具备的核心技能：情感分析、知识问答、创意写作
 */
public interface AISkillService {
    
    /**
     * 情感分析技能
     * 分析用户输入的情感倾向和情绪状态
     * 
     * @param text 用户输入文本
     * @param characterName 角色名称
     * @return 情感分析结果
     */
    EmotionAnalysisResult analyzeEmotion(String text, String characterName);
    
    /**
     * 知识问答技能
     * 基于角色背景和专业知识回答用户问题
     * 
     * @param question 用户问题
     * @param characterName 角色名称
     * @param context 对话上下文
     * @return 知识问答结果
     */
    KnowledgeAnswerResult answerQuestion(String question, String characterName, String context);
    
    /**
     * 创意写作技能
     * 根据用户需求进行创意写作
     * 
     * @param prompt 写作提示
     * @param characterName 角色名称
     * @param writingType 写作类型（诗歌、故事、对话等）
     * @return 创意写作结果
     */
    CreativeWritingResult creativeWriting(String prompt, String characterName, String writingType);
    
    /**
     * 综合技能处理
     * 根据用户输入自动判断并调用相应技能
     * 
     * @param userInput 用户输入
     * @param characterName 角色名称
     * @param conversationHistory 对话历史
     * @return 综合处理结果
     */
    SkillProcessResult processWithSkills(String userInput, String characterName, List<String> conversationHistory);
    
    /**
     * 获取角色专属技能配置
     * 
     * @param characterName 角色名称
     * @return 技能配置信息
     */
    Map<String, Object> getCharacterSkillConfig(String characterName);
    
    /**
     * 情感分析结果类
     */
    class EmotionAnalysisResult {
        private String emotion;           // 主要情感（快乐、悲伤、愤怒、恐惧、惊讶、厌恶、中性）
        private double confidence;        // 置信度 (0-1)
        private String emotionIntensity;  // 情感强度（低、中、高）
        private String emotionDescription; // 情感描述
        private String suggestedResponse; // 建议回应方式
        
        public EmotionAnalysisResult() {}
        
        public EmotionAnalysisResult(String emotion, double confidence, String emotionIntensity, 
                                   String emotionDescription, String suggestedResponse) {
            this.emotion = emotion;
            this.confidence = confidence;
            this.emotionIntensity = emotionIntensity;
            this.emotionDescription = emotionDescription;
            this.suggestedResponse = suggestedResponse;
        }
        
        // Getters and Setters
        public String getEmotion() { return emotion; }
        public void setEmotion(String emotion) { this.emotion = emotion; }
        
        public double getConfidence() { return confidence; }
        public void setConfidence(double confidence) { this.confidence = confidence; }
        
        public String getEmotionIntensity() { return emotionIntensity; }
        public void setEmotionIntensity(String emotionIntensity) { this.emotionIntensity = emotionIntensity; }
        
        public String getEmotionDescription() { return emotionDescription; }
        public void setEmotionDescription(String emotionDescription) { this.emotionDescription = emotionDescription; }
        
        public String getSuggestedResponse() { return suggestedResponse; }
        public void setSuggestedResponse(String suggestedResponse) { this.suggestedResponse = suggestedResponse; }
    }
    
    /**
     * 知识问答结果类
     */
    class KnowledgeAnswerResult {
        private String answer;            // 答案内容
        private String knowledgeSource;   // 知识来源
        private double confidence;        // 答案置信度
        private List<String> relatedTopics; // 相关话题
        private boolean needMoreContext;  // 是否需要更多上下文
        
        public KnowledgeAnswerResult() {}
        
        public KnowledgeAnswerResult(String answer, String knowledgeSource, double confidence, 
                                   List<String> relatedTopics, boolean needMoreContext) {
            this.answer = answer;
            this.knowledgeSource = knowledgeSource;
            this.confidence = confidence;
            this.relatedTopics = relatedTopics;
            this.needMoreContext = needMoreContext;
        }
        
        // Getters and Setters
        public String getAnswer() { return answer; }
        public void setAnswer(String answer) { this.answer = answer; }
        
        public String getKnowledgeSource() { return knowledgeSource; }
        public void setKnowledgeSource(String knowledgeSource) { this.knowledgeSource = knowledgeSource; }
        
        public double getConfidence() { return confidence; }
        public void setConfidence(double confidence) { this.confidence = confidence; }
        
        public List<String> getRelatedTopics() { return relatedTopics; }
        public void setRelatedTopics(List<String> relatedTopics) { this.relatedTopics = relatedTopics; }
        
        public boolean isNeedMoreContext() { return needMoreContext; }
        public void setNeedMoreContext(boolean needMoreContext) { this.needMoreContext = needMoreContext; }
    }
    
    /**
     * 创意写作结果类
     */
    class CreativeWritingResult {
        private String content;           // 写作内容
        private String writingStyle;      // 写作风格
        private String theme;            // 主题
        private List<String> keywords;   // 关键词
        private String inspiration;      // 灵感来源
        
        public CreativeWritingResult() {}
        
        public CreativeWritingResult(String content, String writingStyle, String theme, 
                                   List<String> keywords, String inspiration) {
            this.content = content;
            this.writingStyle = writingStyle;
            this.theme = theme;
            this.keywords = keywords;
            this.inspiration = inspiration;
        }
        
        // Getters and Setters
        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
        
        public String getWritingStyle() { return writingStyle; }
        public void setWritingStyle(String writingStyle) { this.writingStyle = writingStyle; }
        
        public String getTheme() { return theme; }
        public void setTheme(String theme) { this.theme = theme; }
        
        public List<String> getKeywords() { return keywords; }
        public void setKeywords(List<String> keywords) { this.keywords = keywords; }
        
        public String getInspiration() { return inspiration; }
        public void setInspiration(String inspiration) { this.inspiration = inspiration; }
    }
    
    /**
     * 综合技能处理结果类
     */
    class SkillProcessResult {
        private String response;          // 最终回应
        private String skillUsed;         // 使用的技能类型
        private EmotionAnalysisResult emotionResult;     // 情感分析结果
        private KnowledgeAnswerResult knowledgeResult;   // 知识问答结果
        private CreativeWritingResult writingResult;     // 创意写作结果
        private Map<String, Object> metadata; // 元数据
        
        public SkillProcessResult() {}
        
        public SkillProcessResult(String response, String skillUsed) {
            this.response = response;
            this.skillUsed = skillUsed;
        }
        
        // Getters and Setters
        public String getResponse() { return response; }
        public void setResponse(String response) { this.response = response; }
        
        public String getSkillUsed() { return skillUsed; }
        public void setSkillUsed(String skillUsed) { this.skillUsed = skillUsed; }
        
        public EmotionAnalysisResult getEmotionResult() { return emotionResult; }
        public void setEmotionResult(EmotionAnalysisResult emotionResult) { this.emotionResult = emotionResult; }
        
        public KnowledgeAnswerResult getKnowledgeResult() { return knowledgeResult; }
        public void setKnowledgeResult(KnowledgeAnswerResult knowledgeResult) { this.knowledgeResult = knowledgeResult; }
        
        public CreativeWritingResult getWritingResult() { return writingResult; }
        public void setWritingResult(CreativeWritingResult writingResult) { this.writingResult = writingResult; }
        
        public Map<String, Object> getMetadata() { return metadata; }
        public void setMetadata(Map<String, Object> metadata) { this.metadata = metadata; }
    }
}