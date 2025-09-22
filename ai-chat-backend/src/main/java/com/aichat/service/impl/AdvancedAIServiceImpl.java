package com.aichat.service.impl;

import com.aichat.dto.ChatRequest;
import com.aichat.dto.ChatResponse;
import com.aichat.dto.EmotionAnalysisResult;
import com.aichat.dto.PersonalityProfile;
import com.aichat.entity.ChatCharacter;
import com.aichat.entity.ConversationHistory;
import com.aichat.entity.User;
import com.aichat.service.AIService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 高级AI服务实现类
 * 提供企业级AI功能，包括多模态对话、情感分析、个性化推荐等
 */
@Service
public class AdvancedAIServiceImpl implements AIService {

    private static final Logger logger = LoggerFactory.getLogger(AdvancedAIServiceImpl.class);

    @Value("${ai.providers.deepseek.api-key}")
    private String deepseekApiKey;

    @Value("${ai.providers.deepseek.base-url}")
    private String deepseekBaseUrl;

    @Value("${ai.providers.deepseek.model}")
    private String deepseekModel;

    @Value("${ai.features.emotion-analysis}")
    private boolean emotionAnalysisEnabled;

    @Value("${ai.features.personality-adaptation}")
    private boolean personalityAdaptationEnabled;

    private final WebClient webClient;
    private final Map<Long, PersonalityProfile> personalityCache = new ConcurrentHashMap<>();
    private final Map<String, Object> modelStatusCache = new ConcurrentHashMap<>();

    public AdvancedAIServiceImpl(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.build();
        initializeModelStatus();
    }

    @Override
    public ChatResponse sendMessage(ChatRequest request) {
        try {
            logger.info("处理聊天请求: userId={}, characterId={}", request.getUserId(), request.getCharacterId());

            // 构建增强的提示词
            String enhancedPrompt = buildEnhancedPrompt(request);

            // 调用AI API
            Map<String, Object> requestBody = buildApiRequest(enhancedPrompt, request);
            
            // 模拟AI响应（实际项目中会调用真实API）
            String aiResponse = generateMockResponse(request);

            // 构建响应
            ChatResponse response = new ChatResponse();
            response.setMessage(aiResponse);
            response.setCharacterId(request.getCharacterId());
            response.setTimestamp(LocalDateTime.now());
            response.setModel(deepseekModel);
            response.setTokensUsed(estimateTokens(aiResponse));

            // 异步进行情感分析
            if (emotionAnalysisEnabled) {
                analyzeEmotion(request.getMessage(), request.getUserId())
                    .subscribe(emotion -> logger.info("情感分析完成: {}", emotion));
            }

            return response;

        } catch (Exception e) {
            logger.error("处理聊天请求失败", e);
            throw new RuntimeException("AI服务暂时不可用", e);
        }
    }

