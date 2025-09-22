<template>
  <div class="emoji-picker" v-if="visible">
    <div class="emoji-picker-container">
      <!-- 头部搜索 -->
      <div class="emoji-header">
        <div class="search-container">
          <svg class="search-icon" viewBox="0 0 24 24">
            <path d="M9.5,3A6.5,6.5 0 0,1 16,9.5C16,11.11 15.41,12.59 14.44,13.73L14.71,14H15.5L20.5,19L19,20.5L14,15.5V14.71L13.73,14.44C12.59,15.41 11.11,16 9.5,16A6.5,6.5 0 0,1 3,9.5A6.5,6.5 0 0,1 9.5,3M9.5,5C7,5 5,7 5,9.5C5,12 7,14 9.5,14C12,14 14,12 14,9.5C14,7 12,5 9.5,5Z"/>
          </svg>
          <input
            v-model="searchQuery"
            type="text"
            placeholder="搜索表情..."
            class="search-input"
            @input="handleSearch"
          />
          <button v-if="searchQuery" @click="clearSearch" class="clear-btn">
            <svg viewBox="0 0 24 24">
              <path d="M19,6.41L17.59,5L12,10.59L6.41,5L5,6.41L10.59,12L5,17.59L6.41,19L12,13.41L17.59,19L19,17.59L13.41,12L19,6.41Z"/>
            </svg>
          </button>
        </div>
      </div>
      
      <!-- 分类标签 -->
      <div class="emoji-categories">
        <button
          v-for="category in categories"
          :key="category.id"
          :class="['category-btn', { active: activeCategory === category.id }]"
          @click="selectCategory(category.id)"
          :title="category.name"
        >
          <span class="category-icon">{{ category.icon }}</span>
        </button>
      </div>
      
      <!-- 表情网格 -->
      <div class="emoji-content" ref="emojiContent">
        <!-- 搜索结果 -->
        <div v-if="searchQuery && searchResults.length > 0" class="emoji-section">
          <h4 class="section-title">搜索结果</h4>
          <div class="emoji-grid">
            <button
              v-for="emoji in searchResults"
              :key="emoji.code"
              :class="['emoji-btn', { recent: isRecentEmoji(emoji) }]"
              @click="selectEmoji(emoji)"
              :title="emoji.name"
            >
              {{ emoji.emoji }}
            </button>
          </div>
        </div>
        
        <!-- 无搜索结果 -->
        <div v-else-if="searchQuery && searchResults.length === 0" class="no-results">
          <div class="no-results-icon">😕</div>
          <p>没有找到相关表情</p>
        </div>
        
        <!-- 分类表情 -->
        <div v-else>
          <!-- 最近使用 -->
          <div v-if="recentEmojis.length > 0 && activeCategory === 'recent'" class="emoji-section">
            <h4 class="section-title">最近使用</h4>
            <div class="emoji-grid">
              <button
                v-for="emoji in recentEmojis"
                :key="emoji.code"
                class="emoji-btn recent"
                @click="selectEmoji(emoji)"
                :title="emoji.name"
              >
                {{ emoji.emoji }}
              </button>
            </div>
          </div>
          
          <!-- 分类表情 -->
          <div v-for="category in filteredCategories" :key="category.id" class="emoji-section">
            <h4 class="section-title">{{ category.name }}</h4>
            <div class="emoji-grid">
              <button
                v-for="emoji in category.emojis"
                :key="emoji.code"
                :class="['emoji-btn', { recent: isRecentEmoji(emoji) }]"
                @click="selectEmoji(emoji)"
                :title="emoji.name"
              >
                {{ emoji.emoji }}
              </button>
            </div>
          </div>
        </div>
      </div>
      
      <!-- 底部信息 -->
      <div class="emoji-footer">
        <div class="emoji-info">
          <span v-if="hoveredEmoji" class="emoji-name">{{ hoveredEmoji.name }}</span>
          <span v-else class="emoji-tip">点击表情添加到消息</span>
        </div>
        <div class="emoji-actions">
          <button @click="toggleSkinTone" class="skin-tone-btn" title="肤色选择">
            <span class="skin-tone-preview">{{ currentSkinTone }}</span>
          </button>
        </div>
      </div>
      
      <!-- 肤色选择器 -->
      <div v-if="showSkinTones" class="skin-tone-picker">
        <button
          v-for="(tone, index) in skinTones"
          :key="index"
          :class="['skin-tone-option', { active: currentSkinToneIndex === index }]"
          @click="selectSkinTone(index)"
        >
          {{ tone }}
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, nextTick } from 'vue'

