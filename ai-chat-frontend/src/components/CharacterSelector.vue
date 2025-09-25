<template>
  <div class="character-selector" :class="{ 'open': isOpen }">
    <!-- 角色选择器头部 -->
    <div class="selector-header">
      <h3 class="selector-title">
        <svg class="title-icon" viewBox="0 0 24 24">
          <path d="M12,4A4,4 0 0,1 16,8A4,4 0 0,1 12,12A4,4 0 0,1 8,8A4,4 0 0,1 12,4M12,14C16.42,14 20,15.79 20,18V20H4V18C4,15.79 7.58,14 12,14Z"/>
        </svg>
        AI角色选择
      </h3>
      <button class="close-btn" @click="closeSelector">
        <svg viewBox="0 0 24 24">
          <path d="M19,6.41L17.59,5L12,10.59L6.41,5L5,6.41L10.59,12L5,17.59L6.41,19L12,13.41L17.59,19L19,17.59L13.41,12L19,6.41Z"/>
        </svg>
      </button>
    </div>

    <!-- 搜索和筛选 -->
    <div class="selector-controls">
      <div class="search-container">
        <svg class="search-icon" viewBox="0 0 24 24">
          <path d="M9.5,3A6.5,6.5 0 0,1 16,9.5C16,11.11 15.41,12.59 14.44,13.73L14.71,14H15.5L20.5,19L19,20.5L14,15.5V14.71L13.73,14.44C12.59,15.41 11.11,16 9.5,16A6.5,6.5 0 0,1 3,9.5A6.5,6.5 0 0,1 9.5,3M9.5,5C7,5 5,7 5,9.5C5,12 7,14 9.5,14C12,14 14,12 14,9.5C14,7 12,5 9.5,5Z"/>
        </svg>
        <input 
          type="text" 
          v-model="searchQuery" 
          placeholder="搜索角色名称、技能或描述..."
          class="search-input"
          @input="handleSearch"
        />
        <button v-if="searchQuery" @click="clearSearch" class="clear-search">
          <svg viewBox="0 0 24 24">
            <path d="M19,6.41L17.59,5L12,10.59L6.41,5L5,6.41L10.59,12L5,17.59L6.41,19L12,13.41L17.59,19L19,17.59L13.41,12L19,6.41Z"/>
          </svg>
        </button>
      </div>
      
      <div class="filter-tabs">
        <button 
          v-for="category in categories" 
          :key="category.id"
          :class="['filter-tab', { 'active': activeCategory === category.id }]"
          @click="setActiveCategory(category.id)"
        >
          <span class="tab-emoji">{{ category.emoji }}</span>
          {{ category.name }}
          <span class="tab-count">({{ getCategoryCount(category.id) }})</span>
        </button>
      </div>

      <!-- 排序选项 -->
      <div class="sort-options">
        <label class="sort-label">排序:</label>
        <select v-model="sortBy" @change="handleSort" class="sort-select">
          <option value="popularity">人气</option>
          <option value="rating">评分</option>
          <option value="name">名称</option>
          <option value="recent">最近使用</option>
        </select>
      </div>
    </div>

    <!-- 角色列表 -->
    <div class="character-list">
      <div class="current-character" v-if="currentCharacter">
        <h4 class="section-title">当前角色</h4>
        <div class="character-card current" @click="selectCharacter(currentCharacter)">
          <div class="character-avatar">
            <img :src="currentCharacter.avatar" :alt="currentCharacter.name" />
            <div class="active-indicator">
              <svg viewBox="0 0 24 24">
                <path d="M12,2A10,10 0 0,1 22,12A10,10 0 0,1 12,22A10,10 0 0,1 2,12A10,10 0 0,1 12,2M11,16.5L18,9.5L16.59,8.09L11,13.67L7.91,10.59L6.5,12L11,16.5Z"/>
              </svg>
            </div>
          </div>
          <div class="character-info">
            <h5 class="character-name">{{ currentCharacter.name }}</h5>
            <p class="character-description">{{ currentCharacter.description }}</p>
            <div class="character-tags">
              <span v-for="tag in currentCharacter.tags" :key="tag" class="tag">
                {{ tag }}
              </span>
            </div>
          </div>
        </div>
      </div>

      <div class="available-characters">
        <div class="section-header">
          <h4 class="section-title">可选角色</h4>
          <button class="create-btn" @click="showCreateDialog = true">
            <svg viewBox="0 0 24 24">
              <path d="M19,13H13V19H11V13H5V11H11V5H13V11H19V13Z"/>
            </svg>
            创建角色
          </button>
        </div>
        
        <div class="characters-grid">
          <div 
            v-for="character in filteredCharacters" 
            :key="character.id"
            :class="['character-card', { 'selected': character.id === selectedCharacterId }]"
            @click="selectCharacter(character)"
          >
            <div class="character-avatar">
              <img :src="character.avatar" :alt="character.name" />
              <div v-if="character.isPremium" class="premium-badge">
                <svg viewBox="0 0 24 24">
                  <path d="M5,16L3,5L8.5,12L12,4L15.5,12L21,5L19,16H5M12,18A2,2 0 0,1 14,20A2,2 0 0,1 12,22A2,2 0 0,1 10,20A2,2 0 0,1 12,18Z"/>
                </svg>
              </div>
            </div>
            <div class="character-info">
              <h5 class="character-name">{{ character.name }}</h5>
              <p class="character-description">{{ character.description }}</p>
              <div class="character-stats">
                <div class="stat">
                  <svg class="stat-icon" viewBox="0 0 24 24">
                    <path d="M12,17.27L18.18,21L16.54,13.97L22,9.24L14.81,8.62L12,2L9.19,8.62L2,9.24L7.46,13.97L5.82,21L12,17.27Z"/>
                  </svg>
                  <span>{{ character.rating }}</span>
                </div>
                <div class="stat">
                  <svg class="stat-icon" viewBox="0 0 24 24">
                    <path d="M16,6L18.29,8.29L13.41,13.17L9.41,9.17L2,16.59L3.41,18L9.41,12L13.41,16L19.71,9.71L22,12V6H16Z"/>
                  </svg>
                  <span>{{ character.usage }}k</span>
                </div>
              </div>
              <div class="character-tags">
                <span v-for="tag in character.tags.slice(0, 3)" :key="tag" class="tag">
                  {{ tag }}
                </span>
                <span v-if="character.tags.length > 3" class="tag more">
                  +{{ character.tags.length - 3 }}
                </span>
              </div>
            </div>
            <div class="character-actions">
              <button class="action-btn preview" @click.stop="previewCharacter(character)">
                <svg viewBox="0 0 24 24">
                  <path d="M12,9A3,3 0 0,0 9,12A3,3 0 0,0 12,15A3,3 0 0,0 15,12A3,3 0 0,0 12,9M12,17A5,5 0 0,1 7,12A5,5 0 0,1 12,7A5,5 0 0,1 17,12A5,5 0 0,1 12,17M12,4.5C7,4.5 2.73,7.61 1,12C2.73,16.39 7,19.5 12,19.5C17,19.5 21.27,16.39 23,12C21.27,7.61 17,4.5 12,4.5Z"/>
                </svg>
              </button>
              <button class="action-btn favorite" @click.stop="toggleFavorite(character)">
                <svg viewBox="0 0 24 24">
                  <path :d="character.isFavorite ? 'M12,21.35L10.55,20.03C5.4,15.36 2,12.27 2,8.5C2,5.41 4.42,3 7.5,3C9.24,3 10.91,3.81 12,5.08C13.09,3.81 14.76,3 16.5,3C19.58,3 22,5.41 22,8.5C22,12.27 18.6,15.36 13.45,20.03L12,21.35Z' : 'M12.1,18.55L12,18.65L11.89,18.55C7.14,14.24 4,11.39 4,8.5C4,6.5 5.5,5 7.5,5C9.04,5 10.54,6 11.07,7.36H12.93C13.46,6 14.96,5 16.5,5C18.5,5 20,6.5 20,8.5C20,11.39 16.86,14.24 12.1,18.55M16.5,3C14.76,3 13.09,3.81 12,5.08C10.91,3.81 9.24,3 7.5,3C4.42,3 2,5.41 2,8.5C2,12.27 5.4,15.36 10.55,20.03L12,21.35L13.45,20.03C18.6,15.36 22,12.27 22,8.5C22,5.41 19.58,3 16.5,3Z'"/>
                </svg>
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 角色预览模态框 -->
    <div v-if="previewCharacterData" class="preview-modal" @click="closePreview">
      <div class="preview-content" @click.stop>
        <div class="preview-header">
          <div class="preview-avatar">
            <img :src="previewCharacterData.avatar" :alt="previewCharacterData.name" />
          </div>
          <div class="preview-info">
            <h3>{{ previewCharacterData.name }}</h3>
            <p>{{ previewCharacterData.description }}</p>
            <div class="preview-stats">
              <div class="stat">
                <span class="label">评分:</span>
                <span class="value">{{ previewCharacterData.rating }}/5</span>
              </div>
              <div class="stat">
                <span class="label">使用量:</span>
                <span class="value">{{ previewCharacterData.usage }}k</span>
              </div>
              <div class="stat">
                <span class="label">类型:</span>
                <span class="value">{{ previewCharacterData.category }}</span>
              </div>
            </div>
          </div>
          <button class="preview-close" @click="closePreview">
            <svg viewBox="0 0 24 24">
              <path d="M19,6.41L17.59,5L12,10.59L6.41,5L5,6.41L10.59,12L5,17.59L6.41,19L12,13.41L17.59,19L19,17.59L13.41,12L19,6.41Z"/>
            </svg>
          </button>
        </div>
        
        <div class="preview-body">
          <div class="preview-section">
            <h4>角色特点</h4>
            <div class="character-traits">
              <div v-for="trait in previewCharacterData.traits" :key="trait.name" class="trait">
                <div class="trait-name">{{ trait.name }}</div>
                <div class="trait-bar">
                  <div class="trait-fill" :style="{ width: trait.value + '%' }"></div>
                </div>
                <div class="trait-value">{{ trait.value }}%</div>
              </div>
            </div>
          </div>
          
          <div class="preview-section">
            <h4>示例对话</h4>
            <div class="example-chat">
              <div v-for="message in previewCharacterData.examples" :key="message.id" class="example-message">
                <div class="message-avatar">
                  <img v-if="message.role === 'assistant'" :src="previewCharacterData.avatar" :alt="previewCharacterData.name" />
                  <div v-else class="user-avatar">U</div>
                </div>
                <div class="message-content">{{ message.content }}</div>
              </div>
            </div>
          </div>
          
          <div class="preview-actions">
            <button class="action-btn secondary" @click="closePreview">取消</button>
            <button class="action-btn primary" @click="selectCharacter(previewCharacterData!)">选择角色</button>
          </div>
        </div>
      </div>
    </div>

    <!-- 创建角色模态框 -->
    <div v-if="showCreateDialog" class="create-modal" @click="closeCreateDialog">
      <div class="create-content" @click.stop>
        <div class="create-header">
          <h3>创建自定义角色</h3>
          <button class="create-close" @click="closeCreateDialog">
            <svg viewBox="0 0 24 24">
              <path d="M19,6.41L17.59,5L12,10.59L6.41,5L5,6.41L10.59,12L5,17.59L6.41,19L12,13.41L17.59,19L19,17.59L13.41,12L19,6.41Z"/>
            </svg>
          </button>
        </div>
        
        <div class="create-body">
          <div class="form-group">
            <label>角色名称</label>
            <input type="text" v-model="newCharacter.name" placeholder="输入角色名称" />
          </div>
          
          <div class="form-group">
            <label>角色描述</label>
            <textarea v-model="newCharacter.description" placeholder="描述角色的特点和能力"></textarea>
          </div>
          
          <div class="form-group">
            <label>角色头像</label>
            <div class="avatar-selector">
              <div 
                v-for="avatar in avatarOptions" 
                :key="avatar"
                :class="['avatar-option', { 'selected': newCharacter.avatar === avatar }]"
                @click="newCharacter.avatar = avatar"
              >
                <img :src="avatar" :alt="'头像选项'" />
              </div>
            </div>
          </div>
          
          <div class="form-group">
            <label>角色标签</label>
            <div class="tag-input">
              <input 
                type="text" 
                v-model="tagInput" 
                placeholder="输入标签后按回车添加"
                @keyup.enter="addTag"
              />
              <div class="selected-tags">
                <span v-for="tag in newCharacter.tags" :key="tag" class="tag">
                  {{ tag }}
                  <button @click="removeTag(tag)">×</button>
                </span>
              </div>
            </div>
          </div>
          
          <div class="form-group">
            <label>系统提示词</label>
            <textarea 
              v-model="newCharacter.systemPrompt" 
              placeholder="定义角色的行为和回复风格"
              rows="4"
            ></textarea>
          </div>
        </div>
        
        <div class="create-actions">
          <button class="action-btn secondary" @click="closeCreateDialog">取消</button>
          <button class="action-btn primary" @click="createCharacter" :disabled="!canCreateCharacter">创建角色</button>
        </div>
      </div>
    </div>
  </div>

  <!-- 遮罩层 -->
  <div v-if="isOpen" class="selector-overlay" @click="closeSelector"></div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'

