import { ref, onUnmounted } from 'vue'

// 声明全局类型
declare global {
  interface Window {
    SpeechRecognition: typeof SpeechRecognition
    webkitSpeechRecognition: typeof SpeechRecognition
  }
}

// 定义语音识别接口
interface SpeechRecognition extends EventTarget {
  continuous: boolean
  interimResults: boolean
  lang: string
  start(): void
  stop(): void
  abort(): void
  onstart: ((this: SpeechRecognition, ev: Event) => any) | null
  onresult: ((this: SpeechRecognition, ev: SpeechRecognitionEvent) => any) | null
  onerror: ((this: SpeechRecognition, ev: SpeechRecognitionErrorEvent) => any) | null
  onend: ((this: SpeechRecognition, ev: Event) => any) | null
}

interface SpeechRecognitionEvent extends Event {
  results: SpeechRecognitionResultList
  resultIndex: number
}

interface SpeechRecognitionErrorEvent extends Event {
  error: string
  message: string
}

interface SpeechRecognitionResultList {
  readonly length: number
  item(index: number): SpeechRecognitionResult
  [index: number]: SpeechRecognitionResult
}

interface SpeechRecognitionResult {
  readonly length: number
  readonly isFinal: boolean
  item(index: number): SpeechRecognitionAlternative
  [index: number]: SpeechRecognitionAlternative
}

interface SpeechRecognitionAlternative {
  readonly transcript: string
  readonly confidence: number
}

declare var SpeechRecognition: {
  prototype: SpeechRecognition
  new(): SpeechRecognition
}

export interface SpeechRecognitionOptions {
  continuous?: boolean
  interimResults?: boolean
  lang?: string
}

export const useSpeechRecognition = (options: SpeechRecognitionOptions = {}) => {
  const isSupported = ref(false)
  const isListening = ref(false)
  const isRecording = ref(false)
  const transcript = ref('')
  const error = ref<string | null>(null)
  
  let recognition: SpeechRecognition | null = null
  let mediaRecorder: MediaRecorder | null = null
  let audioChunks: Blob[] = []
  let stream: MediaStream | null = null

  // 检查浏览器支持
  const checkSupport = () => {
    const SpeechRecognition = window.SpeechRecognition || window.webkitSpeechRecognition
    isSupported.value = !!SpeechRecognition && !!navigator.mediaDevices?.getUserMedia
    return isSupported.value
  }

  // 初始化语音识别
  const initSpeechRecognition = () => {
    if (!checkSupport()) {
      error.value = '您的浏览器不支持语音识别功能'
      return false
    }

    const SpeechRecognition = window.SpeechRecognition || window.webkitSpeechRecognition
    recognition = new SpeechRecognition()
    
    recognition.continuous = options.continuous ?? false
    recognition.interimResults = options.interimResults ?? true
    recognition.lang = options.lang ?? 'zh-CN'

    recognition.onstart = () => {
      isListening.value = true
      error.value = null
    }

    recognition.onresult = (event) => {
      let finalTranscript = ''
      let interimTranscript = ''

      for (let i = event.resultIndex; i < event.results.length; i++) {
        const result = event.results[i]
        if (result.isFinal) {
          finalTranscript += result[0].transcript
        } else {
          interimTranscript += result[0].transcript
        }
      }

      transcript.value = finalTranscript || interimTranscript
    }

    recognition.onerror = (event) => {
      error.value = `语音识别错误: ${event.error}`
      isListening.value = false
    }

    recognition.onend = () => {
      isListening.value = false
    }

    return true
  }

  // 开始语音识别
  const startListening = () => {
    if (!recognition && !initSpeechRecognition()) {
      return
    }

    if (isListening.value) {
      return
    }

    transcript.value = ''
    error.value = null
    recognition?.start()
  }

  // 停止语音识别
  const stopListening = () => {
    if (recognition && isListening.value) {
      recognition.stop()
    }
  }

  // 开始录音
  const startRecording = async (): Promise<boolean> => {
    try {
      if (!navigator.mediaDevices?.getUserMedia) {
        error.value = '您的浏览器不支持录音功能'
        return false
      }

      stream = await navigator.mediaDevices.getUserMedia({ 
        audio: {
          echoCancellation: true,
          noiseSuppression: true,
          sampleRate: 44100
        } 
      })

      audioChunks = []
      mediaRecorder = new MediaRecorder(stream, {
        mimeType: 'audio/webm;codecs=opus'
      })

      mediaRecorder.ondataavailable = (event) => {
        if (event.data.size > 0) {
          audioChunks.push(event.data)
        }
      }

      mediaRecorder.onstart = () => {
        isRecording.value = true
        error.value = null
      }

      mediaRecorder.onstop = () => {
        isRecording.value = false
        if (stream) {
          stream.getTracks().forEach(track => track.stop())
          stream = null
        }
      }

      mediaRecorder.onerror = (event) => {
        error.value = `录音错误: ${event.error}`
        isRecording.value = false
      }

      mediaRecorder.start(100) // 每100ms收集一次数据
      return true

    } catch (err) {
      error.value = `无法访问麦克风: ${err instanceof Error ? err.message : '未知错误'}`
      isRecording.value = false
      return false
    }
  }

  // 停止录音并返回音频Blob
  const stopRecording = (): Promise<Blob | null> => {
    return new Promise((resolve) => {
      if (!mediaRecorder || !isRecording.value) {
        resolve(null)
        return
      }

      mediaRecorder.onstop = () => {
        isRecording.value = false
        if (stream) {
          stream.getTracks().forEach(track => track.stop())
          stream = null
        }

        if (audioChunks.length > 0) {
          const audioBlob = new Blob(audioChunks, { type: 'audio/webm;codecs=opus' })
          audioChunks = []
          resolve(audioBlob)
        } else {
          resolve(null)
        }
      }

      mediaRecorder.stop()
    })
  }

  // 将WebM格式转换为WAV格式（可选）
  const convertToWav = async (webmBlob: Blob): Promise<Blob> => {
    // 这里可以实现WebM到WAV的转换
    // 为了简化，直接返回原始Blob
    return webmBlob
  }

  // 获取音频URL用于播放
  const getAudioUrl = (audioBlob: Blob): string => {
    return URL.createObjectURL(audioBlob)
  }

  // 清理资源
  const cleanup = () => {
    if (recognition) {
      recognition.abort()
      recognition = null
    }
    
    if (mediaRecorder && isRecording.value) {
      mediaRecorder.stop()
    }
    
    if (stream) {
      stream.getTracks().forEach(track => track.stop())
      stream = null
    }
    
    isListening.value = false
    isRecording.value = false
    transcript.value = ''
    error.value = null
  }

  // 组件卸载时清理
  onUnmounted(() => {
    cleanup()
  })

  // 初始化检查支持
  checkSupport()

  return {
    // 状态
    isSupported,
    isListening,
    isRecording,
    transcript,
    error,
    
    // 方法
    startListening,
    stopListening,
    startRecording,
    stopRecording,
    convertToWav,
    getAudioUrl,
    cleanup
  }
}