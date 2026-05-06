<template>
  <div class="fade-in">
    <!-- 统计卡片 -->
    <div class="row g-4 mb-4">
      <div class="col-md-6 col-lg-3" v-if="isAdmin">
        <div class="stat-card">
          <div class="stat-icon primary">
            <i class="bi bi-people"></i>
          </div>
          <div class="stat-info">
            <h3>{{ stats.userCount || 0 }}</h3>
            <p>用户总数</p>
          </div>
        </div>
      </div>
      
      <div class="col-md-6 col-lg-3" v-if="isAdmin">
        <div class="stat-card">
          <div class="stat-icon success">
            <i class="bi bi-question-circle"></i>
          </div>
          <div class="stat-info">
            <h3>{{ stats.questionCount || 0 }}</h3>
            <p>题目总数</p>
          </div>
        </div>
      </div>
      
      <div class="col-md-6 col-lg-3" v-if="isAdmin">
        <div class="stat-card">
          <div class="stat-icon warning">
            <i class="bi bi-file-earmark-text"></i>
          </div>
          <div class="stat-info">
            <h3>{{ stats.examCount || 0 }}</h3>
            <p>试卷总数</p>
          </div>
        </div>
      </div>
      
      <div class="col-md-6 col-lg-3" v-if="isAdmin">
        <div class="stat-card">
          <div class="stat-icon info">
            <i class="bi bi-graph-up"></i>
          </div>
          <div class="stat-info">
            <h3>{{ stats.examRecordCount || 0 }}</h3>
            <p>考试记录</p>
          </div>
        </div>
      </div>
      
      <!-- 用户统计 -->
      <div class="col-md-6 col-lg-3" v-if="!isAdmin">
        <div class="stat-card">
          <div class="stat-icon primary">
            <i class="bi bi-pencil-square"></i>
          </div>
          <div class="stat-info">
            <h3>{{ stats.practiceCount || 0 }}</h3>
            <p>练习次数</p>
          </div>
        </div>
      </div>
      
      <div class="col-md-6 col-lg-3" v-if="!isAdmin">
        <div class="stat-card">
          <div class="stat-icon success">
            <i class="bi bi-file-earmark-check"></i>
          </div>
          <div class="stat-info">
            <h3>{{ stats.examCount || 0 }}</h3>
            <p>考试次数</p>
          </div>
        </div>
      </div>
      
      <div class="col-md-6 col-lg-3" v-if="!isAdmin">
        <div class="stat-card">
          <div class="stat-icon danger">
            <i class="bi bi-journal-x"></i>
          </div>
          <div class="stat-info">
            <h3>{{ stats.wrongCount || 0 }}</h3>
            <p>错题数量</p>
          </div>
        </div>
      </div>
      
      <div class="col-md-6 col-lg-3" v-if="!isAdmin">
        <div class="stat-card">
          <div class="stat-icon warning">
            <i class="bi bi-calendar-check"></i>
          </div>
          <div class="stat-info">
            <h3>{{ stats.availableExamCount || 0 }}</h3>
            <p>可参加考试</p>
          </div>
        </div>
      </div>
    </div>
    
    <!-- 快捷操作 -->
    <div class="row g-4">
      <div class="col-12">
        <div class="card">
          <div class="card-header d-flex justify-content-between align-items-center">
            <h5 class="mb-0">
              <i class="bi bi-lightning me-2"></i>快捷操作
            </h5>
            <div class="d-flex gap-3 text-muted small">
              <span><i class="bi bi-info-circle me-1"></i>练习题与考试系统 v1.0.0</span>
              <span><i class="bi bi-grid me-1"></i>5种题型</span>
              <span><i class="bi bi-folder me-1"></i>{{ stats.categoryCount || 0 }} 个分类</span>
            </div>
          </div>
          <div class="card-body">
            <div class="row g-3">
              <div class="col-sm-6 col-md-4 col-lg-3 col-xl-2" v-if="!isAdmin">
                <router-link to="/practice" class="text-decoration-none">
                  <div class="p-4 rounded-3 text-center quick-action-card" style="background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);">
                    <i class="bi bi-pencil-square text-white" style="font-size: 2.5rem;"></i>
                    <h6 class="text-white mt-3 mb-0">开始练习</h6>
                  </div>
                </router-link>
              </div>
              
              <div class="col-sm-6 col-md-4 col-lg-3 col-xl-2" v-if="!isAdmin">
                <router-link to="/exams" class="text-decoration-none">
                  <div class="p-4 rounded-3 text-center quick-action-card" style="background: linear-gradient(135deg, #48bb78 0%, #38a169 100%);">
                    <i class="bi bi-file-earmark-text text-white" style="font-size: 2.5rem;"></i>
                    <h6 class="text-white mt-3 mb-0">参加考试</h6>
                  </div>
                </router-link>
              </div>
              
              <div class="col-sm-6 col-md-4 col-lg-3 col-xl-2" v-if="!isAdmin">
                <router-link to="/wrong-book" class="text-decoration-none">
                  <div class="p-4 rounded-3 text-center quick-action-card" style="background: linear-gradient(135deg, #f56565 0%, #e53e3e 100%);">
                    <i class="bi bi-journal-x text-white" style="font-size: 2.5rem;"></i>
                    <h6 class="text-white mt-3 mb-0">错题练习</h6>
                  </div>
                </router-link>
              </div>
              
              <div class="col-sm-6 col-md-4 col-lg-3 col-xl-2" v-if="!isAdmin">
                <router-link to="/exam/records" class="text-decoration-none">
                  <div class="p-4 rounded-3 text-center quick-action-card" style="background: linear-gradient(135deg, #ed8936 0%, #dd6b20 100%);">
                    <i class="bi bi-clock-history text-white" style="font-size: 2.5rem;"></i>
                    <h6 class="text-white mt-3 mb-0">考试记录</h6>
                  </div>
                </router-link>
              </div>
              
              <div class="col-sm-6 col-md-4 col-lg-3 col-xl-2" v-if="!isAdmin">
                <router-link to="/profile" class="text-decoration-none">
                  <div class="p-4 rounded-3 text-center quick-action-card" style="background: linear-gradient(135deg, #4299e1 0%, #3182ce 100%);">
                    <i class="bi bi-person-circle text-white" style="font-size: 2.5rem;"></i>
                    <h6 class="text-white mt-3 mb-0">个人中心</h6>
                  </div>
                </router-link>
              </div>
              
              <div class="col-sm-6 col-md-4 col-lg-3 col-xl-2" v-if="isAdmin">
                <router-link to="/admin/questions" class="text-decoration-none">
                  <div class="p-4 rounded-3 text-center quick-action-card" style="background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);">
                    <i class="bi bi-plus-circle text-white" style="font-size: 2.5rem;"></i>
                    <h6 class="text-white mt-3 mb-0">添加题目</h6>
                  </div>
                </router-link>
              </div>
              
              <div class="col-sm-6 col-md-4 col-lg-3 col-xl-2" v-if="isAdmin">
                <router-link to="/admin/exams" class="text-decoration-none">
                  <div class="p-4 rounded-3 text-center quick-action-card" style="background: linear-gradient(135deg, #48bb78 0%, #38a169 100%);">
                    <i class="bi bi-file-ruled text-white" style="font-size: 2.5rem;"></i>
                    <h6 class="text-white mt-3 mb-0">创建试卷</h6>
                  </div>
                </router-link>
              </div>
              
              <div class="col-sm-6 col-md-4 col-lg-3 col-xl-2" v-if="isAdmin">
                <router-link to="/admin/exam-records" class="text-decoration-none">
                  <div class="p-4 rounded-3 text-center quick-action-card" style="background: linear-gradient(135deg, #ed8936 0%, #dd6b20 100%);">
                    <i class="bi bi-graph-up text-white" style="font-size: 2.5rem;"></i>
                    <h6 class="text-white mt-3 mb-0">查看成绩</h6>
                  </div>
                </router-link>
              </div>
              
              <div class="col-sm-6 col-md-4 col-lg-3 col-xl-2" v-if="isAdmin">
                <router-link to="/admin/users" class="text-decoration-none">
                  <div class="p-4 rounded-3 text-center quick-action-card" style="background: linear-gradient(135deg, #4299e1 0%, #3182ce 100%);">
                    <i class="bi bi-people text-white" style="font-size: 2.5rem;"></i>
                    <h6 class="text-white mt-3 mb-0">用户管理</h6>
                  </div>
                </router-link>
              </div>
              
              <div class="col-sm-6 col-md-4 col-lg-3 col-xl-2" v-if="isAdmin">
                <router-link to="/admin/categories" class="text-decoration-none">
                  <div class="p-4 rounded-3 text-center quick-action-card" style="background: linear-gradient(135deg, #805ad5 0%, #6b46c1 100%);">
                    <i class="bi bi-folder text-white" style="font-size: 2.5rem;"></i>
                    <h6 class="text-white mt-3 mb-0">分类管理</h6>
                  </div>
                </router-link>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, onMounted, computed } from 'vue'
import { isAdmin as checkIsAdmin } from '../utils/auth'
import { getAdminStats } from '../api/admin'
import { getUserStats } from '../api/user'

export default {
  name: 'Dashboard',
  setup() {
    const stats = ref({})
    const isAdmin = computed(() => checkIsAdmin())
    
    const loadStats = async () => {
      try {
        if (isAdmin.value) {
          const res = await getAdminStats()
          stats.value = res.data
        } else {
          const res = await getUserStats()
          stats.value = res.data
        }
      } catch (error) {
        console.error('加载统计失败', error)
      }
    }
    
    onMounted(() => {
      loadStats()
    })
    
    return {
      stats,
      isAdmin
    }
  }
}
</script>
