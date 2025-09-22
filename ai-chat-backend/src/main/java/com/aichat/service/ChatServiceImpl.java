package com.aichat.service;

import com.aichat.dto.ChatRequest;
import com.aichat.dto.ChatResponse;
import com.aichat.entity.ChatCharacter;
import com.aichat.entity.ConversationHistory;
import com.aichat.entity.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 聊天服务实现类
 * 协调LLM服务和语音服务，提供完整的聊天功能
 */
@Service
@Transactional
public class ChatServiceImpl implements ChatService {

    private static final Logger logger = LoggerFactory.getLogger(ChatServiceImpl.class);

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private LLMService llmService;

    @Autowired
    private SpeechService speechService;

    @Value("${app.audio.upload-dir:uploads/audio}")
    private String audioUploadDir;

    @Value("${app.audio.base-url:http://localhost:8080/api/audio}")
    private String audioBaseUrl;

    @Value("${app.chat.enable-tts:true}")
    private boolean enableTTS;

    @Value("${app.chat.history-limit:10}")
    private int historyLimit;

    private static final String DEFAULT_SYSTEM_PROMPT = "你是一个友善、有帮助的AI助手。请用简洁、准确的方式回答用户的问题。";

    // 情绪标签正则表达式
    private static final Pattern EMOTION_PATTERN = Pattern.compile("\\[EMOTION:\\s*(\\w+)\\]");
    
    // 多语言切换命令正则表达式
    private static final Pattern LANGUAGE_SWITCH_PATTERN = Pattern.compile(
        "(?i)(我们以后用|switch\\s+to|use|speak\\s+in|让我们用)\\s*(英语|english|中文|chinese|日语|japanese|韩语|korean|法语|french|德语|german|西班牙语|spanish)\\s*(对话|conversation|chat)?");
    
    // 语言映射
    private static final java.util.Map<String, String> LANGUAGE_MAP;
    static {
        java.util.Map<String, String> map = new java.util.HashMap<>();
        map.put("英语", "en-US");
        map.put("english", "en-US");
        map.put("中文", "zh-CN");
        map.put("chinese", "zh-CN");
        map.put("日语", "ja-JP");
        map.put("japanese", "ja-JP");
        map.put("韩语", "ko-KR");
        map.put("korean", "ko-KR");
        map.put("法语", "fr-FR");
        map.put("french", "fr-FR");
        map.put("德语", "de-DE");
        map.put("german", "de-DE");
        map.put("西班牙语", "es-ES");
        map.put("spanish", "es-ES");
        LANGUAGE_MAP = java.util.Collections.unmodifiableMap(map);
    }

    @Override
    public ChatResponse processTextChat(ChatRequest request) {
        try {
            logger.info("处理文本聊天请求，用户ID: {}, 角色ID: {}", request.getUserId(), request.getCharacterId());

            // 验证请求参数
            validateChatRequest(request);

            // 检查是否为语言切换命令
            String languageSwitch = checkLanguageSwitch(request.getMessage());
            if (languageSwitch != null) {
                // 更新会话语言设置
                updateSessionLanguage(request.getUserId(), request.getCharacterId(), languageSwitch);
                
                // 返回确认消息
                String confirmMessage = getLanguageConfirmMessage(languageSwitch);
                
                // 保存对话记录
                saveConversation(
                    request.getUserId(),
                    request.getCharacterId(),
                    request.getMessage(),
                    confirmMessage,
                    request.getSessionId(),
                    null
                );
                
                return ChatResponse.success(
                    request.getCharacterId(),
                    getCharacterName(request.getCharacterId()),
                    confirmMessage,
                    null,
                    request.getSessionId()
                );
            }

            // 获取当前会话语言
            String currentLanguage = getCurrentSessionLanguage(request.getUserId(), request.getCharacterId());

            // 获取角色系统提示词（动态生成）
            String systemPrompt = buildDynamicSystemPrompt(request.getCharacterId(), currentLanguage);

            // 获取对话历史
            String conversationHistory = getConversationHistory(
                request.getUserId(), 
                request.getCharacterId(), 
                historyLimit
            );

            // 调用LLM生成回复
            String aiResponse = llmService.chatWithContext(
                request.getMessage(), 
                systemPrompt, 
                conversationHistory
            );

            // 提取情绪标签
            String emotion = extractEmotion(aiResponse);
            String cleanedResponse = removeEmotionTags(aiResponse);

            // 生成音频（如果启用TTS）
            String audioUrl = null;
            if (enableTTS) {
                try {
                    audioUrl = generateAudioResponseWithEmotion(cleanedResponse, emotion);
                } catch (Exception e) {
                    logger.warn("生成音频回复失败，继续返回文本回复", e);
                }
            }

            // 保存对话记录（保存清理后的回复）
            saveConversationWithLanguage(
                request.getUserId(),
                request.getCharacterId(),
                request.getMessage(),
                cleanedResponse,
                request.getSessionId(),
                audioUrl,
                currentLanguage
            );

            // 构建响应
            ChatResponse response = ChatResponse.success(
                request.getCharacterId(),
                getCharacterName(request.getCharacterId()),
                cleanedResponse,
                audioUrl,
                request.getSessionId()
            );

            // 添加情绪信息到元数据
            if (emotion != null) {
                response.addMetadata("emotion", emotion);
            }

            logger.info("文本聊天处理完成，响应长度: {}, 情绪: {}", cleanedResponse.length(), emotion);
            return response;

        } catch (Exception e) {
            logger.error("处理文本聊天请求失败", e);
            return ChatResponse.error("处理聊天请求失败: " + e.getMessage());
        }
    }

