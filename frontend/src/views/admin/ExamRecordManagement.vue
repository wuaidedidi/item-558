<template>
  <div class="fade-in">
    <!-- 统计概览 -->
    <div class="row g-4 mb-4">
      <div class="col-sm-6 col-lg-3">
        <div class="stat-card">
          <div class="stat-icon primary">
            <i class="bi bi-people"></i>
          </div>
          <div class="stat-info">
            <h3>{{ stats.totalRecords || 0 }}</h3>
            <p>考试记录总数</p>
          </div>
        </div>
      </div>
      <div class="col-sm-6 col-lg-3">
        <div class="stat-card">
          <div class="stat-icon success">
            <i class="bi bi-check-circle"></i>
          </div>
          <div class="stat-info">
            <h3>{{ stats.passCount || 0 }}</h3>
            <p>通过人数</p>
          </div>
        </div>
      </div>
      <div class="col-sm-6 col-lg-3">
        <div class="stat-card">
          <div class="stat-icon danger">
            <i class="bi bi-x-circle"></i>
          </div>
          <div class="stat-info">
            <h3>{{ stats.failCount || 0 }}</h3>
            <p>未通过人数</p>
          </div>
        </div>
      </div>
      <div class="col-sm-6 col-lg-3">
        <div class="stat-card">
          <div class="stat-icon warning">
            <i class="bi bi-graph-up"></i>
          </div>
          <div class="stat-info">
            <h3>{{ stats.avgScore || 0 }}<small class="text-muted">分</small></h3>
            <p>平均分数</p>
          </div>
        </div>
      </div>
    </div>
    
    <div class="card">
      <div class="card-header d-flex justify-content-between align-items-center flex-wrap gap-2">
        <h5 class="mb-0">
          <i class="bi bi-graph-up-arrow me-2"></i>考试成绩管理
        </h5>
        <div class="d-flex gap-2 flex-wrap">
          <select class="form-select form-select-sm" style="width: 150px;" v-model="filters.examId" @change="loadRecords">
            <option value="">全部试卷</option>
            <option v-for="exam in exams" :key="exam.id" :value="exam.id">{{ exam.title }}</option>
          </select>
          <select class="form-select form-select-sm" style="width: 120px;" v-model="filters.status" @change="loadRecords">
            <option value="">全部状态</option>
            <option value="pass">已通过</option>
            <option value="fail">未通过</option>
          </select>
          <div class="input-group input-group-sm" style="width: 200px;">
            <input type="text" class="form-control" v-model="filters.keyword" placeholder="搜索用户名..." @keyup.enter="loadRecords">
            <button class="btn btn-outline-primary" @click="loadRecords">
              <i class="bi bi-search"></i>
            </button>
          </div>
        </div>
      </div>
      <div class="card-body">
        <div v-if="loading" class="loading-spinner py-5">
          <div class="spinner-border text-primary"></div>
        </div>
        
        <div v-else-if="records.length === 0" class="empty-state py-5">
          <i class="bi bi-clipboard-x"></i>
          <h5>暂无考试记录</h5>
          <p class="text-muted">还没有学生参加考试</p>
        </div>
        
        <div v-else class="table-responsive">
          <table class="table table-hover align-middle mb-0">
            <thead>
              <tr>
                <th class="ps-3" style="width: 60px;">ID</th>
                <th style="width: 120px;">用户</th>
                <th>试卷名称</th>
                <th style="width: 100px;">得分</th>
                <th style="width: 80px;">总分</th>
                <th style="width: 80px;">及格分</th>
                <th style="width: 100px;">状态</th>
                <th style="width: 100px;">正确率</th>
                <th style="width: 160px;">考试时间</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="r in records" :key="r.id">
                <td class="ps-3">{{ r.id }}</td>
                <td>
                  <div class="d-flex align-items-center gap-2">
                    <div class="user-avatar-sm">{{ (r.username || 'U').charAt(0).toUpperCase() }}</div>
                    <span>{{ r.username }}</span>
                  </div>
                </td>
                <td>{{ r.exam?.title || '-' }}</td>
                <td>
                  <span class="fw-bold fs-5" :class="getScoreClass(r.score, r.exam?.passScore)">
                    {{ r.score }}
                  </span>
                </td>
                <td>{{ r.exam?.totalScore || '-' }}</td>
                <td>{{ r.exam?.passScore || '-' }}</td>
                <td>
                  <span class="badge" :class="r.score >= (r.exam?.passScore || 60) ? 'bg-success' : 'bg-danger'">
                    <i :class="r.score >= (r.exam?.passScore || 60) ? 'bi bi-check-circle me-1' : 'bi bi-x-circle me-1'"></i>
                    {{ r.score >= (r.exam?.passScore || 60) ? '通过' : '未通过' }}
                  </span>
                </td>
                <td>
                  <div class="progress" style="height: 6px; width: 60px;">
                    <div class="progress-bar" :class="getProgressClass(r.score, r.exam?.totalScore)" 
                         :style="{ width: getProgressWidth(r.score, r.exam?.totalScore) }"></div>
                  </div>
                  <small class="text-muted">{{ getProgressPercent(r.score, r.exam?.totalScore) }}%</small>
                </td>
                <td>{{ formatDate(r.startTime) }}</td>
              </tr>
            </tbody>
          </table>
        </div>
        
        <!-- 分页 -->
        <nav v-if="total > pageSize" class="mt-4">
          <ul class="pagination justify-content-center mb-0">
            <li class="page-item" :class="{ disabled: page === 1 }">
              <a class="page-link" href="#" @click.prevent="changePage(page - 1)">
                <i class="bi bi-chevron-left"></i>
              </a>
            </li>
            <li class="page-item" v-for="p in displayPages" :key="p" :class="{ active: p === page, disabled: p === '...' }">
              <a v-if="p !== '...'" class="page-link" href="#" @click.prevent="changePage(p)">{{ p }}</a>
              <span v-else class="page-link">...</span>
            </li>
            <li class="page-item" :class="{ disabled: page === totalPages }">
              <a class="page-link" href="#" @click.prevent="changePage(page + 1)">
                <i class="bi bi-chevron-right"></i>
              </a>
            </li>
          </ul>
        </nav>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, computed, onMounted } from 'vue'
