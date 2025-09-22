<template>
  <div class="image-uploader">
    <!-- 拖拽上传区域 -->
    <div 
      class="upload-area"
      :class="{ 
        'drag-over': isDragOver,
        'uploading': isUploading,
        'has-image': previewUrl
      }"
      @dragover.prevent="handleDragOver"
      @dragleave.prevent="handleDragLeave"
      @drop.prevent="handleDrop"
      @click="triggerFileInput"
    >
      <!-- 文件输入 -->
      <input
        ref="fileInput"
        type="file"
        accept="image/*"
        multiple
        @change="handleFileSelect"
        class="file-input"
      />
      
      <!-- 上传状态显示 -->
      <div v-if="!previewUrl && !isUploading" class="upload-prompt">
        <div class="upload-icon">
          <svg viewBox="0 0 24 24">
            <path d="M14,2H6A2,2 0 0,0 4,4V20A2,2 0 0,0 6,22H18A2,2 0 0,0 20,20V8L14,2M18,20H6V4H13V9H18V20Z"/>
          </svg>
        </div>
        <p class="upload-text">点击或拖拽图片到此处上传</p>
        <p class="upload-hint">支持 JPG、PNG、GIF 格式，最大 10MB</p>
      </div>
      
      <!-- 上传进度 -->
      <div v-if="isUploading" class="upload-progress">
        <div class="progress-circle">
          <svg class="progress-ring" viewBox="0 0 120 120">
            <circle
              class="progress-ring-circle"
              cx="60"
              cy="60"
              r="54"
              :stroke-dasharray="circumference"
              :stroke-dashoffset="progressOffset"
            />
          </svg>
          <span class="progress-text">{{ uploadProgress }}%</span>
        </div>
        <p class="upload-status">正在上传...</p>
      </div>
      
      <!-- 图片预览 -->
      <div v-if="previewUrl && !isUploading" class="image-preview">
        <img :src="previewUrl" :alt="fileName" class="preview-image" />
        <div class="image-overlay">
          <div class="image-info">
            <p class="image-name">{{ fileName }}</p>
            <p class="image-size">{{ formatFileSize(fileSize) }}</p>
          </div>
          <div class="image-actions">
            <button @click.stop="removeImage" class="action-btn remove-btn" title="删除">
              <svg viewBox="0 0 24 24">
                <path d="M19,4H15.5L14.5,3H9.5L8.5,4H5V6H19M6,19A2,2 0 0,0 8,21H16A2,2 0 0,0 18,19V7H6V19Z"/>
              </svg>
            </button>
            <button @click.stop="cropImage" class="action-btn crop-btn" title="裁剪">
              <svg viewBox="0 0 24 24">
                <path d="M7,17L10,14H14L17,17H7M12,12.5A0.5,0.5 0 0,1 11.5,12A0.5,0.5 0 0,1 12,11.5A0.5,0.5 0 0,1 12.5,12A0.5,0.5 0 0,1 12,12.5M6,20A2,2 0 0,1 4,18V6A2,2 0 0,1 6,4H18A2,2 0 0,1 20,6V18A2,2 0 0,1 18,20H6Z"/>
              </svg>
            </button>
          </div>
        </div>
      </div>
    </div>
    
    <!-- 多图片预览 -->
    <div v-if="uploadedImages.length > 0" class="image-gallery">
      <h4 class="gallery-title">已上传的图片 ({{ uploadedImages.length }})</h4>
      <div class="gallery-grid">
        <div 
          v-for="(image, index) in uploadedImages" 
          :key="image.id"
          class="gallery-item"
          @click="selectImage(image)"
        >
          <img :src="image.url" :alt="image.name" class="gallery-image" />
          <div class="gallery-overlay">
            <button @click.stop="removeFromGallery(index)" class="gallery-remove">
              <svg viewBox="0 0 24 24">
                <path d="M12,2C17.53,2 22,6.47 22,12C22,17.53 17.53,22 12,22C6.47,22 2,17.53 2,12C2,6.47 6.47,2 12,2M15.59,7L12,10.59L8.41,7L7,8.41L10.59,12L7,15.59L8.41,17L12,13.41L15.59,17L17,15.59L13.41,12L17,8.41L15.59,7Z"/>
              </svg>
            </button>
          </div>
        </div>
      </div>
    </div>
    
    <!-- 图片裁剪模态框 -->
    <div v-if="showCropper" class="cropper-modal" @click.self="closeCropper">
      <div class="cropper-container">
        <div class="cropper-header">
          <h3>裁剪图片</h3>
          <button @click="closeCropper" class="close-btn">
            <svg viewBox="0 0 24 24">
              <path d="M19,6.41L17.59,5L12,10.59L6.41,5L5,6.41L10.59,12L5,17.59L6.41,19L12,13.41L17.59,19L19,17.59L13.41,12L19,6.41Z"/>
            </svg>
          </button>
        </div>
        <div class="cropper-content">
          <canvas ref="cropCanvas" class="crop-canvas"></canvas>
        </div>
        <div class="cropper-actions">
          <button @click="applyCrop" class="btn btn-primary">应用裁剪</button>
          <button @click="closeCropper" class="btn btn-secondary">取消</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, nextTick } from 'vue'

