<template>
  <div class="fade-in">
    <!-- 用户头像和基本信息 -->
    <div class="profile-header mb-4">
      <div class="row align-items-center">
        <div class="col-auto">
          <div class="avatar-large">
            {{ user?.nickname?.charAt(0) || user?.username?.charAt(0) || 'U' }}
          </div>
        </div>
        <div class="col">
          <h4 class="mb-1">{{ user?.nickname || user?.username }}</h4>
          <p class="text-muted mb-0">
            <span class="badge" :class="user?.role === 'ADMIN' ? 'bg-danger' : 'bg-primary'">
              {{ user?.role === 'ADMIN' ? '管理员' : '普通用户' }}
            </span>
            <span class="ms-2">注册于 {{ formatDate(user?.createdAt) }}</span>
          </p>
        </div>
      </div>
    </div>
    
    <div class="row g-4">
      <!-- 个人信息卡片 -->
      <div class="col-lg-8">
        <div class="card h-100">
          <div class="card-header">
            <h5 class="mb-0"><i class="bi bi-person-circle me-2"></i>个人信息</h5>
          </div>
          <div class="card-body">
            <form @submit.prevent="updateProfile">
              <div class="row g-3">
                <div class="col-md-6">
                  <label class="form-label">用户名</label>
                  <div class="input-group">
                    <span class="input-group-text"><i class="bi bi-person"></i></span>
                    <input type="text" class="form-control" :value="user?.username" disabled>
                  </div>
                  <small class="text-muted">用户名不可修改</small>
                </div>
                <div class="col-md-6">
                  <label class="form-label">昵称 <span class="text-danger">*</span></label>
                  <div class="input-group">
                    <span class="input-group-text"><i class="bi bi-tag"></i></span>
                    <input type="text" class="form-control" v-model="form.nickname" placeholder="请输入昵称">
                  </div>
                </div>
                <div class="col-md-6">
                  <label class="form-label">邮箱</label>
                  <div class="input-group">
                    <span class="input-group-text"><i class="bi bi-envelope"></i></span>
                    <input type="email" class="form-control" v-model="form.email" placeholder="请输入邮箱">
                  </div>
                </div>
                <div class="col-md-6">
                  <label class="form-label">手机号</label>
                  <div class="input-group">
                    <span class="input-group-text"><i class="bi bi-phone"></i></span>
                    <input type="tel" class="form-control" v-model="form.phone" placeholder="请输入手机号">
                  </div>
                </div>
              </div>
              <div class="mt-4">
                <button type="submit" class="btn btn-primary" :disabled="saving">
                  <span v-if="saving" class="spinner-border spinner-border-sm me-1"></span>
                  <i v-else class="bi bi-check-lg me-1"></i>
                  保存修改
                </button>
              </div>
            </form>
          </div>
        </div>
      </div>
      
      <!-- 修改密码卡片 -->
      <div class="col-lg-4">
        <div class="card h-100">
          <div class="card-header">
            <h5 class="mb-0"><i class="bi bi-shield-lock me-2"></i>修改密码</h5>
          </div>
          <div class="card-body">
            <form @submit.prevent="changePassword">
              <div class="mb-3">
                <label class="form-label">当前密码</label>
                <input type="password" class="form-control" v-model="pwdForm.oldPassword" required placeholder="请输入当前密码">
              </div>
              <div class="mb-3">
                <label class="form-label">新密码</label>
                <input type="password" class="form-control" v-model="pwdForm.newPassword" required placeholder="请输入新密码">
              </div>
              <div class="mb-3">
                <label class="form-label">确认新密码</label>
                <input type="password" class="form-control" v-model="pwdForm.confirmPassword" required placeholder="再次输入新密码">
              </div>
              <button type="submit" class="btn btn-warning w-100" :disabled="changingPwd">
                <span v-if="changingPwd" class="spinner-border spinner-border-sm me-1"></span>
                <i v-else class="bi bi-key me-1"></i>
                修改密码
              </button>
            </form>
          </div>
        </div>
      </div>
    </div>
    
    <!-- 统计信息 -->
    <div class="row g-4 mt-2">
      <div class="col-md-3">
        <div class="stats-card">
          <div class="stats-icon bg-primary"><i class="bi bi-pencil-square"></i></div>
          <div class="stats-info">
            <div class="stats-value">{{ stats.practiceCount || 0 }}</div>
            <div class="stats-label">练习次数</div>
          </div>
        </div>
      </div>
      <div class="col-md-3">
        <div class="stats-card">
          <div class="stats-icon bg-success"><i class="bi bi-file-text"></i></div>
          <div class="stats-info">
            <div class="stats-value">{{ stats.examCount || 0 }}</div>
            <div class="stats-label">考试次数</div>
          </div>
        </div>
      </div>
      <div class="col-md-3">
        <div class="stats-card">
          <div class="stats-icon bg-warning"><i class="bi bi-trophy"></i></div>
          <div class="stats-info">
            <div class="stats-value">{{ stats.avgScore || 0 }}</div>
            <div class="stats-label">平均分</div>
          </div>
        </div>
      </div>
      <div class="col-md-3">
        <div class="stats-card">
          <div class="stats-icon bg-danger"><i class="bi bi-journal-x"></i></div>
          <div class="stats-info">
            <div class="stats-value">{{ stats.wrongCount || 0 }}</div>
            <div class="stats-label">错题数</div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, onMounted, inject } from 'vue'