// 接口定义
interface Character {
  id: string
  name: string
  description: string
  avatar: string
  category: string
  tags: string[]
  rating: number
  usage: number
  isPremium?: boolean
  isFavorite?: boolean
  traits?: Array<{ name: string; value: number }>
  examples?: Array<{ id: string; role: 'user' | 'assistant'; content: string }>
  systemPrompt?: string
}

interface Category {
  id: string
  name: string
  icon: string
  emoji: string
}

// Props
interface Props {
  isOpen: boolean
  currentCharacter?: Character
}

const props = defineProps<Props>()

// Emits
const emit = defineEmits<{
  close: []
  selectCharacter: [character: Character]
}>()

// 响应式数据
const searchQuery = ref('')
const activeCategory = ref('all')
const selectedCharacterId = ref('')
const previewCharacterData = ref<Character | null>(null)
const showCreateDialog = ref(false)
const tagInput = ref('')
const sortBy = ref('rating')

// 新角色数据
const newCharacter = ref<Partial<Character>>({
  name: '',
  description: '',
  avatar: '',
  tags: [],
  systemPrompt: ''
})

// 分类数据
const categories: Category[] = [
  {
    id: 'all',
    name: '全部',
    icon: 'M12,2A10,10 0 0,1 22,12A10,10 0 0,1 12,22A10,10 0 0,1 2,12A10,10 0 0,1 12,2Z',
    emoji: '🌟'
  },
  {
    id: 'assistant',
    name: '助手',
    icon: 'M12,2A2,2 0 0,1 14,4C14,4.74 13.6,5.39 13,5.73V7H14A7,7 0 0,1 21,14H22A1,1 0 0,1 23,15V18A1,1 0 0,1 22,19H21V20A2,2 0 0,1 19,22H5A2,2 0 0,1 3,20V19H2A1,1 0 0,1 1,18V15A1,1 0 0,1 2,14H3A7,7 0 0,1 10,7H11V5.73C10.4,5.39 10,4.74 10,4A2,2 0 0,1 12,2Z',
    emoji: '🤖'
  },
  {
    id: 'creative',
    name: '创意',
    icon: 'M12,2A7,7 0 0,1 19,9C19,11.38 17.81,13.47 16,14.74V17A1,1 0 0,1 15,18H9A1,1 0 0,1 8,17V14.74C6.19,13.47 5,11.38 5,9A7,7 0 0,1 12,2M9,21V20H15V21A1,1 0 0,1 14,22H10A1,1 0 0,1 9,21Z',
    emoji: '🎨'
  },
  {
    id: 'professional',
    name: '专业',
    icon: 'M12,3L1,9L12,15L21,10.09V17H23V9M5,13.18V17.18L12,21L19,17.18V13.18L12,17L5,13.18Z',
    emoji: '💼'
  },
  {
    id: 'entertainment',
    name: '娱乐',
    icon: 'M12,2A10,10 0 0,0 2,12A10,10 0 0,0 12,22A10,10 0 0,0 22,12A10,10 0 0,0 12,2M12,4A8,8 0 0,1 20,12A8,8 0 0,1 12,20A8,8 0 0,1 4,12A8,8 0 0,1 12,4M12,6A6,6 0 0,0 6,12A6,6 0 0,0 12,18A6,6 0 0,0 18,12A6,6 0 0,0 12,6Z',
    emoji: '🎮'
  },
  {
    id: 'custom',
    name: '自定义',
    icon: 'M12,15.5A3.5,3.5 0 0,1 8.5,12A3.5,3.5 0 0,1 12,8.5A3.5,3.5 0 0,1 15.5,12A3.5,3.5 0 0,1 12,15.5M19.43,12.97C19.47,12.65 19.5,12.33 19.5,12C19.5,11.67 19.47,11.34 19.43,11L21.54,9.37C21.73,9.22 21.78,8.95 21.66,8.73L19.66,5.27C19.54,5.05 19.27,4.96 19.05,5.05L16.56,6.05C16.04,5.66 15.5,5.32 14.87,5.07L14.5,2.42C14.46,2.18 14.25,2 14,2H10C9.75,2 9.54,2.18 9.5,2.42L9.13,5.07C8.5,5.32 7.96,5.66 7.44,6.05L4.95,5.05C4.73,4.96 4.46,5.05 4.34,5.27L2.34,8.73C2.22,8.95 2.27,9.22 2.46,9.37L4.57,11C4.53,11.34 4.5,11.67 4.5,12C4.5,12.33 4.53,12.65 4.57,12.97L2.46,14.63C2.27,14.78 2.22,15.05 2.34,15.27L4.34,18.73C4.46,18.95 4.73,19.03 4.95,18.95L7.44,17.94C7.96,18.34 8.5,18.68 9.13,18.93L9.5,21.58C9.54,21.82 9.75,22 10,22H14C14.25,22 14.46,21.82 14.5,21.58L14.87,18.93C15.5,18.68 16.04,18.34 16.56,17.94L19.05,18.95C19.27,19.03 19.54,18.95 19.66,18.73L21.66,15.27C21.78,15.05 21.73,14.78 21.54,14.63L19.43,12.97Z',
    emoji: '⚙️'
  }
]

