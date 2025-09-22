<template>
  <div class="voice-recorder">
    <!-- 录音按钮 -->
    <div class="recorder-container">
      <button
        :class="['record-btn', { 'recording': isRecording, 'processing': isProcessing }]"
        @mousedown="startRecording"
        @mouseup="stopRecording"
        @mouseleave="stopRecording"
        @touchstart="startRecording"
        @touchend="stopRecording"
        :disabled="isProcessing"
      >
        <div class="btn-content">
          <div class="icon-wrapper">
            <svg v-if="!isRecording && !isProcessing" class="mic-icon" viewBox="0 0 24 24">
              <path d="M12 14c1.66 0 3-1.34 3-3V5c0-1.66-1.34-3-3-3S9 3.34 9 5v6c0 1.66 1.34 3 3 3z"/>
              <path d="M17 11c0 2.76-2.24 5-5 5s-5-2.24-5-5H5c0 3.53 2.61 6.43 6 6.92V21h2v-3.08c3.39-.49 6-3.39 6-6.92h-2z"/>
            </svg>
            <svg v-else-if="isRecording" class="recording-icon" viewBox="0 0 24 24">
              <circle cx="12" cy="12" r="10" fill="currentColor"/>
            </svg>
            <svg v-else class="processing-icon" viewBox="0 0 24 24">
              <path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm-2 15l-5-5 1.41-1.41L10 14.17l7.59-7.59L19 8l-9 9z"/>
            </svg>
          </div>
          <div class="btn-text">
            <span v-if="!isRecording && !isProcessing">按住说话</span>
            <span v-else-if="isRecording">正在录音...</span>
            <span v-else>处理中...</span>
          </div>
        </div>
        
        <!-- 录音波形动画 -->
        <div v-if="isRecording" class="wave-animation">
          <div class="wave" v-for="i in 5" :key="i" :style="{ animationDelay: `${i * 0.1}s` }"></div>
        </div>
        
        <!-- 处理动画 -->
        <div v-if="isProcessing" class="processing-animation">
          <div class="spinner"></div>
        </div>
      </button>
      
      <!-- 录音时长显示 -->
      <div v-if="isRecording" class="recording-duration">
        {{ formatDuration(recordingDuration) }}
      </div>
      
      <!-- 音量指示器 -->
      <div v-if="isRecording" class="volume-indicator">
        <div class="volume-bar" :style="{ height: `${audioLevel * 100}%` }"></div>
      </div>
    </div>
    
    <!-- 录音提示 -->
    <div class="recording-tips" :class="{ 'show': showTips }">
      <div class="tip-item">
        <span class="tip-icon">🎤</span>
        <span>按住按钮开始录音</span>
      </div>
      <div class="tip-item">
        <span class="tip-icon">⏱️</span>
        <span>最长录音60秒</span>
      </div>
      <div class="tip-item">
        <span class="tip-icon">🔊</span>
        <span>请在安静环境下录音</span>
      </div>
    </div>
    
    <!-- 错误提示 -->
    <div v-if="error" class="error-message">
      <span class="error-icon">⚠️</span>
      <span>{{ error }}</span>
      <button @click="clearError" class="close-btn">×</button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, computed } from 'vue'

// Props
interface Props {
  maxDuration?: number
  sampleRate?: number
  bitRate?: number
  format?: 'wav' | 'mp3' | 'webm'
}

const props = withDefaults(defineProps<Props>(), {
  maxDuration: 60000, // 60秒
  sampleRate: 44100,
  bitRate: 128000,
  format: 'webm'
})

// Emits
const emit = defineEmits<{
  recordingStart: []
  recordingStop: [audioBlob: Blob, duration: number]
  recordingError: [error: string]
  audioLevel: [level: number]
}>()

// 状态管理
const isRecording = ref(false)
const isProcessing = ref(false)
const recordingDuration = ref(0)
const audioLevel = ref(0)
const error = ref('')
const showTips = ref(false)

