<template>
  <div class="modern-chat-container" :class="{ 'dark-mode': isDarkMode }">
    <!-- 3D背景效果 -->
    <div class="background-3d">
      <div class="floating-particles"></div>
      <div class="gradient-orbs">
        <div class="orb orb-1"></div>
        <div class="orb orb-2"></div>
        <div class="orb orb-3"></div>
      </div>
    </div>

    <!-- 主聊天界面 -->
    <div class="chat-interface">
      <!-- 顶部导航栏 -->
      <header class="chat-header">
        <div class="header-left">
          <div class="character-info" @click="showCharacterSelector = true">
            <img :src="currentCharacter.avatar || 'https://picsum.photos/40/40?random=1'" 
                 :alt="currentCharacter.name" class="character-avatar">
            <div class="character-details">
              <h3 class="character-name">{{ currentCharacter.name || 'AI助手' }}</h3>
              <span class="character-status">{{ isTyping ? '正在输入...' : '在线' }}</span>
            </div>
            <svg class="dropdown-icon" viewBox="0 0 24 24">
              <path d="M7,10L12,15L17,10H7Z"/>
            </svg>
          </div>
        </div>
        
        <div class="header-actions">
          <button @click="toggleVoiceMode" class="action-btn" :class="{ active: isVoiceMode }" title="语音通话">
            <svg viewBox="0 0 24 24">
              <path d="M6.62,10.79C8.06,13.62 10.38,15.94 13.21,17.38L15.41,15.18C15.69,14.9 16.08,14.82 16.43,14.93C17.55,15.3 18.75,15.5 20,15.5A1,1 0 0,1 21,16.5V20A1,1 0 0,1 20,21A17,17 0 0,1 3,4A1,1 0 0,1 4,3H7.5A1,1 0 0,1 8.5,4C8.5,5.25 8.7,6.45 9.07,7.57C9.18,7.92 9.1,8.31 8.82,8.59L6.62,10.79Z"/>
            </svg>
          </button>
          <button @click="toggleDarkMode" class="action-btn" title="视频通话">
            <svg viewBox="0 0 24 24">
              <path d="M17,10.5V7A1,1 0 0,0 16,6H4A1,1 0 0,0 3,7V17A1,1 0 0,0 4,18H16A1,1 0 0,0 17,17V13.5L21,17.5V6.5L17,10.5Z"/>
            </svg>
          </button>
          <button @click="openSettings" class="action-btn" title="设置">
            <svg viewBox="0 0 24 24">
              <path d="M12,15.5A3.5,3.5 0 0,1 8.5,12A3.5,3.5 0 0,1 12,8.5A3.5,3.5 0 0,1 15.5,12A3.5,3.5 0 0,1 12,15.5M19.43,12.97C19.47,12.65 19.5,12.33 19.5,12C19.5,11.67 19.47,11.34 19.43,11L21.54,9.37C21.73,9.22 21.78,8.95 21.66,8.73L19.66,5.27C19.54,5.05 19.27,4.96 19.05,5.05L16.56,6.05C16.04,5.66 15.5,5.32 14.87,5.07L14.5,2.42C14.46,2.18 14.25,2 14,2H10C9.75,2 9.54,2.18 9.5,2.42L9.13,5.07C8.5,5.32 7.96,5.66 7.44,6.05L4.95,5.05C4.73,4.96 4.46,5.05 4.34,5.27L2.34,8.73C2.22,8.95 2.27,9.22 2.46,9.37L4.57,11C4.53,11.34 4.5,11.67 4.5,12C4.5,12.33 4.53,12.65 4.57,12.97L2.46,14.63C2.27,14.78 2.22,15.05 2.34,15.27L4.34,18.73C4.46,18.95 4.73,19.03 4.95,18.95L7.44,17.94C7.96,18.34 8.5,18.68 9.13,18.93L9.5,21.58C9.54,21.82 9.75,22 10,22H14C14.25,22 14.46,21.82 14.5,21.58L14.87,18.93C15.5,18.68 16.04,18.34 16.56,17.94L19.05,18.95C19.27,19.03 19.54,18.95 19.66,18.73L21.66,15.27C21.78,15.05 21.73,14.78 21.54,14.63L19.43,12.97Z"/>
            </svg>
          </button>
        </div>
      </header>

      <!-- 消息区域 -->
      <main class="messages-container" ref="messagesContainer">
        <div class="messages-wrapper">
          <TransitionGroup name="message" tag="div">
            <div v-for="message in messages" :key="message.id" 
                 class="message-item" :class="message.type">
              
              <!-- 用户消息 -->
              <div v-if="message.type === 'user'" class="message user-message">
                <div class="message-content">
                  <div class="message-bubble">
                    <p>{{ message.content }}</p>
                    <div class="message-meta">
                      <span class="timestamp">{{ formatTime(message.timestamp) }}</span>
                      <i class="status-icon" :class="message.status"></i>
                    </div>
                  </div>
                </div>
                <div class="message-avatar">
                  <img :src="userAvatar" alt="用户头像">
                </div>
              </div>

              <!-- AI消息 -->
              <div v-else class="message ai-message">
                <div class="message-avatar">
                  <img :src="currentCharacter.avatar || 'https://picsum.photos/40/40?random=2'" 
                       :alt="currentCharacter.name">
                </div>
                <div class="message-content">
                  <div class="message-bubble">
                    <div v-if="message.isStreaming" class="streaming-text">
                      <span v-for="(char, index) in message.displayText" :key="index" 
                            class="char" :style="{ animationDelay: index * 50 + 'ms' }">
                        {{ char }}
                      </span>
                      <span class="cursor">|</span>
                    </div>
                    <p v-else>{{ message.content }}</p>
                    
                    <!-- 消息附加功能 -->
                    <div class="message-actions" v-if="!message.isStreaming">
                      <button @click="copyMessage(message)" class="action-btn-small" title="复制消息">
                        📋
                      </button>
                      <button @click="speakMessage(message)" class="action-btn-small" title="播放语音">
                        🔊
                      </button>
                      <button @click="likeMessage(message)" class="action-btn-small" 
                              :class="{ active: message.liked }" title="点赞">
                        {{ message.liked ? '❤️' : '🤍' }}
                      </button>
                    </div>
                    
                    <div class="message-meta">
                      <span class="timestamp">{{ formatTime(message.timestamp) }}</span>
                      <span class="model-info">{{ message.model }}</span>
                    </div>
                  </div>
                </div>
              </div>

              <!-- 系统消息 -->
              <div v-if="message.type === 'system'" class="message system-message">
                <div class="system-content">
                  ℹ️
                  <span>{{ message.content }}</span>
                </div>
              </div>
            </div>
          </TransitionGroup>
        </div>

        <!-- 打字指示器 -->
        <div v-if="isTyping" class="typing-indicator">
          <div class="typing-avatar">
            <img :src="currentCharacter.avatar || 'https://picsum.photos/40/40?random=2'" 
                 :alt="currentCharacter.name">
          </div>
          <div class="typing-animation">
            <div class="dot"></div>
            <div class="dot"></div>
            <div class="dot"></div>
          </div>
        </div>
      </main>

      <!-- 输入区域 -->
      <footer class="input-section">
        <!-- 快捷功能栏 -->
        <div class="quick-actions" v-if="showQuickActions">
          <button @click="insertQuickText('你好！')" class="quick-btn" title="快速问候">👋 问候</button>
          <button @click="insertQuickText('请帮我分析一下')" class="quick-btn" title="请求分析">🔍 分析</button>
          <button @click="insertQuickText('能给我一些建议吗？')" class="quick-btn" title="获取建议">💡 建议</button>
          <button @click="openImageUpload" class="quick-btn" title="上传图片">🖼️ 图片</button>
        </div>

        <!-- 主输入区 -->
        <div class="input-container">
          <div class="input-wrapper">
            <!-- 附件按钮 -->
            <button @click="toggleQuickActions" class="attachment-btn">
              <i class="icon-plus" :class="{ rotated: showQuickActions }"></i>
            </button>

            <!-- 文本输入框 -->
            <div class="text-input-container">
              <textarea 
                v-model="inputMessage" 
                @keydown="handleKeyDown"
                @input="handleInput"
                @focus="onInputFocus"
                @blur="onInputBlur"
                :placeholder="inputPlaceholder"
                class="message-input"
                rows="1"
                ref="messageInput"
                :disabled="isLoading"
              ></textarea>
              
              <!-- 输入增强功能 -->
              <div class="input-enhancements">
                <button @click="toggleEmojiPicker" class="enhancement-btn" title="表情符号">
                  😊
                </button>
                <button @click="toggleVoiceInput" class="enhancement-btn" 
                        :class="{ active: isRecording }" title="语音输入">
                  🎤
                </button>
              </div>
            </div>

            <!-- 发送按钮 -->
            <button @click="sendMessage" 
                    class="send-btn" 
                    :disabled="!canSend"
                    :class="{ loading: isLoading }" title="发送消息">
              <div v-if="isLoading" class="loading-spinner"></div>
              <span v-else>➤</span>
            </button>
          </div>

          <!-- 输入状态指示 -->
          <div class="input-status" v-if="inputStatus">
            <span class="status-text">{{ inputStatus }}</span>
            <div class="status-progress" v-if="uploadProgress > 0">
              <div class="progress-bar" :style="{ width: uploadProgress + '%' }"></div>
            </div>
          </div>
        </div>
      </footer>
    </div>

    <!-- 侧边功能面板 -->
    <aside class="side-panel" :class="{ open: sidePanelOpen }">
      <div class="panel-header">
        <h3>智能功能</h3>
        <button @click="closeSidePanel" class="close-btn">
          <i class="icon-close"></i>
        </button>
      </div>
      
      <div class="panel-content">
        <!-- 角色切换 -->
        <div class="feature-section">
          <h4>AI角色</h4>
          <div class="character-grid">
            <div v-for="character in availableCharacters" :key="character.id"
                 @click="switchCharacter(character)"
                 class="character-card"
                 :class="{ active: character.id === currentCharacter.id }">
              <img :src="character.avatar" :alt="character.name">
              <span>{{ character.name }}</span>
            </div>
          </div>
        </div>

        <!-- 情感分析 -->
        <div class="feature-section" v-if="emotionAnalysis">
          <h4>情感分析</h4>
          <div class="emotion-display">
            <div class="emotion-primary">
              <span class="emotion-icon">{{ getEmotionIcon(emotionAnalysis.primaryEmotion) }}</span>
              <span class="emotion-text">{{ emotionAnalysis.primaryEmotion }}</span>
            </div>
            <div class="emotion-confidence">
              置信度: {{ Math.round(emotionAnalysis.confidence * 100) }}%
            </div>
          </div>
        </div>

        <!-- 对话统计 -->
        <div class="feature-section">
          <h4>对话统计</h4>
          <div class="stats-grid">
            <div class="stat-item">
              <span class="stat-value">{{ conversationStats.messageCount }}</span>
              <span class="stat-label">消息数</span>
            </div>
            <div class="stat-item">
              <span class="stat-value">{{ conversationStats.duration }}</span>
              <span class="stat-label">时长</span>
            </div>
          </div>
        </div>
      </div>
    </aside>

    <!-- 模态框和弹窗 -->
    <Teleport to="body">
      <!-- 表情选择器 -->
      <div v-if="showEmojiPicker" class="emoji-picker-overlay" @click="closeEmojiPicker">
        <div class="emoji-picker" @click.stop>
          <div class="emoji-grid">
            <span v-for="emoji in commonEmojis" :key="emoji" 
                  @click="insertEmoji(emoji)" class="emoji-item">
              {{ emoji }}
            </span>
          </div>
        </div>
      </div>

      <!-- 设置面板 -->
      <div v-if="showSettings" class="settings-overlay" @click="closeSettings">
        <div class="settings-panel" @click.stop>
          <div class="settings-header">
            <h3>聊天设置</h3>
            <button @click="closeSettings" class="close-btn" title="关闭设置">
            ✕
          </button>
          </div>
          <div class="settings-content">
            <!-- 设置选项 -->
            <div class="setting-group">
              <label class="setting-item">
                <span>暗黑模式</span>
                <input type="checkbox" v-model="isDarkMode" @change="saveSettings">
              </label>
              <label class="setting-item">
                <span>语音模式</span>
                <input type="checkbox" v-model="isVoiceMode" @change="saveSettings">
              </label>
              <label class="setting-item">
                <span>自动播放语音</span>
                <input type="checkbox" v-model="autoPlayVoice" @change="saveSettings">
              </label>
            </div>
          </div>
        </div>
      </div>
    </Teleport>
    <!-- 设置面板 -->
    <SettingsPanel 
      v-if="showSettings" 
      :settings="appSettings"
      @close="showSettings = false"
      @update-settings="updateSettings"
    />
    
    <!-- 角色选择器 -->
    <CharacterSelector
      v-if="showCharacterSelector"
      :current-character="currentCharacter"
      @close="showCharacterSelector = false"
      @select-character="selectCharacter"
    />
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, onUnmounted, nextTick, watch } from 'vue'
import { useChatStore } from '@/stores/chatStore'
import SettingsPanel from './SettingsPanel.vue'
import CharacterSelector from './CharacterSelector.vue'

