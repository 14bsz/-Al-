<script setup lang="ts">
import ModernChatWindow from './components/ModernChatWindow.vue'
</script>

<template>
  <div id="app">
    <ModernChatWindow />
  </div>
</template>

<style>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

html, body {
  height: 100%;
  overflow: hidden;
}

#app {
  font-family: 'Inter', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  height: 100vh;
  width: 100vw;
  position: relative;
}

/* 全局主题变量 */
:root {
  --primary-gradient: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  --secondary-gradient: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
  --success-color: #4ecdc4;
  --warning-color: #feca57;
  --error-color: #ff6b6b;
  --text-primary: #2c3e50;
  --text-secondary: #7f8c8d;
  --background-light: rgba(255, 255, 255, 0.9);
  --background-dark: rgba(0, 0, 0, 0.8);
  --border-light: rgba(255, 255, 255, 0.2);
  --border-dark: rgba(0, 0, 0, 0.1);
  --shadow-light: 0 10px 30px rgba(0, 0, 0, 0.1);
  --shadow-dark: 0 10px 30px rgba(0, 0, 0, 0.3);
  --blur-effect: blur(20px);
}

[data-theme="dark"] {
  --text-primary: #ffffff;
  --text-secondary: #b0b0b0;
  --background-light: rgba(255, 255, 255, 0.1);
  --background-dark: rgba(0, 0, 0, 0.9);
  --border-light: rgba(255, 255, 255, 0.1);
  --border-dark: rgba(255, 255, 255, 0.05);
}

/* 全局滚动条样式 */
::-webkit-scrollbar {
  width: 8px;
  height: 8px;
}

::-webkit-scrollbar-track {
  background: var(--background-light);
  border-radius: 4px;
}

::-webkit-scrollbar-thumb {
  background: var(--border-light);
  border-radius: 4px;
  transition: all 0.3s ease;
}

::-webkit-scrollbar-thumb:hover {
  background: var(--border-dark);
}

/* 全局动画 */
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

@keyframes slideDown {
  from { 
    opacity: 0; 
    transform: translateY(-20px); 
  }
  to { 
    opacity: 1; 
    transform: translateY(0); 
  }
}

@keyframes scaleIn {
  from { 
    opacity: 0; 
    transform: scale(0.9); 
  }
  to { 
    opacity: 1; 
    transform: scale(1); 
  }
}

/* 全局工具类 */
.fade-enter-active, .fade-leave-active {
  transition: opacity 0.3s ease;
}

.fade-enter-from, .fade-leave-to {
  opacity: 0;
}

.slide-up-enter-active, .slide-up-leave-active {
  transition: all 0.3s ease;
}

.slide-up-enter-from {
  opacity: 0;
  transform: translateY(20px);
}

.slide-up-leave-to {
  opacity: 0;
  transform: translateY(-20px);
}

.scale-enter-active, .scale-leave-active {
  transition: all 0.3s ease;
}

.scale-enter-from, .scale-leave-to {
  opacity: 0;
  transform: scale(0.9);
}

/* 响应式断点 */
@media (max-width: 1200px) {
  :root {
    font-size: 14px;
  }
}

@media (max-width: 768px) {
  :root {
    font-size: 13px;
  }
}

@media (max-width: 480px) {
  :root {
    font-size: 12px;
  }
}

/* 无障碍支持 */
@media (prefers-reduced-motion: reduce) {
  *,
  *::before,
  *::after {
    animation-duration: 0.01ms !important;
    animation-iteration-count: 1 !important;
    transition-duration: 0.01ms !important;
  }
}

/* 高对比度模式支持 */
@media (prefers-contrast: high) {
  :root {
    --border-light: rgba(0, 0, 0, 0.8);
    --border-dark: rgba(0, 0, 0, 0.9);
  }
  
  [data-theme="dark"] {
    --border-light: rgba(255, 255, 255, 0.8);
    --border-dark: rgba(255, 255, 255, 0.9);
  }
}

/* 打印样式 */
@media print {
  #app {
    background: white !important;
    color: black !important;
  }
  
  .background-3d,
  .gradient-orbs,
  .floating-particles {
    display: none !important;
  }
}
</style>
