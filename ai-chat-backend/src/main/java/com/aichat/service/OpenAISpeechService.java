package com.aichat.service;

import com.aichat.service.SpeechService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.springframework.util.MultiValueMap;
import org.springframework.util.LinkedMultiValueMap;

/**
 * OpenAI语音服务实现类
 * 集成OpenAI Whisper STT和TTS API
 */
@Service
public class OpenAISpeechService implements SpeechService {

    private static final Logger logger = LoggerFactory.getLogger(OpenAISpeechService.class);

    private final WebClient webClient;
    private final String ttsModel;
    private final String ttsVoice;
    private final String whisperModel;

    private static final String[] SUPPORTED_FORMATS = {"mp3", "mp4", "mpeg", "mpga", "m4a", "wav", "webm"};
    
    // 情绪到语音风格的映射
    private static final Map<String, VoiceConfig> EMOTION_VOICE_MAP;
    static {
        Map<String, VoiceConfig> map = new java.util.HashMap<>();
        map.put("HAPPY", new VoiceConfig("alloy", 1.2, 1.1));      // 快语速，高音调
        map.put("EXCITED", new VoiceConfig("nova", 1.3, 1.2));     // 更快语速，更高音调
        map.put("CALM", new VoiceConfig("echo", 0.8, 0.9));        // 慢语速，低音调
        map.put("SAD", new VoiceConfig("fable", 0.7, 0.8));        // 很慢语速，很低音调
        map.put("ANGRY", new VoiceConfig("onyx", 1.1, 0.9));       // 中等语速，低音调
        map.put("SURPRISED", new VoiceConfig("shimmer", 1.2, 1.1)); // 快语速，高音调
        map.put("THOUGHTFUL", new VoiceConfig("echo", 0.9, 0.95));   // 慢语速，中等音调
        EMOTION_VOICE_MAP = java.util.Collections.unmodifiableMap(map);
    }
    
    // 语音配置内部类
    private static class VoiceConfig {
        final String voice;
        final double speed;
        final double pitch;
        
        VoiceConfig(String voice, double speed, double pitch) {
            this.voice = voice;
            this.speed = speed;
            this.pitch = pitch;
        }
    }

    public OpenAISpeechService(@Value("${api.openai.base-url}") String baseUrl,
                              @Value("${api.openai.key}") String apiKey,
                              @Value("${api.openai.tts-model:tts-1}") String ttsModel,
                              @Value("${api.openai.tts-voice:alloy}") String ttsVoice,
                              @Value("${api.openai.whisper-model:whisper-1}") String whisperModel) {
        this.ttsModel = ttsModel;
        this.ttsVoice = ttsVoice;
        this.whisperModel = whisperModel;
        
        this.webClient = WebClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader("Authorization", "Bearer " + apiKey)
                .build();
        
        logger.info("OpenAI语音服务初始化完成，TTS模型: {}, 语音: {}, Whisper模型: {}", 
                   ttsModel, ttsVoice, whisperModel);
    }

