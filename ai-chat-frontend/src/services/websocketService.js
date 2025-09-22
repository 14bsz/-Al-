import { ref } from 'vue'
import { Client } from '@stomp/stompjs'

class WebSocketService {
  constructor() {
    this.client = null
    this.url = 'http://localhost:8080/ws'
    this.reconnectAttempts = 0
    this.maxReconnectAttempts = 5
    this.reconnectInterval = 3000
    
    // 响应式状态
    this.isConnected = ref(false)
    this.connectionStatus = ref('disconnected') // disconnected, connecting, connected, error
    this.lastError = ref(null)
    this.messageQueue = []
    
    // 事件监听器
    this.listeners = {
      message: [],
      connect: [],
      disconnect: [],
      error: [],
      typing: [],
      userJoin: [],
      userLeave: []
    }
  }
  
  // 连接WebSocket
  connect(userId = null, token = null) {
    if (this.client && this.client.connected) {
      console.log('STOMP客户端已连接')
      return Promise.resolve()
    }
    
    return new Promise((resolve, reject) => {
      try {
        this.connectionStatus.value = 'connecting'
        
        // 创建STOMP客户端
        this.client = new Client({
          brokerURL: this.url,
          connectHeaders: {
            userId: userId || 'anonymous',
            token: token || ''
          },
          debug: function (str) {
            console.log('STOMP Debug:', str)
          },
          reconnectDelay: this.reconnectInterval,
          heartbeatIncoming: 4000,
          heartbeatOutgoing: 4000,
          // 添加WebSocket工厂配置
          webSocketFactory: () => {
            return new WebSocket(this.url.replace('http://', 'ws://'))
          }
        })
        
        this.client.onConnect = (frame) => {
          console.log('STOMP连接成功:', frame)
          this.isConnected.value = true
          this.connectionStatus.value = 'connected'
          this.reconnectAttempts = 0
          this.lastError.value = null
          
          // 订阅消息
          this.subscribeToTopics(userId)
          
          // 发送队列中的消息
          this.flushMessageQueue()
          
          // 触发连接事件
          this.emit('connect', frame)
          resolve()
        }
        
        this.client.onStompError = (frame) => {
          console.error('STOMP错误:', frame.headers['message'])
          console.error('详细信息:', frame.body)
          this.lastError.value = frame
          this.connectionStatus.value = 'error'
          this.emit('error', frame)
          reject(frame)
        }
        
        this.client.onWebSocketClose = (event) => {
          console.log('WebSocket连接关闭:', event)
          this.isConnected.value = false
          this.connectionStatus.value = 'disconnected'
          
          // 触发断开连接事件
          this.emit('disconnect', event)
        }
        
        this.client.onWebSocketError = (error) => {
          console.error('WebSocket错误:', error)
          this.lastError.value = error
          this.connectionStatus.value = 'error'
          this.emit('error', error)
          reject(error)
        }
        
        // 激活连接
        this.client.activate()
        
      } catch (error) {
        console.error('创建STOMP客户端失败:', error)
        this.connectionStatus.value = 'error'
        this.lastError.value = error
        reject(error)
      }
    })
  }
  
  // 订阅主题
  subscribeToTopics(userId) {
    if (!this.client || !this.client.connected) {
      return
    }
    
    // 订阅广播消息
    this.client.subscribe('/topic/messages', (message) => {
      try {
        const data = JSON.parse(message.body)
        this.handleMessage(data)
      } catch (error) {
        console.error('解析消息失败:', error)
      }
    })
    
    // 订阅用户私有消息
    if (userId) {
      this.client.subscribe(`/user/${userId}/queue/messages`, (message) => {
        try {
          const data = JSON.parse(message.body)
          this.handleMessage(data)
        } catch (error) {
          console.error('解析私有消息失败:', error)
        }
      })
    }
    
    // 订阅系统通知
    this.client.subscribe('/topic/notifications', (message) => {
      try {
        const data = JSON.parse(message.body)
        this.handleMessage(data)
      } catch (error) {
        console.error('解析系统通知失败:', error)
      }
    })
  }
  
  // 断开连接
  disconnect() {
    if (this.client) {
      this.client.deactivate()
      this.client = null
    }
    this.isConnected.value = false
    this.connectionStatus.value = 'disconnected'
  }
  
  // 发送消息
  send(destination, message) {
    if (!this.isConnected.value || !this.client) {
      console.warn('STOMP客户端未连接，消息已加入队列')
      this.messageQueue.push({ destination, message })
      return false
    }
    
    try {
      const messageData = {
        id: Date.now() + Math.random(),
        timestamp: new Date().toISOString(),
        ...message
      }
      
      this.client.publish({
        destination: destination,
        body: JSON.stringify(messageData)
      })
      return true
    } catch (error) {
      console.error('发送STOMP消息失败:', error)
      return false
    }
  }
  
  // 发送聊天消息
  sendMessage(content, userId = null, messageType = 'text') {
    return this.send('/app/chat.sendMessage', {
      type: 'chat_message',
      content,
      userId,
      messageType
    })
  }
  
  // 发送输入状态
  sendTypingStatus(isTyping, userId = null) {
    return this.send('/app/chat.typing', {
      type: 'typing_status',
      isTyping,
      userId
    })
  }
  
  // 加入房间
  joinRoom(roomId, userId = null) {
    return this.send('/app/chat.joinRoom', {
      type: 'join_room',
      roomId,
      userId
    })
  }
  
  // 离开房间
  leaveRoom(roomId, userId = null) {
    return this.send('/app/chat.leaveRoom', {
      type: 'leave_room',
      roomId,
      userId
    })
  }
  
  // 处理接收到的消息
  handleMessage(data) {
    switch (data.type) {
      case 'chat_message':
        this.emit('message', data)
        break
      case 'typing_status':
        this.emit('typing', data)
        break
      case 'user_join':
        this.emit('userJoin', data)
        break
      case 'user_leave':
        this.emit('userLeave', data)
        break
      case 'system':
        this.emit('message', data)
        break
      case 'error':
        console.error('服务器错误:', data.message)
        this.emit('error', data)
        break
      default:
        console.log('未知消息类型:', data.type, data)
        this.emit('message', data)
    }
  }
  
  // 添加事件监听器
  on(event, callback) {
    if (this.listeners[event]) {
      this.listeners[event].push(callback)
    }
  }
  
  // 移除事件监听器
  off(event, callback) {
    if (this.listeners[event]) {
      const index = this.listeners[event].indexOf(callback)
      if (index > -1) {
        this.listeners[event].splice(index, 1)
      }
    }
  }
  
  // 触发事件
  emit(event, data) {
    if (this.listeners[event]) {
      this.listeners[event].forEach(callback => {
        try {
          callback(data)
        } catch (error) {
          console.error('事件监听器执行失败:', error)
        }
      })
    }
  }
  
  // 发送队列中的消息
  flushMessageQueue() {
    while (this.messageQueue.length > 0) {
      const { destination, message } = this.messageQueue.shift()
      this.send(destination, message)
    }
  }
  
  // 获取连接状态
  getStatus() {
    return {
      isConnected: this.isConnected.value,
      status: this.connectionStatus.value,
      error: this.lastError.value,
      reconnectAttempts: this.reconnectAttempts
    }
  }
}

// 创建单例实例
const websocketService = new WebSocketService()

// 导出服务和响应式状态
export default websocketService
export const useWebSocket = () => {
  return {
    websocketService,
    isConnected: websocketService.isConnected,
    connectionStatus: websocketService.connectionStatus,
    lastError: websocketService.lastError
  }
}