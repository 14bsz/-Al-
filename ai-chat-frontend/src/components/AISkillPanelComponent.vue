<template>
  <div class="ai-skill-panel">
    <!-- 技能面板头部 -->
    <div class="panel-header">
      <div class="header-title">
        <el-icon><MagicStick /></el-icon>
        <span>AI技能中心</span>
      </div>
      
      <div class="header-actions">
        <el-button 
          size="small" 
          @click="refreshSkills"
          :loading="loading"
          icon="Refresh"
        >
          刷新
        </el-button>
        
        <el-button 
          size="small" 
          @click="showSkillStats"
          icon="DataAnalysis"
        >
          统计
        </el-button>
      </div>
    </div>

    <!-- 当前角色信息 -->
    <div v-if="currentCharacter" class="character-info">
      <div class="character-avatar">
        <img :src="currentCharacter.avatarUrl || 'https://picsum.photos/40/40'" :alt="currentCharacter.name">
      </div>
      <div class="character-details">
        <div class="character-name">{{ currentCharacter.name }}</div>
        <div class="character-skills">
          <el-tag 
            v-for="skill in characterSkills" 
            :key="skill" 
            size="small" 
            type="info"
          >
            {{ skill }}
          </el-tag>
        </div>
      </div>
    </div>

    <!-- 技能卡片列表 -->
    <div class="skills-grid">
      <!-- 情感分析技能 -->
      <div class="skill-card emotion-skill" @click="openSkillDialog('emotion')">
        <div class="skill-icon">
          <el-icon><Sunny /></el-icon>
        </div>
        <div class="skill-content">
          <h3>情感分析</h3>
          <p>分析文本情感倾向</p>
          <div class="skill-stats">
            <span>使用次数: {{ skillStats.emotionAnalysis }}</span>
          </div>
        </div>
        <div class="skill-status" :class="{ active: activeSkills.includes('emotion') }">
          <el-icon v-if="activeSkills.includes('emotion')"><CircleCheck /></el-icon>
        </div>
      </div>

      <!-- 知识问答技能 -->
      <div class="skill-card knowledge-skill" @click="openSkillDialog('knowledge')">
        <div class="skill-icon">
          <el-icon><Reading /></el-icon>
        </div>
        <div class="skill-content">
          <h3>知识问答</h3>
          <p>专业知识解答</p>
          <div class="skill-stats">
            <span>使用次数: {{ skillStats.knowledgeQA }}</span>
          </div>
        </div>
        <div class="skill-status" :class="{ active: activeSkills.includes('knowledge') }">
          <el-icon v-if="activeSkills.includes('knowledge')"><CircleCheck /></el-icon>
        </div>
      </div>

      <!-- 创意写作技能 -->
      <div class="skill-card creative-skill" @click="openSkillDialog('creative')">
        <div class="skill-icon">
          <el-icon><EditPen /></el-icon>
        </div>
        <div class="skill-content">
          <h3>创意写作</h3>
          <p>文学创作助手</p>
          <div class="skill-stats">
            <span>使用次数: {{ skillStats.creativeWriting }}</span>
          </div>
        </div>
        <div class="skill-status" :class="{ active: activeSkills.includes('creative') }">
          <el-icon v-if="activeSkills.includes('creative')"><CircleCheck /></el-icon>
        </div>
      </div>

      <!-- 综合技能 -->
      <div class="skill-card comprehensive-skill" @click="openSkillDialog('comprehensive')">
        <div class="skill-icon">
          <el-icon><Connection /></el-icon>
        </div>
        <div class="skill-content">
          <h3>智能对话</h3>
          <p>综合技能处理</p>
          <div class="skill-stats">
            <span>使用次数: {{ skillStats.comprehensive }}</span>
          </div>
        </div>
        <div class="skill-status" :class="{ active: activeSkills.includes('comprehensive') }">
          <el-icon v-if="activeSkills.includes('comprehensive')"><CircleCheck /></el-icon>
        </div>
      </div>
    </div>

    <!-- 技能详情对话框 -->
    <el-dialog 
      v-model="skillDialogVisible" 
      :title="currentSkillInfo.title"
      width="600px"
      :before-close="handleSkillDialogClose"
    >
      <div class="skill-dialog-content">
        <!-- 技能输入区域 -->
        <div class="skill-input-area">
          <div v-if="currentSkillType === 'emotion'" class="emotion-input">
            <el-input
              v-model="skillInputs.emotionText"
              type="textarea"
              :rows="3"
              placeholder="请输入要分析情感的文本..."
              maxlength="500"
              show-word-limit
            />
          </div>

          <div v-else-if="currentSkillType === 'knowledge'" class="knowledge-input">
            <el-input
              v-model="skillInputs.knowledgeQuestion"
              type="textarea"
              :rows="3"
              placeholder="请输入您的问题..."
              maxlength="300"
              show-word-limit
            />
          </div>

          <div v-else-if="currentSkillType === 'creative'" class="creative-input">
            <el-select 
              v-model="skillInputs.writingType" 
              placeholder="选择写作类型"
              style="width: 100%; margin-bottom: 10px;"
            >
              <el-option label="诗歌" value="诗歌" />
              <el-option label="故事" value="故事" />
              <el-option label="对话" value="对话" />
              <el-option label="描述" value="描述" />
              <el-option label="自由创作" value="自由创作" />
            </el-select>
            
            <el-input
              v-model="skillInputs.creativePrompt"
              type="textarea"
              :rows="3"
              placeholder="请输入创作提示或主题..."
              maxlength="200"
              show-word-limit
            />
          </div>

          <div v-else-if="currentSkillType === 'comprehensive'" class="comprehensive-input">
            <el-input
              v-model="skillInputs.comprehensiveInput"
              type="textarea"
              :rows="3"
              placeholder="请输入您想说的话..."
              maxlength="500"
              show-word-limit
            />
          </div>
        </div>

        <!-- 执行按钮 -->
        <div class="skill-actions">
          <el-button 
            type="primary" 
            @click="executeSkill"
            :loading="skillExecuting"
            :disabled="!canExecuteSkill"
          >
            {{ skillExecuting ? '处理中...' : '执行技能' }}
          </el-button>
          
          <el-button @click="clearSkillInputs">
            清空输入
          </el-button>
        </div>

        <!-- 技能结果展示 -->
        <div v-if="skillResult" class="skill-result">
          <div class="result-header">
            <el-icon><SuccessFilled /></el-icon>
            <span>技能执行结果</span>
          </div>

          <!-- 情感分析结果 -->
          <div v-if="currentSkillType === 'emotion' && skillResult.emotionResult" class="emotion-result">
            <div class="emotion-main">
              <el-tag :type="getEmotionTagType(skillResult.emotionResult.emotion)" size="large">
                {{ skillResult.emotionResult.emotion }}
              </el-tag>
              <span class="confidence">置信度: {{ Math.round(skillResult.emotionResult.confidence * 100) }}%</span>
            </div>
            
            <div class="emotion-details">
              <p><strong>强度:</strong> {{ skillResult.emotionResult.emotionIntensity }}</p>
              <p><strong>描述:</strong> {{ skillResult.emotionResult.emotionDescription }}</p>
              <p><strong>建议:</strong> {{ skillResult.emotionResult.suggestedResponse }}</p>
            </div>
          </div>

          <!-- 知识问答结果 -->
          <div v-if="currentSkillType === 'knowledge' && skillResult.knowledgeResult" class="knowledge-result">
            <div class="answer-content">
              <p>{{ skillResult.knowledgeResult.answer }}</p>
            </div>
            
            <div class="answer-meta">
              <div class="meta-item">
                <strong>知识来源:</strong> {{ skillResult.knowledgeResult.knowledgeSource }}
              </div>
              <div class="meta-item">
                <strong>置信度:</strong> {{ Math.round(skillResult.knowledgeResult.confidence * 100) }}%
              </div>
              <div v-if="skillResult.knowledgeResult.relatedTopics?.length" class="meta-item">
                <strong>相关话题:</strong>
                <el-tag 
                  v-for="topic in skillResult.knowledgeResult.relatedTopics" 
                  :key="topic" 
                  size="small" 
                  style="margin-left: 5px;"
                >
                  {{ topic }}
                </el-tag>
              </div>
            </div>
          </div>

          <!-- 创意写作结果 -->
          <div v-if="currentSkillType === 'creative' && skillResult.writingResult" class="creative-result">
            <div class="writing-content">
              <pre>{{ skillResult.writingResult.content }}</pre>
            </div>
            
            <div class="writing-meta">
              <div class="meta-item">
                <strong>写作风格:</strong> {{ skillResult.writingResult.writingStyle }}
              </div>
              <div class="meta-item">
                <strong>主题:</strong> {{ skillResult.writingResult.theme }}
              </div>
              <div v-if="skillResult.writingResult.keywords?.length" class="meta-item">
                <strong>关键词:</strong>
                <el-tag 
                  v-for="keyword in skillResult.writingResult.keywords" 
                  :key="keyword" 
                  size="small" 
                  style="margin-left: 5px;"
                >
                  {{ keyword }}
                </el-tag>
              </div>
              <div class="meta-item">
                <strong>灵感来源:</strong> {{ skillResult.writingResult.inspiration }}
              </div>
            </div>
          </div>

          <!-- 综合技能结果 -->
          <div v-if="currentSkillType === 'comprehensive'" class="comprehensive-result">
            <div class="response-content">
              <p>{{ skillResult.response }}</p>
            </div>
            
            <div class="skill-used">
              <el-tag type="success">使用技能: {{ skillResult.skillUsed }}</el-tag>
            </div>
          </div>
        </div>
      </div>
    </el-dialog>

    <!-- 技能统计对话框 -->
    <el-dialog v-model="statsDialogVisible" title="技能使用统计" width="500px">
      <div class="stats-content">
        <div class="stats-overview">
          <div class="total-usage">
            <h3>总使用次数: {{ skillStats.totalUsage }}</h3>
          </div>
        </div>
        
        <div class="stats-details">
          <div class="stat-item">
            <span>情感分析:</span>
            <el-progress :percentage="getSkillPercentage('emotionAnalysis')" />
            <span>{{ skillStats.emotionAnalysis }} 次</span>
          </div>
          
          <div class="stat-item">
            <span>知识问答:</span>
            <el-progress :percentage="getSkillPercentage('knowledgeQA')" />
            <span>{{ skillStats.knowledgeQA }} 次</span>
          </div>
          
          <div class="stat-item">
            <span>创意写作:</span>
            <el-progress :percentage="getSkillPercentage('creativeWriting')" />
            <span>{{ skillStats.creativeWriting }} 次</span>
          </div>
          
          <div class="stat-item">
            <span>智能对话:</span>
            <el-progress :percentage="getSkillPercentage('comprehensive')" />
            <span>{{ skillStats.comprehensive }} 次</span>
          </div>
        </div>
        
        <div class="stats-actions">
          <el-button type="danger" @click="clearStats" size="small">
            清除统计
          </el-button>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { 
  MagicStick, Sunny, Reading, EditPen, Connection, 
  CircleCheck, SuccessFilled, Refresh, DataAnalysis 
} from '@element-plus/icons-vue'
import aiSkillService, { SKILL_TYPES, EMOTION_TYPES, WRITING_TYPES } from '@/services/aiSkillService.js'