// 媒体相关
let mediaRecorder: MediaRecorder | null = null
let audioStream: MediaStream | null = null
let audioContext: AudioContext | null = null
let analyser: AnalyserNode | null = null
let dataArray: Uint8Array | null = null
let animationFrame: number | null = null
let recordingTimer: number | null = null
let recordingStartTime = 0

// 录音数据
const audioChunks: Blob[] = []

// 计算属性
const formatDuration = computed(() => {
  return (duration: number) => {
    const seconds = Math.floor(duration / 1000)
    const minutes = Math.floor(seconds / 60)
    const remainingSeconds = seconds % 60
    return `${minutes.toString().padStart(2, '0')}:${remainingSeconds.toString().padStart(2, '0')}`
  }
})

// 初始化音频上下文
const initAudioContext = async () => {
  try {
    audioContext = new (window.AudioContext || (window as any).webkitAudioContext)()
    analyser = audioContext.createAnalyser()
    analyser.fftSize = 256
    dataArray = new Uint8Array(analyser.frequencyBinCount)
  } catch (err) {
    console.error('音频上下文初始化失败:', err)
    throw new Error('音频上下文初始化失败')
  }
}

// 获取音频流
const getAudioStream = async () => {
  try {
    audioStream = await navigator.mediaDevices.getUserMedia({
      audio: {
        sampleRate: props.sampleRate,
        channelCount: 1,
        echoCancellation: true,
        noiseSuppression: true,
        autoGainControl: true
      }
    })
    
    if (audioContext && analyser) {
      const source = audioContext.createMediaStreamSource(audioStream)
      source.connect(analyser)
    }
    
    return audioStream
  } catch (err) {
    console.error('获取音频流失败:', err)
    throw new Error('无法访问麦克风，请检查权限设置')
  }
}

// 开始录音
const startRecording = async () => {
  if (isRecording.value || isProcessing.value) return
  
  try {
    clearError()
    isProcessing.value = true
    
    // 初始化音频
    await initAudioContext()
    const stream = await getAudioStream()
    
    // 创建媒体录音器
    const options: MediaRecorderOptions = {
      mimeType: `audio/${props.format}`,
      audioBitsPerSecond: props.bitRate
    }
    
    mediaRecorder = new MediaRecorder(stream, options)
    audioChunks.length = 0
    
    // 设置事件监听
    mediaRecorder.ondataavailable = (event) => {
      if (event.data.size > 0) {
        audioChunks.push(event.data)
      }
    }
    
    mediaRecorder.onstop = () => {
      const audioBlob = new Blob(audioChunks, { type: `audio/${props.format}` })
      const duration = Date.now() - recordingStartTime
      emit('recordingStop', audioBlob, duration)
      cleanup()
    }
    
    mediaRecorder.onerror = (event) => {
      const error = (event as any).error?.message || '录音过程中发生错误'
      handleError(error)
    }
    
    // 开始录音
    mediaRecorder.start(100) // 每100ms收集一次数据
    recordingStartTime = Date.now()
    isRecording.value = true
    isProcessing.value = false
    
    // 开始计时和音量监测
    startTimer()
    startAudioLevelMonitoring()
    
    emit('recordingStart')
    
  } catch (err) {
    isProcessing.value = false
    const errorMessage = err instanceof Error ? err.message : '录音启动失败'
    handleError(errorMessage)
  }
}

// 停止录音
const stopRecording = () => {
  if (!isRecording.value) return
  
  isRecording.value = false
  isProcessing.value = true
  
  if (mediaRecorder && mediaRecorder.state === 'recording') {
    mediaRecorder.stop()
  }
  
  stopTimer()
  stopAudioLevelMonitoring()
}

// 开始计时
const startTimer = () => {
  recordingDuration.value = 0
  recordingTimer = window.setInterval(() => {
    recordingDuration.value = Date.now() - recordingStartTime
    
    // 检查最大录音时长
    if (recordingDuration.value >= props.maxDuration) {
      stopRecording()
    }
  }, 100)
}

// 停止计时
const stopTimer = () => {
  if (recordingTimer) {
    clearInterval(recordingTimer)
    recordingTimer = null
  }
}