// Props
const props = defineProps({
  visible: {
    type: Boolean,
    default: false
  },
  position: {
    type: Object,
    default: () => ({ x: 0, y: 0 })
  }
})

// Emits
const emit = defineEmits(['select', 'close'])

// 响应式数据
const searchQuery = ref('')
const activeCategory = ref('recent')
const hoveredEmoji = ref(null)
const showSkinTones = ref(false)
const currentSkinToneIndex = ref(0)
const emojiContent = ref(null)

// 肤色选项
const skinTones = ['👋', '👋🏻', '👋🏼', '👋🏽', '👋🏾', '👋🏿']
const currentSkinTone = computed(() => skinTones[currentSkinToneIndex.value])

// 最近使用的表情
const recentEmojis = ref([])

// 表情分类数据
const categories = ref([
  { id: 'recent', name: '最近使用', icon: '🕒' },
  { id: 'smileys', name: '笑脸和人物', icon: '😀' },
  { id: 'animals', name: '动物和自然', icon: '🐶' },
  { id: 'food', name: '食物和饮料', icon: '🍎' },
  { id: 'activities', name: '活动', icon: '⚽' },
  { id: 'travel', name: '旅行和地点', icon: '🚗' },
  { id: 'objects', name: '物品', icon: '💡' },
  { id: 'symbols', name: '符号', icon: '❤️' },
  { id: 'flags', name: '旗帜', icon: '🏁' }
])

