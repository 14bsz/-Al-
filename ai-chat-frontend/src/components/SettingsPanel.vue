<template>
  <div class="settings-panel" :class="{ 'open': isOpen }">
    <!-- 设置面板头部 -->
    <div class="panel-header">
      <h3 class="panel-title">
        <svg class="title-icon" viewBox="0 0 24 24">
          <path d="M12,15.5A3.5,3.5 0 0,1 8.5,12A3.5,3.5 0 0,1 12,8.5A3.5,3.5 0 0,1 15.5,12A3.5,3.5 0 0,1 12,15.5M19.43,12.97C19.47,12.65 19.5,12.33 19.5,12C19.5,11.67 19.47,11.34 19.43,11L21.54,9.37C21.73,9.22 21.78,8.95 21.66,8.73L19.66,5.27C19.54,5.05 19.27,4.96 19.05,5.05L16.56,6.05C16.04,5.66 15.5,5.32 14.87,5.07L14.5,2.42C14.46,2.18 14.25,2 14,2H10C9.75,2 9.54,2.18 9.5,2.42L9.13,5.07C8.5,5.32 7.96,5.66 7.44,6.05L4.95,5.05C4.73,4.96 4.46,5.05 4.34,5.27L2.34,8.73C2.22,8.95 2.27,9.22 2.46,9.37L4.57,11C4.53,11.34 4.5,11.67 4.5,12C4.5,12.33 4.53,12.65 4.57,12.97L2.46,14.63C2.27,14.78 2.22,15.05 2.34,15.27L4.34,18.73C4.46,18.95 4.73,19.03 4.95,18.95L7.44,17.94C7.96,18.34 8.5,18.68 9.13,18.93L9.5,21.58C9.54,21.82 9.75,22 10,22H14C14.25,22 14.46,21.82 14.5,21.58L14.87,18.93C15.5,18.68 16.04,18.34 16.56,17.94L19.05,18.95C19.27,19.03 19.54,18.95 19.66,18.73L21.66,15.27C21.78,15.05 21.73,14.78 21.54,14.63L19.43,12.97Z"/>
        </svg>
        设置
      </h3>
      <button class="close-btn" @click="closePanel">
        <svg viewBox="0 0 24 24">
          <path d="M19,6.41L17.59,5L12,10.59L6.41,5L5,6.41L10.59,12L5,17.59L6.41,19L12,13.41L17.59,19L19,17.59L13.41,12L19,6.41Z"/>
        </svg>
      </button>
    </div>

    <!-- 设置内容 -->
    <div class="panel-content">
      <!-- 外观设置 -->
      <div class="settings-section">
        <h4 class="section-title">
          <svg class="section-icon" viewBox="0 0 24 24">
            <path d="M12,18C11.11,18 10.26,17.8 9.5,17.45C11.56,16.5 13,14.42 13,12C13,9.58 11.56,7.5 9.5,6.55C10.26,6.2 11.11,6 12,6A6,6 0 0,1 18,12A6,6 0 0,1 12,18M20,8.69V4H15.31L12,0.69L8.69,4H4V8.69L0.69,12L4,15.31V20H8.69L12,23.31L15.31,20H20V15.31L23.31,12L20,8.69Z"/>
          </svg>
          外观设置
        </h4>
        
        <!-- 主题切换 -->
        <div class="setting-item">
          <label class="setting-label">主题模式</label>
          <div class="theme-selector">
            <button 
              v-for="theme in themes" 
              :key="theme.value"
              :class="['theme-btn', { 'active': currentTheme === theme.value }]"
              @click="changeTheme(theme.value)"
            >
              <div class="theme-preview" :style="theme.style"></div>
              <span>{{ theme.label }}</span>
            </button>
          </div>
        </div>

        <!-- 字体大小 -->
        <div class="setting-item">
          <label class="setting-label">字体大小</label>
          <div class="slider-container">
            <input 
              type="range" 
              v-model="fontSize" 
              min="12" 
              max="20" 
              step="1"
              class="slider"
              @input="updateFontSize"
            />
            <span class="slider-value">{{ fontSize }}px</span>
          </div>
        </div>

        <!-- 动画效果 -->
        <div class="setting-item">
          <label class="setting-label">动画效果</label>
          <div class="toggle-container">
            <input 
              type="checkbox" 
              id="animations" 
              v-model="enableAnimations"
              @change="updateAnimations"
            />
            <label for="animations" class="toggle-label">
              <span class="toggle-slider"></span>
            </label>
          </div>
        </div>

        <!-- 背景效果 -->
        <div class="setting-item">
          <label class="setting-label">3D背景效果</label>
          <div class="toggle-container">
            <input 
              type="checkbox" 
              id="background3d" 
              v-model="enable3DBackground"
              @change="update3DBackground"
            />
            <label for="background3d" class="toggle-label">
              <span class="toggle-slider"></span>
            </label>
          </div>
        </div>
      </div>

      <!-- AI设置 -->
      <div class="settings-section">
        <h4 class="section-title">
          <svg class="section-icon" viewBox="0 0 24 24">
            <path d="M12,2A2,2 0 0,1 14,4C14,4.74 13.6,5.39 13,5.73V7H14A7,7 0 0,1 21,14H22A1,1 0 0,1 23,15V18A1,1 0 0,1 22,19H21V20A2,2 0 0,1 19,22H5A2,2 0 0,1 3,20V19H2A1,1 0 0,1 1,18V15A1,1 0 0,1 2,14H3A7,7 0 0,1 10,7H11V5.73C10.4,5.39 10,4.74 10,4A2,2 0 0,1 12,2M7.5,13A2.5,2.5 0 0,0 5,15.5A2.5,2.5 0 0,0 7.5,18A2.5,2.5 0 0,0 10,15.5A2.5,2.5 0 0,0 7.5,13M16.5,13A2.5,2.5 0 0,0 14,15.5A2.5,2.5 0 0,0 16.5,18A2.5,2.5 0 0,0 19,15.5A2.5,2.5 0 0,0 16.5,13Z"/>
          </svg>
          AI设置
        </h4>

        <!-- AI模型选择 -->
        <div class="setting-item">
          <label class="setting-label">AI模型</label>
          <select v-model="selectedModel" @change="updateModel" class="model-select">
            <option v-for="model in aiModels" :key="model.id" :value="model.id">
              {{ model.name }} - {{ model.description }}
            </option>
          </select>
        </div>

        <!-- 回复速度 -->
        <div class="setting-item">
          <label class="setting-label">回复速度</label>
          <div class="slider-container">
            <input 
              type="range" 
              v-model="responseSpeed" 
              min="1" 
              max="5" 
              step="1"
              class="slider"
              @input="updateResponseSpeed"
            />
            <div class="speed-labels">
              <span>慢</span>
              <span>快</span>
            </div>
          </div>
        </div>

        <!-- 创造性 -->
        <div class="setting-item">
          <label class="setting-label">创造性</label>
          <div class="slider-container">
            <input 
              type="range" 
              v-model="creativity" 
              min="0" 
              max="100" 
              step="10"
              class="slider"
              @input="updateCreativity"
            />
            <span class="slider-value">{{ creativity }}%</span>
          </div>
        </div>

        <!-- 上下文长度 -->
        <div class="setting-item">
          <label class="setting-label">对话记忆长度</label>
          <select v-model="contextLength" @change="updateContextLength" class="context-select">
            <option value="5">短期 (5轮对话)</option>
            <option value="10">中期 (10轮对话)</option>
            <option value="20">长期 (20轮对话)</option>
            <option value="50">超长 (50轮对话)</option>
          </select>
        </div>
      </div>

      <!-- 功能设置 -->
      <div class="settings-section">
        <h4 class="section-title">
          <svg class="section-icon" viewBox="0 0 24 24">
            <path d="M12,2A10,10 0 0,0 2,12A10,10 0 0,0 12,22A10,10 0 0,0 22,12A10,10 0 0,0 12,2M12,4A8,8 0 0,1 20,12A8,8 0 0,1 12,20A8,8 0 0,1 4,12A8,8 0 0,1 12,4M11,16.5L18,9.5L16.59,8.09L11,13.67L7.91,10.59L6.5,12L11,16.5Z"/>
          </svg>
          功能设置
        </h4>

        <!-- 语音识别 -->
        <div class="setting-item">
          <label class="setting-label">语音识别</label>
          <div class="toggle-container">
            <input 
              type="checkbox" 
              id="voiceRecognition" 
              v-model="enableVoiceRecognition"
              @change="updateVoiceRecognition"
            />
            <label for="voiceRecognition" class="toggle-label">
              <span class="toggle-slider"></span>
            </label>
          </div>
        </div>

        <!-- 自动播放语音 -->
        <div class="setting-item">
          <label class="setting-label">自动播放AI语音</label>
          <div class="toggle-container">
            <input 
              type="checkbox" 
              id="autoPlayVoice" 
              v-model="autoPlayVoice"
              @change="updateAutoPlayVoice"
            />
            <label for="autoPlayVoice" class="toggle-label">
              <span class="toggle-slider"></span>
            </label>
          </div>
        </div>

        <!-- 打字机效果 -->
        <div class="setting-item">
          <label class="setting-label">打字机效果</label>
          <div class="toggle-container">
            <input 
              type="checkbox" 
              id="typewriterEffect" 
              v-model="enableTypewriter"
              @change="updateTypewriter"
            />
            <label for="typewriterEffect" class="toggle-label">
              <span class="toggle-slider"></span>
            </label>
          </div>
        </div>

        <!-- 消息通知 -->
        <div class="setting-item">
          <label class="setting-label">消息通知</label>
          <div class="toggle-container">
            <input 
              type="checkbox" 
              id="notifications" 
              v-model="enableNotifications"
              @change="updateNotifications"
            />
            <label for="notifications" class="toggle-label">
              <span class="toggle-slider"></span>
            </label>
          </div>
        </div>
      </div>

      <!-- 隐私设置 -->
      <div class="settings-section">
        <h4 class="section-title">
          <svg class="section-icon" viewBox="0 0 24 24">
            <path d="M12,1L3,5V11C3,16.55 6.84,21.74 12,23C17.16,21.74 21,16.55 21,11V5L12,1M12,7C13.4,7 14.8,8.6 14.8,10V11.5C15.4,11.5 16,12.4 16,13V16C16,17.4 15.4,18 14.8,18H9.2C8.6,18 8,17.4 8,16V13C8,12.4 8.6,11.5 9.2,11.5V10C9.2,8.6 10.6,7 12,7M12,8.2C11.2,8.2 10.5,8.7 10.5,10V11.5H13.5V10C13.5,8.7 12.8,8.2 12,8.2Z"/>
          </svg>
          隐私设置
        </h4>

        <!-- 保存对话历史 -->
        <div class="setting-item">
          <label class="setting-label">保存对话历史</label>
          <div class="toggle-container">
            <input 
              type="checkbox" 
              id="saveHistory" 
              v-model="saveHistory"
              @change="updateSaveHistory"
            />
            <label for="saveHistory" class="toggle-label">
              <span class="toggle-slider"></span>
            </label>
          </div>
        </div>

        <!-- 数据分析 -->
        <div class="setting-item">
          <label class="setting-label">匿名数据分析</label>
          <div class="toggle-container">
            <input 
              type="checkbox" 
              id="analytics" 
              v-model="enableAnalytics"
              @change="updateAnalytics"
            />
            <label for="analytics" class="toggle-label">
              <span class="toggle-slider"></span>
            </label>
          </div>
        </div>

        <!-- 清除数据按钮 -->
        <div class="setting-item">
          <label class="setting-label">数据管理</label>
          <div class="data-actions">
            <button class="action-btn secondary" @click="clearHistory">
              清除对话历史
            </button>
            <button class="action-btn danger" @click="resetSettings">
              重置所有设置
            </button>
          </div>
        </div>
      </div>

      <!-- 关于信息 -->
      <div class="settings-section">
        <h4 class="section-title">
          <svg class="section-icon" viewBox="0 0 24 24">
            <path d="M11,9H13V7H11M12,20C7.59,20 4,16.41 4,12C4,7.59 7.59,4 12,4C16.41,4 20,7.59 20,12C20,16.41 16.41,20 12,20M12,2A10,10 0 0,0 2,12A10,10 0 0,0 12,22A10,10 0 0,0 22,12A10,10 0 0,0 12,2M11,17H13V11H11V17Z"/>
          </svg>
          关于
        </h4>

        <div class="about-info">
          <div class="app-info">
            <h5>AI聊天助手</h5>
            <p>版本 2.0.0</p>
            <p>基于最新AI技术的智能对话系统</p>
          </div>
          
          <div class="links">
            <a href="#" class="info-link">使用帮助</a>
            <a href="#" class="info-link">隐私政策</a>
            <a href="#" class="info-link">服务条款</a>
            <a href="#" class="info-link">反馈建议</a>
          </div>
        </div>
      </div>
    </div>
  </div>

  <!-- 遮罩层 -->
  <div v-if="isOpen" class="panel-overlay" @click="closePanel"></div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'