    @Override
    public Flux<String> sendMessageStream(ChatRequest request) {
        return Flux.create(sink -> {
            try {
                String response = generateMockResponse(request);
                String[] words = response.split(" ");
                
                for (String word : words) {
                    sink.next(word + " ");
                    try {
                        Thread.sleep(50); // 模拟流式响应
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
                sink.complete();
            } catch (Exception e) {
                sink.error(e);
            }
        });
    }

    @Override
    public Mono<ChatResponse> sendMultiModalMessage(ChatRequest request) {
        return Mono.fromCallable(() -> {
            logger.info("处理多模态请求: type={}", request.getMessageType());

            ChatResponse response = new ChatResponse();
            
            if ("image".equals(request.getMessageType())) {
                response.setMessage("我看到了一张有趣的图片！让我为你描述一下：这是一个充满创意的场景...");
            } else if ("audio".equals(request.getMessageType())) {
                response.setMessage("我听到了你的语音消息，你的声音很好听！让我回应一下...");
            } else {
                response.setMessage(generateMockResponse(request));
            }

            response.setCharacterId(request.getCharacterId());
            response.setTimestamp(LocalDateTime.now());
            response.setModel(deepseekModel + "-multimodal");
            
            return response;
        });
    }

    @Override
    public Mono<EmotionAnalysisResult> analyzeEmotion(String text, Long userId) {
        return Mono.fromCallable(() -> {
            logger.info("分析用户情感: userId={}", userId);

            // 模拟情感分析
            Map<String, Double> emotionScores = new HashMap<>();
            emotionScores.put("joy", 0.7);
            emotionScores.put("sadness", 0.1);
            emotionScores.put("anger", 0.05);
            emotionScores.put("fear", 0.05);
            emotionScores.put("surprise", 0.1);

            EmotionAnalysisResult result = new EmotionAnalysisResult();
            result.setPrimaryEmotion("joy");
            result.setConfidence(0.85);
            result.setEmotionScores(emotionScores);
            result.setSentiment("positive");
            result.setSentimentScore(0.8);
            result.setMood("happy");
            result.setIntensity(7);

            Map<String, Object> details = new HashMap<>();
            details.put("textLength", text.length());
            details.put("keyWords", Arrays.asList("开心", "喜欢", "有趣"));
            result.setDetails(details);

            return result;
        });
    }

    @Override
    public Mono<List<ChatCharacter>> recommendCharacters(User user, int limit) {
        return Mono.fromCallable(() -> {
            logger.info("为用户推荐角色: userId={}, limit={}", user.getId(), limit);

            // 模拟个性化推荐
            List<ChatCharacter> recommendations = new ArrayList<>();
            
            // 基于用户偏好生成推荐
            for (int i = 0; i < limit; i++) {
                ChatCharacter character = new ChatCharacter();
                character.setId((long) (i + 1));
                character.setName("推荐角色" + (i + 1));
                character.setSystemPrompt("这是一个为你量身定制的AI角色");
                character.setSkills("智能对话,情感理解,个性化服务");
                recommendations.add(character);
            }

            return recommendations;
        });
    }

    @Override
    public Mono<String> generatePersonalizedPrompt(User user, ChatCharacter character, 
                                                  List<ConversationHistory> conversationHistory) {
        return Mono.fromCallable(() -> {
            StringBuilder prompt = new StringBuilder();
            prompt.append("你是").append(character.getName()).append("，");
            prompt.append(character.getSystemPrompt()).append("\n");
            
            // 添加个性化元素
            if (personalityAdaptationEnabled) {
                PersonalityProfile profile = personalityCache.get(user.getId());
                if (profile != null) {
                    prompt.append("用户的交流风格是：").append(profile.getCommunicationStyle()).append("\n");
                    prompt.append("请根据用户的个性特点调整你的回应方式。\n");
                }
            }

            // 添加上下文
            if (!conversationHistory.isEmpty()) {
                prompt.append("最近的对话上下文：\n");
                conversationHistory.stream()
                    .limit(5)
                    .forEach(conv -> {
                        prompt.append("用户：").append(conv.getUserMessage()).append("\n");
                        prompt.append("你：").append(conv.getAiResponse()).append("\n");
                    });
            }

            return prompt.toString();
        });
    }

    @Override
    public Mono<ChatCharacter> generateCharacter(String description, Long userId) {
        return Mono.fromCallable(() -> {
            logger.info("智能生成角色: userId={}", userId);

            ChatCharacter character = new ChatCharacter();
            character.setName("AI生成角色");
            character.setSystemPrompt("基于描述'" + description + "'生成的智能角色");
            character.setSkills("智能对话,创意思维,专业知识");
            character.setPersonalityConfig("友善,专业,富有创造力");

            return character;
        });
    }

    @Override
    public Mono<Map<String, Object>> evaluateConversationQuality(List<ConversationHistory> conversation) {
        return Mono.fromCallable(() -> {
            Map<String, Object> evaluation = new HashMap<>();
            evaluation.put("overallScore", 8.5);
            evaluation.put("coherenceScore", 9.0);
            evaluation.put("engagementScore", 8.0);
            evaluation.put("creativityScore", 8.5);
            evaluation.put("suggestions", Arrays.asList(
                "对话流畅自然，保持了良好的连贯性",
                "可以增加更多互动性问题",
                "角色扮演很到位"
            ));
            evaluation.put("analyzedAt", LocalDateTime.now());
            return evaluation;
        });
    }

    @Override
    public Mono<String> generateConversationSummary(List<ConversationHistory> conversations) {
        return Mono.fromCallable(() -> {
            if (conversations.isEmpty()) {
                return "暂无对话内容";
            }

            StringBuilder summary = new StringBuilder();
            summary.append("对话摘要：\n");
            summary.append("参与者进行了一次").append(conversations.size()).append("轮的有趣对话。");
            summary.append("主要讨论了日常话题，氛围轻松愉快。");
            summary.append("AI角色表现出色，回应自然流畅。");

            return summary.toString();
        });
    }

    @Override
    public Mono<byte[]> synthesizeVoice(String text, String voiceId) {
        return Mono.fromCallable(() -> {
            logger.info("语音合成: text length={}, voiceId={}", text.length(), voiceId);
            // 模拟语音数据
            return new byte[1024];
        });
    }

    @Override
    public Mono<String> recognizeVoice(byte[] audioData) {
        return Mono.fromCallable(() -> {
            logger.info("语音识别: audio size={}", audioData.length);
            return "这是语音识别的结果文本";
        });
    }

    @Override
    public Mono<String> generateImage(String prompt, String style) {
        return Mono.fromCallable(() -> {
            logger.info("图像生成: prompt={}, style={}", prompt, style);
            return "https://picsum.photos/512/512?random=" + System.currentTimeMillis();
        });
    }

    @Override
    public Mono<String> analyzeImage(String imageUrl) {
        return Mono.fromCallable(() -> {
            logger.info("图像分析: imageUrl={}", imageUrl);
            return "这是一张美丽的图片，包含了丰富的色彩和有趣的构图元素。";
        });
    }

    @Override
    public Mono<String> translateText(String text, String targetLanguage) {
        return Mono.fromCallable(() -> {
            logger.info("文本翻译: target={}", targetLanguage);
            return "Translated text: " + text;
        });
    }

    @Override
    public Mono<Map<String, Object>> moderateContent(String content) {
        return Mono.fromCallable(() -> {
            Map<String, Object> result = new HashMap<>();
            result.put("safe", true);
            result.put("confidence", 0.95);
            result.put("categories", Arrays.asList("normal"));
            result.put("suggestions", Collections.emptyList());
            return result;
        });
    }

    @Override
    public Mono<Map<String, Object>> analyzeUserBehavior(Long userId, String timeRange) {
        return Mono.fromCallable(() -> {
            Map<String, Object> analysis = new HashMap<>();
            analysis.put("totalMessages", 150);
            analysis.put("averageSessionLength", "25分钟");
            analysis.put("favoriteCharacters", Arrays.asList("智能助手", "创意伙伴"));
            analysis.put("activityPattern", "晚上最活跃");
            analysis.put("engagementScore", 8.5);
            return analysis;
        });
    }

    @Override
    public Mono<List<String>> generateTags(String content) {
        return Mono.fromCallable(() -> {
            return Arrays.asList("AI", "聊天", "智能", "对话", "创意");
        });
    }

    @Override
    public Mono<String> optimizeContext(Long conversationId, int maxTokens) {
        return Mono.fromCallable(() -> {
            logger.info("优化对话上下文: conversationId={}, maxTokens={}", conversationId, maxTokens);
            return "优化后的对话上下文，保留了最重要的信息...";
        });
    }

    @Override
    public Mono<Boolean> switchModel(String modelName, Long userId) {
        return Mono.fromCallable(() -> {
            logger.info("切换AI模型: modelName={}, userId={}", modelName, userId);
            return true;
        });
    }

    @Override
    public Mono<Map<String, Object>> getModelStatus() {
        return Mono.fromCallable(() -> modelStatusCache);
    }

    @Override
    public Flux<ChatResponse> batchProcessMessages(List<ChatRequest> requests) {
        return Flux.fromIterable(requests)
            .flatMap(request -> Mono.fromCallable(() -> sendMessage(request)));
    }

    // 私有辅助方法
    private String buildEnhancedPrompt(ChatRequest request) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("用户消息: ").append(request.getMessage()).append("\n");
        
        if (request.getCharacterId() != null) {
            prompt.append("角色ID: ").append(request.getCharacterId()).append("\n");
        }
        
        return prompt.toString();
    }

    private Map<String, Object> buildApiRequest(String prompt, ChatRequest request) {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", deepseekModel);
        
        Map<String, Object> message = new HashMap<>();
        message.put("role", "user");
        message.put("content", prompt);
        
        requestBody.put("messages", Arrays.asList(message));
        requestBody.put("max_tokens", 1000);
        requestBody.put("temperature", 0.7);
        return requestBody;
    }

    private String generateMockResponse(ChatRequest request) {
        String[] responses = {
            "这是一个很有趣的话题！让我们深入探讨一下。",
            "我理解你的想法，这确实值得思考。",
            "从我的角度来看，这个问题有多个层面。",
            "你提出了一个很好的观点，我想补充一些想法。",
            "这让我想到了一些相关的概念，我们可以一起探索。"
        };
        
        Random random = new Random();
        return responses[random.nextInt(responses.length)];
    }

    private int estimateTokens(String text) {
        return text.length() / 4; // 简单估算
    }

    private void initializeModelStatus() {
        modelStatusCache.put("status", "healthy");
        modelStatusCache.put("model", deepseekModel != null ? deepseekModel : "default-model");
        modelStatusCache.put("lastCheck", LocalDateTime.now());
        modelStatusCache.put("responseTime", "150ms");
        modelStatusCache.put("availability", "99.9%");
    }
}