import { getProfile, updateProfile as updateApi, changePassword as changePwdApi, getUserStats } from '../../api/user'
import { setUser } from '../../utils/auth'

export default {
  name: 'Profile',
  setup() {
    const showToast = inject('showToast')
    const saving = ref(false)
    const changingPwd = ref(false)
    const user = ref(null)
    const stats = ref({})
    const form = ref({ nickname: '', email: '', phone: '' })
    const pwdForm = ref({ oldPassword: '', newPassword: '', confirmPassword: '' })
    
    const formatDate = (dateStr) => {
      if (!dateStr) return '-'
      return new Date(dateStr).toLocaleDateString('zh-CN')
    }
    
    const loadProfile = async () => {
      try {
        const res = await getProfile()
        user.value = res.data
        form.value = { 
          nickname: res.data.nickname || '', 
          email: res.data.email || '', 
          phone: res.data.phone || '' 
        }
      } catch (e) {}
    }
    
    const loadStats = async () => {
      try {
        const res = await getUserStats()
        stats.value = res.data
      } catch (e) {}
    }
    
    const updateProfile = async () => {
      // 邮箱格式验证（可为空，若填写则需验证格式）
      if (form.value.email && !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(form.value.email)) {
        showToast('邮箱格式不正确', 'error')
        return
      }
      
      // 手机号格式验证（可为空，若填写则需验证格式）
      if (form.value.phone && !/^1[3-9]\d{9}$/.test(form.value.phone)) {
        showToast('手机号格式不正确', 'error')
        return
      }
      
      saving.value = true
      try {
        await updateApi(form.value)
        const updatedUser = { ...user.value, ...form.value }
        setUser(updatedUser)
        user.value = updatedUser
        showToast('保存成功')
        // 触发全局刷新用户信息
        window.dispatchEvent(new CustomEvent('user-updated', { detail: updatedUser }))
      } catch (e) {}
      saving.value = false
    }
    
    const changePassword = async () => {
      if (pwdForm.value.newPassword !== pwdForm.value.confirmPassword) {
        showToast('两次密码不一致', 'error')
        return
      }
      if (pwdForm.value.newPassword.length < 6) {
        showToast('密码长度至少6位', 'error')
        return
      }
      changingPwd.value = true
      try {
        await changePwdApi({ 
          oldPassword: pwdForm.value.oldPassword, 
          newPassword: pwdForm.value.newPassword 
        })
        showToast('密码修改成功')
        pwdForm.value = { oldPassword: '', newPassword: '', confirmPassword: '' }
      } catch (e) {
        // 显示后端返回的错误信息
        const errorMsg = e.message || '密码修改失败，请重试'
        showToast(errorMsg, 'error')
      }
      changingPwd.value = false
    }
    
    onMounted(() => {
      loadProfile()
      loadStats()
    })
    
    return { user, stats, form, pwdForm, saving, changingPwd, formatDate, updateProfile, changePassword }
  }
}
</script>

<style scoped>
.profile-header {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 12px;
  padding: 24px;
  color: white;
}

.avatar-large {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.2);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 2rem;
  font-weight: 600;
  color: white;
  border: 3px solid rgba(255, 255, 255, 0.5);
}

.profile-header h4 {
  color: white;
}

.profile-header .text-muted {
  color: rgba(255, 255, 255, 0.8) !important;
}

.stats-card {
  background: white;
  border-radius: 12px;
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 16px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}

.stats-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
}

.stats-icon {
  width: 50px;
  height: 50px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.5rem;
  color: white;
}

.stats-value {
  font-size: 1.5rem;
  font-weight: 700;
  color: #2d3748;
}

.stats-label {
  font-size: 0.85rem;
  color: #718096;
}
</style>