// 表情数据
const emojiData = ref({
  smileys: [
    { emoji: '😀', name: '开心', code: 'grinning', keywords: ['开心', '笑', '高兴'] },
    { emoji: '😃', name: '大笑', code: 'smiley', keywords: ['大笑', '开心', '兴奋'] },
    { emoji: '😄', name: '笑眯眯', code: 'smile', keywords: ['笑', '开心', '愉快'] },
    { emoji: '😁', name: '咧嘴笑', code: 'grin', keywords: ['咧嘴笑', '开心'] },
    { emoji: '😆', name: '哈哈', code: 'laughing', keywords: ['哈哈', '大笑', '搞笑'] },
    { emoji: '😅', name: '苦笑', code: 'sweat_smile', keywords: ['苦笑', '尴尬', '汗'] },
    { emoji: '🤣', name: '笑哭', code: 'rofl', keywords: ['笑哭', '搞笑', '爆笑'] },
    { emoji: '😂', name: '喜极而泣', code: 'joy', keywords: ['喜极而泣', '开心哭'] },
    { emoji: '🙂', name: '微笑', code: 'slightly_smiling', keywords: ['微笑', '淡笑'] },
    { emoji: '🙃', name: '倒脸', code: 'upside_down', keywords: ['倒脸', '调皮'] },
    { emoji: '😉', name: '眨眼', code: 'wink', keywords: ['眨眼', '调皮'] },
    { emoji: '😊', name: '害羞', code: 'blush', keywords: ['害羞', '脸红'] },
    { emoji: '😇', name: '天使', code: 'innocent', keywords: ['天使', '纯洁'] },
    { emoji: '🥰', name: '爱心眼', code: 'smiling_face_with_hearts', keywords: ['爱心', '喜欢'] },
    { emoji: '😍', name: '花痴', code: 'heart_eyes', keywords: ['花痴', '爱慕'] },
    { emoji: '🤩', name: '星星眼', code: 'star_struck', keywords: ['星星眼', '崇拜'] },
    { emoji: '😘', name: '飞吻', code: 'kissing_heart', keywords: ['飞吻', '亲吻'] },
    { emoji: '😗', name: '亲亲', code: 'kissing', keywords: ['亲亲', '亲吻'] },
    { emoji: '☺️', name: '满足', code: 'relaxed', keywords: ['满足', '放松'] },
    { emoji: '😚', name: '闭眼亲', code: 'kissing_closed_eyes', keywords: ['闭眼亲', '亲吻'] }
  ],
  animals: [
    { emoji: '🐶', name: '小狗', code: 'dog', keywords: ['狗', '小狗', '宠物'] },
    { emoji: '🐱', name: '小猫', code: 'cat', keywords: ['猫', '小猫', '宠物'] },
    { emoji: '🐭', name: '老鼠', code: 'mouse', keywords: ['老鼠', '鼠'] },
    { emoji: '🐹', name: '仓鼠', code: 'hamster', keywords: ['仓鼠', '小动物'] },
    { emoji: '🐰', name: '兔子', code: 'rabbit', keywords: ['兔子', '小兔'] },
    { emoji: '🦊', name: '狐狸', code: 'fox', keywords: ['狐狸', '狡猾'] },
    { emoji: '🐻', name: '熊', code: 'bear', keywords: ['熊', '大熊'] },
    { emoji: '🐼', name: '熊猫', code: 'panda', keywords: ['熊猫', '国宝'] },
    { emoji: '🐨', name: '考拉', code: 'koala', keywords: ['考拉', '树袋熊'] },
    { emoji: '🐯', name: '老虎', code: 'tiger', keywords: ['老虎', '猛兽'] }
  ],
  food: [
    { emoji: '🍎', name: '苹果', code: 'apple', keywords: ['苹果', '水果'] },
    { emoji: '🍌', name: '香蕉', code: 'banana', keywords: ['香蕉', '水果'] },
    { emoji: '🍇', name: '葡萄', code: 'grapes', keywords: ['葡萄', '水果'] },
    { emoji: '🍓', name: '草莓', code: 'strawberry', keywords: ['草莓', '水果'] },
    { emoji: '🍑', name: '樱桃', code: 'cherries', keywords: ['樱桃', '水果'] },
    { emoji: '🍊', name: '橘子', code: 'tangerine', keywords: ['橘子', '水果'] },
    { emoji: '🍋', name: '柠檬', code: 'lemon', keywords: ['柠檬', '酸'] },
    { emoji: '🍍', name: '菠萝', code: 'pineapple', keywords: ['菠萝', '水果'] },
    { emoji: '🥭', name: '芒果', code: 'mango', keywords: ['芒果', '水果'] },
    { emoji: '🍉', name: '西瓜', code: 'watermelon', keywords: ['西瓜', '夏天'] }
  ],
  activities: [
    { emoji: '⚽', name: '足球', code: 'soccer', keywords: ['足球', '运动'] },
    { emoji: '🏀', name: '篮球', code: 'basketball', keywords: ['篮球', '运动'] },
    { emoji: '🏈', name: '橄榄球', code: 'football', keywords: ['橄榄球', '运动'] },
    { emoji: '⚾', name: '棒球', code: 'baseball', keywords: ['棒球', '运动'] },
    { emoji: '🎾', name: '网球', code: 'tennis', keywords: ['网球', '运动'] },
    { emoji: '🏐', name: '排球', code: 'volleyball', keywords: ['排球', '运动'] },
    { emoji: '🏓', name: '乒乓球', code: 'ping_pong', keywords: ['乒乓球', '运动'] },
    { emoji: '🏸', name: '羽毛球', code: 'badminton', keywords: ['羽毛球', '运动'] },
    { emoji: '🥅', name: '球门', code: 'goal', keywords: ['球门', '足球'] },
    { emoji: '🎯', name: '靶心', code: 'dart', keywords: ['靶心', '目标'] }
  ],
  travel: [
    { emoji: '🚗', name: '汽车', code: 'car', keywords: ['汽车', '交通'] },
    { emoji: '🚕', name: '出租车', code: 'taxi', keywords: ['出租车', '交通'] },
    { emoji: '🚙', name: 'SUV', code: 'suv', keywords: ['SUV', '汽车'] },
    { emoji: '🚌', name: '公交车', code: 'bus', keywords: ['公交车', '交通'] },
    { emoji: '🚎', name: '电车', code: 'trolleybus', keywords: ['电车', '交通'] },
    { emoji: '🏎️', name: '赛车', code: 'race_car', keywords: ['赛车', '速度'] },
    { emoji: '🚓', name: '警车', code: 'police_car', keywords: ['警车', '警察'] },
    { emoji: '🚑', name: '救护车', code: 'ambulance', keywords: ['救护车', '医疗'] },
    { emoji: '🚒', name: '消防车', code: 'fire_engine', keywords: ['消防车', '消防'] },
    { emoji: '🚐', name: '面包车', code: 'minibus', keywords: ['面包车', '交通'] }
  ],
  objects: [
    { emoji: '💡', name: '灯泡', code: 'bulb', keywords: ['灯泡', '想法'] },
    { emoji: '🔦', name: '手电筒', code: 'flashlight', keywords: ['手电筒', '照明'] },
    { emoji: '🕯️', name: '蜡烛', code: 'candle', keywords: ['蜡烛', '浪漫'] },
    { emoji: '🪔', name: '油灯', code: 'diya_lamp', keywords: ['油灯', '传统'] },
    { emoji: '🔥', name: '火', code: 'fire', keywords: ['火', '热'] },
    { emoji: '💥', name: '爆炸', code: 'boom', keywords: ['爆炸', '冲击'] },
    { emoji: '💫', name: '眩晕', code: 'dizzy', keywords: ['眩晕', '星星'] },
    { emoji: '⭐', name: '星星', code: 'star', keywords: ['星星', '闪亮'] },
    { emoji: '🌟', name: '闪亮', code: 'star2', keywords: ['闪亮', '明星'] },
    { emoji: '✨', name: '闪烁', code: 'sparkles', keywords: ['闪烁', '魔法'] }
  ],
  symbols: [
    { emoji: '❤️', name: '红心', code: 'heart', keywords: ['爱', '心', '喜欢'] },
    { emoji: '🧡', name: '橙心', code: 'orange_heart', keywords: ['橙心', '爱'] },
    { emoji: '💛', name: '黄心', code: 'yellow_heart', keywords: ['黄心', '友谊'] },
    { emoji: '💚', name: '绿心', code: 'green_heart', keywords: ['绿心', '自然'] },
    { emoji: '💙', name: '蓝心', code: 'blue_heart', keywords: ['蓝心', '信任'] },
    { emoji: '💜', name: '紫心', code: 'purple_heart', keywords: ['紫心', '神秘'] },
    { emoji: '🖤', name: '黑心', code: 'black_heart', keywords: ['黑心', '酷'] },
    { emoji: '🤍', name: '白心', code: 'white_heart', keywords: ['白心', '纯洁'] },
    { emoji: '🤎', name: '棕心', code: 'brown_heart', keywords: ['棕心', '稳重'] },
    { emoji: '💔', name: '心碎', code: 'broken_heart', keywords: ['心碎', '伤心'] }
  ],
  flags: [
    { emoji: '🏁', name: '方格旗', code: 'checkered_flag', keywords: ['方格旗', '比赛'] },
    { emoji: '🚩', name: '三角旗', code: 'triangular_flag', keywords: ['三角旗', '标记'] },
    { emoji: '🎌', name: '日本国旗', code: 'crossed_flags', keywords: ['日本', '国旗'] },
    { emoji: '🏴', name: '黑旗', code: 'black_flag', keywords: ['黑旗', '海盗'] },
    { emoji: '🏳️', name: '白旗', code: 'white_flag', keywords: ['白旗', '投降'] },
    { emoji: '🏳️‍🌈', name: '彩虹旗', code: 'rainbow_flag', keywords: ['彩虹旗', '多元'] },
    { emoji: '🇨🇳', name: '中国国旗', code: 'cn', keywords: ['中国', '国旗'] },
    { emoji: '🇺🇸', name: '美国国旗', code: 'us', keywords: ['美国', '国旗'] },
    { emoji: '🇯🇵', name: '日本国旗', code: 'jp', keywords: ['日本', '国旗'] },
    { emoji: '🇬🇧', name: '英国国旗', code: 'gb', keywords: ['英国', '国旗'] }
  ]
})