// 使用聊天store
const chatStore = useChatStore()

// 响应式数据
const messagesContainer = ref(null)
const messageInput = ref(null)

// 界面状态
const isDarkMode = ref(false)
const isVoiceMode = ref(false)
const isRecording = ref(false)
const isLoading = ref(false)
const sidePanelOpen = ref(false)
const showQuickActions = ref(false)
const showEmojiPicker = ref(false)
const showSettings = ref(false)
const showCharacterSelector = ref(false)

// 输入相关
const inputMessage = ref('')
const inputStatus = ref('')
const uploadProgress = ref(0)

// 从store获取数据
const messages = computed(() => chatStore.messages)
const currentCharacter = computed(() => chatStore.currentCharacter)
const isTyping = computed(() => chatStore.isTyping)
const isConnected = computed(() => chatStore.isConnected)

// 用户数据
const userAvatar = ref('https://picsum.photos/40/40?random=1')

// 情感分析
const emotionAnalysis = ref(null)

// 对话统计
const conversationStats = reactive({
  messageCount: 0,
  duration: '0分钟'
})

// 设置
const appSettings = ref({
  theme: 'light',
  language: 'zh-CN',
  autoSave: true,
  notifications: true
})

const autoPlayVoice = ref(false)

// 表情符号
const commonEmojis = ['😀', '😂', '🤔', '👍', '❤️', '🎉', '🔥', '💡', '🚀', '⭐']

