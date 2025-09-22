/**
 * 语音服务
 * 集成Web Speech API实现语音识别(ASR)和文本转语音(TTS)功能
 */

class SpeechService {
  constructor() {
    // 语音识别相关
    this.recognition = null
    this.isListening = false
    this.recognitionCallbacks = {
      onResult: null,
      onError: null,
      onStart: null,
      onEnd: null
    }

    // 语音合成相关
    this.synthesis = window.speechSynthesis
    this.voices = []
    this.currentVoice = null
    this.isSpeaking = false

    // 初始化
    this.initSpeechRecognition()
    this.initSpeechSynthesis()
  }

  /**
   * 初始化语音识别
   */
  initSpeechRecognition() {
    if (!('webkitSpeechRecognition' in window) && !('SpeechRecognition' in window)) {
      console.warn('浏览器不支持语音识别功能')
      return
    }

    const SpeechRecognition = window.SpeechRecognition || window.webkitSpeechRecognition
    this.recognition = new SpeechRecognition()

    // 配置语音识别
    this.recognition.continuous = false // 不连续识别
    this.recognition.interimResults = true // 显示中间结果
    this.recognition.lang = 'zh-CN' // 默认中文
    this.recognition.maxAlternatives = 1

    // 绑定事件
    this.recognition.onstart = () => {
      this.isListening = true
      console.log('语音识别开始')
      if (this.recognitionCallbacks.onStart) {
        this.recognitionCallbacks.onStart()
      }
    }

    this.recognition.onresult = (event) => {
      let finalTranscript = ''
      let interimTranscript = ''

      for (let i = event.resultIndex; i < event.results.length; i++) {
        const transcript = event.results[i][0].transcript
        if (event.results[i].isFinal) {
          finalTranscript += transcript
        } else {
          interimTranscript += transcript
        }
      }

      if (this.recognitionCallbacks.onResult) {
        this.recognitionCallbacks.onResult({
          final: finalTranscript,
          interim: interimTranscript,
          isFinal: finalTranscript.length > 0
        })
      }
    }

    this.recognition.onerror = (event) => {
      console.error('语音识别错误:', event.error)
      this.isListening = false
      if (this.recognitionCallbacks.onError) {
        this.recognitionCallbacks.onError(event.error)
      }
    }

    this.recognition.onend = () => {
      this.isListening = false
      console.log('语音识别结束')
      if (this.recognitionCallbacks.onEnd) {
        this.recognitionCallbacks.onEnd()
      }
    }
  }

  /**
   * 初始化语音合成
   */
  initSpeechSynthesis() {
    if (!('speechSynthesis' in window)) {
      console.warn('浏览器不支持语音合成功能')
      return
    }

    // 加载可用的语音
    this.loadVoices()

    // 监听语音列表变化
    if (speechSynthesis.onvoiceschanged !== undefined) {
      speechSynthesis.onvoiceschanged = () => {
        this.loadVoices()
      }
    }
  }

  /**
   * 加载可用的语音
   */
  loadVoices() {
    this.voices = speechSynthesis.getVoices()
    
    // 优先选择中文语音
    const chineseVoice = this.voices.find(voice => 
      voice.lang.includes('zh') || voice.lang.includes('CN')
    )
    
    if (chineseVoice) {
      this.currentVoice = chineseVoice
    } else if (this.voices.length > 0) {
      this.currentVoice = this.voices[0]
    }

    console.log('可用语音:', this.voices.map(v => ({ name: v.name, lang: v.lang })))
  }

  /**
   * 开始语音识别
   */
  startListening(callbacks = {}) {
    if (!this.recognition) {
      throw new Error('语音识别不可用')
    }

    if (this.isListening) {
      console.warn('语音识别已在进行中')
      return
    }

    // 设置回调函数
    this.recognitionCallbacks = { ...this.recognitionCallbacks, ...callbacks }

    try {
      this.recognition.start()
    } catch (error) {
      console.error('启动语音识别失败:', error)
      throw error
    }
  }

  /**
   * 停止语音识别
   */
  stopListening() {
    if (this.recognition && this.isListening) {
      this.recognition.stop()
    }
  }