    @Override
    public ChatResponse processVoiceChat(File audioFile, ChatRequest request) {
        try {
            logger.info("处理语音聊天请求，用户ID: {}, 角色ID: {}, 音频文件: {}", 
                       request.getUserId(), request.getCharacterId(), audioFile.getName());

            // 验证音频文件
            if (audioFile == null || !audioFile.exists()) {
                throw new IllegalArgumentException("音频文件不存在");
            }

            // 语音转文字
            String transcribedText = speechService.transcribe(audioFile);
            logger.debug("语音转文字结果: {}", transcribedText);

            // 更新请求对象的消息内容
            ChatRequest textRequest = new ChatRequest();
            textRequest.setUserId(request.getUserId());
            textRequest.setCharacterId(request.getCharacterId());
            textRequest.setMessage(transcribedText);
            textRequest.setSessionId(request.getSessionId());

            // 处理文本聊天
            ChatResponse response = processTextChat(textRequest);

            // 添加转录文本到响应元数据
            response.addMetadata("transcribed_text", transcribedText);
            response.addMetadata("original_audio_file", audioFile.getName());

            logger.info("语音聊天处理完成");
            return response;

        } catch (Exception e) {
            logger.error("处理语音聊天请求失败", e);
            return ChatResponse.error("处理语音聊天请求失败: " + e.getMessage());
        }
    }

    @Override
    public String getCharacterSystemPrompt(Long characterId) {
        try {
            Optional<ChatCharacter> character = Optional.ofNullable(
                entityManager.find(ChatCharacter.class, characterId)
            );

            if (character.isPresent()) {
                String systemPrompt = character.get().getSystemPrompt();
                return systemPrompt != null && !systemPrompt.trim().isEmpty() 
                    ? systemPrompt : DEFAULT_SYSTEM_PROMPT;
            } else {
                logger.warn("角色ID {} 不存在，使用默认系统提示词", characterId);
                return DEFAULT_SYSTEM_PROMPT;
            }
        } catch (Exception e) {
            logger.error("获取角色系统提示词失败，角色ID: {}", characterId, e);
            return DEFAULT_SYSTEM_PROMPT;
        }
    }

    @Override
    public String getConversationHistory(Long userId, Long characterId, int limit) {
        try {
            TypedQuery<ConversationHistory> query = entityManager.createQuery(
                "SELECT ch FROM ConversationHistory ch " +
                "WHERE ch.userId = :userId AND ch.characterId = :characterId " +
                "ORDER BY ch.timestamp DESC",
                ConversationHistory.class
            );
            
            query.setParameter("userId", userId);
            query.setParameter("characterId", characterId);
            query.setMaxResults(limit);

            List<ConversationHistory> histories = query.getResultList();

            if (histories.isEmpty()) {
                return "";
            }

            // 构建对话历史字符串（按时间正序）
            StringBuilder historyBuilder = new StringBuilder();
            for (int i = histories.size() - 1; i >= 0; i--) {
                ConversationHistory history = histories.get(i);
                historyBuilder.append("用户: ").append(history.getUserMessage()).append("\n");
                historyBuilder.append("助手: ").append(history.getAiResponse()).append("\n");
            }

            return historyBuilder.toString().trim();

        } catch (Exception e) {
            logger.error("获取对话历史失败，用户ID: {}, 角色ID: {}", userId, characterId, e);
            return "";
        }
    }

