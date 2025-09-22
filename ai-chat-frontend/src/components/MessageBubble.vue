<template>
  <div 
    :class="['message-bubble', `message-${message.role}`, { 'typing': isTyping, 'error': message.error }]"
    :data-message-id="message.id"
  >
    <!-- 用户头像 -->
    <div v-if="showAvatar" class="avatar-container">
      <div class="avatar" :style="avatarStyle">
        <img v-if="avatarUrl" :src="avatarUrl" :alt="`${message.role} avatar`" />
        <div v-else class="avatar-placeholder">
          {{ avatarText }}
        </div>
      </div>
      <div v-if="message.role === 'assistant'" class="status-indicator" :class="statusClass">
        <div class="status-dot"></div>
      </div>
    </div>

    <!-- 消息内容容器 -->
    <div class="message-container">
      <!-- 消息头部信息 -->
      <div v-if="showHeader" class="message-header">
        <span class="sender-name">{{ senderName }}</span>
        <span class="timestamp">{{ formatTime(message.timestamp) }}</span>
        <div v-if="message.role === 'assistant' && message.model" class="model-info">
          <span class="model-name">{{ message.model }}</span>
        </div>
      </div>

      <!-- 消息内容 -->
      <div class="message-content">
        <!-- 文本内容 -->
        <div v-if="message.content" class="text-content">
          <div v-if="isTyping" class="typing-animation">
            <span v-for="char in displayedText" :key="char.id" class="char" :style="{ animationDelay: `${char.delay}ms` }">
              {{ char.text }}
            </span>
            <span class="cursor">|</span>
          </div>
          <div v-else class="formatted-text" v-html="formattedContent"></div>
        </div>

        <!-- 图片内容 -->
        <div v-if="message.images && message.images.length > 0" class="image-content">
          <div class="image-gallery" :class="{ 'multiple': message.images.length > 1 }">
            <div 
              v-for="(image, index) in message.images" 
              :key="index"
              class="image-item"
              @click="openImagePreview(image, index)"
            >
              <img 
                :src="image.url" 
                :alt="image.alt || `Image ${index + 1}`"
                :loading="index < 2 ? 'eager' : 'lazy'"
                @load="onImageLoad"
                @error="onImageError"
              />
              <div v-if="image.caption" class="image-caption">
                {{ image.caption }}
              </div>
              <div class="image-overlay">
                <button class="preview-btn" :aria-label="`预览图片 ${index + 1}`">
                  <svg viewBox="0 0 24 24">
                    <path d="M12 4.5C7 4.5 2.73 7.61 1 12c1.73 4.39 6 7.5 11 7.5s9.27-3.11 11-7.5c-1.73-4.39-6-7.5-11-7.5zM12 17c-2.76 0-5-2.24-5-5s2.24-5 5-5 5 2.24 5 5-2.24 5-5 5zm0-8c-1.66 0-3 1.34-3 3s1.34 3 3 3 3-1.34 3-3-1.34-3-3-3z"/>
                  </svg>
                </button>
              </div>
            </div>
          </div>
        </div>

        <!-- 音频内容 -->
        <div v-if="message.audio" class="audio-content">
          <div class="audio-player">
            <button 
              class="play-btn" 
              @click="toggleAudio"
              :class="{ 'playing': isAudioPlaying }"
            >
              <svg v-if="!isAudioPlaying" viewBox="0 0 24 24">
                <path d="M8 5v14l11-7z"/>
              </svg>
              <svg v-else viewBox="0 0 24 24">
                <path d="M6 19h4V5H6v14zm8-14v14h4V5h-4z"/>
              </svg>
            </button>
            <div class="audio-progress">
              <div class="progress-bar" :style="{ width: `${audioProgress}%` }"></div>
            </div>
            <span class="audio-duration">{{ formatDuration(message.audio.duration) }}</span>
          </div>
          <audio 
            ref="audioElement"
            :src="message.audio.url"
            @timeupdate="updateAudioProgress"
            @ended="onAudioEnded"
            @loadedmetadata="onAudioLoaded"
          ></audio>
        </div>

        <!-- 文件内容 -->
        <div v-if="message.files && message.files.length > 0" class="file-content">
          <div 
            v-for="(file, index) in message.files" 
            :key="index"
            class="file-item"
            @click="downloadFile(file)"
          >
            <div class="file-icon">
              <svg viewBox="0 0 24 24">
                <path d="M14,2H6A2,2 0 0,0 4,4V20A2,2 0 0,0 6,22H18A2,2 0 0,0 20,20V8L14,2M18,20H6V4H13V9H18V20Z"/>
              </svg>
            </div>
            <div class="file-info">
              <div class="file-name">{{ file.name }}</div>
              <div class="file-size">{{ formatFileSize(file.size) }}</div>
            </div>
            <div class="download-icon">
              <svg viewBox="0 0 24 24">
                <path d="M5,20H19V18H5M19,9H15V3H9V9H5L12,16L19,9Z"/>
              </svg>
            </div>
          </div>
        </div>

        <!-- 代码块 -->
        <div v-if="message.code" class="code-content">
          <div class="code-header">
            <span class="language">{{ message.code.language || 'text' }}</span>
            <button class="copy-btn" @click="copyCode" :class="{ 'copied': codeCopied }">
              <svg v-if="!codeCopied" viewBox="0 0 24 24">
                <path d="M19,21H8V7H19M19,5H8A2,2 0 0,0 6,7V21A2,2 0 0,0 8,23H19A2,2 0 0,0 21,21V7A2,2 0 0,0 19,5M16,1H4A2,2 0 0,0 2,3V17H4V3H16V1Z"/>
              </svg>
              <svg v-else viewBox="0 0 24 24">
                <path d="M21,7L9,19L3.5,13.5L4.91,12.09L9,16.17L19.59,5.59L21,7Z"/>
              </svg>
              {{ codeCopied ? '已复制' : '复制' }}
            </button>
          </div>
          <pre class="code-block"><code :class="`language-${message.code.language}`">{{ message.code.content }}</code></pre>
        </div>

        <!-- 错误信息 -->
        <div v-if="message.error" class="error-content">
          <div class="error-icon">⚠️</div>
          <div class="error-text">{{ message.error }}</div>
          <button v-if="onRetry" class="retry-btn" @click="onRetry">
            重试
          </button>
        </div>
      </div>

      <!-- 消息操作 -->
      <div v-if="showActions" class="message-actions">
        <button 
          v-if="message.role === 'assistant'" 
          class="action-btn" 
          @click="copyMessage"
          :class="{ 'copied': messageCopied }"
          title="复制消息"
        >
          <svg viewBox="0 0 24 24">
            <path d="M19,21H8V7H19M19,5H8A2,2 0 0,0 6,7V21A2,2 0 0,0 8,23H19A2,2 0 0,0 21,21V7A2,2 0 0,0 19,5M16,1H4A2,2 0 0,0 2,3V17H4V3H16V1Z"/>
          </svg>
        </button>
        
        <button 
          v-if="message.role === 'assistant'" 
          class="action-btn" 
          @click="likeMessage"
          :class="{ 'active': message.liked }"
          title="点赞"
        >
          <svg viewBox="0 0 24 24">
            <path d="M23,10C23,8.89 22.1,8 21,8H14.68L15.64,3.43C15.66,3.33 15.67,3.22 15.67,3.11C15.67,2.7 15.5,2.32 15.23,2.05L14.17,1L7.59,7.58C7.22,7.95 7,8.45 7,9V19A2,2 0 0,0 9,21H18C18.83,21 19.54,20.5 19.84,19.78L22.86,12.73C22.95,12.5 23,12.26 23,12V10.08L23,10M1,21H5V9H1V21Z"/>
          </svg>
        </button>
        
        <button 
          v-if="message.role === 'assistant'" 
          class="action-btn" 
          @click="dislikeMessage"
          :class="{ 'active': message.disliked }"
          title="踩"
        >
          <svg viewBox="0 0 24 24">
            <path d="M19,15H23V3H19V15M15,3H6C5.17,3 4.46,3.5 4.16,4.22L1.14,11.27C1.05,11.5 1,11.74 1,12V14A2,2 0 0,0 3,16H9.31L8.36,20.57C8.34,20.67 8.33,20.78 8.33,20.89C8.33,21.3 8.5,21.68 8.77,21.95L9.83,23L16.41,16.42C16.78,16.05 17,15.55 17,15V5A2,2 0 0,0 15,3Z"/>
          </svg>
        </button>
        
        <button 
          class="action-btn" 
          @click="shareMessage"
          title="分享"
        >
          <svg viewBox="0 0 24 24">
            <path d="M18,16.08C17.24,16.08 16.56,16.38 16.04,16.85L8.91,12.7C8.96,12.47 9,12.24 9,12C9,11.76 8.96,11.53 8.91,11.3L15.96,7.19C16.5,7.69 17.21,8 18,8A3,3 0 0,0 21,5A3,3 0 0,0 18,2A3,3 0 0,0 15,5C15,5.24 15.04,5.47 15.09,5.7L8.04,9.81C7.5,9.31 6.79,9 6,9A3,3 0 0,0 3,12A3,3 0 0,0 6,15C6.79,15 7.5,14.69 8.04,14.19L15.16,18.34C15.11,18.55 15.08,18.77 15.08,19C15.08,20.61 16.39,21.91 18,21.91C19.61,21.91 20.92,20.61 20.92,19A2.92,2.92 0 0,0 18,16.08Z"/>
          </svg>
        </button>
      </div>

      <!-- 消息状态 -->
      <div v-if="showStatus" class="message-status">
        <span v-if="message.status === 'sending'" class="status-text">发送中...</span>
        <span v-else-if="message.status === 'sent'" class="status-text">已发送</span>
        <span v-else-if="message.status === 'delivered'" class="status-text">已送达</span>
        <span v-else-if="message.status === 'read'" class="status-text">已读</span>
        <span v-else-if="message.status === 'failed'" class="status-text error">发送失败</span>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted, nextTick } from 'vue'

