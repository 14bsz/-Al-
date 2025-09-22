<template>
  <div class="voice-chat">
    <!-- 语音控制面板 -->
    <div class="voice-controls">
      <div class="voice-status">
        <div class="status-indicator" :class="{ 
          'listening': isListening, 
          'speaking': isSpeaking,
          'idle': !isListening && !isSpeaking 
        }">
          <el-icon v-if="isListening" class="pulse"><Microphone /></el-icon>
          <el-icon v-else-if="isSpeaking" class="pulse"><Speaker /></el-icon>
          <el-icon v-else><Mute /></el-icon>
        </div>
        
        <div class="status-text">
          <span v-if="isListening">正在听取...</span>
          <span v-else-if="isSpeaking">{{ currentCharacter?.name || 'AI' }}正在说话...</span>
          <span v-else>点击开始语音对话</span>
        </div>
      </div>

      <div class="voice-buttons">
        <el-button
          :type="isListening ? 'danger' : 'primary'"
          :icon="isListening ? 'VideoPause' : 'Microphone'"
          @click="toggleListening"
          :disabled="isSpeaking"
          size="large"
          round
        >
          {{ isListening ? '停止录音' : '开始录音' }}
        </el-button>

        <el-button
          v-if="isSpeaking"
          type="warning"
          icon="VideoPause"
          @click="stopSpeaking"
          size="large"
          round
        >
          停止播放
        </el-button>
      </div>
    </div>

    <!-- 实时语音识别结果 -->
    <div v-if="isListening || recognitionResult" class="recognition-result">
      <div class="result-header">
        <el-icon><Microphone /></el-icon>
        <span>语音识别结果</span>
      </div>
      
      <div class="result-content">
        <div v-if="recognitionResult.interim" class="interim-result">
          {{ recognitionResult.interim }}
        </div>
        <div v-if="recognitionResult.final" class="final-result">
          {{ recognitionResult.final }}
        </div>
        <div v-if="!recognitionResult.interim && !recognitionResult.final && isListening" class="waiting-result">
          请开始说话...
        </div>
      </div>
    </div>

    <!-- 语音设置 -->
    <div class="voice-settings">
      <el-collapse v-model="settingsCollapsed">
        <el-collapse-item title="语音设置" name="settings">
          <div class="settings-content">
            <!-- 语音选择 -->
            <div class="setting-item">
              <label>选择语音：</label>
              <el-select v-model="selectedVoice" @change="handleVoiceChange" placeholder="选择语音">
                <el-option
                  v-for="voice in availableVoices"
                  :key="voice.name"
                  :label="`${voice.name} (${voice.lang})`"
                  :value="voice.name"
                />
              </el-select>
            </div>

            <!-- 语速控制 -->
            <div class="setting-item">
              <label>语速：</label>
              <el-slider
                v-model="voiceSettings.rate"
                :min="0.5"
                :max="2.0"
                :step="0.1"
                show-input
                :show-input-controls="false"
              />
            </div>

            <!-- 音调控制 -->
            <div class="setting-item">
              <label>音调：</label>
              <el-slider
                v-model="voiceSettings.pitch"
                :min="0.5"
                :max="2.0"
                :step="0.1"
                show-input
                :show-input-controls="false"
              />
            </div>

            <!-- 音量控制 -->
            <div class="setting-item">
              <label>音量：</label>
              <el-slider
                v-model="voiceSettings.volume"
                :min="0.1"
                :max="1.0"
                :step="0.1"
                show-input
                :show-input-controls="false"
              />
            </div>

            <!-- 测试按钮 -->
            <div class="setting-item">
              <el-button @click="testVoice" :disabled="isSpeaking">
                测试语音
              </el-button>
            </div>
          </div>
        </el-collapse-item>
      </el-collapse>
    </div>

    <!-- 浏览器支持检查 -->
    <div v-if="!browserSupport.speechRecognition || !browserSupport.speechSynthesis" class="browser-warning">
      <el-alert
        title="浏览器兼容性提醒"
        type="warning"
        :closable="false"
      >
        <template #default>
          <div v-if="!browserSupport.speechRecognition">
            • 您的浏览器不支持语音识别功能，建议使用Chrome浏览器
          </div>
          <div v-if="!browserSupport.speechSynthesis">
            • 您的浏览器不支持语音合成功能，建议使用Chrome浏览器
          </div>
        </template>
      </el-alert>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, watch, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { Microphone, Speaker, Mute, VideoPause } from '@element-plus/icons-vue'