    @Override
    public void saveConversation(Long userId, Long characterId, String userMessage, 
                                String aiResponse, String sessionId, String audioUrl) {
        try {
            // 创建对话记录
            ConversationHistory conversation = new ConversationHistory();
            conversation.setUserId(userId);
            conversation.setCharacterId(characterId);
            conversation.setSessionId(sessionId);
            conversation.setUserMessage(userMessage);
            conversation.setAiResponse(aiResponse);
            conversation.setAudioUrl(audioUrl);
            conversation.setLanguage("zh-CN");
            
            entityManager.persist(conversation);
            entityManager.flush();
            
            logger.debug("对话记录已保存，会话ID: {}", sessionId);

        } catch (Exception e) {
            logger.error("保存对话记录失败", e);
            throw new RuntimeException("保存对话记录失败: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean isServiceAvailable() {
        try {
            return llmService.isServiceAvailable() && speechService.isServiceAvailable();
        } catch (Exception e) {
            logger.error("检查服务可用性失败", e);
            return false;
        }
    }

    /**
     * 验证聊天请求参数
     */
    private void validateChatRequest(ChatRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("聊天请求不能为空");
        }
        if (request.getUserId() == null) {
            throw new IllegalArgumentException("用户ID不能为空");
        }
        if (request.getCharacterId() == null) {
            throw new IllegalArgumentException("角色ID不能为空");
        }
        if (request.getMessage() == null || request.getMessage().trim().isEmpty()) {
            throw new IllegalArgumentException("消息内容不能为空");
        }
        if (request.getSessionId() == null || request.getSessionId().trim().isEmpty()) {
            throw new IllegalArgumentException("会话ID不能为空");
        }

        // 验证用户是否存在
        User user = entityManager.find(User.class, request.getUserId());
        if (user == null) {
            throw new IllegalArgumentException("用户不存在，ID: " + request.getUserId());
        }

        // 验证角色是否存在
        ChatCharacter character = entityManager.find(ChatCharacter.class, request.getCharacterId());
        if (character == null) {
            throw new IllegalArgumentException("角色不存在，ID: " + request.getCharacterId());
        }
    }

    /**
     * 获取角色名称
     */
    private String getCharacterName(Long characterId) {
        try {
            ChatCharacter character = entityManager.find(ChatCharacter.class, characterId);
            return character != null ? character.getName() : "AI助手";
        } catch (Exception e) {
            logger.warn("获取角色名称失败，角色ID: {}", characterId, e);
            return "AI助手";
        }
    }

    /**
     * 生成音频回复
     */
    private String generateAudioResponse(String text) throws IOException {
        return generateAudioResponseWithEmotion(text, null);
    }

    /**
     * 生成带情绪的音频回复
     */
    private String generateAudioResponseWithEmotion(String text, String emotion) throws IOException {
        // 生成唯一的音频文件名
        String fileName = UUID.randomUUID().toString() + ".mp3";
        Path audioDir = Paths.get(audioUploadDir);
        
        // 确保目录存在
        if (!Files.exists(audioDir)) {
            Files.createDirectories(audioDir);
        }
        
        File audioFile = audioDir.resolve(fileName).toFile();
        
        // 生成音频文件（传递情绪参数）
        if (speechService instanceof com.aichat.service.OpenAISpeechService) {
            com.aichat.service.OpenAISpeechService openAIService = (com.aichat.service.OpenAISpeechService) speechService;
            openAIService.synthesizeWithEmotion(text, audioFile, emotion);
        } else {
            speechService.synthesizeToFile(text, audioFile);
        }
        
        // 返回音频URL
        return audioBaseUrl + "/" + fileName;
    }

    /**
     * 检查是否为语言切换命令
     */
    private String checkLanguageSwitch(String message) {
        Matcher matcher = LANGUAGE_SWITCH_PATTERN.matcher(message.toLowerCase());
        if (matcher.find()) {
            String language = matcher.group(2).toLowerCase();
            return LANGUAGE_MAP.get(language);
        }
        return null;
    }

    /**
     * 更新会话语言设置
     */
    private void updateSessionLanguage(Long userId, Long characterId, String language) {
        try {
            // 更新该用户与该角色的最新对话记录的语言设置
            TypedQuery<ConversationHistory> query = entityManager.createQuery(
                "SELECT ch FROM ConversationHistory ch " +
                "WHERE ch.userId = :userId AND ch.characterId = :characterId " +
                "ORDER BY ch.timestamp DESC",
                ConversationHistory.class
            );
            
            query.setParameter("userId", userId);
            query.setParameter("characterId", characterId);
            query.setMaxResults(1);

            List<ConversationHistory> histories = query.getResultList();
            if (!histories.isEmpty()) {
                ConversationHistory latest = histories.get(0);
                latest.setLanguage(language);
                entityManager.merge(latest);
            }
            
            logger.info("会话语言已更新为: {}, 用户ID: {}, 角色ID: {}", language, userId, characterId);
        } catch (Exception e) {
            logger.error("更新会话语言失败", e);
        }
    }

    /**
     * 获取当前会话语言
     */
    private String getCurrentSessionLanguage(Long userId, Long characterId) {
        try {
            TypedQuery<ConversationHistory> query = entityManager.createQuery(
                "SELECT ch FROM ConversationHistory ch " +
                "WHERE ch.userId = :userId AND ch.characterId = :characterId " +
                "AND ch.language IS NOT NULL " +
                "ORDER BY ch.timestamp DESC",
                ConversationHistory.class
            );
            
            query.setParameter("userId", userId);
            query.setParameter("characterId", characterId);
            query.setMaxResults(1);

            List<ConversationHistory> histories = query.getResultList();
            if (!histories.isEmpty()) {
                return histories.get(0).getLanguage();
            }
        } catch (Exception e) {
            logger.error("获取当前会话语言失败", e);
        }
        return "zh-CN"; // 默认中文
    }

    /**
     * 获取语言确认消息
     */
    private String getLanguageConfirmMessage(String language) {
        switch (language) {
            case "en-US":
                return "Okay, I will respond in English from now on.";
            case "ja-JP":
                return "わかりました。これからは日本語で回答します。";
            case "ko-KR":
                return "알겠습니다. 이제부터 한국어로 대답하겠습니다.";
            case "fr-FR":
                return "D'accord, je vais répondre en français à partir de maintenant.";
            case "de-DE":
                return "Verstanden, ich werde ab jetzt auf Deutsch antworten.";
            case "es-ES":
                return "Entendido, responderé en español a partir de ahora.";
            default:
                return "好的，我们将开始用中文对话。";
        }
    }

    /**
     * 构建动态系统提示词
     */
    private String buildDynamicSystemPrompt(Long characterId, String language) {
        try {
            ChatCharacter character = entityManager.find(ChatCharacter.class, characterId);
            if (character == null) {
                return DEFAULT_SYSTEM_PROMPT;
            }

            StringBuilder promptBuilder = new StringBuilder();
            
            // 使用背景提示词（如果存在）
            if (character.getBackgroundPrompt() != null && !character.getBackgroundPrompt().trim().isEmpty()) {
                promptBuilder.append(character.getBackgroundPrompt());
            } else {
                promptBuilder.append(character.getSystemPrompt() != null ? 
                    character.getSystemPrompt() : DEFAULT_SYSTEM_PROMPT);
            }

            // 添加情绪标签指令
            promptBuilder.append("\n\n重要指令：");
            promptBuilder.append("\n1. 在每次回复的末尾，请根据你的情绪状态添加情绪标签，格式为 [EMOTION: 情绪名称]");
            promptBuilder.append("\n   可用情绪：HAPPY(开心), SAD(悲伤), EXCITED(兴奋), CALM(平静), ANGRY(愤怒), SURPRISED(惊讶), THOUGHTFUL(深思)");

            // 添加多语言指令
            if (!"zh-CN".equals(language)) {
                promptBuilder.append("\n2. 无论用户使用什么语言，你必须只用").append(getLanguageName(language)).append("回复。");
            }

            return promptBuilder.toString();

        } catch (Exception e) {
            logger.error("构建动态系统提示词失败", e);
            return DEFAULT_SYSTEM_PROMPT;
        }
    }

    /**
     * 获取语言名称
     */
    private String getLanguageName(String languageCode) {
        switch (languageCode) {
            case "en-US": return "英语";
            case "ja-JP": return "日语";
            case "ko-KR": return "韩语";
            case "fr-FR": return "法语";
            case "de-DE": return "德语";
            case "es-ES": return "西班牙语";
            default: return "中文";
        }
    }

    /**
     * 提取情绪标签
     */
    private String extractEmotion(String text) {
        Matcher matcher = EMOTION_PATTERN.matcher(text);
        if (matcher.find()) {
            return matcher.group(1).toUpperCase();
        }
        return null;
    }

    /**
     * 移除情绪标签
     */
    private String removeEmotionTags(String text) {
        return EMOTION_PATTERN.matcher(text).replaceAll("").trim();
    }

    /**
     * 保存带语言信息的对话记录
     */
    private void saveConversationWithLanguage(Long userId, Long characterId, String userMessage, 
                                            String aiResponse, String sessionId, String audioUrl, String language) {
        try {
            // 创建对话记录
            ConversationHistory conversation = new ConversationHistory();
            conversation.setUserId(userId);
            conversation.setCharacterId(characterId);
            conversation.setSessionId(sessionId);
            conversation.setUserMessage(userMessage);
            conversation.setAiResponse(aiResponse);
            conversation.setAudioUrl(audioUrl);
            conversation.setLanguage(language);
            
            entityManager.persist(conversation);
            entityManager.flush();
            
            logger.debug("对话记录已保存，会话ID: {}, 语言: {}", sessionId, language);

        } catch (Exception e) {
            logger.error("保存对话记录失败", e);
            throw new RuntimeException("保存对话记录失败: " + e.getMessage(), e);
        }
    }
}