  /**
   * 文本转语音
   */
  speak(text, options = {}) {
    return new Promise((resolve, reject) => {
      if (!this.synthesis) {
        reject(new Error('语音合成不可用'))
        return
      }

      if (this.isSpeaking) {
        this.synthesis.cancel() // 取消当前播放
      }

      const utterance = new SpeechSynthesisUtterance(text)
      
      // 设置语音参数
      utterance.voice = options.voice || this.currentVoice
      utterance.rate = options.rate || 1.0 // 语速
      utterance.pitch = options.pitch || 1.0 // 音调
      utterance.volume = options.volume || 1.0 // 音量
      utterance.lang = options.lang || 'zh-CN'

      // 绑定事件
      utterance.onstart = () => {
        this.isSpeaking = true
        console.log('开始语音播放')
        if (options.onStart) options.onStart()
      }

      utterance.onend = () => {
        this.isSpeaking = false
        console.log('语音播放结束')
        if (options.onEnd) options.onEnd()
        resolve()
      }

      utterance.onerror = (event) => {
        this.isSpeaking = false
        console.error('语音播放错误:', event.error)
        if (options.onError) options.onError(event.error)
        reject(new Error(event.error))
      }

      // 开始播放
      this.synthesis.speak(utterance)
    })
  }

  /**
   * 停止语音播放
   */
  stopSpeaking() {
    if (this.synthesis && this.isSpeaking) {
      this.synthesis.cancel()
      this.isSpeaking = false
    }
  }

  /**
   * 暂停语音播放
   */
  pauseSpeaking() {
    if (this.synthesis && this.isSpeaking) {
      this.synthesis.pause()
    }
  }

  /**
   * 恢复语音播放
   */
  resumeSpeaking() {
    if (this.synthesis) {
      this.synthesis.resume()
    }
  }

  /**
   * 设置语音
   */
  setVoice(voiceName) {
    const voice = this.voices.find(v => v.name === voiceName)
    if (voice) {
      this.currentVoice = voice
      return true
    }
    return false
  }

  /**
   * 获取可用语音列表
   */
  getVoices() {
    return this.voices.map(voice => ({
      name: voice.name,
      lang: voice.lang,
      gender: voice.name.toLowerCase().includes('female') ? 'female' : 'male'
    }))
  }

  /**
   * 检查浏览器支持情况
   */
  checkSupport() {
    return {
      speechRecognition: !!(window.SpeechRecognition || window.webkitSpeechRecognition),
      speechSynthesis: !!window.speechSynthesis,
      getUserMedia: !!(navigator.mediaDevices && navigator.mediaDevices.getUserMedia)
    }
  }

  /**
   * 获取当前状态
   */
  getStatus() {
    return {
      isListening: this.isListening,
      isSpeaking: this.isSpeaking,
      currentVoice: this.currentVoice ? this.currentVoice.name : null,
      voicesLoaded: this.voices.length > 0
    }
  }

  /**
   * 设置语言
   */
  setLanguage(lang) {
    if (this.recognition) {
      this.recognition.lang = lang
    }
  }

  /**
   * 角色语音配置
   * 为不同角色配置不同的语音特征
   */
  getCharacterVoiceConfig(characterName) {
    const configs = {
      '哈利波特': { rate: 1.1, pitch: 1.1, volume: 0.9 },
      '苏格拉底': { rate: 0.9, pitch: 0.9, volume: 1.0 },
      '爱因斯坦': { rate: 0.95, pitch: 0.95, volume: 0.95 },
      '莎士比亚': { rate: 1.0, pitch: 1.05, volume: 1.0 },
      '孔子': { rate: 0.9, pitch: 0.9, volume: 1.0 },
      '居里夫人': { rate: 1.0, pitch: 1.1, volume: 0.9 }
    }

    return configs[characterName] || { rate: 1.0, pitch: 1.0, volume: 1.0 }
  }

  /**
   * 为角色说话
   */
  speakAsCharacter(text, characterName, options = {}) {
    const characterConfig = this.getCharacterVoiceConfig(characterName)
    const mergedOptions = { ...characterConfig, ...options }
    
    return this.speak(text, mergedOptions)
  }
}

// 创建单例实例
const speechService = new SpeechService()

export default speechService