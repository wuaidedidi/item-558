<template>
  <div class="auth-container">
    <div class="auth-card fade-in">
      <div class="text-center mb-4">
        <div class="d-inline-flex align-items-center justify-content-center" 
             style="width: 64px; height: 64px; background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); border-radius: 16px; margin-bottom: 1rem;">
          <i class="bi bi-book-half text-white" style="font-size: 2rem;"></i>
        </div>
        <h1 class="auth-title">练习题与考试系统</h1>
        <p class="auth-subtitle">欢迎回来，请登录您的账户</p>
      </div>
      
      <form @submit.prevent="handleLogin">
        <div class="mb-3">
          <label class="form-label">用户名</label>
          <div class="input-group">
            <span class="input-group-text bg-light border-end-0">
              <i class="bi bi-person text-muted"></i>
            </span>
            <input 
              type="text" 
              class="form-control border-start-0" 
              v-model="form.username" 
              placeholder="请输入用户名"
              required
              autocomplete="username"
            >
          </div>
        </div>
        
        <div class="mb-4">
          <label class="form-label">密码</label>
          <div class="input-group">
            <span class="input-group-text bg-light border-end-0">
              <i class="bi bi-lock text-muted"></i>
            </span>
            <input 
              :type="showPassword ? 'text' : 'password'" 
              class="form-control border-start-0 border-end-0" 
              v-model="form.password" 
              placeholder="请输入密码"
              required
              autocomplete="current-password"
            >
            <span class="input-group-text bg-light border-start-0 cursor-pointer" @click="showPassword = !showPassword">
              <i :class="showPassword ? 'bi bi-eye-slash' : 'bi bi-eye'" class="text-muted"></i>
            </span>
          </div>
        </div>
        
        <button type="submit" class="btn btn-primary w-100 py-2" :disabled="loading">
          <span v-if="loading" class="spinner-border spinner-border-sm me-2"></span>
          {{ loading ? '登录中...' : '登 录' }}
        </button>
      </form>
      
      <div class="text-center mt-4">
        <span class="text-muted">还没有账户？</span>
        <router-link to="/register" class="text-decoration-none ms-1">立即注册</router-link>
      </div>
      
      <div class="mt-4 p-3 bg-light rounded-3">
        <p class="text-muted mb-2 small"><i class="bi bi-info-circle me-1"></i>测试账号：</p>
        <div class="d-flex gap-3 small">
          <span><strong>管理员：</strong>admin / 123456</span>
          <span><strong>用户：</strong>user / 123456</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, inject } from 'vue'
import { useRouter } from 'vue-router'
import { login } from '../api/user'
import { setToken, setUser } from '../utils/auth'

export default {
  name: 'Login',
  setup() {
    const router = useRouter()
    const showToast = inject('showToast')
    
    const form = ref({
      username: '',
      password: ''
    })
    const loading = ref(false)
    const showPassword = ref(false)
    
    const handleLogin = async () => {
      if (!form.value.username || !form.value.password) {
        showToast('请输入用户名和密码', 'error')
        return
      }
      
      loading.value = true
      try {
        const res = await login(form.value)
        setToken(res.data.token)
        setUser({
          id: res.data.userId,
          username: res.data.username,
          nickname: res.data.nickname,
          role: res.data.role,
          avatar: res.data.avatar
        })
        showToast('登录成功')
        router.push('/')
      } catch (error) {
        // 错误已在拦截器处理
      } finally {
        loading.value = false
      }
    }
    
    return {
      form,
      loading,
      showPassword,
      handleLogin
    }
  }
}
</script>

<style scoped>
.cursor-pointer {
  cursor: pointer;
}

.input-group-text {
  border-radius: 8px 0 0 8px;
}

.input-group .form-control {
  border-radius: 0;
}

.input-group .form-control:last-child,
.input-group .input-group-text:last-child {
  border-radius: 0 8px 8px 0;
}
</style>
