package com.aichat.service.impl;

import com.aichat.service.AISkillService;
import com.aichat.service.AISkillService.EmotionAnalysisResult;
import com.aichat.service.AISkillService.KnowledgeAnswerResult;
import com.aichat.service.AISkillService.CreativeWritingResult;
import com.aichat.service.AISkillService.SkillProcessResult;
import com.aichat.service.AIService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Pattern;

/**
 * AI技能服务实现类
 * 实现情感分析、知识问答、创意写作等核心技能
 */
@Service
public class AISkillServiceImpl implements AISkillService {
    
    @Autowired
    private AIService aiService;
    
    // 情感关键词映射
    private static final Map<String, List<String>> EMOTION_KEYWORDS = new HashMap<>();
    
    // 问题类型模式
    private static final Map<String, Pattern> QUESTION_PATTERNS = new HashMap<>();
    
    // 创意写作类型
    private static final Set<String> CREATIVE_WRITING_TRIGGERS = new HashSet<>();
    
    static {
        // 初始化情感关键词
        EMOTION_KEYWORDS.put("快乐", Arrays.asList("开心", "高兴", "快乐", "兴奋", "愉快", "满意", "欣喜", "喜悦"));
        EMOTION_KEYWORDS.put("悲伤", Arrays.asList("难过", "悲伤", "沮丧", "失望", "痛苦", "忧郁", "伤心", "哭"));
        EMOTION_KEYWORDS.put("愤怒", Arrays.asList("生气", "愤怒", "恼火", "烦躁", "气愤", "暴怒", "讨厌", "恨"));
        EMOTION_KEYWORDS.put("恐惧", Arrays.asList("害怕", "恐惧", "担心", "焦虑", "紧张", "不安", "惊慌", "畏惧"));
        EMOTION_KEYWORDS.put("惊讶", Arrays.asList("惊讶", "震惊", "意外", "吃惊", "惊奇", "不敢相信", "没想到"));
        EMOTION_KEYWORDS.put("厌恶", Arrays.asList("厌恶", "恶心", "反感", "讨厌", "嫌弃", "排斥"));
        
        // 初始化问题模式
        QUESTION_PATTERNS.put("what", Pattern.compile(".*?(什么|啥|何为).*?"));
        QUESTION_PATTERNS.put("how", Pattern.compile(".*?(怎么|如何|怎样).*?"));
        QUESTION_PATTERNS.put("why", Pattern.compile(".*?(为什么|为啥|原因).*?"));
        QUESTION_PATTERNS.put("when", Pattern.compile(".*?(什么时候|何时|时间).*?"));
        QUESTION_PATTERNS.put("where", Pattern.compile(".*?(哪里|何处|地方).*?"));
        QUESTION_PATTERNS.put("who", Pattern.compile(".*?(谁|什么人|哪个人).*?"));
        
        // 初始化创意写作触发词
        CREATIVE_WRITING_TRIGGERS.addAll(Arrays.asList(
            "写", "创作", "编", "作诗", "写诗", "故事", "小说", "剧本", "对话", "描述", "想象"
        ));
    }
    
    @Override
    public EmotionAnalysisResult analyzeEmotion(String text, String characterName) {
        if (text == null || text.trim().isEmpty()) {
            return new EmotionAnalysisResult("中性", 0.5, "低", "无明显情感表达", "保持友好对话");
        }
        
        String cleanText = text.toLowerCase();
        Map<String, Integer> emotionScores = new HashMap<>();
        
        // 计算各种情感的得分
        for (Map.Entry<String, List<String>> entry : EMOTION_KEYWORDS.entrySet()) {
            String emotion = entry.getKey();
            List<String> keywords = entry.getValue();
            int score = 0;
            
            for (String keyword : keywords) {
                if (cleanText.contains(keyword)) {
                    score += 2; // 直接匹配得2分
                }
            }
            
            if (score > 0) {
                emotionScores.put(emotion, score);
            }
        }
        
        // 确定主要情感
        String primaryEmotion = "中性";
        int maxScore = 0;
        
        for (Map.Entry<String, Integer> entry : emotionScores.entrySet()) {
            if (entry.getValue() > maxScore) {
                maxScore = entry.getValue();
                primaryEmotion = entry.getKey();
            }
        }
        
        // 计算置信度和强度
        double confidence = Math.min(maxScore * 0.2, 1.0);
        String intensity = maxScore >= 4 ? "高" : (maxScore >= 2 ? "中" : "低");
        
        // 生成情感描述和建议回应
        String description = generateEmotionDescription(primaryEmotion, intensity, characterName);
        String suggestedResponse = generateSuggestedResponse(primaryEmotion, characterName);
        
        return new EmotionAnalysisResult(primaryEmotion, confidence, intensity, description, suggestedResponse);
    }
    