// Props
interface Props {
  isOpen: boolean
}

const props = defineProps<Props>()

// Emits
const emit = defineEmits<{
  close: []
  settingsChange: [settings: any]
}>()

// 响应式数据
const currentTheme = ref('auto')
const fontSize = ref(14)
const enableAnimations = ref(true)
const enable3DBackground = ref(true)
const selectedModel = ref('gpt-4')
const responseSpeed = ref(3)
const creativity = ref(70)
const contextLength = ref(10)
const enableVoiceRecognition = ref(true)
const autoPlayVoice = ref(false)
const enableTypewriter = ref(true)
const enableNotifications = ref(true)
const saveHistory = ref(true)
const enableAnalytics = ref(false)

// 主题选项
const themes = [
  {
    value: 'light',
    label: '浅色',
    style: {
      background: 'linear-gradient(135deg, #ffffff 0%, #f8f9fa 100%)'
    }
  },
  {
    value: 'dark',
    label: '深色',
    style: {
      background: 'linear-gradient(135deg, #1a1a1a 0%, #2d2d2d 100%)'
    }
  },
  {
    value: 'auto',
    label: '自动',
    style: {
      background: 'linear-gradient(135deg, #ffffff 0%, #1a1a1a 100%)'
    }
  }
]

// AI模型选项
const aiModels = [
  {
    id: 'gpt-4',
    name: 'GPT-4',
    description: '最强大的通用AI模型'
  },
  {
    id: 'gpt-3.5-turbo',
    name: 'GPT-3.5 Turbo',
    description: '快速响应的AI模型'
  },
  {
    id: 'claude-3',
    name: 'Claude-3',
    description: '擅长分析和推理'
  },
  {
    id: 'deepseek',
    name: 'DeepSeek',
    description: '国产优秀AI模型'
  }
]

