package com.aichat.service;

import java.io.File;

/**
 * 语音服务接口
 * 定义语音转文字(STT)和文字转语音(TTS)的核心方法
 */
public interface SpeechService {

    /**
     * 语音转文字 (Speech-to-Text)
     * 使用Whisper API将音频文件转换为文本
     * 
     * @param audioFile 音频文件（支持mp3, wav, m4a等格式）
     * @return 转换后的文本内容
     * @throws RuntimeException 当API调用失败或文件格式不支持时抛出异常
     */
    String transcribe(File audioFile);

    /**
     * 文字转语音 (Text-to-Speech)
     * 使用TTS API将文本转换为音频数据
     * 
     * @param text 要转换的文本内容
     * @return 音频数据的字节数组
     * @throws RuntimeException 当API调用失败时抛出异常
     */
    byte[] synthesize(String text);

    /**
     * 文字转语音并保存到文件
     * 
     * @param text 要转换的文本内容
     * @param outputFile 输出音频文件
     * @return 生成的音频文件路径
     * @throws RuntimeException 当API调用失败或文件写入失败时抛出异常
     */
    String synthesizeToFile(String text, File outputFile);

    /**
     * 检查语音服务是否可用
     * 
     * @return true表示服务可用，false表示不可用
     */
    boolean isServiceAvailable();

    /**
     * 获取支持的音频格式列表
     * 
     * @return 支持的音频格式数组
     */
    String[] getSupportedAudioFormats();
}