// Props
const props = defineProps({
  currentCharacter: {
    type: Object,
    default: null
  },
  conversationHistory: {
    type: Array,
    default: () => []
  }
})

// Emits
const emit = defineEmits(['skill-executed', 'skill-result'])

// 响应式数据
const loading = ref(false)
const skillDialogVisible = ref(false)
const statsDialogVisible = ref(false)
const skillExecuting = ref(false)
const currentSkillType = ref('')
const skillResult = ref(null)
const activeSkills = ref(['comprehensive'])

// 技能统计
const skillStats = ref({
  emotionAnalysis: 0,
  knowledgeQA: 0,
  creativeWriting: 0,
  comprehensive: 0,
  totalUsage: 0
})

// 技能输入
const skillInputs = ref({
  emotionText: '',
  knowledgeQuestion: '',
  creativePrompt: '',
  writingType: '自由创作',
  comprehensiveInput: ''
})

// 角色技能配置
const characterSkillConfig = ref({})

// 计算属性
const characterSkills = computed(() => {
  if (!characterSkillConfig.value.specialties) return []
  return characterSkillConfig.value.specialties
})

const currentSkillInfo = computed(() => {
  const skillInfoMap = {
    emotion: { title: '情感分析技能', description: '分析文本中的情感倾向和情绪状态' },
    knowledge: { title: '知识问答技能', description: '基于角色专业知识回答问题' },
    creative: { title: '创意写作技能', description: '进行各种类型的文学创作' },
    comprehensive: { title: '智能对话技能', description: '综合运用多种技能进行对话' }
  }
  
  return skillInfoMap[currentSkillType.value] || { title: '技能详情', description: '' }
})