import { getExamRecordList, getExamList } from '../../api/admin'

export default {
  name: 'ExamRecordManagement',
  setup() {
    const loading = ref(false)
    const records = ref([])
    const exams = ref([])
    const page = ref(1)
    const pageSize = ref(10)
    const total = ref(0)
    const filters = ref({ examId: '', status: '', keyword: '' })
    const stats = ref({ totalRecords: 0, passCount: 0, failCount: 0, avgScore: 0 })
    
    const totalPages = computed(() => Math.ceil(total.value / pageSize.value))
    
    const displayPages = computed(() => {
      const pages = []
      const tp = totalPages.value
      const cp = page.value
      if (tp <= 7) {
        for (let i = 1; i <= tp; i++) pages.push(i)
      } else {
        if (cp <= 3) {
          for (let i = 1; i <= 5; i++) pages.push(i)
          pages.push('...', tp)
        } else if (cp >= tp - 2) {
          pages.push(1, '...')
          for (let i = tp - 4; i <= tp; i++) pages.push(i)
        } else {
          pages.push(1, '...')
          for (let i = cp - 1; i <= cp + 1; i++) pages.push(i)
          pages.push('...', tp)
        }
      }
      return pages
    })
    
    const formatDate = (dateStr) => dateStr ? new Date(dateStr).toLocaleString('zh-CN') : '-'
    
    const getScoreClass = (score, passScore) => {
      const ps = passScore || 60
      if (score >= ps) return 'text-success'
      if (score >= ps * 0.8) return 'text-warning'
      return 'text-danger'
    }
    
    const getProgressWidth = (score, totalScore) => {
      if (!totalScore) return '0%'
      return Math.min(100, Math.round(score / totalScore * 100)) + '%'
    }
    
    const getProgressPercent = (score, totalScore) => {
      if (!totalScore) return 0
      return Math.round(score / totalScore * 100)
    }
    
    const getProgressClass = (score, totalScore) => {
      if (!totalScore) return 'bg-secondary'
      const percent = score / totalScore * 100
      if (percent >= 80) return 'bg-success'
      if (percent >= 60) return 'bg-warning'
      return 'bg-danger'
    }
    
    const loadExams = async () => {
      try {
        const res = await getExamList({ page: 1, size: 100 })
        exams.value = res.data.records || res.data
      } catch (e) {}
    }
    
    const loadRecords = async () => {
      loading.value = true
      try {
        const params = { page: page.value, size: pageSize.value }
        if (filters.value.examId) params.examId = filters.value.examId
        if (filters.value.keyword) params.keyword = filters.value.keyword
        
        const res = await getExamRecordList(params)
        let allRecords = res.data.records || res.data
        total.value = res.data.total || allRecords.length
        
        // 客户端筛选状态
        if (filters.value.status) {
          allRecords = allRecords.filter(r => {
            const passed = r.score >= (r.exam?.passScore || 60)
            return filters.value.status === 'pass' ? passed : !passed
          })
        }
        
        records.value = allRecords
        
        // 计算统计数据
        const allData = res.data.records || res.data
        stats.value.totalRecords = allData.length
        stats.value.passCount = allData.filter(r => r.score >= (r.exam?.passScore || 60)).length
        stats.value.failCount = allData.length - stats.value.passCount
        stats.value.avgScore = allData.length ? Math.round(allData.reduce((sum, r) => sum + r.score, 0) / allData.length) : 0
      } catch (e) {}
      loading.value = false
    }
    
    const changePage = (p) => {
      if (p >= 1 && p <= totalPages.value) {
        page.value = p
        loadRecords()
      }
    }
    
    onMounted(() => {
      loadExams()
      loadRecords()
    })
    
    return { 
      loading, records, exams, page, pageSize, total, totalPages, displayPages, filters, stats,
      formatDate, getScoreClass, getProgressWidth, getProgressPercent, getProgressClass, loadRecords, changePage 
    }
  }
}
</script>

<style scoped>
.user-avatar-sm {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-weight: 600;
  font-size: 0.875rem;
}
</style>
