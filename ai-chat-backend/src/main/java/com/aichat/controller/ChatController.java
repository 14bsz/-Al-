package com.aichat.controller;

import com.aichat.dto.ChatRequest;
import com.aichat.dto.ChatResponse;
import com.aichat.service.ChatService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;

import javax.validation.Valid;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.UUID;

/**
 * 聊天控制器
 * 提供文本聊天和语音聊天的REST API端点
 */
@RestController
@RequestMapping("/api/chat")
@CrossOrigin(origins = "*", maxAge = 3600)
public class ChatController {

    private static final Logger logger = LoggerFactory.getLogger(ChatController.class);

    @Autowired
    private ChatService chatService;

    /**
     * 处理文本聊天请求
     * 
     * @param request 聊天请求对象
     * @return 聊天响应
     */
    @PostMapping("/text")
    public ResponseEntity<ChatResponse> processTextChat(@Valid @RequestBody ChatRequest request) {
        try {
            logger.info("收到文本聊天请求，用户ID: {}, 角色ID: {}", 
                       request.getUserId(), request.getCharacterId());

            // 处理聊天请求
            ChatResponse response = chatService.processTextChat(request);

            // 根据响应状态返回相应的HTTP状态码
            HttpStatus status = response.getStatus() == ChatResponse.ResponseStatus.SUCCESS 
                ? HttpStatus.OK : HttpStatus.INTERNAL_SERVER_ERROR;

            return ResponseEntity.status(status).body(response);

        } catch (IllegalArgumentException e) {
            logger.warn("文本聊天请求参数错误", e);
            ChatResponse errorResponse = ChatResponse.error("请求参数错误: " + e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        } catch (Exception e) {
            logger.error("处理文本聊天请求时发生异常", e);
            ChatResponse errorResponse = ChatResponse.error("服务器内部错误，请稍后重试");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * 处理语音聊天请求
     * 
     * @param audioFile 音频文件
     * @param userId 用户ID
     * @param characterId 角色ID
     * @param sessionId 会话ID
     * @return 聊天响应
     */
    @PostMapping(value = "/voice", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ChatResponse> processVoiceChat(
            @RequestParam("audioFile") MultipartFile audioFile,
            @RequestParam("userId") Long userId,
            @RequestParam("characterId") Long characterId,
            @RequestParam("sessionId") String sessionId) {
        
        File tempFile = null;
        try {
            logger.info("收到语音聊天请求，用户ID: {}, 角色ID: {}, 文件大小: {} bytes", 
                       userId, characterId, audioFile.getSize());

            // 验证音频文件
            if (audioFile.isEmpty()) {
                ChatResponse errorResponse = ChatResponse.error("音频文件不能为空");
                return ResponseEntity.badRequest().body(errorResponse);
            }

            // 验证文件类型
            String contentType = audioFile.getContentType();
            if (contentType == null || !isValidAudioType(contentType)) {
                ChatResponse errorResponse = ChatResponse.error("不支持的音频格式，请上传mp3、wav、m4a等格式的文件");
                return ResponseEntity.badRequest().body(errorResponse);
            }

            // 验证文件大小（限制为10MB）
            if (audioFile.getSize() > 10 * 1024 * 1024) {
                ChatResponse errorResponse = ChatResponse.error("音频文件大小不能超过10MB");
                return ResponseEntity.badRequest().body(errorResponse);
            }

            // 保存临时文件
            tempFile = saveTemporaryFile(audioFile);

            // 构建聊天请求对象
            ChatRequest request = new ChatRequest();
            request.setUserId(userId);
            request.setCharacterId(characterId);
            request.setSessionId(sessionId);
            // 注意：语音聊天的message字段将从音频中提取，这里不设置

            // 处理语音聊天请求
            ChatResponse response = chatService.processVoiceChat(tempFile, request);

            // 根据响应状态返回相应的HTTP状态码
            HttpStatus status = response.getStatus() == ChatResponse.ResponseStatus.SUCCESS 
                ? HttpStatus.OK : HttpStatus.INTERNAL_SERVER_ERROR;

            return ResponseEntity.status(status).body(response);

        } catch (IllegalArgumentException e) {
            logger.warn("语音聊天请求参数错误", e);
            ChatResponse errorResponse = ChatResponse.error("请求参数错误: " + e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        } catch (IOException e) {
            logger.error("处理音频文件时发生IO异常", e);
            ChatResponse errorResponse = ChatResponse.error("音频文件处理失败");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        } catch (Exception e) {
            logger.error("处理语音聊天请求时发生异常", e);
            ChatResponse errorResponse = ChatResponse.error("服务器内部错误，请稍后重试");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        } finally {
            // 清理临时文件
            if (tempFile != null && tempFile.exists()) {
                try {
                    Files.deleteIfExists(tempFile.toPath());
                    logger.debug("临时音频文件已删除: {}", tempFile.getAbsolutePath());
                } catch (IOException e) {
                    logger.warn("删除临时音频文件失败: {}", tempFile.getAbsolutePath(), e);
                }
            }
        }
    }

    /**
     * 获取服务健康状态
     * 
     * @return 服务状态信息
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> getHealthStatus() {
        try {
            boolean isAvailable = chatService.isServiceAvailable();
            
            Map<String, Object> status = new HashMap<>();
            status.put("status", isAvailable ? "UP" : "DOWN");
            status.put("timestamp", System.currentTimeMillis());
            status.put("service", "AI Chat Service");

            HttpStatus httpStatus = isAvailable ? HttpStatus.OK : HttpStatus.SERVICE_UNAVAILABLE;
            return ResponseEntity.status(httpStatus).body(status);

        } catch (Exception e) {
            logger.error("检查服务健康状态时发生异常", e);
            Map<String, Object> status = new HashMap<>();
            status.put("status", "DOWN");
            status.put("timestamp", System.currentTimeMillis());
            status.put("service", "AI Chat Service");
            status.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(status);
        }
    }

    /**
     * 验证音频文件类型
     */
    private boolean isValidAudioType(String contentType) {
        return contentType.startsWith("audio/") || 
               contentType.equals("application/octet-stream"); // 某些客户端可能发送这个类型
    }

    /**
     * 保存临时音频文件
     */
    private File saveTemporaryFile(MultipartFile audioFile) throws IOException {
        // 获取文件扩展名
        String originalFilename = audioFile.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        } else {
            // 根据Content-Type推断扩展名
            String contentType = audioFile.getContentType();
            if (contentType != null) {
                if (contentType.contains("mp3")) {
                    extension = ".mp3";
                } else if (contentType.contains("wav")) {
                    extension = ".wav";
                } else if (contentType.contains("m4a")) {
                    extension = ".m4a";
                } else {
                    extension = ".tmp";
                }
            }
        }

        // 创建临时文件
        String tempFileName = "voice_" + UUID.randomUUID().toString() + extension;
        Path tempDir = Paths.get(System.getProperty("java.io.tmpdir"));
        Path tempFilePath = tempDir.resolve(tempFileName);

        // 写入文件内容
        Files.write(tempFilePath, audioFile.getBytes());

        File tempFile = tempFilePath.toFile();
        logger.debug("临时音频文件已创建: {}", tempFile.getAbsolutePath());

        return tempFile;
    }
}