// 计算属性
const searchResults = computed(() => {
  if (!searchQuery.value) return []
  
  const query = searchQuery.value.toLowerCase()
  const results = []
  
  Object.values(emojiData.value).forEach(categoryEmojis => {
    categoryEmojis.forEach(emoji => {
      if (
        emoji.name.toLowerCase().includes(query) ||
        emoji.keywords.some(keyword => keyword.toLowerCase().includes(query))
      ) {
        results.push(emoji)
      }
    })
  })
  
  return results.slice(0, 50) // 限制搜索结果数量
})

const filteredCategories = computed(() => {
  if (activeCategory.value === 'recent') {
    return recentEmojis.value.length > 0 ? [] : [categories.value.find(c => c.id === 'smileys')]
  }
  
  if (activeCategory.value === 'all') {
    return categories.value.slice(1).map(category => ({
      ...category,
      emojis: emojiData.value[category.id] || []
    }))
  }
  
  const category = categories.value.find(c => c.id === activeCategory.value)
  if (category) {
    return [{
      ...category,
      emojis: emojiData.value[category.id] || []
    }]
  }
  
  return []
})

// 方法
const selectCategory = (categoryId) => {
  activeCategory.value = categoryId
  searchQuery.value = ''
  
  nextTick(() => {
    if (emojiContent.value) {
      emojiContent.value.scrollTop = 0
    }
  })
}

