<template>
  <div class="chat-window">
    <!-- 头部区域 -->
    <div class="chat-header">
      <div class="character-selector">
        <el-select 
          v-model="selectedCharacterId" 
          placeholder="选择聊天角色"
          @change="handleCharacterChange"
          class="character-select"
        >
          <el-option
            v-for="character in chatStore.characters"
            :key="character.id"
            :label="character.name"
            :value="character.id"
          >
            <div class="character-option">
              <img :src="character.avatar" :alt="character.name" class="character-avatar" />
              <div class="character-info">
                <div class="character-name">{{ character.name }}</div>
                <div class="character-desc">{{ character.description }}</div>
              </div>
            </div>
          </el-option>
        </el-select>
      </div>
      
      <div class="chat-actions">
        <el-button 
          @click="clearChat" 
          type="danger" 
          size="small" 
          :disabled="!chatStore.hasMessages"
          plain
        >
          清空对话
        </el-button>
      </div>
    </div>

    <!-- 消息区域 -->
    <div class="messages-container" ref="messagesContainer">
      <div v-if="!chatStore.hasMessages" class="welcome-message">
        <div class="welcome-content">
          <img 
            v-if="chatStore.selectedCharacter" 
            :src="chatStore.selectedCharacter.avatar" 
            :alt="chatStore.selectedCharacter.name"
            class="welcome-avatar"
          />
          <h3 v-if="chatStore.selectedCharacter">
            你好！我是 {{ chatStore.selectedCharacter.name }}
          </h3>
          <p v-if="chatStore.selectedCharacter">
            {{ chatStore.selectedCharacter.description }}
          </p>
          <p v-else>请选择一个聊天角色开始对话</p>
        </div>
      </div>

      <div 
        v-for="message in chatStore.messages" 
        :key="message.id" 
        :class="['message', { 'user-message': message.isUser, 'ai-message': !message.isUser }]"
      >
        <div class="message-content">
          <div class="message-avatar">
            <img 
              v-if="message.isUser" 
              src="https://picsum.photos/40/40?random=user" 
              alt="用户头像"
            />
            <img 
              v-else 
              :src="chatStore.selectedCharacter?.avatar" 
              :alt="chatStore.selectedCharacter?.name"
            />
          </div>
          
          <div class="message-bubble">
            <div class="message-text">{{ message.content }}</div>
            <div class="message-meta">
              <span class="message-time">{{ formatTime(message.timestamp) }}</span>
              <el-button
                v-if="message.audioUrl && !message.isUser"
                @click="playAudio(message)"
                :loading="message.isPlaying"
                type="primary"
                size="small"
                plain
                class="play-button"
              >
                <el-icon><VideoPlay /></el-icon>
                {{ message.isPlaying ? '播放中...' : '播放语音' }}
              </el-button>
            </div>
          </div>
        </div>
      </div>

      <!-- 加载状态 -->
      <div v-if="chatStore.isLoading" class="message ai-message">
        <div class="message-content">
          <div class="message-avatar">
            <img 
              :src="chatStore.selectedCharacter?.avatar" 
              :alt="chatStore.selectedCharacter?.name"
            />
          </div>
          <div class="message-bubble">
            <div class="typing-indicator">
              <span></span>
              <span></span>
              <span></span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 输入区域 -->
    <div class="input-area">
      <div class="input-container">
        <el-input
          v-model="inputMessage"
          placeholder="输入消息..."
          @keyup.enter="sendMessage"
          :disabled="!chatStore.selectedCharacter || chatStore.isLoading || speechRecognition.isRecording"
          class="message-input"
          maxlength="1000"
          show-word-limit
        />
        
        <div class="input-actions">
          <!-- 语音录制按钮 -->
          <el-button
            @click="toggleRecording"
            :disabled="!chatStore.selectedCharacter || chatStore.isLoading || !speechRecognition.isSupported"
            :type="speechRecognition.isRecording ? 'danger' : 'primary'"
            class="voice-button"
            circle
          >
            <el-icon>
              <Microphone v-if="!speechRecognition.isRecording" />
              <VideoPause v-else />
            </el-icon>
          </el-button>
          
          <!-- 发送按钮 -->
          <el-button
            @click="sendMessage"
            :disabled="!inputMessage.trim() || !chatStore.selectedCharacter || chatStore.isLoading"
            type="primary"
            class="send-button"
          >
            <el-icon><Promotion /></el-icon>
            发送
          </el-button>
        </div>
      </div>
      
      <!-- 语音识别状态 -->
      <div v-if="speechRecognition.isRecording || speechRecognition.transcript" class="voice-status">
        <div v-if="speechRecognition.isRecording" class="recording-status">
          <el-icon class="recording-icon"><Microphone /></el-icon>
          正在录音...
        </div>
        <div v-if="speechRecognition.transcript" class="transcript">
          识别结果: {{ speechRecognition.transcript }}
        </div>
      </div>
      
      <!-- 错误提示 -->
      <div v-if="speechRecognition.error" class="error-message">
        {{ speechRecognition.error }}
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, nextTick, onMounted, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { VideoPlay, Microphone, VideoPause, Promotion } from '@element-plus/icons-vue'
import { useChatStore, type ChatMessage } from '../stores/chatStore'
import { useSpeechRecognition } from '../composables/useSpeechRecognition'