// 计算属性
const allSettings = computed(() => ({
  theme: currentTheme.value,
  fontSize: fontSize.value,
  enableAnimations: enableAnimations.value,
  enable3DBackground: enable3DBackground.value,
  selectedModel: selectedModel.value,
  responseSpeed: responseSpeed.value,
  creativity: creativity.value,
  contextLength: contextLength.value,
  enableVoiceRecognition: enableVoiceRecognition.value,
  autoPlayVoice: autoPlayVoice.value,
  enableTypewriter: enableTypewriter.value,
  enableNotifications: enableNotifications.value,
  saveHistory: saveHistory.value,
  enableAnalytics: enableAnalytics.value
}))

// 方法
const closePanel = () => {
  emit('close')
}

const changeTheme = (theme: string) => {
  currentTheme.value = theme
  applyTheme(theme)
}

const applyTheme = (theme: string) => {
  const root = document.documentElement
  
  if (theme === 'dark') {
    root.setAttribute('data-theme', 'dark')
  } else if (theme === 'light') {
    root.setAttribute('data-theme', 'light')
  } else {
    // 自动模式：根据系统偏好设置
    const prefersDark = window.matchMedia('(prefers-color-scheme: dark)').matches
    root.setAttribute('data-theme', prefersDark ? 'dark' : 'light')
  }
}

