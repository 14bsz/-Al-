import axios from 'axios'

const API_BASE_URL = 'http://localhost:8080/api/ai-skills'

/**
 * AI技能服务类
 * 提供情感分析、知识问答、创意写作等功能的API调用
 */
class AISkillService {
  constructor() {
    this.axiosInstance = axios.create({
      baseURL: API_BASE_URL,
      timeout: 30000,
      headers: {
        'Content-Type': 'application/json'
      }
    })
    
    // 请求拦截器
    this.axiosInstance.interceptors.request.use(
      config => {
        console.log('AI技能请求:', config.method?.toUpperCase(), config.url, config.data)
        return config
      },
      error => {
        console.error('AI技能请求错误:', error)
        return Promise.reject(error)
      }
    )
    
    // 响应拦截器
    this.axiosInstance.interceptors.response.use(
      response => {
        console.log('AI技能响应:', response.status, response.data)
        return response
      },
      error => {
        console.error('AI技能响应错误:', error.response?.status, error.response?.data || error.message)
        return Promise.reject(error)
      }
    )
  }
  
  /**
   * 情感分析
   * @param {string} text - 要分析的文本
   * @param {string} characterName - 角色名称
   * @returns {Promise<Object>} 情感分析结果
   */
  async analyzeEmotion(text, characterName = '') {
    try {
      const response = await this.axiosInstance.post('/emotion-analysis', {
        text,
        characterName
      })
      
      return {
        success: true,
        data: response.data,
        message: '情感分析完成'
      }
    } catch (error) {
      console.error('情感分析失败:', error)
      return {
        success: false,
        data: null,
        message: error.response?.data?.message || '情感分析失败，请稍后重试'
      }
    }
  }
  
  /**
   * 知识问答
   * @param {string} question - 用户问题
   * @param {string} characterName - 角色名称
   * @param {string} context - 对话上下文
   * @returns {Promise<Object>} 知识问答结果
   */
  async answerQuestion(question, characterName = '', context = '') {
    try {
      const response = await this.axiosInstance.post('/knowledge-qa', {
        question,
        characterName,
        context
      })
      
      return {
        success: true,
        data: response.data,
        message: '知识问答完成'
      }
    } catch (error) {
      console.error('知识问答失败:', error)
      return {
        success: false,
        data: null,
        message: error.response?.data?.message || '知识问答失败，请稍后重试'
      }
    }
  }
  
  /**
   * 创意写作
   * @param {string} prompt - 写作提示
   * @param {string} characterName - 角色名称
   * @param {string} writingType - 写作类型
   * @returns {Promise<Object>} 创意写作结果
   */
  async creativeWriting(prompt, characterName = '', writingType = '') {
    try {
      const response = await this.axiosInstance.post('/creative-writing', {
        prompt,
        characterName,
        writingType
      })
      
      return {
        success: true,
        data: response.data,
        message: '创意写作完成'
      }
    } catch (error) {
      console.error('创意写作失败:', error)
      return {
        success: false,
        data: null,
        message: error.response?.data?.message || '创意写作失败，请稍后重试'
      }
    }
  }
  
  /**
   * 综合技能处理
   * @param {string} userInput - 用户输入
   * @param {string} characterName - 角色名称
   * @param {Array<string>} conversationHistory - 对话历史
   * @returns {Promise<Object>} 综合处理结果
   */
  async processWithSkills(userInput, characterName = '', conversationHistory = []) {
    try {
      const response = await this.axiosInstance.post('/process-with-skills', {
        userInput,
        characterName,
        conversationHistory
      })
      
      return {
        success: true,
        data: response.data,
        message: '技能处理完成'
      }
    } catch (error) {
      console.error('技能处理失败:', error)
      return {
        success: false,
        data: null,
        message: error.response?.data?.message || '技能处理失败，请稍后重试'
      }
    }
  }
  
  /**
   * 获取角色技能配置
   * @param {string} characterName - 角色名称
   * @returns {Promise<Object>} 角色技能配置
   */
  async getCharacterSkillConfig(characterName) {
    try {
      const response = await this.axiosInstance.get(`/character-config/${encodeURIComponent(characterName)}`)
      
      return {
        success: true,
        data: response.data,
        message: '获取角色配置成功'
      }
    } catch (error) {
      console.error('获取角色配置失败:', error)
      return {
        success: false,
        data: null,
        message: error.response?.data?.message || '获取角色配置失败'
      }
    }
  }
  
  /**
   * 获取可用技能列表
   * @returns {Promise<Object>} 可用技能列表
   */
  async getAvailableSkills() {
    try {
      const response = await this.axiosInstance.get('/available-skills')
      
      return {
        success: true,
        data: response.data,
        message: '获取技能列表成功'
      }
    } catch (error) {
      console.error('获取技能列表失败:', error)
      return {
        success: false,
        data: null,
        message: error.response?.data?.message || '获取技能列表失败'
      }
    }
  }
  
