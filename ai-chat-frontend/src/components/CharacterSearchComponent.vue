<template>
  <div class="character-search">
    <!-- 搜索栏 -->
    <div class="search-section">
      <div class="search-bar">
        <el-input
          v-model="searchKeyword"
          placeholder="搜索角色名称或描述..."
          @input="handleSearch"
          @keyup.enter="handleSearch"
          clearable
          size="large"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
      </div>
      
      <!-- 快速筛选标签 -->
      <div class="filter-tags">
        <el-tag
          v-for="tag in quickFilters"
          :key="tag"
          :type="selectedFilter === tag ? 'primary' : 'info'"
          @click="handleFilterClick(tag)"
          class="filter-tag"
        >
          {{ tag }}
        </el-tag>
      </div>
    </div>

    <!-- 加载状态 -->
    <div v-if="loading" class="loading-section">
      <el-skeleton :rows="3" animated />
    </div>

    <!-- 角色列表 -->
    <div v-else class="characters-grid">
      <div
        v-for="character in filteredCharacters"
        :key="character.id"
        class="character-card"
        @click="selectCharacter(character)"
      >
        <div class="character-avatar">
          <img 
            :src="character.avatarUrl || 'https://picsum.photos/100/100?random=' + character.id" 
            :alt="character.name"
            @error="handleImageError"
          />
        </div>
        
        <div class="character-info">
          <h3 class="character-name">{{ character.name }}</h3>
          <p class="character-description">{{ character.description }}</p>
          
          <div class="character-skills" v-if="character.skills">
            <el-tag
              v-for="skill in getSkillsList(character.skills)"
              :key="skill"
              size="small"
              type="success"
              class="skill-tag"
            >
              {{ skill }}
            </el-tag>
          </div>
          
          <div class="character-personality" v-if="character.personality">
            <span class="personality-label">性格特征：</span>
            <span class="personality-text">{{ character.personality }}</span>
          </div>
        </div>
        
        <div class="character-actions">
          <el-button type="primary" size="small" @click.stop="startChat(character)">
            开始对话
          </el-button>
          <el-button size="small" @click.stop="viewDetails(character)">
            查看详情
          </el-button>
        </div>
      </div>
    </div>

    <!-- 空状态 -->
    <div v-if="!loading && filteredCharacters.length === 0" class="empty-state">
      <el-empty description="没有找到匹配的角色">
        <el-button type="primary" @click="initializeCharacters">
          初始化默认角色
        </el-button>
      </el-empty>
    </div>

    <!-- 角色详情对话框 -->
    <el-dialog
      v-model="detailDialogVisible"
      :title="selectedCharacter?.name"
      width="600px"
      class="character-detail-dialog"
    >
      <div v-if="selectedCharacter" class="character-detail">
        <div class="detail-avatar">
          <img 
            :src="selectedCharacter.avatarUrl || 'https://picsum.photos/200/200?random=' + selectedCharacter.id" 
            :alt="selectedCharacter.name"
          />
        </div>
        
        <div class="detail-content">
          <h3>{{ selectedCharacter.name }}</h3>
          <p class="detail-description">{{ selectedCharacter.description }}</p>
          
          <div class="detail-section" v-if="selectedCharacter.personality">
            <h4>性格特征</h4>
            <p>{{ selectedCharacter.personality }}</p>
          </div>
          
          <div class="detail-section" v-if="selectedCharacter.skills">
            <h4>专业技能</h4>
            <div class="skills-list">
              <el-tag
                v-for="skill in getSkillsList(selectedCharacter.skills)"
                :key="skill"
                type="success"
                class="skill-tag"
              >
                {{ skill }}
              </el-tag>
            </div>
          </div>
        </div>
      </div>
      
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="detailDialogVisible = false">关闭</el-button>
          <el-button type="primary" @click="startChatFromDialog">开始对话</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { Search } from '@element-plus/icons-vue'
import characterService from '@/services/characterService.js'

// 响应式数据
const characters = ref([])
const searchKeyword = ref('')
const selectedFilter = ref('')
const loading = ref(false)
const detailDialogVisible = ref(false)
const selectedCharacter = ref(null)

// 快速筛选标签
const quickFilters = ['全部', '历史人物', '文学角色', '科学家', '哲学家', '艺术家']