// Props
const props = defineProps({
  maxSize: {
    type: Number,
    default: 10 * 1024 * 1024 // 10MB
  },
  maxFiles: {
    type: Number,
    default: 5
  },
  acceptTypes: {
    type: Array,
    default: () => ['image/jpeg', 'image/png', 'image/gif', 'image/webp']
  }
})

// Emits
const emit = defineEmits(['upload', 'remove', 'select'])

// 响应式数据
const fileInput = ref(null)
const cropCanvas = ref(null)
const isDragOver = ref(false)
const isUploading = ref(false)
const uploadProgress = ref(0)
const previewUrl = ref('')
const fileName = ref('')
const fileSize = ref(0)
const currentFile = ref(null)
const uploadedImages = ref([])
const showCropper = ref(false)

// 计算属性
const circumference = computed(() => 2 * Math.PI * 54)
const progressOffset = computed(() => {
  return circumference.value - (uploadProgress.value / 100) * circumference.value
})

// 拖拽处理
const handleDragOver = (e) => {
  e.preventDefault()
  isDragOver.value = true
}

const handleDragLeave = (e) => {
  e.preventDefault()
  isDragOver.value = false
}

const handleDrop = (e) => {
  e.preventDefault()
  isDragOver.value = false
  
  const files = Array.from(e.dataTransfer.files)
  handleFiles(files)
}

// 文件选择处理
const triggerFileInput = () => {
  if (!isUploading.value) {
    fileInput.value?.click()
  }
}

const handleFileSelect = (e) => {
  const files = Array.from(e.target.files)
  handleFiles(files)
}

// 文件处理
const handleFiles = (files) => {
  if (files.length === 0) return
  
  // 检查文件数量限制
  if (uploadedImages.value.length + files.length > props.maxFiles) {
    alert(`最多只能上传 ${props.maxFiles} 张图片`)
    return
  }
  
  files.forEach(file => {
    if (validateFile(file)) {
      uploadFile(file)
    }
  })
}

// 文件验证
const validateFile = (file) => {
  // 检查文件类型
  if (!props.acceptTypes.includes(file.type)) {
    alert('不支持的文件格式')
    return false
  }
  
  // 检查文件大小
  if (file.size > props.maxSize) {
    alert(`文件大小不能超过 ${formatFileSize(props.maxSize)}`)
    return false
  }
  
  return true
}

// 文件上传
const uploadFile = async (file) => {
  isUploading.value = true
  uploadProgress.value = 0
  fileName.value = file.name
  fileSize.value = file.size
  currentFile.value = file
  
  // 创建预览
  const reader = new FileReader()
  reader.onload = (e) => {
    previewUrl.value = e.target.result
  }
  reader.readAsDataURL(file)
  
  try {
    // 模拟上传进度
    const uploadInterval = setInterval(() => {
      uploadProgress.value += Math.random() * 30
      if (uploadProgress.value >= 100) {
        uploadProgress.value = 100
        clearInterval(uploadInterval)
        completeUpload(file)
      }
    }, 200)
    
  } catch (error) {
    console.error('上传失败:', error)
    alert('上传失败，请重试')
    resetUpload()
  }
}

// 完成上传
const completeUpload = (file) => {
  const imageData = {
    id: Date.now(),
    name: file.name,
    size: file.size,
    url: previewUrl.value,
    file: file
  }
  
  uploadedImages.value.push(imageData)
  emit('upload', imageData)
  
  setTimeout(() => {
    resetUpload()
  }, 1000)
}