// 可用角色列表
const availableCharacters = ref([
  {
    id: 1,
    name: 'AI助手',
    avatar: 'https://picsum.photos/40/40?random=1'
  },
  {
    id: 2,
    name: '孔子',
    avatar: 'https://picsum.photos/40/40?random=2'
  },
  {
    id: 3,
    name: '爱因斯坦',
    avatar: 'https://picsum.photos/40/40?random=3'
  }
])

// 计算属性
const canSend = computed(() => {
  return inputMessage.value.trim().length > 0 && !isTyping.value
})

const inputPlaceholder = computed(() => {
  if (isRecording.value) return '正在录音...'
  if (isTyping.value) return 'AI正在思考...'
  return '输入消息...'
})

// 方法
const sendMessage = async () => {
  if (!canSend.value) return
  
  const messageText = inputMessage.value.trim()
  if (!messageText) return
  
  // 通过store发送消息（支持WebSocket）
  chatStore.sendMessage(messageText, 'text')
  
  inputMessage.value = ''
  
  // 滚动到底部
  await nextTick()
  scrollToBottom()
  
  // 如果未连接WebSocket，模拟AI回复
  if (!chatStore.isConnected) {
    chatStore.setTyping(true)
    
    setTimeout(() => {
      chatStore.addMessage({
        type: 'ai',
        content: `收到您的消息："${messageText}"。这是一个模拟回复。`,
        isUser: false,
        avatar: currentCharacter.value.avatar,
        model: 'DeepSeek-V2'
      })
      
      chatStore.setTyping(false)
      
      nextTick(() => {
        scrollToBottom()
      })
    }, 1000 + Math.random() * 2000)
  }
}

