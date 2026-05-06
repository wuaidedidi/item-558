<template>
  <div class="d-flex">
    <!-- 侧边栏 -->
    <aside class="sidebar">
      <div class="sidebar-brand">
        <div class="sidebar-brand-icon">
          <i class="bi bi-book-half"></i>
        </div>
        <span class="sidebar-brand-text">考试系统</span>
      </div>
      
      <nav class="sidebar-nav">
        <ul class="nav flex-column">
          <li class="nav-item">
            <router-link to="/" class="nav-link" :class="{ active: $route.path === '/' }">
              <i class="bi bi-speedometer2"></i>
              <span>仪表盘</span>
            </router-link>
          </li>
          
          <!-- 用户菜单 -->
          <li class="nav-item">
            <router-link to="/practice" class="nav-link" :class="{ active: $route.path.startsWith('/practice') }">
              <i class="bi bi-pencil-square"></i>
              <span>练习中心</span>
            </router-link>
          </li>
          
          <li class="nav-item">
            <router-link to="/exams" class="nav-link" :class="{ active: $route.path === '/exams' || $route.path.startsWith('/exam/do') }">
              <i class="bi bi-file-earmark-text"></i>
              <span>在线考试</span>
            </router-link>
          </li>
          
          <li class="nav-item">
            <router-link to="/exam/records" class="nav-link" :class="{ active: $route.path === '/exam/records' }">
              <i class="bi bi-clock-history"></i>
              <span>考试记录</span>
            </router-link>
          </li>
          
          <li class="nav-item">
            <router-link to="/wrong-book" class="nav-link" :class="{ active: $route.path === '/wrong-book' }">
              <i class="bi bi-journal-x"></i>
              <span>错题本</span>
            </router-link>
          </li>
          
          <!-- 管理员菜单 -->
          <template v-if="isAdmin">
            <li class="nav-item mt-3">
              <div class="px-3 py-2 text-white-50 small text-uppercase">管理</div>
            </li>
            
            <li class="nav-item">
              <router-link to="/admin/users" class="nav-link" :class="{ active: $route.path === '/admin/users' }">
                <i class="bi bi-people"></i>
                <span>用户管理</span>
              </router-link>
            </li>
            
            <li class="nav-item">
              <router-link to="/admin/categories" class="nav-link" :class="{ active: $route.path === '/admin/categories' }">
                <i class="bi bi-folder"></i>
                <span>分类管理</span>
              </router-link>
            </li>
            
            <li class="nav-item">
              <router-link to="/admin/questions" class="nav-link" :class="{ active: $route.path === '/admin/questions' }">
                <i class="bi bi-question-circle"></i>
                <span>题目管理</span>
              </router-link>
            </li>
            
            <li class="nav-item">
              <router-link to="/admin/exams" class="nav-link" :class="{ active: $route.path === '/admin/exams' }">
                <i class="bi bi-file-ruled"></i>
                <span>试卷管理</span>
              </router-link>
            </li>
            
            <li class="nav-item">
              <router-link to="/admin/exam-records" class="nav-link" :class="{ active: $route.path === '/admin/exam-records' }">
                <i class="bi bi-graph-up"></i>
                <span>成绩管理</span>
              </router-link>
            </li>
          </template>
        </ul>
      </nav>
    </aside>
    
    <!-- 主内容区 -->
    <main class="main-content">
      <!-- 顶部导航 -->
      <header class="top-navbar">
        <h1 class="page-title">{{ pageTitle }}</h1>
        
        <div class="dropdown" ref="dropdownContainer">
          <div class="user-dropdown" @click="toggleDropdown" role="button">
            <div class="user-info">
              <div class="user-name">{{ user?.nickname || user?.username }}</div>
              <div class="user-role">{{ user?.role === 'ADMIN' ? '管理员' : '普通用户' }}</div>
            </div>
            <div class="user-avatar">
              {{ (user?.nickname || user?.username || 'U').charAt(0).toUpperCase() }}
            </div>
          </div>
          <ul class="dropdown-menu dropdown-menu-end" :class="{ show: dropdownVisible }">
            <li>
              <router-link to="/profile" class="dropdown-item" @click="dropdownVisible = false">
                <i class="bi bi-person me-2"></i>个人中心
              </router-link>
            </li>
            <li><hr class="dropdown-divider"></li>
            <li>
              <a href="#" class="dropdown-item text-danger" @click.prevent="handleLogout">
                <i class="bi bi-box-arrow-right me-2"></i>退出登录
              </a>
            </li>
          </ul>
        </div>
      </header>
      
      <!-- 内容区域 -->
      <div class="content-area">
        <router-view />
      </div>
    </main>
  </div>
</template>

<script>
import { computed, ref, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getUser, logout, isAdmin as checkIsAdmin } from '../utils/auth'
import { getProfile } from '../api/user'
import { setUser } from '../utils/auth'

export default {
  name: 'Layout',
  setup() {
    const route = useRoute()
    const router = useRouter()
    const user = ref(getUser())
    const isAdmin = computed(() => checkIsAdmin())
    const dropdownVisible = ref(false)
    const dropdownContainer = ref(null)
    
    const pageTitle = computed(() => {
      const titles = {
        '/': '仪表盘',
        '/practice': '练习中心',
        '/exams': '在线考试',
        '/exam/records': '考试记录',
        '/wrong-book': '错题本',
        '/profile': '个人中心',
        '/admin/users': '用户管理',
        '/admin/categories': '分类管理',
        '/admin/questions': '题目管理',
        '/admin/exams': '试卷管理',
        '/admin/exam-records': '成绩管理'
      }
      return titles[route.path] || '练习题与考试系统'
    })
    
    const toggleDropdown = () => {
      dropdownVisible.value = !dropdownVisible.value
    }
    
    const handleLogout = () => {
      dropdownVisible.value = false
      logout()
      router.push('/login')
    }
    
    // 点击外部关闭dropdown
    const handleClickOutside = (event) => {
      if (dropdownContainer.value && !dropdownContainer.value.contains(event.target)) {
        dropdownVisible.value = false
      }
    }
    
    // 刷新用户信息
    onMounted(async () => {
      // 监听点击事件
      document.addEventListener('click', handleClickOutside)
      
      try {
        const res = await getProfile()
        user.value = res.data
        setUser(res.data)
      } catch (error) {
        // 忽略错误
      }
      
      // 监听用户信息更新事件
      const handleUserUpdate = (e) => {
        user.value = e.detail
      }
      window.addEventListener('user-updated', handleUserUpdate)
    })
    
    onUnmounted(() => {
      document.removeEventListener('click', handleClickOutside)
    })
    
    return {
      user,
      isAdmin,
      pageTitle,
      dropdownVisible,
      dropdownContainer,
      toggleDropdown,
      handleLogout
    }
  }
}
</script>