// 预设角色数据
const characters = ref<Character[]>([
  {
    id: '1',
    name: '智能助手',
    description: '专业的AI助手，能够帮助您解决各种问题',
    avatar: 'https://picsum.photos/64/64?random=1',
    category: 'assistant',
    tags: ['助手', '专业', '全能'],
    rating: 4.8,
    usage: 125,
    traits: [
      { name: '专业性', value: 95 },
      { name: '友好度', value: 88 },
      { name: '创造力', value: 75 },
      { name: '逻辑性', value: 92 }
    ],
    examples: [
      { id: '1', role: 'user', content: '你好，能帮我分析一下这个问题吗？' },
      { id: '2', role: 'assistant', content: '当然可以！我很乐意帮助您分析问题。请详细描述一下您遇到的具体情况，我会为您提供专业的建议和解决方案。' }
    ]
  },
  {
    id: '2',
    name: '创意作家',
    description: '富有想象力的创意写作专家，擅长故事创作',
    avatar: 'https://picsum.photos/64/64?random=2',
    category: 'creative',
    tags: ['创意', '写作', '故事'],
    rating: 4.6,
    usage: 89,
    isPremium: true,
    traits: [
      { name: '创造力', value: 98 },
      { name: '文学性', value: 94 },
      { name: '想象力', value: 96 },
      { name: '表达力', value: 91 }
    ],
    examples: [
      { id: '1', role: 'user', content: '帮我写一个科幻故事的开头' },
      { id: '2', role: 'assistant', content: '在2157年的新东京，霓虹灯的光芒穿透了永恒的雾霾。艾莉亚站在摩天大楼的顶端，看着下方川流不息的飞行汽车，她知道今晚的任务将改变一切...' }
    ]
  },
  {
    id: '3',
    name: '编程导师',
    description: '经验丰富的编程专家，提供代码指导和技术支持',
    avatar: 'https://picsum.photos/64/64?random=3',
    category: 'professional',
    tags: ['编程', '技术', '教学'],
    rating: 4.9,
    usage: 156,
    traits: [
      { name: '技术能力', value: 97 },
      { name: '教学能力', value: 89 },
      { name: '耐心度', value: 93 },
      { name: '实用性', value: 95 }
    ],
    examples: [
      { id: '1', role: 'user', content: '如何优化这段JavaScript代码？' },
      { id: '2', role: 'assistant', content: '让我来看看您的代码。我会从性能、可读性和最佳实践三个方面为您提供优化建议，并解释每个改进的原因。' }
    ]
  },
  {
    id: '4',
    name: '心理咨询师',
    description: '温暖贴心的心理健康顾问，提供情感支持',
    avatar: 'https://picsum.photos/64/64?random=4',
    category: 'professional',
    tags: ['心理', '咨询', '情感'],
    rating: 4.7,
    usage: 78,
    traits: [
      { name: '同理心', value: 96 },
      { name: '专业性', value: 88 },
      { name: '温暖度', value: 94 },
      { name: '耐心度', value: 97 }
    ],
    examples: [
      { id: '1', role: 'user', content: '最近感觉压力很大，不知道该怎么办' },
      { id: '2', role: 'assistant', content: '我理解您现在的感受，压力确实会让人感到困扰。让我们一起来分析一下压力的来源，并找到一些有效的应对方法。您愿意和我分享一下具体是什么让您感到有压力吗？' }
    ]
  },
  {
    id: '5',
    name: '游戏伙伴',
    description: '有趣的游戏伙伴，陪您聊天游戏放松心情',
    avatar: 'https://picsum.photos/64/64?random=5',
    category: 'entertainment',
    tags: ['游戏', '娱乐', '轻松'],
    rating: 4.4,
    usage: 203,
    traits: [
      { name: '趣味性', value: 95 },
      { name: '活跃度', value: 92 },
      { name: '幽默感', value: 88 },
      { name: '互动性', value: 94 }
    ],
    examples: [
      { id: '1', role: 'user', content: '我们来玩个文字游戏吧' },
      { id: '2', role: 'assistant', content: '太好了！我最喜欢文字游戏了！🎮 我们可以玩成语接龙、猜谜语、或者创意故事接力。您想玩哪一个呢？' }
    ]
  }
])

