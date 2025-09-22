package com.aichat.service;

import com.aichat.dto.ChatRequest;
import com.aichat.dto.ChatResponse;
import com.aichat.dto.EmotionAnalysisResult;
import com.aichat.dto.PersonalityProfile;
import com.aichat.entity.ChatCharacter;
import com.aichat.entity.ConversationHistory;
import com.aichat.entity.User;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

/**
 * 高级AI服务接口
 * 支持多模态对话、情感分析、个性化推荐等企业级AI功能
 */
public interface AIService {

    /**
     * 发送聊天消息（同步）
     * @param request 聊天请求
     * @return 聊天响应
     */
    ChatResponse sendMessage(ChatRequest request);

    /**
     * 发送聊天消息（异步流式）
     * @param request 聊天请求
     * @return 流式聊天响应
     */
    Flux<String> sendMessageStream(ChatRequest request);

    /**
     * 多模态对话（支持文本、图片、音频）
     * @param request 多模态请求
     * @return 多模态响应
     */
    Mono<ChatResponse> sendMultiModalMessage(ChatRequest request);

    /**
     * 情感分析
     * @param text 待分析文本
     * @param userId 用户ID
     * @return 情感分析结果
     */
    Mono<EmotionAnalysisResult> analyzeEmotion(String text, Long userId);

    /**
     * 个性化推荐角色
     * @param user 用户信息
     * @param limit 推荐数量
     * @return 推荐角色列表
     */
    Mono<List<ChatCharacter>> recommendCharacters(User user, int limit);

    /**
     * 生成个性化对话风格
     * @param user 用户信息
     * @param character 角色信息
     * @param conversationHistory 对话历史
     * @return 个性化提示词
     */
    Mono<String> generatePersonalizedPrompt(User user, ChatCharacter character, 
                                           List<ConversationHistory> conversationHistory);

    /**
     * 智能角色创建助手
     * @param description 角色描述
     * @param userId 用户ID
     * @return 生成的角色信息
     */
    Mono<ChatCharacter> generateCharacter(String description, Long userId);

    /**
     * 对话质量评估
     * @param conversation 对话内容
     * @return 质量评分和建议
     */
    Mono<Map<String, Object>> evaluateConversationQuality(List<ConversationHistory> conversation);

    /**
     * 智能摘要生成
     * @param conversations 对话列表
     * @return 对话摘要
     */
    Mono<String> generateConversationSummary(List<ConversationHistory> conversations);

    /**
     * 语音合成
     * @param text 文本内容
     * @param voiceId 语音ID
     * @return 音频数据
     */
    Mono<byte[]> synthesizeVoice(String text, String voiceId);

    /**
     * 语音识别
     * @param audioData 音频数据
     * @return 识别的文本
     */
    Mono<String> recognizeVoice(byte[] audioData);

    /**
     * 图像生成
     * @param prompt 生成提示词
     * @param style 图像风格
     * @return 图像URL
     */
    Mono<String> generateImage(String prompt, String style);

    /**
     * 图像分析
     * @param imageUrl 图像URL
     * @return 图像描述
     */
    Mono<String> analyzeImage(String imageUrl);

    /**
     * 智能翻译
     * @param text 待翻译文本
     * @param targetLanguage 目标语言
     * @return 翻译结果
     */
    Mono<String> translateText(String text, String targetLanguage);

    /**
     * 内容安全检测
     * @param content 待检测内容
     * @return 安全检测结果
     */
    Mono<Map<String, Object>> moderateContent(String content);

    /**
     * 用户行为分析
     * @param userId 用户ID
     * @param timeRange 时间范围
     * @return 行为分析报告
     */
    Mono<Map<String, Object>> analyzeUserBehavior(Long userId, String timeRange);

    /**
     * 智能标签生成
     * @param content 内容
     * @return 标签列表
     */
    Mono<List<String>> generateTags(String content);

    /**
     * 对话上下文管理
     * @param conversationId 对话ID
     * @param maxTokens 最大token数
     * @return 优化后的上下文
     */
    Mono<String> optimizeContext(Long conversationId, int maxTokens);

    /**
     * AI模型切换
     * @param modelName 模型名称
     * @param userId 用户ID
     * @return 切换结果
     */
    Mono<Boolean> switchModel(String modelName, Long userId);

    /**
     * 获取AI模型状态
     * @return 模型状态信息
     */
    Mono<Map<String, Object>> getModelStatus();

    /**
     * 批量处理消息
     * @param requests 请求列表
     * @return 响应列表
     */
    Flux<ChatResponse> batchProcessMessages(List<ChatRequest> requests);
}