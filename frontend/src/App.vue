<template>
  <div id="app">
    <router-view />
    <!-- Toast Container -->
    <div class="toast-container position-fixed top-0 end-0 p-3" style="z-index: 9999;">
      <div 
        v-for="(toast, index) in toasts" 
        :key="index"
        class="toast show" 
        :class="toast.type === 'success' ? 'bg-success' : 'bg-danger'"
        role="alert"
      >
        <div class="toast-body text-white d-flex align-items-center">
          <i :class="toast.type === 'success' ? 'bi bi-check-circle-fill' : 'bi bi-exclamation-circle-fill'" class="me-2"></i>
          {{ toast.message }}
          <button type="button" class="btn-close btn-close-white ms-auto" @click="removeToast(index)"></button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { provide, ref, onMounted, onUnmounted } from 'vue'

export default {
  name: 'App',
  setup() {
    const toasts = ref([])

    const showToast = (message, type = 'success') => {
      const toast = { message, type }
      toasts.value.push(toast)
      setTimeout(() => {
        const idx = toasts.value.indexOf(toast)
        if (idx > -1) {
          toasts.value.splice(idx, 1)
        }
      }, 3000)
    }

    const removeToast = (index) => {
      toasts.value.splice(index, 1)
    }

    // 监听全局toast事件（用于request.js中的错误提示）
    const handleGlobalToast = (event) => {
      const { message, type } = event.detail
      showToast(message, type)
    }

    onMounted(() => {
      window.addEventListener('show-toast', handleGlobalToast)
    })

    onUnmounted(() => {
      window.removeEventListener('show-toast', handleGlobalToast)
    })

    provide('showToast', showToast)

    return {
      toasts,
      removeToast
    }
  }
}
</script>

<style>
#app {
  min-height: 100vh;
}
</style>