const streamText = (message) => {
  const fullText = message.content
  let currentIndex = 0
  
  const streamInterval = setInterval(() => {
    if (currentIndex < fullText.length) {
      message.displayText += fullText[currentIndex]
      currentIndex++
      scrollToBottom()
    } else {
      message.isStreaming = false
      isLoading.value = false
      clearInterval(streamInterval)
    }
  }, 50)
}

const generateAIResponse = (userMessage) => {
  const responses = [
    '这是一个很有趣的问题！让我来为你详细分析一下。',
    '我理解你的想法，这确实值得深入探讨。',
    '从多个角度来看，这个话题有很多值得思考的地方。',
    '你提出了一个很好的观点，我想分享一些相关的见解。',
    '这让我想到了一些相关的概念，我们可以一起探索。'
  ]
  return responses[Math.floor(Math.random() * responses.length)]
}

const handleKeyDown = (event) => {
  if (event.key === 'Enter' && !event.shiftKey) {
    event.preventDefault()
    sendMessage()
  }
}

const handleInput = () => {
  // 自动调整输入框高度
  const textarea = messageInput.value
  textarea.style.height = 'auto'
  textarea.style.height = Math.min(textarea.scrollHeight, 120) + 'px'
}

const scrollToBottom = () => {
  if (messagesContainer.value) {
    messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
  }
}