// 开始音量监测
const startAudioLevelMonitoring = () => {
  const updateAudioLevel = () => {
    if (!analyser || !dataArray || !isRecording.value) return
    
    analyser.getByteFrequencyData(dataArray)
    
    // 计算平均音量
    let sum = 0
    for (let i = 0; i < dataArray.length; i++) {
      sum += dataArray[i]
    }
    const average = sum / dataArray.length
    const level = average / 255
    
    audioLevel.value = level
    emit('audioLevel', level)
    
    animationFrame = requestAnimationFrame(updateAudioLevel)
  }
  
  updateAudioLevel()
}

// 停止音量监测
const stopAudioLevelMonitoring = () => {
  if (animationFrame) {
    cancelAnimationFrame(animationFrame)
    animationFrame = null
  }
  audioLevel.value = 0
}

// 清理资源
const cleanup = () => {
  isProcessing.value = false
  recordingDuration.value = 0
  
  if (audioStream) {
    audioStream.getTracks().forEach(track => track.stop())
    audioStream = null
  }
  
  if (audioContext) {
    audioContext.close()
    audioContext = null
  }
  
  analyser = null
  dataArray = null
  mediaRecorder = null
}

// 错误处理
const handleError = (errorMessage: string) => {
  error.value = errorMessage
  emit('recordingError', errorMessage)
  cleanup()
}

// 清除错误
const clearError = () => {
  error.value = ''
}

// 显示/隐藏提示
const toggleTips = () => {
  showTips.value = !showTips.value
}

// 生命周期
onMounted(() => {
  // 检查浏览器支持
  if (!navigator.mediaDevices || !navigator.mediaDevices.getUserMedia) {
    handleError('您的浏览器不支持录音功能')
  }
})

onUnmounted(() => {
  cleanup()
  stopTimer()
  stopAudioLevelMonitoring()
})

// 暴露方法
defineExpose({
  startRecording,
  stopRecording,
  toggleTips,
  clearError
})
</script>

<style scoped>
.voice-recorder {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 1rem;
  padding: 1rem;
}

.recorder-container {
  position: relative;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 0.5rem;
}

.record-btn {
  position: relative;
  width: 80px;
  height: 80px;
  border-radius: 50%;
  border: none;
  background: var(--primary-gradient);
  color: white;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: var(--shadow-light);
  overflow: hidden;
  user-select: none;
  -webkit-tap-highlight-color: transparent;
}

.record-btn:hover {
  transform: scale(1.05);
  box-shadow: var(--shadow-dark);
}

.record-btn:active {
  transform: scale(0.95);
}

.record-btn.recording {
  background: var(--error-color);
  animation: pulse 1.5s infinite;
}

.record-btn.processing {
  background: var(--warning-color);
  cursor: not-allowed;
}

.record-btn:disabled {
  cursor: not-allowed;
  opacity: 0.7;
}

.btn-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 0.25rem;
  position: relative;
  z-index: 2;
}