const canExecuteSkill = computed(() => {
  switch (currentSkillType.value) {
    case 'emotion':
      return skillInputs.value.emotionText.trim().length > 0
    case 'knowledge':
      return skillInputs.value.knowledgeQuestion.trim().length > 0
    case 'creative':
      return skillInputs.value.creativePrompt.trim().length > 0
    case 'comprehensive':
      return skillInputs.value.comprehensiveInput.trim().length > 0
    default:
      return false
  }
})

// 方法
const refreshSkills = async () => {
  loading.value = true
  try {
    // 刷新技能统计
    skillStats.value = aiSkillService.getSkillUsageStats()
    
    // 如果有当前角色，刷新角色技能配置
    if (props.currentCharacter) {
      await loadCharacterSkillConfig()
    }
    
    ElMessage.success('技能信息已刷新')
  } catch (error) {
    ElMessage.error('刷新失败: ' + error.message)
  } finally {
    loading.value = false
  }
}

const loadCharacterSkillConfig = async () => {
  if (!props.currentCharacter) return
  
  try {
    const result = await aiSkillService.getCharacterSkillConfig(props.currentCharacter.name)
    if (result.success) {
      characterSkillConfig.value = result.data
    }
  } catch (error) {
    console.error('加载角色技能配置失败:', error)
  }
}

