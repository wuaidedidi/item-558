<template>
  <div class="auth-container">
    <div class="auth-card fade-in">
      <div class="text-center mb-4">
        <div class="d-inline-flex align-items-center justify-content-center" 
             style="width: 64px; height: 64px; background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); border-radius: 16px; margin-bottom: 1rem;">
          <i class="bi bi-person-plus text-white" style="font-size: 2rem;"></i>
        </div>
        <h1 class="auth-title">创建账户</h1>
        <p class="auth-subtitle">填写信息完成注册</p>
      </div>
      
      <form @submit.prevent="handleRegister">
        <div class="mb-3">
          <label class="form-label">用户名 <span class="text-danger">*</span></label>
          <input 
            type="text" 
            class="form-control" 
            v-model="form.username" 
            placeholder="3-20个字符"
            required
          >
        </div>
        
        <div class="mb-3">
          <label class="form-label">密码 <span class="text-danger">*</span></label>
          <input 
            type="password" 
            class="form-control" 
            v-model="form.password" 
            placeholder="6-20个字符"
            required
          >
        </div>
        
        <div class="mb-3">
          <label class="form-label">确认密码 <span class="text-danger">*</span></label>
          <input 
            type="password" 
            class="form-control" 
            v-model="form.confirmPassword" 
            placeholder="再次输入密码"
            required
          >
        </div>
        
        <div class="mb-3">
          <label class="form-label">昵称</label>
          <input 
            type="text" 
            class="form-control" 
            v-model="form.nickname" 
            placeholder="可选"
          >
        </div>
        
        <div class="mb-3">
          <label class="form-label">邮箱</label>
          <input 
            type="email" 
            class="form-control" 
            v-model="form.email" 
            placeholder="可选"
          >
          <div v-if="emailError" class="text-danger small mt-1">{{ emailError }}</div>
        </div>
        
        <div class="mb-4">
          <label class="form-label">手机号</label>
          <input 
            type="tel" 
            class="form-control" 
            v-model="form.phone" 
            placeholder="可选"
          >
          <div v-if="phoneError" class="text-danger small mt-1">{{ phoneError }}</div>
        </div>
        
        <button type="submit" class="btn btn-primary w-100 py-2" :disabled="loading">
          <span v-if="loading" class="spinner-border spinner-border-sm me-2"></span>
          {{ loading ? '注册中...' : '注 册' }}
        </button>
      </form>
      
      <div class="text-center mt-4">
        <span class="text-muted">已有账户？</span>
        <router-link to="/login" class="text-decoration-none ms-1">立即登录</router-link>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, computed, inject } from 'vue'
import { useRouter } from 'vue-router'
import { register } from '../api/user'

export default {
  name: 'Register',
  setup() {
    const router = useRouter()
    const showToast = inject('showToast')
    
    const form = ref({
      username: '',
      password: '',
      confirmPassword: '',
      nickname: '',
      email: '',
      phone: ''
    })
    const loading = ref(false)
    
    const emailError = computed(() => {
      if (form.value.email && !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(form.value.email)) {
        return '邮箱格式不正确'
      }
      return ''
    })
    
    const phoneError = computed(() => {
      if (form.value.phone && !/^1[3-9]\d{9}$/.test(form.value.phone)) {
        return '手机号格式不正确'
      }
      return ''
    })
    
    const handleRegister = async () => {
      if (form.value.password !== form.value.confirmPassword) {
        showToast('两次输入的密码不一致', 'error')
        return
      }
      
      if (form.value.password.length < 6) {
        showToast('密码长度至少6位', 'error')
        return
      }
      
      if (emailError.value || phoneError.value) {
        showToast('请检查输入格式', 'error')
        return
      }
      
      loading.value = true
      try {
        await register({
          username: form.value.username,
          password: form.value.password,
          nickname: form.value.nickname || undefined,
          email: form.value.email || undefined,
          phone: form.value.phone || undefined
        })
        showToast('注册成功，请登录')
        router.push('/login')
      } catch (error) {
        // 错误已在拦截器处理
      } finally {
        loading.value = false
      }
    }
    
    return {
      form,
      loading,
      emailError,
      phoneError,
      handleRegister
    }
  }
}
</script>