.icon-wrapper {
  width: 24px;
  height: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.mic-icon,
.recording-icon,
.processing-icon {
  width: 100%;
  height: 100%;
  fill: currentColor;
}

.recording-icon {
  animation: recordingPulse 1s infinite alternate;
}

.processing-icon {
  animation: spin 1s linear infinite;
}

.btn-text {
  font-size: 0.7rem;
  font-weight: 500;
  text-align: center;
  line-height: 1;
}

/* 录音波形动画 */
.wave-animation {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  display: flex;
  align-items: center;
  gap: 2px;
  z-index: 1;
}

.wave {
  width: 2px;
  height: 10px;
  background: rgba(255, 255, 255, 0.6);
  border-radius: 1px;
  animation: waveAnimation 1s infinite ease-in-out;
}

/* 处理动画 */
.processing-animation {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  z-index: 1;
}

.spinner {
  width: 20px;
  height: 20px;
  border: 2px solid rgba(255, 255, 255, 0.3);
  border-top: 2px solid white;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

/* 录音时长显示 */
.recording-duration {
  font-size: 0.9rem;
  font-weight: 600;
  color: var(--error-color);
  background: var(--background-light);
  padding: 0.25rem 0.75rem;
  border-radius: 20px;
  backdrop-filter: var(--blur-effect);
  border: 1px solid var(--border-light);
}

/* 音量指示器 */
.volume-indicator {
  width: 4px;
  height: 40px;
  background: var(--border-light);
  border-radius: 2px;
  overflow: hidden;
  position: relative;
}

.volume-bar {
  position: absolute;
  bottom: 0;
  left: 0;
  width: 100%;
  background: var(--success-color);
  transition: height 0.1s ease;
  border-radius: 2px;
}

/* 录音提示 */
.recording-tips {
  background: var(--background-light);
  backdrop-filter: var(--blur-effect);
  border: 1px solid var(--border-light);
  border-radius: 12px;
  padding: 1rem;
  max-width: 300px;
  opacity: 0;
  transform: translateY(10px);
  transition: all 0.3s ease;
  pointer-events: none;
}

.recording-tips.show {
  opacity: 1;
  transform: translateY(0);
  pointer-events: auto;
}

.tip-item {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  margin-bottom: 0.5rem;
  font-size: 0.85rem;
  color: var(--text-secondary);
}

.tip-item:last-child {
  margin-bottom: 0;
}

.tip-icon {
  font-size: 1rem;
}

/* 错误提示 */
.error-message {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  background: rgba(255, 107, 107, 0.1);
  border: 1px solid var(--error-color);
  color: var(--error-color);
  padding: 0.75rem 1rem;
  border-radius: 8px;
  font-size: 0.85rem;
  max-width: 300px;
  animation: slideUp 0.3s ease;
}

.error-icon {
  font-size: 1rem;
  flex-shrink: 0;
}

.close-btn {
  background: none;
  border: none;
  color: var(--error-color);
  font-size: 1.2rem;
  cursor: pointer;
  padding: 0;
  margin-left: auto;
  width: 20px;
  height: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  transition: background-color 0.2s ease;
}

.close-btn:hover {
  background: rgba(255, 107, 107, 0.1);
}

/* 动画 */
@keyframes pulse {
  0%, 100% {
    transform: scale(1);
  }
  50% {
    transform: scale(1.1);
  }
}

@keyframes recordingPulse {
  0% {
    opacity: 1;
  }
  100% {
    opacity: 0.5;
  }
}

@keyframes waveAnimation {
  0%, 100% {
    height: 10px;
  }
  50% {
    height: 20px;
  }
}

@keyframes spin {
  0% {
    transform: rotate(0deg);
  }
  100% {
    transform: rotate(360deg);
  }
}

/* 响应式设计 */
@media (max-width: 768px) {
  .record-btn {
    width: 70px;
    height: 70px;
  }
  
  .btn-text {
    font-size: 0.6rem;
  }
  
  .recording-tips {
    max-width: 250px;
    padding: 0.75rem;
  }
  
  .tip-item {
    font-size: 0.8rem;
  }
}

@media (max-width: 480px) {
  .record-btn {
    width: 60px;
    height: 60px;
  }
  
  .icon-wrapper {
    width: 20px;
    height: 20px;
  }
  
  .btn-text {
    font-size: 0.55rem;
  }
}

/* 暗黑模式适配 */
[data-theme="dark"] .recording-duration {
  color: #ff8a80;
}

[data-theme="dark"] .tip-item {
  color: var(--text-secondary);
}

/* 高对比度模式 */
@media (prefers-contrast: high) {
  .record-btn {
    border: 2px solid currentColor;
  }
  
  .recording-tips {
    border-width: 2px;
  }
  
  .error-message {
    border-width: 2px;
  }
}

/* 减少动画模式 */
@media (prefers-reduced-motion: reduce) {
  .record-btn,
  .wave,
  .spinner,
  .recording-tips {
    animation: none !important;
    transition: none !important;
  }
}
</style>