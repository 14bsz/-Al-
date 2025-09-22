package com.aichat.service;

import com.aichat.dto.ChatRequest;
import com.aichat.dto.ChatResponse;

import java.io.File;

/**
 * 聊天服务接口
 * 协调LLM服务和语音服务，提供统一的聊天功能
 */
public interface ChatService {

    /**
     * 处理文本聊天请求
     * 
     * @param request 聊天请求对象，包含用户ID、角色ID、消息内容等
     * @return 聊天响应对象，包含AI回复和相关信息
     * @throws RuntimeException 当处理过程中发生错误时抛出异常
     */
    ChatResponse processTextChat(ChatRequest request);

    /**
     * 处理语音聊天请求
     * 先将语音转换为文本，然后处理文本聊天，最后可选择性地生成语音回复
     * 
     * @param audioFile 用户上传的音频文件
     * @param request 聊天请求对象（不包含message字段，将从音频中提取）
     * @return 聊天响应对象，包含AI回复文本和可能的音频URL
     * @throws RuntimeException 当处理过程中发生错误时抛出异常
     */
    ChatResponse processVoiceChat(File audioFile, ChatRequest request);

    /**
     * 获取角色的系统提示词
     * 
     * @param characterId 角色ID
     * @return 角色的系统提示词，如果角色不存在则返回默认提示词
     */
    String getCharacterSystemPrompt(Long characterId);

    /**
     * 获取用户与指定角色的对话历史
     * 
     * @param userId 用户ID
     * @param characterId 角色ID
     * @param limit 获取的历史记录数量限制
     * @return 格式化的对话历史字符串
     */
    String getConversationHistory(Long userId, Long characterId, int limit);

    /**
     * 保存对话记录
     * 
     * @param userId 用户ID
     * @param characterId 角色ID
     * @param userMessage 用户消息
     * @param aiResponse AI回复
     * @param sessionId 会话ID
     * @param audioUrl 音频URL（可选）
     */
    void saveConversation(Long userId, Long characterId, String userMessage, 
                         String aiResponse, String sessionId, String audioUrl);

    /**
     * 检查聊天服务是否可用
     * 
     * @return true表示服务可用，false表示不可用
     */
    boolean isServiceAvailable();
}