// 消息接口定义
interface MessageImage {
  url: string
  alt?: string
  caption?: string
}

interface MessageAudio {
  url: string
  duration: number
}

interface MessageFile {
  name: string
  url: string
  size: number
  type: string
}

interface MessageCode {
  language: string
  content: string
}

interface Message {
  id: string
  role: 'user' | 'assistant'
  content?: string
  images?: MessageImage[]
  audio?: MessageAudio
  files?: MessageFile[]
  code?: MessageCode
  timestamp: number
  status?: 'sending' | 'sent' | 'delivered' | 'read' | 'failed'
  error?: string
  model?: string
  liked?: boolean
  disliked?: boolean
}

// Props
interface Props {
  message: Message
  showAvatar?: boolean
  showHeader?: boolean
  showActions?: boolean
  showStatus?: boolean
  isTyping?: boolean
  typingSpeed?: number
  onRetry?: () => void
}

const props = withDefaults(defineProps<Props>(), {
  showAvatar: true,
  showHeader: true,
  showActions: true,
  showStatus: true,
  isTyping: false,
  typingSpeed: 50
})

// Emits
const emit = defineEmits<{
  imagePreview: [image: MessageImage, index: number]
  messageCopy: [content: string]
  messageLike: [messageId: string]
  messageDislike: [messageId: string]
  messageShare: [message: Message]
}>()