const formatTime = (timestamp) => {
  return new Date(timestamp).toLocaleTimeString('zh-CN', {
    hour: '2-digit',
    minute: '2-digit'
  })
}

// 功能方法
const toggleDarkMode = () => {
  isDarkMode.value = !isDarkMode.value
  saveSettings()
}

const toggleVoiceMode = () => {
  isVoiceMode.value = !isVoiceMode.value
}

const toggleQuickActions = () => {
  showQuickActions.value = !showQuickActions.value
}

const toggleEmojiPicker = () => {
  showEmojiPicker.value = !showEmojiPicker.value
}

const toggleVoiceInput = () => {
  isRecording.value = !isRecording.value
  // 这里实现语音录制逻辑
}

const openSettings = () => {
  showSettings.value = true
}

const closeSettings = () => {
  showSettings.value = false
}

const closeSidePanel = () => {
  sidePanelOpen.value = false
}

const closeEmojiPicker = () => {
  showEmojiPicker.value = false
}

const insertQuickText = (text) => {
  inputMessage.value = text
  showQuickActions.value = false
  messageInput.value?.focus()
}

const insertEmoji = (emoji) => {
  inputMessage.value += emoji
  showEmojiPicker.value = false
  messageInput.value?.focus()
}

const copyMessage = (message) => {
  navigator.clipboard.writeText(message.content)
  // 显示复制成功提示
}

const speakMessage = (message) => {
  // 实现语音播放
  console.log('播放语音:', message.content)
}

const likeMessage = (message) => {
  message.liked = !message.liked
}

const switchCharacter = (character) => {
  currentCharacter.value = character
  // 发送系统消息
  messages.value.push({
    id: Date.now(),
    type: 'system',
    content: `已切换到 ${character.name}`,
    timestamp: new Date()
  })
}

const saveSettings = () => {
  const settings = {
    isDarkMode: isDarkMode.value,
    isVoiceMode: isVoiceMode.value,
    autoPlayVoice: autoPlayVoice.value
  }
  localStorage.setItem('chatSettings', JSON.stringify(settings))
}

const loadSettings = () => {
  const saved = localStorage.getItem('chatSettings')
  if (saved) {
    const savedSettings = JSON.parse(saved)
    isDarkMode.value = savedSettings.isDarkMode || false
    isVoiceMode.value = savedSettings.isVoiceMode || false
    autoPlayVoice.value = savedSettings.autoPlayVoice || false
  }
}

const onInputFocus = () => {
  // 输入框获得焦点时的处理
}

const onInputBlur = () => {
  // 输入框失去焦点时的处理
}

const openImageUpload = () => {
  // 实现图片上传
  console.log('打开图片上传')
}

const getEmotionIcon = (emotion) => {
  const icons = {
    joy: '😊',
    sadness: '😢',
    anger: '😠',
    fear: '😨',
    surprise: '😲'
  }
  return icons[emotion] || '😐'
}

// 设置相关方法
const updateSettings = (newSettings) => {
  Object.assign(appSettings.value, newSettings)
  
  // 应用主题
  if (newSettings.theme) {
    isDarkMode.value = newSettings.theme === 'dark'
  }
  
  showSettings.value = false
}

const selectCharacter = (character) => {
  chatStore.setCurrentCharacter(character)
  showCharacterSelector.value = false
  
  // 添加角色切换消息
  chatStore.addMessage({
    type: 'system',
    content: `已切换到 ${character.name}`,
    isUser: false
  })
}

// 生命周期
onMounted(async () => {
  loadSettings()
  
  // 初始化聊天store
  chatStore.initializeChat()
  
  // 尝试连接WebSocket
  const connected = await chatStore.connectWebSocket()
  if (connected) {
    console.log('WebSocket连接成功')
    // 可以加入默认房间
    chatStore.joinRoom('general')
  } else {
    console.log('WebSocket连接失败，使用离线模式')
  }
  
  conversationStats.messageCount = chatStore.messageCount
})

onUnmounted(() => {
  // 清理资源
  chatStore.disconnectWebSocket()
})

// 监听暗黑模式变化
watch(isDarkMode, (newValue) => {
  document.documentElement.setAttribute('data-theme', newValue ? 'dark' : 'light')
})
</script>

<style scoped>
/* 基础样式 */
.modern-chat-container {
  position: relative;
  width: 100%;
  height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  overflow: hidden;
  font-family: 'Inter', -apple-system, BlinkMacSystemFont, sans-serif;
  transition: all 0.3s ease;
}

