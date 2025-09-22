import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import axios from 'axios'

// 定义聊天角色接口
export interface ChatCharacter {
  id: number
  name: string
  description: string
  systemPrompt: string
  avatar?: string
}

// 定义消息接口
export interface ChatMessage {
  id: string
  content: string
  isUser: boolean
  timestamp: Date
  audioUrl?: string
  isPlaying?: boolean
}

// 定义聊天请求接口
export interface ChatRequest {
  message: string
  characterId: number
  userId: number
  conversationId?: number
}

// 定义聊天响应接口
export interface ChatResponse {
  response: string
  audioUrl?: string
  conversationId: number
}

export const useChatStore = defineStore('chat', () => {
  // 状态定义
  const currentUser = ref<{ id: number; username: string }>({ id: 1, username: 'User' })
  const selectedCharacter = ref<ChatCharacter | null>(null)
  const messages = ref<ChatMessage[]>([])
  const isLoading = ref(false)
  const isRecording = ref(false)
  const currentConversationId = ref<number | null>(null)
  
  // 预定义角色列表
  const characters = ref<ChatCharacter[]>([
    {
      id: 1,
      name: '智能助手',
      description: '友好的AI助手，可以帮助您解答各种问题',
      systemPrompt: '你是一个友好、有帮助的AI助手。请用简洁明了的方式回答用户的问题。',
      avatar: 'https://picsum.photos/60/60?random=1'
    },
    {
      id: 2,
      name: '编程导师',
      description: '专业的编程指导老师，擅长各种编程语言',
      systemPrompt: '你是一个专业的编程导师。请用清晰的解释和实用的代码示例来帮助用户学习编程。',
      avatar: 'https://picsum.photos/60/60?random=2'
    },
    {
      id: 3,
      name: '创意写手',
      description: '富有创意的写作助手，帮助您创作各种文本',
      systemPrompt: '你是一个富有创意的写作助手。请用生动有趣的语言帮助用户创作和改进文本。',
      avatar: 'https://picsum.photos/60/60?random=3'
    }
  ])

  // 计算属性
  const hasMessages = computed(() => messages.value.length > 0)
  const lastMessage = computed(() => messages.value[messages.value.length - 1])

  // 方法定义
  const selectCharacter = (character: ChatCharacter) => {
    selectedCharacter.value = character
    // 切换角色时清空对话历史
    messages.value = []
    currentConversationId.value = null
  }

  const addMessage = (content: string, isUser: boolean, audioUrl?: string) => {
    const message: ChatMessage = {
      id: Date.now().toString(),
      content,
      isUser,
      timestamp: new Date(),
      audioUrl,
      isPlaying: false
    }
    messages.value.push(message)
    return message
  }

  const sendTextMessage = async (content: string) => {
    if (!selectedCharacter.value || isLoading.value) return

    // 添加用户消息
    addMessage(content, true)
    isLoading.value = true

    try {
      const request: ChatRequest = {
        message: content,
        characterId: selectedCharacter.value.id,
        userId: currentUser.value.id,
        conversationId: currentConversationId.value || undefined
      }

      const response = await axios.post<ChatResponse>('http://localhost:8080/api/chat/text', request)
      
      // 更新对话ID
      if (response.data.conversationId) {
        currentConversationId.value = response.data.conversationId
      }

      // 添加AI回复
      addMessage(response.data.response, false, response.data.audioUrl)
      
    } catch (error) {
      console.error('发送消息失败:', error)
      addMessage('抱歉，发送消息时出现错误，请稍后重试。', false)
    } finally {
      isLoading.value = false
    }
  }

  const sendVoiceMessage = async (audioBlob: Blob) => {
    if (!selectedCharacter.value || isLoading.value) return

    isLoading.value = true

    try {
      const formData = new FormData()
      formData.append('audioFile', audioBlob, 'voice.wav')
      formData.append('characterId', selectedCharacter.value.id.toString())
      formData.append('userId', currentUser.value.id.toString())
      if (currentConversationId.value) {
        formData.append('conversationId', currentConversationId.value.toString())
      }

      const response = await axios.post<ChatResponse>('http://localhost:8080/api/chat/voice', formData, {
        headers: {
          'Content-Type': 'multipart/form-data'
        }
      })

      // 更新对话ID
      if (response.data.conversationId) {
        currentConversationId.value = response.data.conversationId
      }

      // 添加AI回复
      addMessage(response.data.response, false, response.data.audioUrl)
      
    } catch (error) {
      console.error('发送语音消息失败:', error)
      addMessage('抱歉，发送语音消息时出现错误，请稍后重试。', false)
    } finally {
      isLoading.value = false
    }
  }

  const playAudio = async (message: ChatMessage) => {
    if (!message.audioUrl || message.isPlaying) return

    try {
      message.isPlaying = true
      const audio = new Audio(message.audioUrl)
      
      audio.onended = () => {
        message.isPlaying = false
      }
      
      audio.onerror = () => {
        message.isPlaying = false
        console.error('音频播放失败')
      }
      
      await audio.play()
    } catch (error) {
      message.isPlaying = false
      console.error('播放音频时出错:', error)
    }
  }

  const clearMessages = () => {
    messages.value = []
    currentConversationId.value = null
  }

  const setRecordingState = (recording: boolean) => {
    isRecording.value = recording
  }

  return {
    // 状态
    currentUser,
    selectedCharacter,
    messages,
    isLoading,
    isRecording,
    characters,
    currentConversationId,
    
    // 计算属性
    hasMessages,
    lastMessage,
    
    // 方法
    selectCharacter,
    addMessage,
    sendTextMessage,
    sendVoiceMessage,
    playAudio,
    clearMessages,
    setRecordingState
  }
})