const openSkillDialog = (skillType) => {
  currentSkillType.value = skillType
  skillDialogVisible.value = true
  skillResult.value = null
  
  // 清空之前的输入
  clearSkillInputs()
}

const handleSkillDialogClose = () => {
  skillDialogVisible.value = false
  skillResult.value = null
  skillExecuting.value = false
}

const clearSkillInputs = () => {
  skillInputs.value = {
    emotionText: '',
    knowledgeQuestion: '',
    creativePrompt: '',
    writingType: '自由创作',
    comprehensiveInput: ''
  }
}

const executeSkill = async () => {
  if (!canExecuteSkill.value) {
    ElMessage.warning('请先输入相关内容')
    return
  }
  
  skillExecuting.value = true
  skillResult.value = null
  
  try {
    let result
    const characterName = props.currentCharacter?.name || ''
    
    switch (currentSkillType.value) {
      case 'emotion':
        result = await aiSkillService.analyzeEmotion(
          skillInputs.value.emotionText, 
          characterName
        )
        if (result.success) {
          skillResult.value = { emotionResult: result.data }
        }
        break
        
      case 'knowledge':
        const context = props.conversationHistory.slice(-3).join('\n')
        result = await aiSkillService.answerQuestion(
          skillInputs.value.knowledgeQuestion,
          characterName,
          context
        )
        if (result.success) {
          skillResult.value = { knowledgeResult: result.data }
        }
        break
        
      case 'creative':
        result = await aiSkillService.creativeWriting(
          skillInputs.value.creativePrompt,
          characterName,
          skillInputs.value.writingType
        )
        if (result.success) {
          skillResult.value = { writingResult: result.data }
        }
        break
        
      case 'comprehensive':
        result = await aiSkillService.processWithSkills(
          skillInputs.value.comprehensiveInput,
          characterName,
          props.conversationHistory
        )
        if (result.success) {
          skillResult.value = result.data
        }
        break
    }
    
    if (result && result.success) {
      // 更新技能使用统计
      aiSkillService.updateSkillUsageStats(currentSkillType.value)
      skillStats.value = aiSkillService.getSkillUsageStats()
      
      // 发送事件
      emit('skill-executed', {
        skillType: currentSkillType.value,
        result: skillResult.value
      })
      
      emit('skill-result', skillResult.value)
      
      ElMessage.success('技能执行成功')
    } else {
      ElMessage.error(result?.message || '技能执行失败')
    }
  } catch (error) {
    ElMessage.error('技能执行出错: ' + error.message)
  } finally {
    skillExecuting.value = false
  }
}

const showSkillStats = () => {
  statsDialogVisible.value = true
}

const getSkillPercentage = (skillType) => {
  if (skillStats.value.totalUsage === 0) return 0
  return Math.round((skillStats.value[skillType] / skillStats.value.totalUsage) * 100)
}

const clearStats = () => {
  aiSkillService.clearSkillUsageStats()
  skillStats.value = {
    emotionAnalysis: 0,
    knowledgeQA: 0,
    creativeWriting: 0,
    comprehensive: 0,
    totalUsage: 0
  }
  ElMessage.success('统计数据已清除')
}

const getEmotionTagType = (emotion) => {
  const typeMap = {
    '快乐': 'success',
    '悲伤': 'info',
    '愤怒': 'danger',
    '恐惧': 'warning',
    '惊讶': 'primary',
    '厌恶': 'danger',
    '中性': ''
  }
  return typeMap[emotion] || ''
}

// 监听角色变化
watch(() => props.currentCharacter, (newCharacter) => {
  if (newCharacter) {
    loadCharacterSkillConfig()
  }
}, { immediate: true })

// 生命周期
onMounted(() => {
  skillStats.value = aiSkillService.getSkillUsageStats()
})
</script>

<style scoped>
.ai-skill-panel {
  background: white;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 15px;
  border-bottom: 1px solid #e4e7ed;
}

.header-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 18px;
  font-weight: bold;
  color: #303133;
}

.header-actions {
  display: flex;
  gap: 10px;
}