// 状态管理
const chatStore = useChatStore()
const speechRecognition = useSpeechRecognition({
  continuous: false,
  interimResults: true,
  lang: 'zh-CN'
})

// 响应式数据
const inputMessage = ref('')
const selectedCharacterId = ref<number | null>(null)
const messagesContainer = ref<HTMLElement>()

// 处理角色选择
const handleCharacterChange = (characterId: number) => {
  const character = chatStore.characters.find(c => c.id === characterId)
  if (character) {
    chatStore.selectCharacter(character)
    ElMessage.success(`已选择角色: ${character.name}`)
  }
}

// 发送文本消息
const sendMessage = async () => {
  if (!inputMessage.value.trim() || !chatStore.selectedCharacter) return
  
  const message = inputMessage.value.trim()
  inputMessage.value = ''
  
  await chatStore.sendTextMessage(message)
  scrollToBottom()
}

// 切换录音状态
const toggleRecording = async () => {
  if (!chatStore.selectedCharacter) {
    ElMessage.warning('请先选择聊天角色')
    return
  }

  if (speechRecognition.isRecording) {
    // 停止录音
    await stopRecording()
  } else {
    // 开始录音
    await startRecording()
  }
}

// 开始录音
const startRecording = async () => {
  const success = await speechRecognition.startRecording()
  if (success) {
    speechRecognition.startListening()
    ElMessage.info('开始录音，请说话...')
  } else {
    ElMessage.error('无法开始录音，请检查麦克风权限')
  }
}

// 停止录音
const stopRecording = async () => {
  if (!speechRecognition.isRecording) return

  speechRecognition.stopListening()
  
  const audioBlob = await speechRecognition.stopRecording()
  if (audioBlob) {
    // 如果有语音识别结果，显示给用户确认
    if (speechRecognition.transcript.trim()) {
      inputMessage.value = speechRecognition.transcript.trim()
      ElMessage.success('语音识别完成，您可以编辑后发送')
    } else {
      // 直接发送语音消息
      await chatStore.sendVoiceMessage(audioBlob)
      scrollToBottom()
      ElMessage.success('语音消息已发送')
    }
  } else {
    ElMessage.warning('录音失败，请重试')
  }
}

// 播放音频
const playAudio = async (message: ChatMessage) => {
  await chatStore.playAudio(message)
}

// 清空对话
const clearChat = () => {
  chatStore.clearMessages()
  ElMessage.success('对话已清空')
}

// 格式化时间
const formatTime = (date: Date) => {
  return date.toLocaleTimeString('zh-CN', { 
    hour: '2-digit', 
    minute: '2-digit' 
  })
}

// 滚动到底部
const scrollToBottom = () => {
  nextTick(() => {
    if (messagesContainer.value) {
      messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
    }
  })
}

// 监听消息变化，自动滚动
watch(() => chatStore.messages.length, () => {
  scrollToBottom()
})

// 组件挂载时初始化
onMounted(() => {
  // 默认选择第一个角色
  if (chatStore.characters.length > 0) {
    selectedCharacterId.value = chatStore.characters[0].id
    handleCharacterChange(chatStore.characters[0].id)
  }
})
</script>