    @Override
    public KnowledgeAnswerResult answerQuestion(String question, String characterName, String context) {
        if (question == null || question.trim().isEmpty()) {
            return new KnowledgeAnswerResult("请提出具体问题，我很乐意为您解答。", 
                "通用回应", 0.8, Arrays.asList("提问技巧"), false);
        }
        
        // 分析问题类型
        String questionType = analyzeQuestionType(question);
        
        // 构建角色专属的知识问答提示
        String prompt = buildKnowledgePrompt(question, characterName, questionType, context);
        
        try {
            // 调用AI服务获取答案
            String answer = aiService.sendMessage(new com.aichat.dto.ChatRequest(1L, 1L, prompt)).getMessage();
            
            // 分析答案质量和相关话题
            double confidence = calculateAnswerConfidence(answer, question);
            List<String> relatedTopics = extractRelatedTopics(answer, questionType);
            boolean needMoreContext = answer.contains("需要更多信息") || answer.contains("不够清楚");
            
            return new KnowledgeAnswerResult(answer, characterName + "的知识库", 
                confidence, relatedTopics, needMoreContext);
            
        } catch (Exception e) {
            return new KnowledgeAnswerResult("抱歉，我现在无法回答这个问题，请稍后再试。", 
                "错误处理", 0.3, Arrays.asList("技术问题"), true);
        }
    }
    
    @Override
    public CreativeWritingResult creativeWriting(String prompt, String characterName, String writingType) {
        if (prompt == null || prompt.trim().isEmpty()) {
            prompt = "请进行自由创作";
        }
        
        if (writingType == null || writingType.trim().isEmpty()) {
            writingType = detectWritingType(prompt);
        }
        
        // 构建创意写作提示
        String creativePrompt = buildCreativePrompt(prompt, characterName, writingType);
        
        try {
            // 调用AI服务进行创作
            String content = aiService.sendMessage(new com.aichat.dto.ChatRequest(1L, 1L, creativePrompt)).getMessage();
            
            // 分析写作风格和主题
            String style = analyzeWritingStyle(content, characterName);
            String theme = extractTheme(content, prompt);
            List<String> keywords = extractKeywords(content);
            String inspiration = generateInspiration(characterName, writingType);
            
            return new CreativeWritingResult(content, style, theme, keywords, inspiration);
            
        } catch (Exception e) {
            String fallbackContent = generateFallbackCreativeContent(characterName, writingType);
            return new CreativeWritingResult(fallbackContent, characterName + "风格", 
                "即兴创作", Arrays.asList("创意", "想象"), "角色特色");
        }
    }
    
    @Override
    public SkillProcessResult processWithSkills(String userInput, String characterName, List<String> conversationHistory) {
        if (userInput == null || userInput.trim().isEmpty()) {
            return new SkillProcessResult("请告诉我您想聊什么？", "通用对话");
        }
        
        // 1. 首先进行情感分析
        EmotionAnalysisResult emotionResult = analyzeEmotion(userInput, characterName);
        
        // 2. 判断用户意图
        String intent = detectUserIntent(userInput);
        
        SkillProcessResult result = new SkillProcessResult();
        result.setEmotionResult(emotionResult);
        
        String response;
        String skillUsed;
        
        switch (intent) {
            case "question":
                // 知识问答
                String context = buildContextFromHistory(conversationHistory);
                KnowledgeAnswerResult knowledgeResult = answerQuestion(userInput, characterName, context);
                result.setKnowledgeResult(knowledgeResult);
                response = enhanceResponseWithEmotion(knowledgeResult.getAnswer(), emotionResult, characterName);
                skillUsed = "知识问答";
                break;
                
            case "creative":
                // 创意写作
                String writingType = detectWritingType(userInput);
                CreativeWritingResult writingResult = creativeWriting(userInput, characterName, writingType);
                result.setWritingResult(writingResult);
                response = enhanceResponseWithEmotion(writingResult.getContent(), emotionResult, characterName);
                skillUsed = "创意写作";
                break;
                
            default:
                // 情感对话
                response = generateEmotionalResponse(userInput, emotionResult, characterName, conversationHistory);
                skillUsed = "情感对话";
                break;
        }
        
        result.setResponse(response);
        result.setSkillUsed(skillUsed);
        
        // 添加元数据
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("userIntent", intent);
        metadata.put("emotionConfidence", emotionResult.getConfidence());
        metadata.put("responseLength", response.length());
        metadata.put("timestamp", System.currentTimeMillis());
        result.setMetadata(metadata);
        
        return result;
    }
    