  /**
   * 批量处理多个技能
   * @param {Array} requests - 技能请求数组
   * @returns {Promise<Array>} 批量处理结果
   */
  async batchProcessSkills(requests) {
    try {
      const promises = requests.map(request => {
        switch (request.type) {
          case 'emotion':
            return this.analyzeEmotion(request.text, request.characterName)
          case 'knowledge':
            return this.answerQuestion(request.question, request.characterName, request.context)
          case 'creative':
            return this.creativeWriting(request.prompt, request.characterName, request.writingType)
          case 'comprehensive':
            return this.processWithSkills(request.userInput, request.characterName, request.conversationHistory)
          default:
            return Promise.resolve({ success: false, message: '未知的技能类型' })
        }
      })
      
      const results = await Promise.allSettled(promises)
      
      return results.map((result, index) => ({
        index,
        success: result.status === 'fulfilled' && result.value.success,
        data: result.status === 'fulfilled' ? result.value.data : null,
        message: result.status === 'fulfilled' ? result.value.message : result.reason?.message || '处理失败'
      }))
    } catch (error) {
      console.error('批量处理技能失败:', error)
      return []
    }
  }
  
  /**
   * 智能技能选择
   * 根据用户输入自动选择最合适的技能
   * @param {string} userInput - 用户输入
   * @returns {string} 推荐的技能类型
   */
  detectBestSkill(userInput) {
    if (!userInput || typeof userInput !== 'string') {
      return 'comprehensive'
    }
    
    const input = userInput.toLowerCase()
    
    // 情感分析触发词
    const emotionTriggers = ['感觉', '心情', '情绪', '开心', '难过', '生气', '害怕', '担心', '兴奋']
    if (emotionTriggers.some(trigger => input.includes(trigger))) {
      return 'emotion'
    }
    
    // 知识问答触发词
    const questionTriggers = ['什么', '怎么', '为什么', '如何', '哪里', '什么时候', '谁', '?', '？']
    if (questionTriggers.some(trigger => input.includes(trigger))) {
      return 'knowledge'
    }
    
    // 创意写作触发词
    const creativeTriggers = ['写', '创作', '编', '诗', '故事', '想象', '描述', '作文']
    if (creativeTriggers.some(trigger => input.includes(trigger))) {
      return 'creative'
    }
    
    // 默认使用综合技能
    return 'comprehensive'
  }
  
  /**
   * 格式化情感分析结果
   * @param {Object} emotionResult - 情感分析结果
   * @returns {string} 格式化的情感描述
   */
  formatEmotionResult(emotionResult) {
    if (!emotionResult) return '无法分析情感'
    
    const { emotion, confidence, emotionIntensity, emotionDescription } = emotionResult
    
    return `检测到${emotionIntensity}强度的${emotion}情感（置信度：${Math.round(confidence * 100)}%）- ${emotionDescription}`
  }
  
  /**
   * 格式化知识问答结果
   * @param {Object} knowledgeResult - 知识问答结果
   * @returns {Object} 格式化的问答信息
   */
  formatKnowledgeResult(knowledgeResult) {
    if (!knowledgeResult) return { answer: '无法获取答案', confidence: 0 }
    
    const { answer, confidence, relatedTopics, knowledgeSource } = knowledgeResult
    
    return {
      answer,
      confidence: Math.round(confidence * 100),
      relatedTopics: relatedTopics || [],
      source: knowledgeSource || '未知来源'
    }
  }
  
  /**
   * 格式化创意写作结果
   * @param {Object} writingResult - 创意写作结果
   * @returns {Object} 格式化的写作信息
   */
  formatWritingResult(writingResult) {
    if (!writingResult) return { content: '无法生成内容' }
    
    const { content, writingStyle, theme, keywords, inspiration } = writingResult
    
    return {
      content,
      style: writingStyle || '未知风格',
      theme: theme || '未知主题',
      keywords: keywords || [],
      inspiration: inspiration || '无特定灵感'
    }
  }
  
  /**
   * 获取技能使用统计
   * @returns {Object} 技能使用统计信息
   */
  getSkillUsageStats() {
    const stats = JSON.parse(localStorage.getItem('aiSkillStats') || '{}')
    
    return {
      emotionAnalysis: stats.emotionAnalysis || 0,
      knowledgeQA: stats.knowledgeQA || 0,
      creativeWriting: stats.creativeWriting || 0,
      comprehensive: stats.comprehensive || 0,
      totalUsage: Object.values(stats).reduce((sum, count) => sum + count, 0)
    }
  }
  
  /**
   * 更新技能使用统计
   * @param {string} skillType - 技能类型
   */
  updateSkillUsageStats(skillType) {
    const stats = this.getSkillUsageStats()
    stats[skillType] = (stats[skillType] || 0) + 1
    
    localStorage.setItem('aiSkillStats', JSON.stringify(stats))
  }
  
  /**
   * 清除技能使用统计
   */
  clearSkillUsageStats() {
    localStorage.removeItem('aiSkillStats')
  }
}

// 创建单例实例
const aiSkillService = new AISkillService()

export default aiSkillService

// 导出技能类型常量
export const SKILL_TYPES = {
  EMOTION: 'emotion',
  KNOWLEDGE: 'knowledge',
  CREATIVE: 'creative',
  COMPREHENSIVE: 'comprehensive'
}

// 导出情感类型常量
export const EMOTION_TYPES = {
  HAPPY: '快乐',
  SAD: '悲伤',
  ANGRY: '愤怒',
  FEAR: '恐惧',
  SURPRISE: '惊讶',
  DISGUST: '厌恶',
  NEUTRAL: '中性'
}

// 导出写作类型常量
export const WRITING_TYPES = {
  POETRY: '诗歌',
  STORY: '故事',
  DIALOGUE: '对话',
  DESCRIPTION: '描述',
  FREE: '自由创作'
}