// 头像选项
const avatarOptions = [
  'https://picsum.photos/64/64?random=10',
  'https://picsum.photos/64/64?random=11',
  'https://picsum.photos/64/64?random=12',
  'https://picsum.photos/64/64?random=13',
  'https://picsum.photos/64/64?random=14',
  'https://picsum.photos/64/64?random=15',
  'https://picsum.photos/64/64?random=16',
  'https://picsum.photos/64/64?random=17'
]

// 计算属性
const filteredCharacters = computed(() => {
  let filtered = characters.value

  // 按分类筛选
  if (activeCategory.value !== 'all') {
    filtered = filtered.filter(char => char.category === activeCategory.value)
  }

  // 按搜索关键词筛选
  if (searchQuery.value.trim()) {
    const query = searchQuery.value.toLowerCase()
    filtered = filtered.filter(char => 
      char.name.toLowerCase().includes(query) ||
      char.description.toLowerCase().includes(query) ||
      char.tags.some(tag => tag.toLowerCase().includes(query))
    )
  }

  return filtered
})

const canCreateCharacter = computed(() => {
  return newCharacter.value.name && 
         newCharacter.value.description && 
         newCharacter.value.avatar &&
         newCharacter.value.tags && 
         newCharacter.value.tags.length > 0
})

// 方法
const closeSelector = () => {
  emit('close')
}