// 响应式数据
const audioElement = ref<HTMLAudioElement>()
const isAudioPlaying = ref(false)
const audioProgress = ref(0)
const messageCopied = ref(false)
const codeCopied = ref(false)
const displayedText = ref<Array<{ id: number, text: string, delay: number }>>([])

// 计算属性
const avatarUrl = computed(() => {
  if (props.message.role === 'user') {
    return 'https://picsum.photos/40/40?random=user'
  } else {
    return 'https://picsum.photos/40/40?random=ai'
  }
})

const avatarText = computed(() => {
  return props.message.role === 'user' ? 'U' : 'AI'
})

const avatarStyle = computed(() => {
  return {
    background: props.message.role === 'user' 
      ? 'var(--primary-gradient)' 
      : 'var(--secondary-gradient)'
  }
})

const senderName = computed(() => {
  return props.message.role === 'user' ? '我' : 'AI助手'
})

const statusClass = computed(() => {
  return {
    'online': true,
    'thinking': props.isTyping
  }
})

const formattedContent = computed(() => {
  if (!props.message.content) return ''
  
  // 简单的 Markdown 渲染
  let content = props.message.content
  
  // 代码块
  content = content.replace(/```(\w+)?\n([\s\S]*?)```/g, (match, lang, code) => {
    return `<pre class="code-block"><code class="language-${lang || 'text'}">${escapeHtml(code.trim())}</code></pre>`
  })
  
  // 行内代码
  content = content.replace(/`([^`]+)`/g, '<code class="inline-code">$1</code>')
  
  // 粗体
  content = content.replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>')
  
  // 斜体
  content = content.replace(/\*(.*?)\*/g, '<em>$1</em>')
  
  // 链接
  content = content.replace(/\[([^\]]+)\]\(([^)]+)\)/g, '<a href="$2" target="_blank" rel="noopener noreferrer">$1</a>')
  
  // 换行
  content = content.replace(/\n/g, '<br>')
  
  return content
})

// 方法
const escapeHtml = (text: string) => {
  const div = document.createElement('div')
  div.textContent = text
  return div.innerHTML
}

const formatTime = (timestamp: number) => {
  const date = new Date(timestamp)
  const now = new Date()
  const diff = now.getTime() - date.getTime()
  
  if (diff < 60000) { // 1分钟内
    return '刚刚'
  } else if (diff < 3600000) { // 1小时内
    return `${Math.floor(diff / 60000)}分钟前`
  } else if (diff < 86400000) { // 24小时内
    return `${Math.floor(diff / 3600000)}小时前`
  } else {
    return date.toLocaleDateString('zh-CN', {
      month: 'short',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    })
  }
}

const formatDuration = (seconds: number) => {
  const mins = Math.floor(seconds / 60)
  const secs = Math.floor(seconds % 60)
  return `${mins}:${secs.toString().padStart(2, '0')}`
}

const formatFileSize = (bytes: number) => {
  if (bytes === 0) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
}

// 打字机效果
const startTypingAnimation = async () => {
  if (!props.isTyping || !props.message.content) return
  
  displayedText.value = []
  const text = props.message.content
  
  for (let i = 0; i < text.length; i++) {
    displayedText.value.push({
      id: i,
      text: text[i],
      delay: i * props.typingSpeed
    })
    
    await new Promise(resolve => setTimeout(resolve, props.typingSpeed))
  }
}

// 音频控制
const toggleAudio = () => {
  if (!audioElement.value) return
  
  if (isAudioPlaying.value) {
    audioElement.value.pause()
  } else {
    audioElement.value.play()
  }
}

const updateAudioProgress = () => {
  if (!audioElement.value) return
  
  const { currentTime, duration } = audioElement.value
  audioProgress.value = (currentTime / duration) * 100
}

const onAudioEnded = () => {
  isAudioPlaying.value = false
  audioProgress.value = 0
}

const onAudioLoaded = () => {
  // 音频加载完成
}

// 图片处理
const openImagePreview = (image: MessageImage, index: number) => {
  emit('imagePreview', image, index)
}

const onImageLoad = (event: Event) => {
  // 图片加载完成
  const img = event.target as HTMLImageElement
  img.classList.add('loaded')
}

const onImageError = (event: Event) => {
  // 图片加载失败
  const img = event.target as HTMLImageElement
  img.src = 'https://picsum.photos/300/200?random=error'
}

// 文件下载
const downloadFile = (file: MessageFile) => {
  const link = document.createElement('a')
  link.href = file.url
  link.download = file.name
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
}

// 代码复制
const copyCode = async () => {
  if (!props.message.code) return
  
  try {
    await navigator.clipboard.writeText(props.message.code.content)
    codeCopied.value = true
    setTimeout(() => {
      codeCopied.value = false
    }, 2000)
  } catch (err) {
    console.error('复制失败:', err)
  }
}

// 消息操作
const copyMessage = async () => {
  if (!props.message.content) return
  
  try {
    await navigator.clipboard.writeText(props.message.content)
    messageCopied.value = true
    emit('messageCopy', props.message.content)
    setTimeout(() => {
      messageCopied.value = false
    }, 2000)
  } catch (err) {
    console.error('复制失败:', err)
  }
}

const likeMessage = () => {
  emit('messageLike', props.message.id)
}

const dislikeMessage = () => {
  emit('messageDislike', props.message.id)
}

const shareMessage = () => {
  emit('messageShare', props.message)
}

// 生命周期
onMounted(() => {
  if (props.isTyping) {
    startTypingAnimation()
  }
  
  // 音频事件监听
  if (audioElement.value) {
    audioElement.value.addEventListener('play', () => {
      isAudioPlaying.value = true
    })
    
    audioElement.value.addEventListener('pause', () => {
      isAudioPlaying.value = false
    })
  }
})

onUnmounted(() => {
  if (audioElement.value) {
    audioElement.value.pause()
  }
})
</script>

<style scoped>
.message-bubble {
  display: flex;
  gap: 0.75rem;
  margin-bottom: 1.5rem;
  animation: slideUp 0.3s ease;
  max-width: 100%;
}

.message-user {
  flex-direction: row-reverse;
}

.message-user .message-container {
  align-items: flex-end;
}

.message-user .message-content {
  background: var(--primary-gradient);
  color: white;
}

.message-assistant .message-content {
  background: var(--background-light);
  backdrop-filter: var(--blur-effect);
  border: 1px solid var(--border-light);
  color: var(--text-primary);
}

/* 头像 */
.avatar-container {
  position: relative;
  flex-shrink: 0;
}

.avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: var(--shadow-light);
}

.avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.avatar-placeholder {
  font-weight: 600;
  color: white;
  font-size: 0.9rem;
}

.status-indicator {
  position: absolute;
  bottom: -2px;
  right: -2px;
  width: 12px;
  height: 12px;
  border-radius: 50%;
  background: white;
  display: flex;
  align-items: center;
  justify-content: center;
}

.status-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: var(--success-color);
}

.status-indicator.thinking .status-dot {
  background: var(--warning-color);
  animation: pulse 1.5s infinite;
}

/* 消息容器 */
.message-container {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-width: 0;
}

.message-header {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  margin-bottom: 0.5rem;
  font-size: 0.8rem;
  color: var(--text-secondary);
}

.sender-name {
  font-weight: 600;
}

.timestamp {
  opacity: 0.7;
}

.model-info {
  margin-left: auto;
}

.model-name {
  background: var(--border-light);
  padding: 0.2rem 0.5rem;
  border-radius: 12px;
  font-size: 0.7rem;
}

/* 消息内容 */
.message-content {
  border-radius: 18px;
  padding: 1rem 1.25rem;
  position: relative;
  word-wrap: break-word;
  overflow-wrap: break-word;
}

.text-content {
  line-height: 1.5;
}

.typing-animation {
  display: inline;
}

.char {
  opacity: 0;
  animation: fadeIn 0.1s ease forwards;
}

.cursor {
  animation: blink 1s infinite;
  margin-left: 2px;
}

.formatted-text {
  white-space: pre-wrap;
}

.formatted-text code.inline-code {
  background: rgba(0, 0, 0, 0.1);
  padding: 0.2rem 0.4rem;
  border-radius: 4px;
  font-family: 'Fira Code', monospace;
  font-size: 0.9em;
}

.formatted-text strong {
  font-weight: 600;
}

.formatted-text em {
  font-style: italic;
}

.formatted-text a {
  color: var(--primary-color);
  text-decoration: none;
}

.formatted-text a:hover {
  text-decoration: underline;
}

/* 图片内容 */
.image-content {
  margin-top: 0.75rem;
}

.image-gallery {
  display: grid;
  gap: 0.5rem;
  border-radius: 12px;
  overflow: hidden;
}

.image-gallery.multiple {
  grid-template-columns: repeat(auto-fit, minmax(150px, 1fr));
}

.image-item {
  position: relative;
  cursor: pointer;
  border-radius: 8px;
  overflow: hidden;
  transition: transform 0.2s ease;
}

.image-item:hover {
  transform: scale(1.02);
}

.image-item img {
  width: 100%;
  height: auto;
  display: block;
  opacity: 0;
  transition: opacity 0.3s ease;
}

.image-item img.loaded {
  opacity: 1;
}

.image-caption {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  background: linear-gradient(transparent, rgba(0, 0, 0, 0.7));
  color: white;
  padding: 1rem 0.75rem 0.75rem;
  font-size: 0.85rem;
}

.image-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity 0.2s ease;
}

.image-item:hover .image-overlay {
  opacity: 1;
}

.preview-btn {
  background: rgba(255, 255, 255, 0.9);
  border: none;
  border-radius: 50%;
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: transform 0.2s ease;
}

.preview-btn:hover {
  transform: scale(1.1);
}

.preview-btn svg {
  width: 20px;
  height: 20px;
  fill: #333;
}

/* 音频内容 */
.audio-content {
  margin-top: 0.75rem;
}

.audio-player {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  background: rgba(0, 0, 0, 0.05);
  padding: 0.75rem;
  border-radius: 12px;
}

.play-btn {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  border: none;
  background: var(--primary-gradient);
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: transform 0.2s ease;
}

.play-btn:hover {
  transform: scale(1.05);
}

.play-btn svg {
  width: 16px;
  height: 16px;
  fill: currentColor;
}

.audio-progress {
  flex: 1;
  height: 4px;
  background: rgba(0, 0, 0, 0.1);
  border-radius: 2px;
  overflow: hidden;
}

.progress-bar {
  height: 100%;
  background: var(--primary-gradient);
  transition: width 0.1s ease;
}

.audio-duration {
  font-size: 0.85rem;
  color: var(--text-secondary);
  min-width: 40px;
}

/* 文件内容 */
.file-content {
  margin-top: 0.75rem;
}

.file-item {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  background: rgba(0, 0, 0, 0.05);
  padding: 0.75rem;
  border-radius: 12px;
  cursor: pointer;
  transition: background-color 0.2s ease;
  margin-bottom: 0.5rem;
}

.file-item:last-child {
  margin-bottom: 0;
}

.file-item:hover {
  background: rgba(0, 0, 0, 0.1);
}

.file-icon {
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--border-light);
  border-radius: 8px;
}

.file-icon svg {
  width: 18px;
  height: 18px;
  fill: var(--text-secondary);
}

.file-info {
  flex: 1;
}

.file-name {
  font-weight: 500;
  margin-bottom: 0.25rem;
}

.file-size {
  font-size: 0.8rem;
  color: var(--text-secondary);
}

.download-icon {
  width: 20px;
  height: 20px;
  opacity: 0.6;
}

.download-icon svg {
  width: 100%;
  height: 100%;
  fill: var(--text-secondary);
}

/* 代码内容 */
.code-content {
  margin-top: 0.75rem;
}

.code-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: rgba(0, 0, 0, 0.05);
  padding: 0.5rem 0.75rem;
  border-radius: 8px 8px 0 0;
  border-bottom: 1px solid var(--border-light);
}

.language {
  font-size: 0.8rem;
  color: var(--text-secondary);
  font-weight: 500;
}

.copy-btn {
  display: flex;
  align-items: center;
  gap: 0.25rem;
  background: none;
  border: none;
  color: var(--text-secondary);
  cursor: pointer;
  font-size: 0.8rem;
  padding: 0.25rem 0.5rem;
  border-radius: 4px;
  transition: all 0.2s ease;
}

.copy-btn:hover {
  background: rgba(0, 0, 0, 0.1);
}

.copy-btn.copied {
  color: var(--success-color);
}

.copy-btn svg {
  width: 14px;
  height: 14px;
  fill: currentColor;
}

.code-block {
  background: rgba(0, 0, 0, 0.05);
  padding: 1rem;
  margin: 0;
  border-radius: 0 0 8px 8px;
  overflow-x: auto;
  font-family: 'Fira Code', monospace;
  font-size: 0.85rem;
  line-height: 1.4;
}

/* 错误内容 */
.error-content {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  background: rgba(255, 107, 107, 0.1);
  border: 1px solid var(--error-color);
  color: var(--error-color);
  padding: 0.75rem;
  border-radius: 8px;
  margin-top: 0.75rem;
}

.error-icon {
  font-size: 1.2rem;
  flex-shrink: 0;
}

.error-text {
  flex: 1;
  font-size: 0.9rem;
}

.retry-btn {
  background: var(--error-color);
  color: white;
  border: none;
  padding: 0.5rem 1rem;
  border-radius: 6px;
  cursor: pointer;
  font-size: 0.8rem;
  transition: opacity 0.2s ease;
}

.retry-btn:hover {
  opacity: 0.8;
}

/* 消息操作 */
.message-actions {
  display: flex;
  gap: 0.25rem;
  margin-top: 0.5rem;
  opacity: 0;
  transition: opacity 0.2s ease;
}

.message-bubble:hover .message-actions {
  opacity: 1;
}

.action-btn {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  border: none;
  background: var(--border-light);
  color: var(--text-secondary);
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s ease;
}

.action-btn:hover {
  background: var(--border-dark);
  transform: scale(1.1);
}

.action-btn.active {
  background: var(--primary-gradient);
  color: white;
}

.action-btn.copied {
  background: var(--success-color);
  color: white;
}

.action-btn svg {
  width: 14px;
  height: 14px;
  fill: currentColor;
}

/* 消息状态 */
.message-status {
  margin-top: 0.5rem;
  text-align: right;
}

.status-text {
  font-size: 0.75rem;
  color: var(--text-secondary);
}

.status-text.error {
  color: var(--error-color);
}

/* 错误状态 */
.message-bubble.error .message-content {
  border-color: var(--error-color);
  background: rgba(255, 107, 107, 0.05);
}

/* 动画 */
@keyframes blink {
  0%, 50% { opacity: 1; }
  51%, 100% { opacity: 0; }
}

/* 响应式设计 */
@media (max-width: 768px) {
  .message-bubble {
    gap: 0.5rem;
    margin-bottom: 1rem;
  }
  
  .avatar {
    width: 32px;
    height: 32px;
  }
  
  .message-content {
    padding: 0.75rem 1rem;
  }
  
  .image-gallery.multiple {
    grid-template-columns: 1fr;
  }
  
  .message-header {
    font-size: 0.75rem;
  }
  
  .code-block {
    font-size: 0.8rem;
  }
}

@media (max-width: 480px) {
  .message-content {
    padding: 0.6rem 0.8rem;
    border-radius: 12px;
  }
  
  .audio-player {
    padding: 0.5rem;
  }
  
  .file-item {
    padding: 0.5rem;
  }
}

/* 暗黑模式适配 */
[data-theme="dark"] .message-assistant .message-content {
  background: rgba(255, 255, 255, 0.05);
  border-color: rgba(255, 255, 255, 0.1);
}

[data-theme="dark"] .formatted-text code.inline-code {
  background: rgba(255, 255, 255, 0.1);
}

[data-theme="dark"] .audio-player,
[data-theme="dark"] .file-item,
[data-theme="dark"] .code-header,
[data-theme="dark"] .code-block {
  background: rgba(255, 255, 255, 0.05);
}

/* 高对比度模式 */
@media (prefers-contrast: high) {
  .message-content {
    border-width: 2px;
  }
  
  .action-btn {
    border: 1px solid currentColor;
  }
}

/* 减少动画模式 */
@media (prefers-reduced-motion: reduce) {
  .message-bubble,
  .char,
  .cursor,
  .image-item,
  .action-btn {
    animation: none !important;
    transition: none !important;
  }
}
</style>