import speechService from '@/services/speechService.js'

// Props
const props = defineProps({
  currentCharacter: {
    type: Object,
    default: null
  }
})

// Emits
const emit = defineEmits(['voice-input', 'voice-output-start', 'voice-output-end'])

// 响应式数据
const isListening = ref(false)
const isSpeaking = ref(false)
const recognitionResult = ref({ final: '', interim: '' })
const selectedVoice = ref('')
const availableVoices = ref([])
const settingsCollapsed = ref([])
const browserSupport = ref({
  speechRecognition: false,
  speechSynthesis: false,
  getUserMedia: false
})

// 语音设置
const voiceSettings = ref({
  rate: 1.0,
  pitch: 1.0,
  volume: 1.0
})

// 计算属性
const canUseVoice = computed(() => {
  return browserSupport.value.speechRecognition && browserSupport.value.speechSynthesis
})

// 方法
const initVoiceService = () => {
  // 检查浏览器支持
  browserSupport.value = speechService.checkSupport()
  
  if (!canUseVoice.value) {
    ElMessage.warning('您的浏览器不完全支持语音功能，建议使用Chrome浏览器')
    return
  }

  // 加载可用语音
  loadAvailableVoices()
  
  // 监听语音状态变化
  watchVoiceStatus()
}

const loadAvailableVoices = () => {
  // 延迟加载语音列表，确保浏览器已加载完成
  setTimeout(() => {
    availableVoices.value = speechService.getVoices()
    if (availableVoices.value.length > 0 && !selectedVoice.value) {
      // 优先选择中文语音
      const chineseVoice = availableVoices.value.find(v => 
        v.lang.includes('zh') || v.lang.includes('CN')
      )
      selectedVoice.value = chineseVoice ? chineseVoice.name : availableVoices.value[0].name
    }
  }, 1000)
}

const watchVoiceStatus = () => {
  // 定期检查语音状态
  setInterval(() => {
    const status = speechService.getStatus()
    isListening.value = status.isListening
    isSpeaking.value = status.isSpeaking
  }, 100)
}

const toggleListening = () => {
  if (isListening.value) {
    stopListening()
  } else {
    startListening()
  }
}

const startListening = () => {
  if (!browserSupport.value.speechRecognition) {
    ElMessage.error('您的浏览器不支持语音识别功能')
    return
  }

  try {
    speechService.startListening({
      onStart: () => {
        recognitionResult.value = { final: '', interim: '' }
        ElMessage.success('开始语音识别')
      },
      onResult: (result) => {
        recognitionResult.value = result
        if (result.isFinal && result.final.trim()) {
          // 发送识别结果
          emit('voice-input', result.final.trim())
        }
      },
      onError: (error) => {
        ElMessage.error(`语音识别错误: ${error}`)
      },
      onEnd: () => {
        // 识别结束后清理
        setTimeout(() => {
          if (!isListening.value) {
            recognitionResult.value = { final: '', interim: '' }
          }
        }, 2000)
      }
    })
  } catch (error) {
    ElMessage.error('启动语音识别失败: ' + error.message)
  }
}

const stopListening = () => {
  speechService.stopListening()
}