// 计算属性
const filteredCharacters = computed(() => {
  let result = characters.value

  // 关键词搜索
  if (searchKeyword.value.trim()) {
    const keyword = searchKeyword.value.toLowerCase().trim()
    result = result.filter(character => 
      character.name.toLowerCase().includes(keyword) ||
      character.description.toLowerCase().includes(keyword) ||
      (character.personality && character.personality.toLowerCase().includes(keyword))
    )
  }

  // 分类筛选
  if (selectedFilter.value && selectedFilter.value !== '全部') {
    result = result.filter(character => {
      const filter = selectedFilter.value
      const name = character.name.toLowerCase()
      const description = character.description.toLowerCase()
      
      switch (filter) {
        case '历史人物':
          return ['苏格拉底', '孔子', '爱因斯坦', '居里夫人'].some(n => name.includes(n.toLowerCase()))
        case '文学角色':
          return ['哈利波特', '莎士比亚'].some(n => name.includes(n.toLowerCase()))
        case '科学家':
          return ['爱因斯坦', '居里夫人'].some(n => name.includes(n.toLowerCase()))
        case '哲学家':
          return ['苏格拉底', '孔子'].some(n => name.includes(n.toLowerCase()))
        case '艺术家':
          return ['莎士比亚'].some(n => name.includes(n.toLowerCase()))
        default:
          return true
      }
    })
  }

  return result
})

// 事件定义
const emit = defineEmits(['character-selected'])

// 方法
const loadCharacters = async () => {
  loading.value = true
  try {
    characters.value = await characterService.getAllCharacters()
  } catch (error) {
    ElMessage.error('加载角色列表失败: ' + error.message)
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  // 搜索逻辑已在计算属性中处理
}

const handleFilterClick = (filter) => {
  selectedFilter.value = selectedFilter.value === filter ? '' : filter
}

const selectCharacter = (character) => {
  emit('character-selected', character)
}

const startChat = (character) => {
  emit('character-selected', character)
}

const viewDetails = (character) => {
  selectedCharacter.value = character
  detailDialogVisible.value = true
}

const startChatFromDialog = () => {
  detailDialogVisible.value = false
  emit('character-selected', selectedCharacter.value)
}

const getSkillsList = (skills) => {
  if (!skills) return []
  return skills.split(',').map(skill => skill.trim()).filter(skill => skill)
}

const handleImageError = (event) => {
  event.target.src = 'https://picsum.photos/100/100?random=default'
}

const initializeCharacters = async () => {
  try {
    await characterService.initializeDefaultCharacters()
    ElMessage.success('默认角色初始化成功')
    await loadCharacters()
  } catch (error) {
    ElMessage.error('初始化失败: ' + error.message)
  }
}

// 生命周期
onMounted(() => {
  loadCharacters()
})
</script>

<style scoped>
.character-search {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
}

.search-section {
  margin-bottom: 30px;
}

.search-bar {
  margin-bottom: 20px;
}

.filter-tags {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.filter-tag {
  cursor: pointer;
  transition: all 0.3s;
}

.filter-tag:hover {
  transform: translateY(-2px);
}

.loading-section {
  margin: 40px 0;
}

.characters-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(350px, 1fr));
  gap: 20px;
  margin-top: 20px;
}

.character-card {
  border: 1px solid #e4e7ed;
  border-radius: 12px;
  padding: 20px;
  cursor: pointer;
  transition: all 0.3s;
  background: white;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.character-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.15);
  border-color: #409eff;
}

.character-avatar {
  text-align: center;
  margin-bottom: 15px;
}

.character-avatar img {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  object-fit: cover;
  border: 3px solid #f0f0f0;
}

.character-info {
  margin-bottom: 15px;
}

.character-name {
  font-size: 18px;
  font-weight: bold;
  color: #303133;
  margin: 0 0 10px 0;
  text-align: center;
}

.character-description {
  color: #606266;
  font-size: 14px;
  line-height: 1.5;
  margin: 0 0 15px 0;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.character-skills {
  margin-bottom: 10px;
}

.skill-tag {
  margin: 2px 4px 2px 0;
  font-size: 12px;
}

.character-personality {
  font-size: 12px;
  color: #909399;
}

.personality-label {
  font-weight: bold;
}

.character-actions {
  display: flex;
  gap: 10px;
  justify-content: center;
}

.empty-state {
  text-align: center;
  margin: 60px 0;
}

.character-detail-dialog .character-detail {
  display: flex;
  gap: 20px;
}

.detail-avatar img {
  width: 120px;
  height: 120px;
  border-radius: 50%;
  object-fit: cover;
}

.detail-content {
  flex: 1;
}

.detail-content h3 {
  margin: 0 0 15px 0;
  color: #303133;
}

.detail-description {
  color: #606266;
  line-height: 1.6;
  margin-bottom: 20px;
}

.detail-section {
  margin-bottom: 20px;
}

.detail-section h4 {
  margin: 0 0 10px 0;
  color: #409eff;
  font-size: 14px;
}

.detail-section p {
  margin: 0;
  color: #606266;
  line-height: 1.5;
}

.skills-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

@media (max-width: 768px) {
  .characters-grid {
    grid-template-columns: 1fr;
  }
  
  .character-detail {
    flex-direction: column;
    text-align: center;
  }
  
  .filter-tags {
    justify-content: center;
  }
}
</style>