// 重置上传状态
const resetUpload = () => {
  isUploading.value = false
  uploadProgress.value = 0
  previewUrl.value = ''
  fileName.value = ''
  fileSize.value = 0
  currentFile.value = null
  
  if (fileInput.value) {
    fileInput.value.value = ''
  }
}

// 移除图片
const removeImage = () => {
  resetUpload()
}

const removeFromGallery = (index) => {
  const removedImage = uploadedImages.value.splice(index, 1)[0]
  emit('remove', removedImage)
}

// 选择图片
const selectImage = (image) => {
  emit('select', image)
}

// 图片裁剪
const cropImage = () => {
  if (previewUrl.value) {
    showCropper.value = true
    nextTick(() => {
      initCropper()
    })
  }
}

const initCropper = () => {
  const canvas = cropCanvas.value
  const ctx = canvas.getContext('2d')
  const img = new Image()
  
  img.onload = () => {
    canvas.width = img.width
    canvas.height = img.height
    ctx.drawImage(img, 0, 0)
  }
  
  img.src = previewUrl.value
}

const applyCrop = () => {
  // 这里可以实现具体的裁剪逻辑
  closeCropper()
}

const closeCropper = () => {
  showCropper.value = false
}

// 工具函数
const formatFileSize = (bytes) => {
  if (bytes === 0) return '0 Bytes'
  
  const k = 1024
  const sizes = ['Bytes', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  
  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
}
</script>

<style scoped>
.image-uploader {
  width: 100%;
}

.upload-area {
  position: relative;
  border: 2px dashed var(--border-color, #e1e5e9);
  border-radius: 12px;
  padding: 2rem;
  text-align: center;
  cursor: pointer;
  transition: all 0.3s ease;
  background: var(--bg-secondary, #f8f9fa);
  min-height: 200px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.upload-area:hover {
  border-color: var(--primary-color, #007bff);
  background: var(--bg-hover, #f0f8ff);
}

.upload-area.drag-over {
  border-color: var(--primary-color, #007bff);
  background: var(--bg-hover, #f0f8ff);
  transform: scale(1.02);
}

.upload-area.uploading {
  border-color: var(--success-color, #28a745);
}

.upload-area.has-image {
  padding: 0;
  border: none;
  background: transparent;
}

.file-input {
  display: none;
}

.upload-prompt {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 1rem;
}

.upload-icon {
  width: 48px;
  height: 48px;
  color: var(--text-secondary, #6c757d);
}

.upload-icon svg {
  width: 100%;
  height: 100%;
  fill: currentColor;
}

.upload-text {
  font-size: 1.1rem;
  font-weight: 500;
  color: var(--text-primary, #212529);
  margin: 0;
}

.upload-hint {
  font-size: 0.9rem;
  color: var(--text-secondary, #6c757d);
  margin: 0;
}

.upload-progress {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 1rem;
}

.progress-circle {
  position: relative;
  width: 80px;
  height: 80px;
}

.progress-ring {
  width: 100%;
  height: 100%;
  transform: rotate(-90deg);
}

.progress-ring-circle {
  fill: none;
  stroke: var(--primary-color, #007bff);
  stroke-width: 4;
  stroke-linecap: round;
  transition: stroke-dashoffset 0.3s ease;
}

.progress-text {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  font-weight: 600;
  color: var(--primary-color, #007bff);
}

.upload-status {
  font-size: 0.9rem;
  color: var(--text-secondary, #6c757d);
  margin: 0;
}

.image-preview {
  position: relative;
  width: 100%;
  height: 200px;
  border-radius: 12px;
  overflow: hidden;
}

.preview-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.image-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(to top, rgba(0,0,0,0.7) 0%, transparent 50%);
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  padding: 1rem;
  opacity: 0;
  transition: opacity 0.3s ease;
}

.image-preview:hover .image-overlay {
  opacity: 1;
}

.image-info {
  align-self: flex-start;
}

.image-name {
  color: white;
  font-weight: 500;
  margin: 0 0 0.25rem 0;
  font-size: 0.9rem;
}

.image-size {
  color: rgba(255,255,255,0.8);
  font-size: 0.8rem;
  margin: 0;
}

.image-actions {
  align-self: flex-end;
  display: flex;
  gap: 0.5rem;
}

.action-btn {
  width: 36px;
  height: 36px;
  border: none;
  border-radius: 8px;
  background: rgba(255,255,255,0.2);
  color: white;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s ease;
  backdrop-filter: blur(10px);
}

.action-btn:hover {
  background: rgba(255,255,255,0.3);
  transform: scale(1.1);
}

.action-btn svg {
  width: 18px;
  height: 18px;
  fill: currentColor;
}

.remove-btn:hover {
  background: rgba(220, 53, 69, 0.8);
}

.crop-btn:hover {
  background: rgba(40, 167, 69, 0.8);
}

.image-gallery {
  margin-top: 2rem;
}

.gallery-title {
  font-size: 1rem;
  font-weight: 600;
  color: var(--text-primary, #212529);
  margin: 0 0 1rem 0;
}

.gallery-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(120px, 1fr));
  gap: 1rem;
}

.gallery-item {
  position: relative;
  aspect-ratio: 1;
  border-radius: 8px;
  overflow: hidden;
  cursor: pointer;
  transition: transform 0.3s ease;
}

.gallery-item:hover {
  transform: scale(1.05);
}

.gallery-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.gallery-overlay {
  position: absolute;
  top: 0;
  right: 0;
  padding: 0.5rem;
  opacity: 0;
  transition: opacity 0.3s ease;
}

.gallery-item:hover .gallery-overlay {
  opacity: 1;
}

.gallery-remove {
  width: 24px;
  height: 24px;
  border: none;
  border-radius: 50%;
  background: rgba(220, 53, 69, 0.8);
  color: white;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s ease;
}

.gallery-remove:hover {
  background: rgba(220, 53, 69, 1);
  transform: scale(1.1);
}

.gallery-remove svg {
  width: 14px;
  height: 14px;
  fill: currentColor;
}

.cropper-modal {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0,0,0,0.8);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.cropper-container {
  background: var(--bg-primary, white);
  border-radius: 12px;
  max-width: 90vw;
  max-height: 90vh;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.cropper-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1rem 1.5rem;
  border-bottom: 1px solid var(--border-color, #e1e5e9);
}

.cropper-header h3 {
  margin: 0;
  font-size: 1.1rem;
  color: var(--text-primary, #212529);
}

.close-btn {
  width: 32px;
  height: 32px;
  border: none;
  border-radius: 6px;
  background: transparent;
  color: var(--text-secondary, #6c757d);
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s ease;
}

.close-btn:hover {
  background: var(--bg-secondary, #f8f9fa);
  color: var(--text-primary, #212529);
}

.close-btn svg {
  width: 18px;
  height: 18px;
  fill: currentColor;
}

.cropper-content {
  padding: 1.5rem;
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
}

.crop-canvas {
  max-width: 100%;
  max-height: 400px;
  border: 1px solid var(--border-color, #e1e5e9);
  border-radius: 8px;
}

.cropper-actions {
  display: flex;
  gap: 1rem;
  padding: 1rem 1.5rem;
  border-top: 1px solid var(--border-color, #e1e5e9);
  justify-content: flex-end;
}

.btn {
  padding: 0.5rem 1rem;
  border: none;
  border-radius: 6px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
}

.btn-primary {
  background: var(--primary-color, #007bff);
  color: white;
}

.btn-primary:hover {
  background: var(--primary-hover, #0056b3);
}

.btn-secondary {
  background: var(--bg-secondary, #f8f9fa);
  color: var(--text-primary, #212529);
  border: 1px solid var(--border-color, #e1e5e9);
}

.btn-secondary:hover {
  background: var(--bg-hover, #e9ecef);
}

/* 暗黑模式适配 */
@media (prefers-color-scheme: dark) {
  .upload-area {
    --bg-secondary: #2d3748;
    --border-color: #4a5568;
    --bg-hover: #374151;
  }
  
  .cropper-container {
    --bg-primary: #1a202c;
    --border-color: #4a5568;
  }
}

/* 响应式设计 */
@media (max-width: 768px) {
  .upload-area {
    padding: 1.5rem;
    min-height: 150px;
  }
  
  .gallery-grid {
    grid-template-columns: repeat(auto-fill, minmax(100px, 1fr));
    gap: 0.75rem;
  }
  
  .cropper-container {
    margin: 1rem;
    max-width: calc(100vw - 2rem);
    max-height: calc(100vh - 2rem);
  }
}
</style>