const updateFontSize = () => {
  document.documentElement.style.setProperty('--base-font-size', `${fontSize.value}px`)
}

const updateAnimations = () => {
  document.documentElement.style.setProperty(
    '--animation-duration', 
    enableAnimations.value ? '0.3s' : '0s'
  )
}

const update3DBackground = () => {
  // 通过事件通知父组件更新3D背景
  emit('settingsChange', { enable3DBackground: enable3DBackground.value })
}

const updateModel = () => {
  emit('settingsChange', { selectedModel: selectedModel.value })
}

const updateResponseSpeed = () => {
  emit('settingsChange', { responseSpeed: responseSpeed.value })
}

const updateCreativity = () => {
  emit('settingsChange', { creativity: creativity.value })
}

const updateContextLength = () => {
  emit('settingsChange', { contextLength: contextLength.value })
}

const updateVoiceRecognition = () => {
  emit('settingsChange', { enableVoiceRecognition: enableVoiceRecognition.value })
}

const updateAutoPlayVoice = () => {
  emit('settingsChange', { autoPlayVoice: autoPlayVoice.value })
}

const updateTypewriter = () => {
  emit('settingsChange', { enableTypewriter: enableTypewriter.value })
}

const updateNotifications = () => {
  if (enableNotifications.value && 'Notification' in window) {
    Notification.requestPermission()
  }
  emit('settingsChange', { enableNotifications: enableNotifications.value })
}