    // 移除不在接口中的方法 - 已注释掉整个方法
    /*
    public Map<String, Object> getCharacterSkillConfig(String characterName) {
        Map<String, Object> config = new HashMap<>();
        
        switch (characterName) {
            case "哈利波特":
                config.put("specialties", Arrays.asList("魔法知识", "冒险故事", "友谊与勇气"));
                config.put("emotionalStyle", "勇敢乐观");
                config.put("knowledgeDomain", "魔法世界");
                config.put("writingStyle", "冒险奇幻");
                break;
                
            case "苏格拉底":
                config.put("specialties", Arrays.asList("哲学思辨", "逻辑推理", "人生智慧"));
                config.put("emotionalStyle", "理性深刻");
                config.put("knowledgeDomain", "哲学伦理");
                config.put("writingStyle", "思辨对话");
                break;
                
            case "爱因斯坦":
                config.put("specialties", Arrays.asList("科学原理", "创新思维", "宇宙奥秘"));
                config.put("emotionalStyle", "好奇睿智");
                config.put("knowledgeDomain", "物理科学");
                config.put("writingStyle", "科学诗意");
                break;
                
            case "莎士比亚":
                config.put("specialties", Arrays.asList("文学创作", "人性洞察", "戏剧艺术"));
                config.put("emotionalStyle", "浪漫深邃");
                config.put("knowledgeDomain", "文学艺术");
                config.put("writingStyle", "诗意戏剧");
                break;
                
            case "孔子":
                config.put("specialties", Arrays.asList("道德修养", "教育智慧", "人际关系"));
                config.put("emotionalStyle", "温和睿智");
                config.put("knowledgeDomain", "儒家思想");
                config.put("writingStyle", "格言警句");
                break;
                
            case "达芬奇":
                config.put("specialties", Arrays.asList("艺术创作", "科学发明", "跨界思维"));
                config.put("emotionalStyle", "创新好奇");
                config.put("knowledgeDomain", "艺术科学");
                config.put("writingStyle", "创意描述");
                break;
                
            default:
                config.put("specialties", Arrays.asList("通用对话", "情感交流", "知识分享"));
                config.put("emotionalStyle", "友好亲切");
                config.put("knowledgeDomain", "通用知识");
                config.put("writingStyle", "自然对话");
                break;
        }

        return config;
    }
    */
    
    // 私有辅助方法
    
    private String generateEmotionDescription(String emotion, String intensity, String characterName) {
        return String.format("检测到%s强度的%s情感，%s会以理解和共情的方式回应", intensity, emotion, characterName);
    }
    
    private String generateSuggestedResponse(String emotion, String characterName) {
        switch (emotion) {
            case "快乐": return "分享这份快乐，给予积极回应";
            case "悲伤": return "提供安慰和支持，表达理解";
            case "愤怒": return "保持冷静，帮助疏导情绪";
            case "恐惧": return "给予安全感，提供鼓励";
            case "惊讶": return "分享惊喜，提供更多信息";
            case "厌恶": return "理解感受，转移话题";
            default: return "保持友好，自然对话";
        }
    }
    
    private String analyzeQuestionType(String question) {
        for (Map.Entry<String, Pattern> entry : QUESTION_PATTERNS.entrySet()) {
            if (entry.getValue().matcher(question).matches()) {
                return entry.getKey();
            }
        }
        return "general";
    }
    
    private String buildKnowledgePrompt(String question, String characterName, String questionType, String context) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("作为").append(characterName).append("，请回答以下问题：\n");
        prompt.append("问题：").append(question).append("\n");
        
        if (context != null && !context.trim().isEmpty()) {
            prompt.append("对话背景：").append(context).append("\n");
        }
        
        prompt.append("请以").append(characterName).append("的身份、知识背景和说话风格来回答。");
        