<style scoped>
.chat-window {
  display: flex;
  flex-direction: column;
  height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.chat-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  border-bottom: 1px solid rgba(0, 0, 0, 0.1);
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
}

.character-select {
  min-width: 300px;
}

.character-option {
  display: flex;
  align-items: center;
  gap: 12px;
}

.character-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  object-fit: cover;
}

.character-info {
  flex: 1;
}

.character-name {
  font-weight: 600;
  color: #333;
}

.character-desc {
  font-size: 12px;
  color: #666;
  margin-top: 2px;
}

.messages-container {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
  background: rgba(255, 255, 255, 0.1);
}

.welcome-message {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100%;
  text-align: center;
}

.welcome-content {
  background: rgba(255, 255, 255, 0.9);
  padding: 40px;
  border-radius: 20px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.2);
}

.welcome-avatar {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  margin-bottom: 16px;
  object-fit: cover;
}

.welcome-content h3 {
  margin: 0 0 12px 0;
  color: #333;
  font-size: 24px;
}

.welcome-content p {
  margin: 0;
  color: #666;
  font-size: 16px;
}

.message {
  margin-bottom: 20px;
}

.message-content {
  display: flex;
  gap: 12px;
}

.user-message .message-content {
  flex-direction: row-reverse;
}

.message-avatar img {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  object-fit: cover;
}

.message-bubble {
  max-width: 70%;
  padding: 12px 16px;
  border-radius: 18px;
  position: relative;
}

.user-message .message-bubble {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border-bottom-right-radius: 6px;
}

.ai-message .message-bubble {
  background: rgba(255, 255, 255, 0.95);
  color: #333;
  border-bottom-left-radius: 6px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
}

.message-text {
  line-height: 1.5;
  word-wrap: break-word;
}

.message-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 8px;
  gap: 12px;
}

.message-time {
  font-size: 12px;
  opacity: 0.7;
}

.play-button {
  font-size: 12px;
}

.typing-indicator {
  display: flex;
  gap: 4px;
  align-items: center;
}

.typing-indicator span {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #999;
  animation: typing 1.4s infinite ease-in-out;
}

.typing-indicator span:nth-child(1) { animation-delay: -0.32s; }
.typing-indicator span:nth-child(2) { animation-delay: -0.16s; }

@keyframes typing {
  0%, 80%, 100% { transform: scale(0.8); opacity: 0.5; }
  40% { transform: scale(1); opacity: 1; }
}

.input-area {
  padding: 20px;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  border-top: 1px solid rgba(0, 0, 0, 0.1);
}

.input-container {
  display: flex;
  gap: 12px;
  align-items: flex-end;
}

.message-input {
  flex: 1;
}

.input-actions {
  display: flex;
  gap: 8px;
}

.voice-button {
  width: 40px;
  height: 40px;
}

.voice-button.is-danger {
  animation: pulse 1s infinite;
}

@keyframes pulse {
  0% { transform: scale(1); }
  50% { transform: scale(1.1); }
  100% { transform: scale(1); }
}

.voice-status {
  margin-top: 12px;
  padding: 8px 12px;
  background: rgba(103, 126, 234, 0.1);
  border-radius: 8px;
  font-size: 14px;
}

.recording-status {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #e74c3c;
}

.recording-icon {
  animation: pulse 1s infinite;
}

.transcript {
  color: #333;
  margin-top: 4px;
}

.error-message {
  margin-top: 8px;
  padding: 8px 12px;
  background: rgba(231, 76, 60, 0.1);
  color: #e74c3c;
  border-radius: 8px;
  font-size: 14px;
}

/* 滚动条样式 */
.messages-container::-webkit-scrollbar {
  width: 6px;
}

.messages-container::-webkit-scrollbar-track {
  background: rgba(255, 255, 255, 0.1);
  border-radius: 3px;
}

.messages-container::-webkit-scrollbar-thumb {
  background: rgba(255, 255, 255, 0.3);
  border-radius: 3px;
}

.messages-container::-webkit-scrollbar-thumb:hover {
  background: rgba(255, 255, 255, 0.5);
}
</style>