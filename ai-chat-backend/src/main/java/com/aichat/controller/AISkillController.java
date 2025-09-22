package com.aichat.controller;

import com.aichat.service.AISkillService;
import com.aichat.service.AISkillService.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * AI技能控制器
 * 提供情感分析、知识问答、创意写作等技能的REST API
 */
@RestController
@RequestMapping("/api/ai-skills")
@CrossOrigin(origins = "*")
public class AISkillController {
    
    @Autowired
    private AISkillService aiSkillService;
    
    /**
     * 情感分析接口
     */
    @PostMapping("/emotion-analysis")
    public ResponseEntity<EmotionAnalysisResult> analyzeEmotion(@RequestBody EmotionAnalysisRequest request) {
        try {
            EmotionAnalysisResult result = aiSkillService.analyzeEmotion(
                request.getText(), 
                request.getCharacterName()
            );
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * 知识问答接口
     */
    @PostMapping("/knowledge-qa")
    public ResponseEntity<KnowledgeAnswerResult> answerQuestion(@RequestBody KnowledgeQARequest request) {
        try {
            KnowledgeAnswerResult result = aiSkillService.answerQuestion(
                request.getQuestion(),
                request.getCharacterName(),
                request.getContext()
            );
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * 创意写作接口
     */
    @PostMapping("/creative-writing")
    public ResponseEntity<CreativeWritingResult> creativeWriting(@RequestBody CreativeWritingRequest request) {
        try {
            CreativeWritingResult result = aiSkillService.creativeWriting(
                request.getPrompt(),
                request.getCharacterName(),
                request.getWritingType()
            );
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * 综合技能处理接口
     */
    @PostMapping("/process-with-skills")
    public ResponseEntity<SkillProcessResult> processWithSkills(@RequestBody SkillProcessRequest request) {
        try {
            SkillProcessResult result = aiSkillService.processWithSkills(
                request.getUserInput(),
                request.getCharacterName(),
                request.getConversationHistory()
            );
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * 获取角色技能配置接口
     */
    @GetMapping("/character-config/{characterName}")
    public ResponseEntity<Map<String, Object>> getCharacterSkillConfig(@PathVariable String characterName) {
        try {
            // 返回默认技能配置
        java.util.HashMap<String, Object> config = new java.util.HashMap<>();
        config.put("characterName", characterName);
        config.put("availableSkills", java.util.Arrays.asList("emotion_analysis", "knowledge_qa", "creative_writing"));
        config.put("defaultSkill", "emotion_analysis");
        return ResponseEntity.ok(config);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * 获取所有可用技能列表
     */
    @GetMapping("/available-skills")
    public ResponseEntity<Map<String, Object>> getAvailableSkills() {
        try {
            Map<String, Object> skills = new HashMap<>();
            
            Map<String, Object> emotionAnalysis = new HashMap<>();
            emotionAnalysis.put("name", "情感分析");
            emotionAnalysis.put("description", "分析用户输入的情感倾向和情绪状态");
            emotionAnalysis.put("supportedEmotions", Arrays.asList("快乐", "悲伤", "愤怒", "恐惧", "惊讶", "厌恶", "中性"));
            skills.put("emotionAnalysis", emotionAnalysis);
            
            Map<String, Object> knowledgeQA = new HashMap<>();
            knowledgeQA.put("name", "知识问答");
            knowledgeQA.put("description", "基于角色背景和专业知识回答用户问题");
            knowledgeQA.put("questionTypes", Arrays.asList("what", "how", "why", "when", "where", "who", "general"));
            skills.put("knowledgeQA", knowledgeQA);
            
            Map<String, Object> creativeWriting = new HashMap<>();
            creativeWriting.put("name", "创意写作");
            creativeWriting.put("description", "根据用户需求进行创意写作");
            creativeWriting.put("writingTypes", Arrays.asList("诗歌", "故事", "对话", "描述", "自由创作"));
            skills.put("creativeWriting", creativeWriting);
            return ResponseEntity.ok(skills);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    // 请求DTO类
    
    /**
     * 情感分析请求
     */
    public static class EmotionAnalysisRequest {
        private String text;
        private String characterName;
        
        public EmotionAnalysisRequest() {}
        
        public EmotionAnalysisRequest(String text, String characterName) {
            this.text = text;
            this.characterName = characterName;
        }
        
        public String getText() { return text; }
        public void setText(String text) { this.text = text; }
        
        public String getCharacterName() { return characterName; }
        public void setCharacterName(String characterName) { this.characterName = characterName; }
    }
    
    /**
     * 知识问答请求
     */
    public static class KnowledgeQARequest {
        private String question;
        private String characterName;
        private String context;
        
        public KnowledgeQARequest() {}
        
        public KnowledgeQARequest(String question, String characterName, String context) {
            this.question = question;
            this.characterName = characterName;
            this.context = context;
        }
        
        public String getQuestion() { return question; }
        public void setQuestion(String question) { this.question = question; }
        
        public String getCharacterName() { return characterName; }
        public void setCharacterName(String characterName) { this.characterName = characterName; }
        
        public String getContext() { return context; }
        public void setContext(String context) { this.context = context; }
    }
    
    /**
     * 创意写作请求
     */
    public static class CreativeWritingRequest {
        private String prompt;
        private String characterName;
        private String writingType;
        
        public CreativeWritingRequest() {}
        
        public CreativeWritingRequest(String prompt, String characterName, String writingType) {
            this.prompt = prompt;
            this.characterName = characterName;
            this.writingType = writingType;
        }
        
        public String getPrompt() { return prompt; }
        public void setPrompt(String prompt) { this.prompt = prompt; }
        
        public String getCharacterName() { return characterName; }
        public void setCharacterName(String characterName) { this.characterName = characterName; }
        
        public String getWritingType() { return writingType; }
        public void setWritingType(String writingType) { this.writingType = writingType; }
    }
    
    /**
     * 综合技能处理请求
     */
    public static class SkillProcessRequest {
        private String userInput;
        private String characterName;
        private List<String> conversationHistory;
        
        public SkillProcessRequest() {}
        
        public SkillProcessRequest(String userInput, String characterName, List<String> conversationHistory) {
            this.userInput = userInput;
            this.characterName = characterName;
            this.conversationHistory = conversationHistory;
        }
        
        public String getUserInput() { return userInput; }
        public void setUserInput(String userInput) { this.userInput = userInput; }
        
        public String getCharacterName() { return characterName; }
        public void setCharacterName(String characterName) { this.characterName = characterName; }
        
        public List<String> getConversationHistory() { return conversationHistory; }
        public void setConversationHistory(List<String> conversationHistory) { this.conversationHistory = conversationHistory; }
    }
}