const speakText = async (text) => {
  if (!browserSupport.value.speechSynthesis) {
    ElMessage.error('您的浏览器不支持语音合成功能')
    return
  }

  if (!text || text.trim() === '') {
    return
  }

  try {
    emit('voice-output-start')
    
    const options = {
      ...voiceSettings.value,
      onStart: () => {
        ElMessage.info('开始语音播放')
      },
      onEnd: () => {
        emit('voice-output-end')
      },
      onError: (error) => {
        ElMessage.error(`语音播放错误: ${error}`)
        emit('voice-output-end')
      }
    }

    // 如果有选中的角色，使用角色专属语音配置
    if (props.currentCharacter) {
      await speechService.speakAsCharacter(text, props.currentCharacter.name, options)
    } else {
      await speechService.speak(text, options)
    }
  } catch (error) {
    ElMessage.error('语音播放失败: ' + error.message)
    emit('voice-output-end')
  }
}

const stopSpeaking = () => {
  speechService.stopSpeaking()
  emit('voice-output-end')
}

const handleVoiceChange = (voiceName) => {
  speechService.setVoice(voiceName)
}

const testVoice = () => {
  const testText = props.currentCharacter 
    ? `你好，我是${props.currentCharacter.name}，很高兴与你对话！` 
    : '你好，这是语音测试。'
  speakText(testText)
}

// 监听角色变化，调整语音设置
watch(() => props.currentCharacter, (newCharacter) => {
  if (newCharacter) {
    const characterConfig = speechService.getCharacterVoiceConfig(newCharacter.name)
    voiceSettings.value = { ...voiceSettings.value, ...characterConfig }
  }
})

// 暴露方法给父组件
defineExpose({
  speakText,
  stopSpeaking,
  startListening,
  stopListening
})

// 生命周期
onMounted(() => {
  initVoiceService()
})
</script>

<style scoped>
.voice-chat {
  padding: 20px;
  border: 1px solid #e4e7ed;
  border-radius: 12px;
  background: #fafafa;
  margin-bottom: 20px;
}

.voice-controls {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 20px;
  margin-bottom: 20px;
}

.voice-status {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
}

.status-indicator {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 32px;
  transition: all 0.3s;
}

.status-indicator.idle {
  background: #f0f0f0;
  color: #909399;
}

.status-indicator.listening {
  background: linear-gradient(45deg, #409eff, #67c23a);
  color: white;
  animation: pulse 1.5s infinite;
}

.status-indicator.speaking {
  background: linear-gradient(45deg, #e6a23c, #f56c6c);
  color: white;
  animation: pulse 1.5s infinite;
}

@keyframes pulse {
  0% { transform: scale(1); }
  50% { transform: scale(1.1); }
  100% { transform: scale(1); }
}

.pulse {
  animation: pulse 1.5s infinite;
}

.status-text {
  font-size: 16px;
  color: #606266;
  text-align: center;
}

.voice-buttons {
  display: flex;
  gap: 15px;
  flex-wrap: wrap;
  justify-content: center;
}

.recognition-result {
  background: white;
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  padding: 15px;
  margin-bottom: 20px;
}

.result-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 10px;
  font-weight: bold;
  color: #409eff;
}

.result-content {
  min-height: 40px;
}

.interim-result {
  color: #909399;
  font-style: italic;
  margin-bottom: 5px;
}

.final-result {
  color: #303133;
  font-weight: bold;
}

.waiting-result {
  color: #c0c4cc;
  font-style: italic;
}

.voice-settings {
  margin-top: 20px;
}

.settings-content {
  padding: 10px 0;
}

.setting-item {
  display: flex;
  align-items: center;
  margin-bottom: 15px;
  gap: 15px;
}

.setting-item label {
  min-width: 80px;
  font-weight: bold;
  color: #606266;
}

.setting-item .el-select {
  flex: 1;
  max-width: 300px;
}

.setting-item .el-slider {
  flex: 1;
  max-width: 200px;
}

.browser-warning {
  margin-top: 20px;
}

@media (max-width: 768px) {
  .voice-chat {
    padding: 15px;
  }
  
  .voice-buttons {
    flex-direction: column;
    align-items: center;
  }
  
  .setting-item {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }
  
  .setting-item label {
    min-width: auto;
  }
  
  .setting-item .el-select,
  .setting-item .el-slider {
    width: 100%;
    max-width: none;
  }
}
</style>