.character-info {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 15px;
  background: #f8f9fa;
  border-radius: 8px;
  margin-bottom: 20px;
}

.character-avatar img {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  object-fit: cover;
}

.character-details {
  flex: 1;
}

.character-name {
  font-weight: bold;
  color: #303133;
  margin-bottom: 5px;
}

.character-skills {
  display: flex;
  gap: 5px;
  flex-wrap: wrap;
}

.skills-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 15px;
}

.skill-card {
  position: relative;
  padding: 20px;
  border: 2px solid #e4e7ed;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.3s;
  background: white;
}

.skill-card:hover {
  border-color: #409eff;
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.15);
  transform: translateY(-2px);
}

.skill-card.emotion-skill:hover { border-color: #f56c6c; }
.skill-card.knowledge-skill:hover { border-color: #67c23a; }
.skill-card.creative-skill:hover { border-color: #e6a23c; }
.skill-card.comprehensive-skill:hover { border-color: #909399; }

.skill-icon {
  font-size: 32px;
  margin-bottom: 10px;
  color: #409eff;
}

.emotion-skill .skill-icon { color: #f56c6c; }
.knowledge-skill .skill-icon { color: #67c23a; }
.creative-skill .skill-icon { color: #e6a23c; }
.comprehensive-skill .skill-icon { color: #909399; }

.skill-content h3 {
  margin: 0 0 8px 0;
  font-size: 16px;
  color: #303133;
}

.skill-content p {
  margin: 0 0 10px 0;
  color: #606266;
  font-size: 14px;
}

.skill-stats {
  font-size: 12px;
  color: #909399;
}

.skill-status {
  position: absolute;
  top: 10px;
  right: 10px;
  width: 20px;
  height: 20px;
  border-radius: 50%;
  background: #f0f0f0;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
}

.skill-status.active {
  background: #67c23a;
  color: white;
}

.skill-dialog-content {
  padding: 10px 0;
}

.skill-input-area {
  margin-bottom: 20px;
}

.skill-actions {
  display: flex;
  gap: 10px;
  margin-bottom: 20px;
}

.skill-result {
  border-top: 1px solid #e4e7ed;
  padding-top: 20px;
}

.result-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 15px;
  font-weight: bold;
  color: #67c23a;
}

.emotion-result .emotion-main {
  display: flex;
  align-items: center;
  gap: 15px;
  margin-bottom: 15px;
}

.emotion-result .confidence {
  color: #606266;
  font-size: 14px;
}

.emotion-details p {
  margin: 8px 0;
  line-height: 1.5;
}

.knowledge-result .answer-content {
  background: #f8f9fa;
  padding: 15px;
  border-radius: 8px;
  margin-bottom: 15px;
  line-height: 1.6;
}

.answer-meta, .writing-meta {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
}

.creative-result .writing-content {
  background: #f8f9fa;
  padding: 15px;
  border-radius: 8px;
  margin-bottom: 15px;
}

.creative-result pre {
  white-space: pre-wrap;
  word-wrap: break-word;
  font-family: inherit;
  line-height: 1.6;
  margin: 0;
}

.comprehensive-result .response-content {
  background: #f8f9fa;
  padding: 15px;
  border-radius: 8px;
  margin-bottom: 15px;
  line-height: 1.6;
}

.skill-used {
  text-align: center;
}

.stats-content {
  padding: 10px 0;
}

.stats-overview {
  text-align: center;
  margin-bottom: 20px;
}

.total-usage h3 {
  color: #409eff;
  margin: 0;
}

.stats-details {
  display: flex;
  flex-direction: column;
  gap: 15px;
  margin-bottom: 20px;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 10px;
}

.stat-item span:first-child {
  min-width: 80px;
  font-weight: bold;
}

.stat-item .el-progress {
  flex: 1;
}

.stat-item span:last-child {
  min-width: 50px;
  text-align: right;
  font-size: 12px;
  color: #606266;
}

.stats-actions {
  text-align: center;
}

@media (max-width: 768px) {
  .skills-grid {
    grid-template-columns: 1fr;
  }
  
  .panel-header {
    flex-direction: column;
    gap: 10px;
    align-items: stretch;
  }
  
  .header-actions {
    justify-content: center;
  }
  
  .character-info {
    flex-direction: column;
    text-align: center;
  }
  
  .skill-actions {
    flex-direction: column;
  }
  
  .emotion-result .emotion-main {
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
  }
  
  .stat-item {
    flex-direction: column;
    align-items: stretch;
    gap: 5px;
  }
  
  .stat-item span:first-child,
  .stat-item span:last-child {
    text-align: center;
  }
}
</style>