const updateSaveHistory = () => {
  emit('settingsChange', { saveHistory: saveHistory.value })
}

const updateAnalytics = () => {
  emit('settingsChange', { enableAnalytics: enableAnalytics.value })
}

const clearHistory = async () => {
  if (confirm('确定要清除所有对话历史吗？此操作不可撤销。')) {
    try {
      // 清除本地存储的对话历史
      localStorage.removeItem('chat-history')
      
      // 通知父组件清除历史
      emit('settingsChange', { action: 'clearHistory' })
      
      // 显示成功提示
      showNotification('对话历史已清除', 'success')
    } catch (error) {
      showNotification('清除失败，请重试', 'error')
    }
  }
}

const resetSettings = async () => {
  if (confirm('确定要重置所有设置吗？此操作将恢复默认配置。')) {
    try {
      // 重置所有设置为默认值
      currentTheme.value = 'auto'
      fontSize.value = 14
      enableAnimations.value = true
      enable3DBackground.value = true
      selectedModel.value = 'gpt-4'
      responseSpeed.value = 3
      creativity.value = 70
      contextLength.value = 10
      enableVoiceRecognition.value = true
      autoPlayVoice.value = false
      enableTypewriter.value = true
      enableNotifications.value = true
      saveHistory.value = true
      enableAnalytics.value = false
      
      // 应用默认设置
      applyTheme('auto')
      updateFontSize()
      updateAnimations()
      
      // 清除本地存储的设置
      localStorage.removeItem('app-settings')
      
      // 通知父组件
      emit('settingsChange', { action: 'resetSettings', settings: allSettings.value })
      
      showNotification('设置已重置', 'success')
    } catch (error) {
      showNotification('重置失败，请重试', 'error')
    }
  }
}

const showNotification = (message: string, type: 'success' | 'error' | 'info' = 'info') => {
  // 简单的通知实现
  const notification = document.createElement('div')
  notification.className = `notification notification-${type}`
  notification.textContent = message
  notification.style.cssText = `
    position: fixed;
    top: 20px;
    right: 20px;
    padding: 12px 20px;
    border-radius: 8px;
    color: white;
    font-size: 14px;
    z-index: 10000;
    animation: slideIn 0.3s ease;
    background: ${type === 'success' ? '#4ecdc4' : type === 'error' ? '#ff6b6b' : '#667eea'};
  `
  
  document.body.appendChild(notification)
  
  setTimeout(() => {
    notification.remove()
  }, 3000)
}

const loadSettings = () => {
  try {
    const saved = localStorage.getItem('app-settings')
    if (saved) {
      const settings = JSON.parse(saved)
      
      // 恢复设置
      currentTheme.value = settings.theme || 'auto'
      fontSize.value = settings.fontSize || 14
      enableAnimations.value = settings.enableAnimations !== false
      enable3DBackground.value = settings.enable3DBackground !== false
      selectedModel.value = settings.selectedModel || 'gpt-4'
      responseSpeed.value = settings.responseSpeed || 3
      creativity.value = settings.creativity || 70
      contextLength.value = settings.contextLength || 10
      enableVoiceRecognition.value = settings.enableVoiceRecognition !== false
      autoPlayVoice.value = settings.autoPlayVoice || false
      enableTypewriter.value = settings.enableTypewriter !== false
      enableNotifications.value = settings.enableNotifications !== false
      saveHistory.value = settings.saveHistory !== false
      enableAnalytics.value = settings.enableAnalytics || false
      
      // 应用设置
      applyTheme(currentTheme.value)
      updateFontSize()
      updateAnimations()
    }
  } catch (error) {
    console.error('加载设置失败:', error)
  }
}

