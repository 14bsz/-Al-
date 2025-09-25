package com.aichat.controller;

import com.aichat.dto.ChatRequest;
import com.aichat.dto.ChatResponse;
import com.aichat.entity.ChatMessage;
import com.aichat.entity.ChatSession;
import com.aichat.repository.ChatMessageRepository;
import com.aichat.repository.ChatSessionRepository;
import com.aichat.repository.AICharacterRepository;
import com.aichat.service.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 聊天控制器
 * 处理聊天相关的HTTP请求
 */
@RestController
@RequestMapping("/api/chat")
@CrossOrigin(origins = "*")
public class ChatController {

    @Autowired
    private ChatService chatService;

    @Autowired
    private ChatSessionRepository sessionRepository;

    @Autowired
    private ChatMessageRepository messageRepository;
    
    @Autowired
    private AICharacterRepository characterRepository;

    /**
     * 处理文本聊天请求
     */
    @PostMapping("/text")
    public ResponseEntity<ChatResponse> textChat(@RequestBody ChatRequest request) {
        try {
            // 验证请求参数
            if (request.getUserId() == null || request.getCharacterId() == null || 
                request.getMessage() == null || request.getMessage().trim().isEmpty()) {
                return ResponseEntity.badRequest().build();
            }

            ChatResponse response = chatService.processTextChat(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            ChatResponse errorResponse = new ChatResponse();
            errorResponse.setMessage("抱歉，我现在无法回复您的消息，请稍后再试。");
            errorResponse.setError(e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    /**
     * 处理语音聊天请求
     */
    @PostMapping("/voice")
    public ResponseEntity<ChatResponse> voiceChat(
            @RequestParam("audio") MultipartFile audioFile,
            @RequestParam("userId") Long userId,
            @RequestParam("characterId") Long characterId,
            @RequestParam(value = "needVoiceResponse", defaultValue = "true") boolean needVoiceResponse) {
        
        try {
            // 验证文件
            if (audioFile.isEmpty()) {
                return ResponseEntity.badRequest().build();
            }

            // 保存临时音频文件
            String tempDir = System.getProperty("java.io.tmpdir");
            String fileName = "audio_" + System.currentTimeMillis() + ".wav";
            Path tempFile = Paths.get(tempDir, fileName);
            Files.write(tempFile, audioFile.getBytes());

            // 构建请求对象
            ChatRequest request = new ChatRequest();
            request.setUserId(userId);
            request.setCharacterId(characterId);
            request.setNeedVoiceResponse(needVoiceResponse);

            // 处理语音聊天
            ChatResponse response = chatService.processVoiceChat(tempFile.toFile(), request);

            // 清理临时文件
            Files.deleteIfExists(tempFile);

            return ResponseEntity.ok(response);
        } catch (IOException e) {
            ChatResponse errorResponse = new ChatResponse();
            errorResponse.setMessage("音频文件处理失败，请重试。");
            errorResponse.setError(e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        } catch (Exception e) {
            ChatResponse errorResponse = new ChatResponse();
            errorResponse.setMessage("语音聊天处理失败，请稍后再试。");
            errorResponse.setError(e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    /**
     * 获取用户的聊天会话列表
     */
    @GetMapping("/sessions/{userId}")
    public ResponseEntity<List<ChatSession>> getUserSessions(@PathVariable String userId) {
        try {
            List<ChatSession> sessions = sessionRepository.findByUserIdOrderByLastActivityDesc(userId);
            return ResponseEntity.ok(sessions);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 获取特定会话的消息列表
     */
    @GetMapping("/sessions/{sessionId}/messages")
    public ResponseEntity<Page<ChatMessage>> getSessionMessages(
            @PathVariable Long sessionId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        try {
            Optional<ChatSession> sessionOpt = sessionRepository.findById(sessionId);
            if (!sessionOpt.isPresent()) {
                return ResponseEntity.notFound().build();
            }

            Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "createdAt"));
            Page<ChatMessage> messagesPage = messageRepository.findBySessionOrderByCreatedAtDesc(sessionOpt.get(), pageable);
            return ResponseEntity.ok(messagesPage);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 获取会话的最新消息
     */
    @GetMapping("/sessions/{sessionId}/recent")
    public ResponseEntity<List<ChatMessage>> getRecentMessages(
            @PathVariable Long sessionId,
            @RequestParam(defaultValue = "10") int limit) {
        try {
            Optional<ChatSession> sessionOpt = sessionRepository.findById(sessionId);
            if (!sessionOpt.isPresent()) {
                return ResponseEntity.notFound().build();
            }

            Pageable pageable = PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "createdAt"));
            Page<ChatMessage> messagesPage = messageRepository.findBySessionOrderByCreatedAtDesc(sessionOpt.get(), pageable);
            return ResponseEntity.ok(messagesPage.getContent());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 搜索会话中的消息
     */
    @GetMapping("/sessions/{sessionId}/search")
    public ResponseEntity<List<ChatMessage>> searchMessages(
            @PathVariable Long sessionId,
            @RequestParam String keyword) {
        try {
            Optional<ChatSession> sessionOpt = sessionRepository.findById(sessionId);
            if (!sessionOpt.isPresent()) {
                return ResponseEntity.notFound().build();
            }

            List<ChatMessage> messages = messageRepository.searchMessagesByContent(sessionOpt.get().getUserId(), keyword);
            return ResponseEntity.ok(messages);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 创建新的聊天会话
     */
    @PostMapping("/sessions")
    public ResponseEntity<ChatSession> createSession(
            @RequestParam String userId,
            @RequestParam Long characterId) {
        try {
            // 获取角色对象
            Optional<com.aichat.entity.AICharacter> characterOpt = characterRepository.findById(characterId);
            if (!characterOpt.isPresent()) {
                return ResponseEntity.badRequest().build();
            }
            
            // 检查是否已存在会话
            List<ChatSession> existingSessions = sessionRepository.findByUserIdAndCharacterOrderByLastActivityDesc(userId, characterOpt.get());
            if (!existingSessions.isEmpty()) {
                return ResponseEntity.ok(existingSessions.get(0));
            }

            // 创建新会话的逻辑会在第一次发送消息时自动处理
            // 这里返回一个临时响应
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 删除聊天会话
     */
    @DeleteMapping("/sessions/{sessionId}")
    public ResponseEntity<Void> deleteSession(@PathVariable Long sessionId) {
        try {
            Optional<ChatSession> sessionOpt = sessionRepository.findById(sessionId);
            if (!sessionOpt.isPresent()) {
                return ResponseEntity.notFound().build();
            }

            // 删除会话相关的消息
            List<ChatMessage> messages = messageRepository.findBySessionOrderByCreatedAtAsc(sessionOpt.get());
            messageRepository.deleteAll(messages);
            
            // 删除会话
            sessionRepository.deleteById(sessionId);
            
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 清空会话消息
     */
    @DeleteMapping("/sessions/{sessionId}/messages")
    public ResponseEntity<Void> clearSessionMessages(@PathVariable Long sessionId) {
        try {
            Optional<ChatSession> sessionOpt = sessionRepository.findById(sessionId);
            if (!sessionOpt.isPresent()) {
                return ResponseEntity.notFound().build();
            }

            // 删除会话的所有消息
            List<ChatMessage> messages = messageRepository.findBySessionOrderByCreatedAtAsc(sessionOpt.get());
            messageRepository.deleteAll(messages);
            
            // 重置会话消息计数
            ChatSession session = sessionOpt.get();
            session.setMessageCount(0);
            sessionRepository.save(session);
            
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 获取会话统计信息
     */
    @GetMapping("/sessions/{sessionId}/statistics")
    public ResponseEntity<Map<String, Object>> getSessionStatistics(@PathVariable Long sessionId) {
        try {
            Optional<ChatSession> sessionOpt = sessionRepository.findById(sessionId);
            if (!sessionOpt.isPresent()) {
                return ResponseEntity.notFound().build();
            }

            ChatSession session = sessionOpt.get();
            long userMessageCount = messageRepository.findBySessionAndSenderTypeOrderByCreatedAtAsc(
                session, ChatMessage.SenderType.USER
            ).size();
            long aiMessageCount = messageRepository.findBySessionAndSenderTypeOrderByCreatedAtAsc(
                session, ChatMessage.SenderType.AI_CHARACTER
            ).size();

            java.util.HashMap<String, Object> statistics = new java.util.HashMap<>();
            statistics.put("totalMessages", session.getMessageCount());
            statistics.put("userMessages", userMessageCount);
            statistics.put("aiMessages", aiMessageCount);
            statistics.put("createdAt", session.getCreatedAt());
            statistics.put("lastActiveAt", session.getLastActivity());
            statistics.put("status", session.getSessionStatus());

            return ResponseEntity.ok(statistics);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 获取对话历史
     */
    @GetMapping("/history")
    public ResponseEntity<String> getConversationHistory(
            @RequestParam Long userId,
            @RequestParam Long characterId,
            @RequestParam(defaultValue = "10") int limit) {
        try {
            String history = chatService.getConversationHistory(userId, characterId, limit);
            return ResponseEntity.ok(history);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 检查服务可用性
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> checkHealth() {
        try {
            boolean isAvailable = chatService.isServiceAvailable();
            java.util.HashMap<String, Object> health = new java.util.HashMap<>();
            health.put("status", isAvailable ? "UP" : "DOWN");
            health.put("timestamp", System.currentTimeMillis());
            return ResponseEntity.ok(health);
        } catch (Exception e) {
            java.util.HashMap<String, Object> health = new java.util.HashMap<>();
            health.put("status", "DOWN");
            health.put("error", e.getMessage());
            health.put("timestamp", System.currentTimeMillis());
             return ResponseEntity.internalServerError().body(health);
        }
    }

    /**
     * 获取角色的系统提示词
     */
    @GetMapping("/characters/{characterId}/prompt")
    public ResponseEntity<String> getCharacterPrompt(@PathVariable Long characterId) {
        try {
            String prompt = chatService.getCharacterSystemPrompt(characterId);
            return ResponseEntity.ok(prompt);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}