.modern-chat-container.dark-mode {
  background: linear-gradient(135deg, #1a1a2e 0%, #16213e 100%);
}

/* 3D背景效果 */
.background-3d {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  pointer-events: none;
  overflow: hidden;
}

.floating-particles {
  position: absolute;
  width: 100%;
  height: 100%;
  background-image: 
    radial-gradient(2px 2px at 20px 30px, rgba(255,255,255,0.3), transparent),
    radial-gradient(2px 2px at 40px 70px, rgba(255,255,255,0.2), transparent),
    radial-gradient(1px 1px at 90px 40px, rgba(255,255,255,0.4), transparent);
  background-repeat: repeat;
  background-size: 200px 200px;
  animation: float 20s linear infinite;
}

@keyframes float {
  0% { transform: translateY(0px) translateX(0px); }
  33% { transform: translateY(-10px) translateX(10px); }
  66% { transform: translateY(5px) translateX(-5px); }
  100% { transform: translateY(0px) translateX(0px); }
}

.gradient-orbs {
  position: absolute;
  width: 100%;
  height: 100%;
}

.orb {
  position: absolute;
  border-radius: 50%;
  filter: blur(40px);
  opacity: 0.6;
  animation: orbit 15s linear infinite;
}

.orb-1 {
  width: 200px;
  height: 200px;
  background: radial-gradient(circle, #ff6b6b, #ee5a24);
  top: 20%;
  left: 10%;
  animation-delay: 0s;
}

.orb-2 {
  width: 150px;
  height: 150px;
  background: radial-gradient(circle, #4ecdc4, #45b7aa);
  top: 60%;
  right: 20%;
  animation-delay: -5s;
}

.orb-3 {
  width: 100px;
  height: 100px;
  background: radial-gradient(circle, #a8e6cf, #7fcdcd);
  bottom: 30%;
  left: 60%;
  animation-delay: -10s;
}

@keyframes orbit {
  0% { transform: rotate(0deg) translateX(50px) rotate(0deg); }
  100% { transform: rotate(360deg) translateX(50px) rotate(-360deg); }
}

/* 主聊天界面 */
.chat-interface {
  position: relative;
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  backdrop-filter: blur(10px);
  background: rgba(255, 255, 255, 0.1);
  border-radius: 20px;
  margin: 20px;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.1);
  overflow: hidden;
}

.dark-mode .chat-interface {
  background: rgba(0, 0, 0, 0.3);
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.3);
}

/* 顶部导航栏 */
.chat-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 30px;
  background: rgba(255, 255, 255, 0.2);
  backdrop-filter: blur(20px);
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.dark-mode .chat-header {
  background: rgba(0, 0, 0, 0.2);
  border-bottom: 1px solid rgba(255, 255, 255, 0.05);
}

.header-left {
  display: flex;
  align-items: center;
  gap: 15px;
}

.avatar-container {
  position: relative;
}

.character-avatar {
  width: 50px;
  height: 50px;
  border-radius: 50%;
  border: 3px solid rgba(255, 255, 255, 0.3);
  transition: all 0.3s ease;
}

.character-avatar:hover {
  transform: scale(1.1);
  border-color: rgba(255, 255, 255, 0.6);
}

.status-indicator {
  position: absolute;
  bottom: 2px;
  right: 2px;
  width: 12px;
  height: 12px;
  border-radius: 50%;
  border: 2px solid white;
}

.status-indicator.online {
  background: #4ecdc4;
  animation: pulse 2s infinite;
}

@keyframes pulse {
  0% { box-shadow: 0 0 0 0 rgba(78, 205, 196, 0.7); }
  70% { box-shadow: 0 0 0 10px rgba(78, 205, 196, 0); }
  100% { box-shadow: 0 0 0 0 rgba(78, 205, 196, 0); }
}

.character-info h3 {
  margin: 0;
  color: white;
  font-size: 18px;
  font-weight: 600;
}

.character-status {
  margin: 0;
  color: rgba(255, 255, 255, 0.7);
  font-size: 14px;
}

.header-actions {
  display: flex;
  gap: 10px;
}

.action-btn {
  width: 40px;
  height: 40px;
  border: none;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.2);
  color: white;
  cursor: pointer;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  justify-content: center;
}

.action-btn:hover {
  background: rgba(255, 255, 255, 0.3);
  transform: scale(1.1);
}

.action-btn.active {
  background: #4ecdc4;
}

/* 消息区域 */
.messages-container {
  flex: 1;
  overflow-y: auto;
  padding: 20px 30px;
  scroll-behavior: smooth;
}

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

.message-item {
  margin-bottom: 20px;
}

.message {
  display: flex;
  align-items: flex-end;
  gap: 12px;
  max-width: 80%;
}

.user-message {
  flex-direction: row-reverse;
  margin-left: auto;
}

.ai-message {
  margin-right: auto;
}

.message-avatar img {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  border: 2px solid rgba(255, 255, 255, 0.3);
}

.message-content {
  flex: 1;
}

.message-bubble {
  position: relative;
  padding: 15px 20px;
  border-radius: 20px;
  backdrop-filter: blur(10px);
  transition: all 0.3s ease;
}

.user-message .message-bubble {
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: white;
  border-bottom-right-radius: 5px;
}

.ai-message .message-bubble {
  background: rgba(255, 255, 255, 0.9);
  color: #333;
  border-bottom-left-radius: 5px;
}

.dark-mode .ai-message .message-bubble {
  background: rgba(255, 255, 255, 0.1);
  color: white;
}

.message-bubble:hover {
  transform: translateY(-2px);
  box-shadow: 0 10px 20px rgba(0, 0, 0, 0.1);
}

.message-bubble p {
  margin: 0;
  line-height: 1.5;
  word-wrap: break-word;
}

.streaming-text .char {
  opacity: 0;
  animation: fadeInChar 0.1s ease forwards;
}

@keyframes fadeInChar {
  to { opacity: 1; }
}

.cursor {
  animation: blink 1s infinite;
}

@keyframes blink {
  0%, 50% { opacity: 1; }
  51%, 100% { opacity: 0; }
}

.message-actions {
  display: flex;
  gap: 8px;
  margin-top: 10px;
  opacity: 0;
  transition: opacity 0.3s ease;
}

.message-bubble:hover .message-actions {
  opacity: 1;
}

.action-btn-small {
  width: 28px;
  height: 28px;
  border: none;
  border-radius: 50%;
  background: rgba(0, 0, 0, 0.1);
  cursor: pointer;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  justify-content: center;
}

.action-btn-small:hover {
  background: rgba(0, 0, 0, 0.2);
  transform: scale(1.1);
}

.action-btn-small.active {
  background: #ff6b6b;
  color: white;
}

.message-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 8px;
  font-size: 12px;
  opacity: 0.7;
}