const setActiveCategory = (categoryId: string) => {
  activeCategory.value = categoryId
}

const selectCharacter = (character: Character) => {
  selectedCharacterId.value = character.id
  emit('selectCharacter', character)
  closePreview()
  closeSelector()
}

const previewCharacter = (character: Character) => {
  previewCharacterData.value = character
}

const closePreview = () => {
  previewCharacterData.value = null
}

const toggleFavorite = (character: Character) => {
  character.isFavorite = !character.isFavorite
  // 这里可以添加保存到后端的逻辑
}

const closeCreateDialog = () => {
  showCreateDialog.value = false
  resetNewCharacter()
}

const resetNewCharacter = () => {
  newCharacter.value = {
    name: '',
    description: '',
    avatar: '',
    tags: [],
    systemPrompt: ''
  }
  tagInput.value = ''
}

const addTag = () => {
  const tag = tagInput.value.trim()
  if (tag && !newCharacter.value.tags?.includes(tag)) {
    if (!newCharacter.value.tags) {
      newCharacter.value.tags = []
    }
    newCharacter.value.tags.push(tag)
    tagInput.value = ''
  }
}

const removeTag = (tag: string) => {
  if (newCharacter.value.tags) {
    const index = newCharacter.value.tags.indexOf(tag)
    if (index > -1) {
      newCharacter.value.tags.splice(index, 1)
    }
  }
}