        return prompt.toString();
    }
    
    private double calculateAnswerConfidence(String answer, String question) {
        if (answer == null || answer.trim().isEmpty()) return 0.0;
        if (answer.contains("不知道") || answer.contains("不清楚")) return 0.3;
        if (answer.length() < 20) return 0.5;
        if (answer.contains("具体来说") || answer.contains("详细地")) return 0.8;
        return 0.7;
    }
    
    private List<String> extractRelatedTopics(String answer, String questionType) {
        // 简单的关键词提取逻辑
        List<String> topics = new ArrayList<>();
        topics.add(questionType + "类问题");
        
        if (answer.contains("历史")) topics.add("历史");
        if (answer.contains("科学")) topics.add("科学");
        if (answer.contains("哲学")) topics.add("哲学");
        if (answer.contains("艺术")) topics.add("艺术");
        if (answer.contains("文学")) topics.add("文学");
        
        return topics;
    }
    
    private String detectWritingType(String prompt) {
        String lowerPrompt = prompt.toLowerCase();
        
        if (lowerPrompt.contains("诗") || lowerPrompt.contains("诗歌")) return "诗歌";
        if (lowerPrompt.contains("故事") || lowerPrompt.contains("小说")) return "故事";
        if (lowerPrompt.contains("对话") || lowerPrompt.contains("剧本")) return "对话";
        if (lowerPrompt.contains("描述") || lowerPrompt.contains("描写")) return "描述";
        
        return "自由创作";
    }
    
    private String buildCreativePrompt(String prompt, String characterName, String writingType) {
        StringBuilder creativePrompt = new StringBuilder();
        creativePrompt.append("作为").append(characterName).append("，请进行").append(writingType).append("创作：\n");
        creativePrompt.append("创作要求：").append(prompt).append("\n");
        creativePrompt.append("请以").append(characterName).append("的风格和特色来创作。");
        
        return creativePrompt.toString();
    }
    
    private String analyzeWritingStyle(String content, String characterName) {
        // 简化实现，直接返回默认写作风格
        switch (characterName) {
            case "哈利波特":
                return "冒险奇幻";
            case "苏格拉底":
                return "思辨对话";
            case "爱因斯坦":
                return "科学诗意";
            case "莎士比亚":
                return "诗意戏剧";
            case "孔子":
                return "格言警句";
            case "达芬奇":
                return "创意描述";
            default:
                return "自然对话";
        }
    }
    
    private String extractTheme(String content, String prompt) {
        // 简单的主题提取
        if (content.contains("爱") || content.contains("情")) return "爱情";
        if (content.contains("友") || content.contains("朋友")) return "友谊";
        if (content.contains("冒险") || content.contains("探索")) return "冒险";
        if (content.contains("智慧") || content.contains("思考")) return "智慧";
        
        return "人生感悟";
    }
    
    private List<String> extractKeywords(String content) {
        // 简单的关键词提取
        List<String> keywords = new ArrayList<>();
        String[] words = content.split("[，。！？；：\\s]+");
        
        for (String word : words) {
            if (word.length() >= 2 && word.length() <= 4) {
                keywords.add(word);
            }
        }
        
        return keywords.subList(0, Math.min(keywords.size(), 5));
    }
    
    private String generateInspiration(String characterName, String writingType) {
        return characterName + "的" + writingType + "创作灵感";
    }
    
    private String generateFallbackCreativeContent(String characterName, String writingType) {
        return String.format("作为%s，我想与你分享一个关于%s的思考...", characterName, writingType);
    }
    
    private String detectUserIntent(String userInput) {
        String lowerInput = userInput.toLowerCase();
        
        // 检查是否是问题
        for (Pattern pattern : QUESTION_PATTERNS.values()) {
            if (pattern.matcher(lowerInput).matches()) {
                return "question";
            }
        }
        
        // 检查是否是创意写作请求
        for (String trigger : CREATIVE_WRITING_TRIGGERS) {
            if (lowerInput.contains(trigger)) {
                return "creative";
            }
        }
        
        return "conversation";
    }
    
    private String buildContextFromHistory(List<String> conversationHistory) {
        if (conversationHistory == null || conversationHistory.isEmpty()) {
            return "";
        }
        
        // 取最近的3条对话作为上下文
        int start = Math.max(0, conversationHistory.size() - 3);
        return String.join("\n", conversationHistory.subList(start, conversationHistory.size()));
    }
    
    private String enhanceResponseWithEmotion(String baseResponse, EmotionAnalysisResult emotionResult, String characterName) {
        String emotion = emotionResult.getEmotion();
        
        // 根据情感调整回应语调
        switch (emotion) {
            case "悲伤":
                return "我能理解你的感受。" + baseResponse + " 希望这能给你一些安慰。";
            case "愤怒":
                return "我注意到你可能有些不快。" + baseResponse + " 让我们冷静地讨论这个问题。";
            case "快乐":
                return "很高兴看到你的好心情！" + baseResponse + " 让我们继续这个愉快的话题。";
            case "恐惧":
                return "不用担心，我在这里陪伴你。" + baseResponse + " 一切都会好起来的。";
            default:
                return baseResponse;
        }
    }
    
    private String generateEmotionalResponse(String userInput, EmotionAnalysisResult emotionResult, 
                                           String characterName, List<String> conversationHistory) {
        try {
            String emotionalPrompt = String.format(
                "作为%s，用户说：'%s'。用户当前的情感状态是%s（%s强度）。请以理解和共情的方式回应，体现%s的性格特点。",
                characterName, userInput, emotionResult.getEmotion(), 
                emotionResult.getEmotionIntensity(), characterName
            );
            
            return aiService.sendMessage(new com.aichat.dto.ChatRequest(1L, 1L, emotionalPrompt)).getMessage();
        } catch (Exception e) {
            return String.format("作为%s，我很理解你的感受。让我们继续聊聊吧。", characterName);
        }
    }
}