.system-message {
  text-align: center;
  margin: 20px 0;
}

.system-content {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 8px 16px;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 20px;
  color: white;
  font-size: 14px;
}

/* 打字指示器 */
.typing-indicator {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 20px;
}

.typing-avatar img {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  border: 2px solid rgba(255, 255, 255, 0.3);
}

.typing-animation {
  display: flex;
  gap: 4px;
  padding: 15px 20px;
  background: rgba(255, 255, 255, 0.9);
  border-radius: 20px;
  border-bottom-left-radius: 5px;
}

.dark-mode .typing-animation {
  background: rgba(255, 255, 255, 0.1);
}

.typing-animation .dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #999;
  animation: typing 1.4s infinite ease-in-out;
}

.typing-animation .dot:nth-child(1) { animation-delay: -0.32s; }
.typing-animation .dot:nth-child(2) { animation-delay: -0.16s; }

@keyframes typing {
  0%, 80%, 100% { transform: scale(0.8); opacity: 0.5; }
  40% { transform: scale(1); opacity: 1; }
}

/* 输入区域 */
.input-section {
  padding: 20px 30px;
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(20px);
  border-top: 1px solid rgba(255, 255, 255, 0.1);
}

.dark-mode .input-section {
  background: rgba(0, 0, 0, 0.2);
  border-top: 1px solid rgba(255, 255, 255, 0.05);
}

.quick-actions {
  display: flex;
  gap: 10px;
  margin-bottom: 15px;
  padding: 10px 0;
  animation: slideDown 0.3s ease;
}

@keyframes slideDown {
  from { opacity: 0; transform: translateY(-10px); }
  to { opacity: 1; transform: translateY(0); }
}

.quick-btn {
  padding: 8px 16px;
  border: none;
  border-radius: 20px;
  background: rgba(255, 255, 255, 0.2);
  color: white;
  cursor: pointer;
  transition: all 0.3s ease;
  font-size: 14px;
}

.quick-btn:hover {
  background: rgba(255, 255, 255, 0.3);
  transform: translateY(-2px);
}

.input-container {
  position: relative;
}