const saveSettings = () => {
  try {
    localStorage.setItem('app-settings', JSON.stringify(allSettings.value))
  } catch (error) {
    console.error('保存设置失败:', error)
  }
}

// 监听设置变化并自动保存
watch(allSettings, (newSettings) => {
  saveSettings()
  emit('settingsChange', newSettings)
}, { deep: true })

// 生命周期
onMounted(() => {
  loadSettings()
  
  // 监听系统主题变化
  if (window.matchMedia) {
    const mediaQuery = window.matchMedia('(prefers-color-scheme: dark)')
    mediaQuery.addEventListener('change', () => {
      if (currentTheme.value === 'auto') {
        applyTheme('auto')
      }
    })
  }
})
</script>

<style scoped>
.settings-panel {
  position: fixed;
  top: 0;
  right: -400px;
  width: 400px;
  height: 100vh;
  background: var(--background-light);
  backdrop-filter: var(--blur-effect);
  border-left: 1px solid var(--border-light);
  z-index: 1000;
  transition: right 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.settings-panel.open {
  right: 0;
}

.panel-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100vw;
  height: 100vh;
  background: rgba(0, 0, 0, 0.5);
  z-index: 999;
  animation: fadeIn 0.3s ease;
}

/* 面板头部 */
.panel-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 1.5rem;
  border-bottom: 1px solid var(--border-light);
  background: var(--background-light);
}

.panel-title {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  margin: 0;
  font-size: 1.2rem;
  font-weight: 600;
  color: var(--text-primary);
}

.title-icon {
  width: 20px;
  height: 20px;
  fill: var(--text-primary);
}

.close-btn {
  width: 32px;
  height: 32px;
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

.close-btn:hover {
  background: var(--border-dark);
  transform: scale(1.05);
}

.close-btn svg {
  width: 16px;
  height: 16px;
  fill: currentColor;
}

/* 面板内容 */
.panel-content {
  flex: 1;
  overflow-y: auto;
  padding: 1rem;
}

.settings-section {
  margin-bottom: 2rem;
}

.section-title {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  margin: 0 0 1rem 0;
  font-size: 1rem;
  font-weight: 600;
  color: var(--text-primary);
}

.section-icon {
  width: 16px;
  height: 16px;
  fill: var(--text-primary);
}

.setting-item {
  margin-bottom: 1.5rem;
}

.setting-label {
  display: block;
  margin-bottom: 0.5rem;
  font-size: 0.9rem;
  font-weight: 500;
  color: var(--text-primary);
}

/* 主题选择器 */
.theme-selector {
  display: flex;
  gap: 0.5rem;
}

.theme-btn {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 0.5rem;
  padding: 0.75rem;
  border: 2px solid var(--border-light);
  border-radius: 8px;
  background: transparent;
  cursor: pointer;
  transition: all 0.2s ease;
  font-size: 0.8rem;
  color: var(--text-secondary);
}

.theme-btn:hover {
  border-color: var(--border-dark);
}

.theme-btn.active {
  border-color: var(--primary-color);
  color: var(--primary-color);
}

.theme-preview {
  width: 30px;
  height: 20px;
  border-radius: 4px;
  border: 1px solid var(--border-light);
}

/* 滑块 */
.slider-container {
  display: flex;
  align-items: center;
  gap: 1rem;
}

.slider {
  flex: 1;
  height: 4px;
  border-radius: 2px;
  background: var(--border-light);
  outline: none;
  -webkit-appearance: none;
  appearance: none;
}

.slider::-webkit-slider-thumb {
  -webkit-appearance: none;
  appearance: none;
  width: 16px;
  height: 16px;
  border-radius: 50%;
  background: var(--primary-gradient);
  cursor: pointer;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.2);
}

.slider::-moz-range-thumb {
  width: 16px;
  height: 16px;
  border-radius: 50%;
  background: var(--primary-gradient);
  cursor: pointer;
  border: none;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.2);
}

.slider-value {
  min-width: 40px;
  font-size: 0.85rem;
  color: var(--text-secondary);
  text-align: right;
}

.speed-labels {
  display: flex;
  justify-content: space-between;
  width: 100%;
  font-size: 0.8rem;
  color: var(--text-secondary);
  margin-top: 0.25rem;
}