const createCharacter = async () => {
  if (!canCreateCharacter.value) return

  try {
    const character: Character = {
      id: Date.now().toString(),
      name: newCharacter.value.name!,
      description: newCharacter.value.description!,
      avatar: newCharacter.value.avatar!,
      category: 'custom',
      tags: newCharacter.value.tags!,
      rating: 0,
      usage: 0,
      systemPrompt: newCharacter.value.systemPrompt
    }

    characters.value.push(character)
    
    // 这里可以添加保存到后端的逻辑
    
    closeCreateDialog()
    
    // 显示成功提示
    showNotification('角色创建成功！', 'success')
  } catch (error) {
    showNotification('创建失败，请重试', 'error')
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

// 获取分类角色数量
const getCategoryCount = (categoryId: string) => {
  if (categoryId === 'all') {
    return filteredCharacters.value.length
  }
  return filteredCharacters.value.filter(char => char.category === categoryId).length
}

// 处理排序
const handleSort = () => {
  // 排序逻辑已在computed中实现
}

// 处理搜索
const handleSearch = () => {
  // 搜索逻辑已在computed中实现
}

// 清除搜索
const clearSearch = () => {
  searchQuery.value = ''
}

// 生命周期
onMounted(() => {
  // 设置默认头像
  if (!newCharacter.value.avatar) {
    newCharacter.value.avatar = avatarOptions[0]
  }
  
  // 加载用户收藏的角色
  loadFavorites()
})

const loadFavorites = () => {
  try {
    const favorites = localStorage.getItem('favorite-characters')
    if (favorites) {
      const favoriteIds = JSON.parse(favorites)
      characters.value.forEach(char => {
        char.isFavorite = favoriteIds.includes(char.id)
      })
    }
  } catch (error) {
    console.error('加载收藏失败:', error)
  }
}
</script>

<style scoped>
.character-selector {
  position: fixed;
  top: 0;
  left: -500px;
  width: 500px;
  height: 100vh;
  background: var(--background-light);
  backdrop-filter: var(--blur-effect);
  border-right: 1px solid var(--border-light);
  z-index: 1000;
  transition: left 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.character-selector.open {
  left: 0;
}

.selector-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100vw;
  height: 100vh;
  background: rgba(0, 0, 0, 0.5);
  z-index: 999;
  animation: fadeIn 0.3s ease;
}

/* 头部样式 */
.selector-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 1.5rem;
  border-bottom: 1px solid var(--border-light);
  background: var(--background-light);
}

.selector-title {
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

/* 控制区域 */
.selector-controls {
  padding: 1rem;
  border-bottom: 1px solid var(--border-light);
}

.search-container {
  position: relative;
  margin-bottom: 1rem;
}

.search-icon {
  position: absolute;
  left: 12px;
  top: 50%;
  transform: translateY(-50%);
  width: 16px;
  height: 16px;
  fill: var(--text-secondary);
}

.search-input {
  width: 100%;
  padding: 0.75rem 0.75rem 0.75rem 2.5rem;
  border: 1px solid var(--border-light);
  border-radius: 8px;
  background: var(--background-light);
  color: var(--text-primary);
  font-size: 0.9rem;
  transition: border-color 0.2s ease;
}

.search-input:focus {
  outline: none;
  border-color: var(--primary-color);
}

.filter-tabs {
  display: flex;
  gap: 0.5rem;
  overflow-x: auto;
  padding-bottom: 0.5rem;
}

.filter-tab {
  display: flex;
  align-items: center;
  gap: 0.25rem;
  padding: 0.5rem 0.75rem;
  border: 1px solid var(--border-light);
  border-radius: 20px;
  background: transparent;
  color: var(--text-secondary);
  font-size: 0.8rem;
  cursor: pointer;
  transition: all 0.2s ease;
  white-space: nowrap;
}

.filter-tab:hover {
  border-color: var(--border-dark);
}

.filter-tab.active {
  border-color: var(--primary-color);
  background: var(--primary-gradient);
  color: white;
}

.tab-icon {
  width: 14px;
  height: 14px;
  fill: currentColor;
}

/* 角色列表 */
.character-list {
  flex: 1;
  overflow-y: auto;
  padding: 1rem;
}

.section-title {
  margin: 0 0 1rem 0;
  font-size: 1rem;
  font-weight: 600;
  color: var(--text-primary);
}

.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 1rem;
}

.create-btn {
  display: flex;
  align-items: center;
  gap: 0.25rem;
  padding: 0.5rem 0.75rem;
  border: 1px solid var(--primary-color);
  border-radius: 6px;
  background: transparent;
  color: var(--primary-color);
  font-size: 0.8rem;
  cursor: pointer;
  transition: all 0.2s ease;
}

.create-btn:hover {
  background: var(--primary-gradient);
  color: white;
}

.create-btn svg {
  width: 14px;
  height: 14px;
  fill: currentColor;
}

.current-character {
  margin-bottom: 2rem;
}

.characters-grid {
  display: grid;
  gap: 1rem;
}

/* 角色卡片 */
.character-card {
  display: flex;
  gap: 1rem;
  padding: 1rem;
  border: 1px solid var(--border-light);
  border-radius: 12px;
  background: var(--background-light);
  cursor: pointer;
  transition: all 0.2s ease;
  position: relative;
}

.character-card:hover {
  border-color: var(--border-dark);
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.character-card.selected {
  border-color: var(--primary-color);
  background: var(--primary-light);
}

.character-card.current {
  border-color: var(--success-color);
  background: var(--success-light);
}

.character-avatar {
  position: relative;
  flex-shrink: 0;
}

.character-avatar img {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  object-fit: cover;
}

.active-indicator {
  position: absolute;
  bottom: -2px;
  right: -2px;
  width: 16px;
  height: 16px;
  background: var(--success-color);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.active-indicator svg {
  width: 10px;
  height: 10px;
  fill: white;
}

.premium-badge {
  position: absolute;
  top: -4px;
  right: -4px;
  width: 16px;
  height: 16px;
  background: var(--warning-color);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.premium-badge svg {
  width: 10px;
  height: 10px;
  fill: white;
}

.character-info {
  flex: 1;
  min-width: 0;
}

.character-name {
  margin: 0 0 0.25rem 0;
  font-size: 0.95rem;
  font-weight: 600;
  color: var(--text-primary);
}

.character-description {
  margin: 0 0 0.5rem 0;
  font-size: 0.8rem;
  color: var(--text-secondary);
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.character-stats {
  display: flex;
  gap: 1rem;
  margin-bottom: 0.5rem;
}

.stat {
  display: flex;
  align-items: center;
  gap: 0.25rem;
  font-size: 0.75rem;
  color: var(--text-secondary);
}

.stat-icon {
  width: 12px;
  height: 12px;
  fill: currentColor;
}

.character-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 0.25rem;
}

.tag {
  padding: 0.125rem 0.375rem;
  background: var(--border-light);
  color: var(--text-secondary);
  font-size: 0.7rem;
  border-radius: 10px;
}

.tag.more {
  background: var(--primary-light);
  color: var(--primary-color);
}

.character-actions {
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
}

.action-btn {
  width: 28px;
  height: 28px;
  border: none;
  border-radius: 6px;
  background: var(--border-light);
  color: var(--text-secondary);
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s ease;
}

.action-btn:hover {
  background: var(--border-dark);
}

.action-btn.preview:hover {
  background: var(--info-color);
  color: white;
}

.action-btn.favorite:hover {
  background: var(--error-color);
  color: white;
}

.action-btn svg {
  width: 14px;
  height: 14px;
  fill: currentColor;
}

/* 预览模态框 */
.preview-modal,
.create-modal {
  position: fixed;
  top: 0;
  left: 0;
  width: 100vw;
  height: 100vh;
  background: rgba(0, 0, 0, 0.7);
  z-index: 2000;
  display: flex;
  align-items: center;
  justify-content: center;
  animation: fadeIn 0.3s ease;
}

.preview-content,
.create-content {
  width: 90%;
  max-width: 600px;
  max-height: 90vh;
  background: var(--background-light);
  border-radius: 16px;
  overflow: hidden;
  animation: slideUp 0.3s ease;
}

.preview-header,
.create-header {
  display: flex;
  align-items: flex-start;
  gap: 1rem;
  padding: 1.5rem;
  border-bottom: 1px solid var(--border-light);
}

.preview-avatar img {
  width: 64px;
  height: 64px;
  border-radius: 50%;
  object-fit: cover;
}

.preview-info {
  flex: 1;
}

.preview-info h3,
.create-header h3 {
  margin: 0 0 0.5rem 0;
  font-size: 1.2rem;
  font-weight: 600;
  color: var(--text-primary);
}

.preview-info p {
  margin: 0 0 1rem 0;
  color: var(--text-secondary);
  line-height: 1.5;
}

.preview-stats {
  display: flex;
  gap: 1rem;
}

.preview-stats .stat {
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
}

.preview-stats .label {
  font-size: 0.75rem;
  color: var(--text-secondary);
}

.preview-stats .value {
  font-size: 0.9rem;
  font-weight: 600;
  color: var(--text-primary);
}

.preview-close,
.create-close {
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

.preview-close:hover,
.create-close:hover {
  background: var(--border-dark);
}

.preview-close svg,
.create-close svg {
  width: 16px;
  height: 16px;
  fill: currentColor;
}

.preview-body,
.create-body {
  padding: 1.5rem;
  max-height: 60vh;
  overflow-y: auto;
}

.preview-section {
  margin-bottom: 2rem;
}

.preview-section h4 {
  margin: 0 0 1rem 0;
  font-size: 1rem;
  font-weight: 600;
  color: var(--text-primary);
}

/* 角色特点 */
.character-traits {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}

.trait {
  display: flex;
  align-items: center;
  gap: 1rem;
}

.trait-name {
  min-width: 80px;
  font-size: 0.85rem;
  color: var(--text-secondary);
}

.trait-bar {
  flex: 1;
  height: 6px;
  background: var(--border-light);
  border-radius: 3px;
  overflow: hidden;
}

.trait-fill {
  height: 100%;
  background: var(--primary-gradient);
  transition: width 0.3s ease;
}

.trait-value {
  min-width: 40px;
  font-size: 0.8rem;
  color: var(--text-primary);
  text-align: right;
}

/* 示例对话 */
.example-chat {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}

.example-message {
  display: flex;
  gap: 0.75rem;
  align-items: flex-start;
}

.message-avatar img {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  object-fit: cover;
}

.user-avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: var(--primary-gradient);
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 0.8rem;
  font-weight: 600;
}

.message-content {
  flex: 1;
  padding: 0.75rem;
  background: var(--border-light);
  border-radius: 12px;
  font-size: 0.85rem;
  line-height: 1.4;
  color: var(--text-primary);
}

/* 表单样式 */
.form-group {
  margin-bottom: 1.5rem;
}

.form-group label {
  display: block;
  margin-bottom: 0.5rem;
  font-size: 0.9rem;
  font-weight: 500;
  color: var(--text-primary);
}

.form-group input,
.form-group textarea {
  width: 100%;
  padding: 0.75rem;
  border: 1px solid var(--border-light);
  border-radius: 8px;
  background: var(--background-light);
  color: var(--text-primary);
  font-size: 0.9rem;
  transition: border-color 0.2s ease;
  resize: vertical;
}

.form-group input:focus,
.form-group textarea:focus {
  outline: none;
  border-color: var(--primary-color);
}

.avatar-selector {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(60px, 1fr));
  gap: 0.5rem;
}

.avatar-option {
  width: 60px;
  height: 60px;
  border: 2px solid var(--border-light);
  border-radius: 50%;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.2s ease;
}

.avatar-option:hover {
  border-color: var(--border-dark);
}

.avatar-option.selected {
  border-color: var(--primary-color);
}

.avatar-option img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.tag-input input {
  margin-bottom: 0.5rem;
}

.selected-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 0.25rem;
}

.selected-tags .tag {
  display: flex;
  align-items: center;
  gap: 0.25rem;
  padding: 0.25rem 0.5rem;
  background: var(--primary-light);
  color: var(--primary-color);
  border-radius: 12px;
  font-size: 0.8rem;
}

.selected-tags .tag button {
  background: none;
  border: none;
  color: inherit;
  cursor: pointer;
  font-size: 1rem;
  line-height: 1;
}

/* 操作按钮 */
.preview-actions,
.create-actions {
  display: flex;
  gap: 1rem;
  padding: 1.5rem;
  border-top: 1px solid var(--border-light);
  justify-content: flex-end;
}

.action-btn.primary {
  padding: 0.75rem 1.5rem;
  background: var(--primary-gradient);
  color: white;
  border: none;
  border-radius: 8px;
  font-size: 0.9rem;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s ease;
}

.action-btn.primary:hover:not(:disabled) {
  opacity: 0.9;
  transform: translateY(-1px);
}

.action-btn.primary:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.action-btn.secondary {
  padding: 0.75rem 1.5rem;
  background: transparent;
  color: var(--text-secondary);
  border: 1px solid var(--border-light);
  border-radius: 8px;
  font-size: 0.9rem;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s ease;
}

.action-btn.secondary:hover {
  border-color: var(--border-dark);
  color: var(--text-primary);
}

/* 响应式设计 */
@media (max-width: 768px) {
  .character-selector {
    width: 100vw;
    left: -100vw;
  }
  
  .preview-content,
  .create-content {
    width: 95%;
    margin: 1rem;
  }
  
  .preview-header,
  .create-header {
    flex-direction: column;
    align-items: flex-start;
  }
  
  .preview-close,
  .create-close {
    position: absolute;
    top: 1rem;
    right: 1rem;
  }
  
  .characters-grid {
    grid-template-columns: 1fr;
  }
  
  .character-card {
    flex-direction: column;
    text-align: center;
  }
  
  .character-actions {
    flex-direction: row;
    justify-content: center;
  }
}

/* 暗黑模式适配 */
[data-theme="dark"] .character-selector {
  background: rgba(0, 0, 0, 0.9);
  border-right-color: rgba(255, 255, 255, 0.1);
}

[data-theme="dark"] .selector-header {
  background: rgba(0, 0, 0, 0.9);
  border-bottom-color: rgba(255, 255, 255, 0.1);
}

[data-theme="dark"] .character-card {
  background: rgba(255, 255, 255, 0.05);
}

[data-theme="dark"] .preview-content,
[data-theme="dark"] .create-content {
  background: rgba(0, 0, 0, 0.9);
}

/* 动画 */
@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}

@keyframes slideUp {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

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

/* 减少动画模式 */
@media (prefers-reduced-motion: reduce) {
  .character-selector,
  .character-card,
  .action-btn,
  .preview-modal,
  .create-modal {
    transition: none !important;
    animation: none !important;
  }
}
</style>