.input-wrapper {
  display: flex;
  align-items: flex-end;
  gap: 12px;
  background: rgba(255, 255, 255, 0.9);
  border-radius: 25px;
  padding: 8px;
  transition: all 0.3s ease;
}

.dark-mode .input-wrapper {
  background: rgba(255, 255, 255, 0.1);
}

.input-wrapper:focus-within {
  box-shadow: 0 0 0 2px rgba(102, 126, 234, 0.5);
}

.attachment-btn {
  width: 40px;
  height: 40px;
  border: none;
  border-radius: 50%;
  background: transparent;
  color: #666;
  cursor: pointer;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  justify-content: center;
}

.attachment-btn:hover {
  background: rgba(0, 0, 0, 0.1);
}

.attachment-btn .icon-plus.rotated {
  transform: rotate(45deg);
}

.text-input-container {
  flex: 1;
  position: relative;
}

.message-input {
  width: 100%;
  border: none;
  outline: none;
  background: transparent;
  resize: none;
  font-size: 16px;
  line-height: 1.5;
  padding: 8px 12px;
  color: #333;
  min-height: 24px;
  max-height: 120px;
}

.dark-mode .message-input {
  color: white;
}

.message-input::placeholder {
  color: #999;
}

.input-enhancements {
  position: absolute;
  right: 8px;
  top: 50%;
  transform: translateY(-50%);
  display: flex;
  gap: 4px;
}

.enhancement-btn {
  width: 32px;
  height: 32px;
  border: none;
  border-radius: 50%;
  background: transparent;
  color: #666;
  cursor: pointer;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  justify-content: center;
}

.enhancement-btn:hover {
  background: rgba(0, 0, 0, 0.1);
}

.enhancement-btn.active {
  background: #ff6b6b;
  color: white;
}

.send-btn {
  width: 40px;
  height: 40px;
  border: none;
  border-radius: 50%;
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: white;
  cursor: pointer;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  justify-content: center;
}

.send-btn:hover:not(:disabled) {
  transform: scale(1.1);
  box-shadow: 0 5px 15px rgba(102, 126, 234, 0.4);
}

.send-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.send-btn.loading {
  animation: pulse 1s infinite;
}

.loading-spinner {
  width: 20px;
  height: 20px;
  border: 2px solid rgba(255, 255, 255, 0.3);
  border-top: 2px solid white;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.input-status {
  margin-top: 10px;
  font-size: 14px;
  color: rgba(255, 255, 255, 0.7);
}

.status-progress {
  width: 100%;
  height: 4px;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 2px;
  margin-top: 5px;
  overflow: hidden;
}

.progress-bar {
  height: 100%;
  background: linear-gradient(90deg, #667eea, #764ba2);
  border-radius: 2px;
  transition: width 0.3s ease;
}

/* 消息动画 */
.message-enter-active {
  transition: all 0.3s ease;
}

.message-enter-from {
  opacity: 0;
  transform: translateY(20px);
}

.message-leave-active {
  transition: all 0.3s ease;
}

.message-leave-to {
  opacity: 0;
  transform: translateX(-20px);
}

/* 图标样式 */
.icon-mic::before { content: '🎤'; }
.icon-sun::before { content: '☀️'; }
.icon-moon::before { content: '🌙'; }
.icon-settings::before { content: '⚙️'; }
.icon-send::before { content: '➤'; }
.icon-plus::before { content: '+'; }
.icon-emoji::before { content: '😊'; }
.icon-copy::before { content: '📋'; }
.icon-volume::before { content: '🔊'; }
.icon-heart::before { content: '❤️'; }
.icon-info::before { content: 'ℹ️'; }
.icon-close::before { content: '✕'; }

/* 响应式设计 */
@media (max-width: 768px) {
  .modern-chat-container {
    margin: 0;
    border-radius: 0;
  }
  
  .chat-interface {
    margin: 0;
    border-radius: 0;
    height: 100vh;
  }
  
  .chat-header {
    padding: 15px 20px;
  }
  
  .messages-container {
    padding: 15px 20px;
  }
  
  .input-section {
    padding: 15px 20px;
  }
  
  .message {
    max-width: 90%;
  }
  
  .character-avatar {
    width: 40px;
    height: 40px;
  }
  
  .message-avatar img {
    width: 32px;
    height: 32px;
  }
}

/* 侧边面板和模态框样式省略，保持简洁 */
.side-panel,
.emoji-picker-overlay,
.settings-overlay {
  /* 这些样式会在后续完善 */
  display: none;
}
</style>