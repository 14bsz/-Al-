import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import websocketService from '@/services/websocketService'

export const useChatStore = defineStore('chat', () => {
  // 状态
  const messages = ref([])
  const currentUser = ref({
    id: 'user-1',
    name: '用户',
    avatar: 'https://picsum.photos/40/40?random=1'
  })
  
  const currentCharacter = ref({
    id: 'ai-1',
    name: '智能助手',
    description: '专业的AI助手，能够帮助您解决各种问题',
    avatar: 'https://picsum.photos/40/40?random=2',
    category: 'assistant',
    tags: ['助手', '专业', '全能'],
    rating: 4.8,
    usage: 125
  })
  
  const isTyping = ref(false)
  const isConnected = ref(false)
  const connectionStatus = ref('disconnected')
  const currentRoom = ref(null)
  const onlineUsers = ref([])
  
  // 计算属性
  const messageCount = computed(() => messages.value.length)
  const lastMessage = computed(() => {
    return messages.value.length > 0 ? messages.value[messages.value.length - 1] : null
  })
  
  // WebSocket相关方法
  const connectWebSocket = async () => {
    try {
      await websocketService.connect(currentUser.value.id)
      isConnected.value = websocketService.isConnected.value
      connectionStatus.value = websocketService.connectionStatus.value
      
      // 设置事件监听器
      setupWebSocketListeners()
      
      return true
    } catch (error) {
      console.error('WebSocket连接失败:', error)
      return false
    }
  }
  
  const disconnectWebSocket = () => {
    websocketService.disconnect()
    isConnected.value = false
    connectionStatus.value = 'disconnected'
  }
  
  const setupWebSocketListeners = () => {
    // 监听消息
    websocketService.on('message', (data) => {
      addMessage({
        type: data.messageType || 'text',
        content: data.content,
        isUser: data.userId === currentUser.value.id,
        avatar: data.avatar || currentCharacter.value.avatar,
        userId: data.userId,
        metadata: data.metadata
      })
    })
    
    // 监听输入状态
    websocketService.on('typing', (data) => {
      if (data.userId !== currentUser.value.id) {
        setTyping(data.isTyping)
      }
    })
    
    // 监听用户加入
    websocketService.on('userJoin', (data) => {
      if (!onlineUsers.value.find(user => user.id === data.userId)) {
        onlineUsers.value.push({
          id: data.userId,
          name: data.userName || '用户',
          avatar: data.avatar || 'https://picsum.photos/40/40?random=3'
        })
      }
      
      addMessage({
        type: 'system',
        content: `${data.userName || '用户'} 加入了聊天`,
        isUser: false
      })
    })
    
    // 监听用户离开
    websocketService.on('userLeave', (data) => {
      onlineUsers.value = onlineUsers.value.filter(user => user.id !== data.userId)
      
      addMessage({
        type: 'system',
        content: `${data.userName || '用户'} 离开了聊天`,
        isUser: false
      })
    })
    
    // 监听连接状态变化
    websocketService.on('connect', () => {
      isConnected.value = true
      connectionStatus.value = 'connected'
    })
    
    websocketService.on('disconnect', () => {
      isConnected.value = false
      connectionStatus.value = 'disconnected'
    })
    
    websocketService.on('error', (error) => {
      console.error('WebSocket错误:', error)
      connectionStatus.value = 'error'
    })
  }
  
  // 方法
  const addMessage = (message) => {
    const newMessage = {
      id: Date.now() + Math.random(),
      timestamp: new Date(),
      ...message
    }
    messages.value.push(newMessage)
    return newMessage
  }
  
  const removeMessage = (messageId) => {
    const index = messages.value.findIndex(msg => msg.id === messageId)
    if (index > -1) {
      messages.value.splice(index, 1)
    }
  }
  
  const clearMessages = () => {
    messages.value = []
  }
  
  const updateMessage = (messageId, updates) => {
    const message = messages.value.find(msg => msg.id === messageId)
    if (message) {
      Object.assign(message, updates)
    }
  }
  
  const setTyping = (typing) => {
    isTyping.value = typing
  }
  
  const setConnectionStatus = (status) => {
    connectionStatus.value = status
    isConnected.value = status === 'connected'
  }
  
  const setCurrentCharacter = (character) => {
    currentCharacter.value = character
  }
  
  // 初始化示例消息
  const initializeChat = () => {
    if (messages.value.length === 0) {
      addMessage({
        type: 'system',
        content: '欢迎使用AI智能聊天系统！',
        isUser: false
      })
      
      addMessage({
        type: 'ai',
        content: '你好！我是你的AI助手，有什么可以帮助你的吗？',
        isUser: false,
        model: 'DeepSeek-V2'
      })
    }
  }
  
  // 发送消息（通过WebSocket）
  const sendMessage = (content, type = 'text', metadata = {}) => {
    if (isConnected.value) {
      // 通过WebSocket发送
      websocketService.sendMessage(content, currentUser.value.id, type)
    } else {
      // 离线模式，直接添加到本地
      addMessage({
        type,
        content,
        isUser: true,
        avatar: currentUser.value.avatar,
        userId: currentUser.value.id
      })
    }
  }
  
  // 发送输入状态
  const sendTypingStatus = (isTyping) => {
    if (isConnected.value) {
      websocketService.sendTypingStatus(isTyping, currentUser.value.id)
    }
  }
  
  // 加入房间
  const joinRoom = (roomId) => {
    currentRoom.value = roomId
    if (isConnected.value) {
      websocketService.joinRoom(roomId, currentUser.value.id)
    }
  }
  
  // 离开房间
  const leaveRoom = () => {
    if (currentRoom.value && isConnected.value) {
      websocketService.leaveRoom(currentRoom.value, currentUser.value.id)
    }
    currentRoom.value = null
  }
  
  return {
    // 状态
    messages,
    currentUser,
    currentCharacter,
    isTyping,
    isConnected,
    connectionStatus,
    currentRoom,
    onlineUsers,
    
    // 计算属性
    messageCount,
    lastMessage,
    
    // WebSocket方法
    connectWebSocket,
    disconnectWebSocket,
    sendMessage,
    sendTypingStatus,
    joinRoom,
    leaveRoom,
    
    // 本地方法
    addMessage,
    removeMessage,
    clearMessages,
    updateMessage,
    setTyping,
    setConnectionStatus,
    setCurrentCharacter,
    initializeChat
  }
})