const selectEmoji = (emoji) => {
  // 添加到最近使用
  addToRecent(emoji)
  
  // 发送选择事件
  emit('select', emoji)
}

const addToRecent = (emoji) => {
  // 移除已存在的相同表情
  const existingIndex = recentEmojis.value.findIndex(e => e.code === emoji.code)
  if (existingIndex > -1) {
    recentEmojis.value.splice(existingIndex, 1)
  }
  
  // 添加到开头
  recentEmojis.value.unshift(emoji)
  
  // 限制最近使用数量
  if (recentEmojis.value.length > 20) {
    recentEmojis.value = recentEmojis.value.slice(0, 20)
  }
  
  // 保存到本地存储
  localStorage.setItem('recentEmojis', JSON.stringify(recentEmojis.value))
}

const isRecentEmoji = (emoji) => {
  return recentEmojis.value.some(e => e.code === emoji.code)
}

const handleSearch = () => {
  // 搜索时自动切换到搜索结果
  if (searchQuery.value) {
    activeCategory.value = 'search'
  }
}

const clearSearch = () => {
  searchQuery.value = ''
  activeCategory.value = 'recent'
}

const toggleSkinTone = () => {
  showSkinTones.value = !showSkinTones.value
}

const selectSkinTone = (index) => {
  currentSkinToneIndex.value = index
  showSkinTones.value = false
  
  // 保存肤色选择
  localStorage.setItem('selectedSkinTone', index.toString())
}