/* 开关 */
.toggle-container {
  display: flex;
  align-items: center;
}

.toggle-container input[type="checkbox"] {
  display: none;
}

.toggle-label {
  position: relative;
  width: 44px;
  height: 24px;
  background: var(--border-light);
  border-radius: 12px;
  cursor: pointer;
  transition: background-color 0.3s ease;
}

.toggle-slider {
  position: absolute;
  top: 2px;
  left: 2px;
  width: 20px;
  height: 20px;
  background: white;
  border-radius: 50%;
  transition: transform 0.3s ease;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.2);
}

.toggle-container input[type="checkbox"]:checked + .toggle-label {
  background: var(--primary-gradient);
}

.toggle-container input[type="checkbox"]:checked + .toggle-label .toggle-slider {
  transform: translateX(20px);
}

/* 选择框 */
.model-select,
.context-select {
  width: 100%;
  padding: 0.75rem;
  border: 1px solid var(--border-light);
  border-radius: 8px;
  background: var(--background-light);
  color: var(--text-primary);
  font-size: 0.9rem;
  cursor: pointer;
  transition: border-color 0.2s ease;
}

.model-select:focus,
.context-select:focus {
  outline: none;
  border-color: var(--primary-color);
}

/* 操作按钮 */
.data-actions {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.action-btn {
  padding: 0.75rem 1rem;
  border: none;
  border-radius: 8px;
  font-size: 0.9rem;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s ease;
}

.action-btn.secondary {
  background: var(--border-light);
  color: var(--text-primary);
}

.action-btn.secondary:hover {
  background: var(--border-dark);
}

.action-btn.danger {
  background: var(--error-color);
  color: white;
}

.action-btn.danger:hover {
  opacity: 0.8;
}

/* 关于信息 */
.about-info {
  padding: 1rem;
  background: var(--border-light);
  border-radius: 8px;
}

.app-info h5 {
  margin: 0 0 0.5rem 0;
  font-size: 1rem;
  font-weight: 600;
  color: var(--text-primary);
}

.app-info p {
  margin: 0.25rem 0;
  font-size: 0.85rem;
  color: var(--text-secondary);
}

.links {
  display: flex;
  flex-wrap: wrap;
  gap: 1rem;
  margin-top: 1rem;
  padding-top: 1rem;
  border-top: 1px solid var(--border-light);
}

.info-link {
  font-size: 0.85rem;
  color: var(--primary-color);
  text-decoration: none;
  transition: opacity 0.2s ease;
}

.info-link:hover {
  opacity: 0.7;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .settings-panel {
    width: 100vw;
    right: -100vw;
  }
  
  .theme-selector {
    flex-direction: column;
  }
  
  .theme-btn {
    flex-direction: row;
    justify-content: flex-start;
  }
  
  .links {
    flex-direction: column;
    gap: 0.5rem;
  }
}

@media (max-width: 480px) {
  .panel-header {
    padding: 1rem;
  }
  
  .panel-content {
    padding: 0.75rem;
  }
  
  .setting-item {
    margin-bottom: 1rem;
  }
}

/* 暗黑模式适配 */
[data-theme="dark"] .settings-panel {
  background: rgba(0, 0, 0, 0.9);
  border-left-color: rgba(255, 255, 255, 0.1);
}

[data-theme="dark"] .panel-header {
  background: rgba(0, 0, 0, 0.9);
  border-bottom-color: rgba(255, 255, 255, 0.1);
}

[data-theme="dark"] .about-info {
  background: rgba(255, 255, 255, 0.05);
}

/* 高对比度模式 */
@media (prefers-contrast: high) {
  .theme-btn,
  .model-select,
  .context-select,
  .action-btn {
    border-width: 2px;
  }
  
  .toggle-label {
    border: 1px solid currentColor;
  }
}

/* 减少动画模式 */
@media (prefers-reduced-motion: reduce) {
  .settings-panel,
  .panel-overlay,
  .theme-btn,
  .toggle-slider,
  .action-btn {
    transition: none !important;
    animation: none !important;
  }
}

/* 通知样式 */
@keyframes slideIn {
  from {
    transform: translateX(100%);
    opacity: 0;
  }
  to {
    transform: translateX(0);
    opacity: 1;
  }
}
</style>