    @Override
    public String transcribe(File audioFile) {
        if (audioFile == null || !audioFile.exists()) {
            throw new IllegalArgumentException("音频文件不存在或为空");
        }

        try {
            // 构建multipart请求
            MultiValueMap<String, Object> parts = new LinkedMultiValueMap<>();
            parts.add("file", new FileSystemResource(audioFile));
            parts.add("model", whisperModel);
            parts.add("response_format", "json");

            logger.debug("发送Whisper API请求，文件: {}", audioFile.getName());

            // 发送API请求
            Map<String, Object> response = webClient.post()
                    .uri("/audio/transcriptions")
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .body(BodyInserters.fromMultipartData(parts))
                    .retrieve()
                    .bodyToMono(Map.class)
                    .timeout(Duration.ofSeconds(60))
                    .block();

            // 提取转录文本
            String transcription = (String) response.get("text");
            
            if (transcription == null || transcription.trim().isEmpty()) {
                throw new RuntimeException("Whisper API返回空的转录结果");
            }

            logger.debug("Whisper转录结果: {}", transcription);
            return transcription.trim();

        } catch (WebClientResponseException e) {
            logger.error("Whisper API调用失败，状态码: {}, 响应: {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("语音转文字失败: " + e.getMessage(), e);
        } catch (Exception e) {
            logger.error("语音转文字服务异常", e);
            throw new RuntimeException("语音转文字服务异常: " + e.getMessage(), e);
        }
    }

    @Override
    public byte[] synthesize(String text) {
        return synthesizeWithEmotion(text, null);
    }

    /**
     * 带情绪的语音合成
     */
    public byte[] synthesizeWithEmotion(String text, String emotion) {
        if (text == null || text.trim().isEmpty()) {
            throw new IllegalArgumentException("文本内容不能为空");
        }

        try {
            // 根据情绪选择语音配置
            VoiceConfig voiceConfig = getVoiceConfigForEmotion(emotion);
            
            // 构建TTS请求
            Map<String, Object> requestBody = new java.util.HashMap<>();
            requestBody.put("model", ttsModel);
            requestBody.put("input", text.trim());
            requestBody.put("voice", voiceConfig.voice);
            requestBody.put("response_format", "mp3");
            requestBody.put("speed", voiceConfig.speed);

            logger.debug("发送TTS API请求，文本长度: {}, 情绪: {}, 语音: {}, 语速: {}", 
                        text.length(), emotion, voiceConfig.voice, voiceConfig.speed);

            // 发送API请求并获取音频数据
            byte[] audioData = webClient.post()
                    .uri("/audio/speech")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(byte[].class)
                    .timeout(Duration.ofSeconds(30))
                    .block();

            if (audioData == null || audioData.length == 0) {
                throw new RuntimeException("TTS API返回空的音频数据");
            }

            logger.debug("TTS生成音频数据大小: {} bytes", audioData.length);
            return audioData;

        } catch (WebClientResponseException e) {
            logger.error("TTS API调用失败，状态码: {}, 响应: {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("文字转语音失败: " + e.getMessage(), e);
        } catch (Exception e) {
            logger.error("文字转语音服务异常", e);
            throw new RuntimeException("文字转语音服务异常: " + e.getMessage(), e);
        }
    }

    /**
     * 带情绪的语音合成到文件
     */
    public String synthesizeWithEmotion(String text, File outputFile, String emotion) {
        byte[] audioData = synthesizeWithEmotion(text, emotion);
        
        try {
            // 确保输出目录存在
            File parentDir = outputFile.getParentFile();
            if (parentDir != null && !parentDir.exists()) {
                parentDir.mkdirs();
            }

            // 写入音频数据到文件
            try (FileOutputStream fos = new FileOutputStream(outputFile)) {
                fos.write(audioData);
                fos.flush();
            }

            logger.info("TTS音频文件已保存: {}, 情绪: {}", outputFile.getAbsolutePath(), emotion);
            return outputFile.getAbsolutePath();

        } catch (IOException e) {
            logger.error("保存TTS音频文件失败", e);
            throw new RuntimeException("保存音频文件失败: " + e.getMessage(), e);
        }
    }

    /**
     * 根据情绪获取语音配置
     */
    private VoiceConfig getVoiceConfigForEmotion(String emotion) {
        if (emotion != null && EMOTION_VOICE_MAP.containsKey(emotion.toUpperCase())) {
            return EMOTION_VOICE_MAP.get(emotion.toUpperCase());
        }
        // 默认配置
        return new VoiceConfig(ttsVoice, 1.0, 1.0);
    }

    @Override
    public String synthesizeToFile(String text, File outputFile) {
        return synthesizeWithEmotion(text, outputFile, null);
    }

    @Override
    public boolean isServiceAvailable() {
        try {
            // 测试TTS服务
            Map<String, Object> testRequest = new java.util.HashMap<>();
            testRequest.put("model", ttsModel);
            testRequest.put("input", "test");
            testRequest.put("voice", ttsVoice);

            webClient.post()
                    .uri("/audio/speech")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(testRequest)
                    .retrieve()
                    .bodyToMono(byte[].class)
                    .timeout(Duration.ofSeconds(10))
                    .block();

            return true;
        } catch (Exception e) {
            logger.warn("OpenAI语音服务不可用: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public String[] getSupportedAudioFormats() {
        return SUPPORTED_FORMATS.clone();
    }
}