// 生命周期
onMounted(() => {
  // 加载最近使用的表情
  const saved = localStorage.getItem('recentEmojis')
  if (saved) {
    try {
      recentEmojis.value = JSON.parse(saved)
    } catch (e) {
      console.error('Failed to load recent emojis:', e)
    }
  }
  
  // 加载肤色选择
  const savedSkinTone = localStorage.getItem('selectedSkinTone')
  if (savedSkinTone) {
    currentSkinToneIndex.value = parseInt(savedSkinTone) || 0
  }
  
  // 点击外部关闭
  document.addEventListener('click', handleClickOutside)
})

onUnmounted(() => {
  document.removeEventListener('click', handleClickOutside)
})

const handleClickOutside = (event) => {
  if (props.visible && !event.target.closest('.emoji-picker')) {
    emit('close')
  }
}
</script>

<style scoped>
.emoji-picker {
  position: fixed;
  z-index: 1000;
  bottom: 60px;
  right: 20px;
}

.emoji-picker-container {
  width: 320px;
  height: 400px;
  background: var(--bg-primary, white);
  border: 1px solid var(--border-color, #e1e5e9);
  border-radius: 12px;
  box-shadow: 0 8px 32px rgba(0,0,0,0.1);
  display: flex;
  flex-direction: column;
  overflow: hidden;
  backdrop-filter: blur(10px);
}

.emoji-header {
  padding: 1rem;
  border-bottom: 1px solid var(--border-color, #e1e5e9);
}

.search-container {
  position: relative;
  display: flex;
  align-items: center;
}

.search-icon {
  position: absolute;
  left: 12px;
  width: 16px;
  height: 16px;
  color: var(--text-secondary, #6c757d);
  fill: currentColor;
  z-index: 1;
}

.search-input {
  width: 100%;
  padding: 8px 12px 8px 36px;
  border: 1px solid var(--border-color, #e1e5e9);
  border-radius: 8px;
  font-size: 0.9rem;
  background: var(--bg-secondary, #f8f9fa);
  color: var(--text-primary, #212529);
  transition: all 0.3s ease;
}

.search-input:focus {
  outline: none;
  border-color: var(--primary-color, #007bff);
  background: var(--bg-primary, white);
}

.clear-btn {
  position: absolute;
  right: 8px;
  width: 20px;
  height: 20px;
  border: none;
  background: transparent;
  color: var(--text-secondary, #6c757d);
  cursor: pointer;
  border-radius: 4px;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s ease;
}

.clear-btn:hover {
  background: var(--bg-hover, #e9ecef);
  color: var(--text-primary, #212529);
}

.clear-btn svg {
  width: 12px;
  height: 12px;
  fill: currentColor;
}

.emoji-categories {
  display: flex;
  padding: 0.5rem;
  border-bottom: 1px solid var(--border-color, #e1e5e9);
  overflow-x: auto;
  gap: 0.25rem;
}

.category-btn {
  min-width: 32px;
  height: 32px;
  border: none;
  border-radius: 6px;
  background: transparent;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s ease;
  font-size: 1.1rem;
}

.category-btn:hover {
  background: var(--bg-hover, #e9ecef);
}

.category-btn.active {
  background: var(--primary-color, #007bff);
  color: white;
}

.category-icon {
  font-size: 1rem;
}

.emoji-content {
  flex: 1;
  overflow-y: auto;
  padding: 0.5rem;
}

.emoji-section {
  margin-bottom: 1rem;
}

.section-title {
  font-size: 0.8rem;
  font-weight: 600;
  color: var(--text-secondary, #6c757d);
  margin: 0 0 0.5rem 0;
  padding: 0 0.5rem;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.emoji-grid {
  display: grid;
  grid-template-columns: repeat(8, 1fr);
  gap: 0.25rem;
}

.emoji-btn {
  width: 32px;
  height: 32px;
  border: none;
  border-radius: 6px;
  background: transparent;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.2rem;
  transition: all 0.3s ease;
  position: relative;
}

.emoji-btn:hover {
  background: var(--bg-hover, #e9ecef);
  transform: scale(1.2);
}

.emoji-btn.recent::after {
  content: '';
  position: absolute;
  top: 2px;
  right: 2px;
  width: 6px;
  height: 6px;
  background: var(--primary-color, #007bff);
  border-radius: 50%;
}

.no-results {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 2rem;
  text-align: center;
  color: var(--text-secondary, #6c757d);
}

.no-results-icon {
  font-size: 2rem;
  margin-bottom: 0.5rem;
}

.emoji-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0.75rem 1rem;
  border-top: 1px solid var(--border-color, #e1e5e9);
  background: var(--bg-secondary, #f8f9fa);
}

.emoji-info {
  flex: 1;
}

.emoji-name {
  font-size: 0.8rem;
  font-weight: 500;
  color: var(--text-primary, #212529);
}

.emoji-tip {
  font-size: 0.8rem;
  color: var(--text-secondary, #6c757d);
}

.emoji-actions {
  display: flex;
  gap: 0.5rem;
}

.skin-tone-btn {
  width: 28px;
  height: 28px;
  border: 1px solid var(--border-color, #e1e5e9);
  border-radius: 6px;
  background: var(--bg-primary, white);
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s ease;
}

.skin-tone-btn:hover {
  border-color: var(--primary-color, #007bff);
  background: var(--bg-hover, #f0f8ff);
}

.skin-tone-preview {
  font-size: 1rem;
}

.skin-tone-picker {
  position: absolute;
  bottom: 100%;
  right: 1rem;
  background: var(--bg-primary, white);
  border: 1px solid var(--border-color, #e1e5e9);
  border-radius: 8px;
  padding: 0.5rem;
  display: flex;
  gap: 0.25rem;
  box-shadow: 0 4px 16px rgba(0,0,0,0.1);
}

.skin-tone-option {
  width: 32px;
  height: 32px;
  border: 1px solid transparent;
  border-radius: 6px;
  background: transparent;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1rem;
  transition: all 0.3s ease;
}

.skin-tone-option:hover {
  background: var(--bg-hover, #e9ecef);
}

.skin-tone-option.active {
  border-color: var(--primary-color, #007bff);
  background: var(--bg-hover, #f0f8ff);
}

/* 滚动条样式 */
.emoji-content::-webkit-scrollbar {
  width: 6px;
}

.emoji-content::-webkit-scrollbar-track {
  background: transparent;
}

.emoji-content::-webkit-scrollbar-thumb {
  background: var(--border-color, #e1e5e9);
  border-radius: 3px;
}

.emoji-content::-webkit-scrollbar-thumb:hover {
  background: var(--text-secondary, #6c757d);
}

.emoji-categories::-webkit-scrollbar {
  height: 4px;
}

.emoji-categories::-webkit-scrollbar-track {
  background: transparent;
}

.emoji-categories::-webkit-scrollbar-thumb {
  background: var(--border-color, #e1e5e9);
  border-radius: 2px;
}

/* 暗黑模式适配 */
@media (prefers-color-scheme: dark) {
  .emoji-picker-container {
    --bg-primary: #1a202c;
    --bg-secondary: #2d3748;
    --bg-hover: #374151;
    --border-color: #4a5568;
    --text-primary: #f7fafc;
    --text-secondary: #a0aec0;
  }
}

/* 响应式设计 */
@media (max-width: 768px) {
  .emoji-picker {
    bottom: 10px;
    right: 10px;
    left: 10px;
  }
  
  .emoji-picker-container {
    width: 100%;
    max-width: 100%;
  }
  
  .emoji-grid {
    grid-template-columns: repeat(6, 1fr);
  }
}
</style>