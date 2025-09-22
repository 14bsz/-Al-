package com.aichat.service;

/**
 * 大语言模型服务接口
 * 定义与LLM API交互的核心方法
 */
public interface LLMService {

    /**
     * 与AI进行对话
     * 
     * @param userMessage 用户输入的消息
     * @param systemPrompt 系统提示词（角色设定）
     * @return AI生成的回复文本
     * @throws RuntimeException 当API调用失败时抛出异常
     */
    String chat(String userMessage, String systemPrompt);

    /**
     * 与AI进行对话（带上下文）
     * 
     * @param userMessage 用户输入的消息
     * @param systemPrompt 系统提示词（角色设定）
     * @param conversationHistory 对话历史上下文
     * @return AI生成的回复文本
     * @throws RuntimeException 当API调用失败时抛出异常
     */
    String chatWithContext(String userMessage, String systemPrompt, String conversationHistory);

    /**
     * 检查LLM服务是否可用
     * 
     * @return true表示服务可用，false表示不可用
     */
    boolean isServiceAvailable();
}