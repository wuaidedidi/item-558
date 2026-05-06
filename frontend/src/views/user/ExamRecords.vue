<template>
  <div class="fade-in">
    <div class="card">
      <div class="card-header d-flex justify-content-between align-items-center flex-wrap gap-2">
        <h5 class="mb-0">
          <i class="bi bi-clock-history me-2"></i>我的考试记录
          <span class="badge bg-primary ms-2">{{ total }}</span>
        </h5>
        <router-link to="/exams" class="btn btn-primary btn-sm">
          <i class="bi bi-play-fill me-1"></i>参加考试
        </router-link>
      </div>
      <div class="card-body">
        <div v-if="loading" class="loading-spinner py-5">
          <div class="spinner-border text-primary"></div>
        </div>
        
        <div v-else-if="records.length === 0" class="empty-state py-5">
          <i class="bi bi-clipboard-x"></i>
          <h5>暂无考试记录</h5>
          <p class="text-muted">快去参加考试吧！</p>
          <router-link to="/exams" class="btn btn-primary mt-2">
            <i class="bi bi-play-fill me-1"></i>参加考试
          </router-link>
        </div>
        
        <div v-else>
          <!-- 统计摘要 -->
          <div class="row g-3 mb-4">
            <div class="col-sm-6 col-md-3">
              <div class="stats-summary">
                <div class="stats-icon bg-primary">
                  <i class="bi bi-file-earmark-text"></i>
                </div>
                <div class="stats-detail">
                  <div class="stats-value">{{ total }}</div>
                  <div class="stats-label">考试次数</div>
                </div>
              </div>
            </div>
            <div class="col-sm-6 col-md-3">
              <div class="stats-summary">
                <div class="stats-icon bg-success">
                  <i class="bi bi-check-circle"></i>
                </div>
                <div class="stats-detail">
                  <div class="stats-value">{{ passCount }}</div>
                  <div class="stats-label">通过次数</div>
                </div>
              </div>
            </div>
            <div class="col-sm-6 col-md-3">
              <div class="stats-summary">
                <div class="stats-icon bg-warning">
                  <i class="bi bi-trophy"></i>
                </div>
                <div class="stats-detail">
                  <div class="stats-value">{{ highestScore }}</div>
                  <div class="stats-label">最高分</div>
                </div>
              </div>
            </div>
            <div class="col-sm-6 col-md-3">
              <div class="stats-summary">
                <div class="stats-icon bg-info">
                  <i class="bi bi-graph-up"></i>
                </div>
                <div class="stats-detail">
                  <div class="stats-value">{{ avgScore }}</div>
                  <div class="stats-label">平均分</div>
                </div>
              </div>
            </div>
          </div>
          
          <!-- 记录列表 -->
          <div class="exam-record-list">
            <div class="exam-record-item" v-for="record in records" :key="record.id">
              <div class="record-header">
                <h6 class="record-title mb-0">{{ record.exam?.title || '未知试卷' }}</h6>
                <span class="badge" :class="record.score >= (record.exam?.passScore || 60) ? 'bg-success' : 'bg-danger'">
                  {{ record.score >= (record.exam?.passScore || 60) ? '通过' : '未通过' }}
                </span>
              </div>
              <div class="record-body">
                <div class="row g-3 align-items-center">
                  <div class="col-6 col-md-3">
                    <div class="record-score" :class="getScoreClass(record.score, record.exam?.passScore)">
                      {{ record.score }}<small>/{{ record.exam?.totalScore || 100 }}</small>
                    </div>
                    <div class="text-muted small">得分</div>
                  </div>
                  <div class="col-6 col-md-3">
                    <div class="progress" style="height: 8px;">
                      <div class="progress-bar" :class="getProgressClass(record.score, record.exam?.totalScore)"
                           :style="{ width: getProgressWidth(record.score, record.exam?.totalScore) }"></div>
                    </div>
                    <div class="text-muted small mt-1">正确率 {{ getProgressPercent(record.score, record.exam?.totalScore) }}%</div>
                  </div>
                  <div class="col-6 col-md-3">
                    <div class="fw-medium">{{ record.exam?.passScore || 60 }}分</div>
                    <div class="text-muted small">及格线</div>
                  </div>
                  <div class="col-6 col-md-3">
                    <div class="text-muted small">
                      <i class="bi bi-calendar3 me-1"></i>{{ formatDate(record.startTime) }}
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
          
          <!-- 分页 -->
          <nav v-if="total > pageSize" class="mt-4">
            <ul class="pagination justify-content-center mb-0">
              <li class="page-item" :class="{ disabled: page === 1 }">
                <a class="page-link" href="#" @click.prevent="changePage(page - 1)">
                  <i class="bi bi-chevron-left"></i>
                </a>
              </li>
              <li class="page-item" v-for="p in totalPages" :key="p" :class="{ active: p === page }">
                <a class="page-link" href="#" @click.prevent="changePage(p)">{{ p }}</a>
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
  </div>
</template>

<script>
import { ref, computed, onMounted } from 'vue'
import { getExamRecords } from '../../api/exam'

export default {
  name: 'ExamRecords',
  setup() {
    const loading = ref(false)
    const records = ref([])
    const page = ref(1)
    const pageSize = ref(10)
    const total = ref(0)
    
    const totalPages = computed(() => Math.ceil(total.value / pageSize.value))
    
    const passCount = computed(() => 
      records.value.filter(r => r.score >= (r.exam?.passScore || 60)).length
    )
    
    const highestScore = computed(() => 
      records.value.length ? Math.max(...records.value.map(r => r.score)) : 0
    )
    
    const avgScore = computed(() => {
      if (!records.value.length) return 0
      return Math.round(records.value.reduce((sum, r) => sum + r.score, 0) / records.value.length)
    })
    
    const formatDate = (dateStr) => {
      if (!dateStr) return '-'
      return new Date(dateStr).toLocaleString('zh-CN')
    }
    
    const getScoreClass = (score, passScore) => {
      const ps = passScore || 60
      if (score >= ps) return 'text-success'
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
    
    const loadRecords = async () => {
      loading.value = true
      try {
        const res = await getExamRecords({ page: page.value, size: pageSize.value })
        records.value = res.data.records || res.data
        total.value = res.data.total || records.value.length
      } catch (error) {
        console.error('加载考试记录失败', error)
      } finally {
        loading.value = false
      }
    }
    
    const changePage = (p) => {
      if (p >= 1 && p <= totalPages.value) {
        page.value = p
        loadRecords()
      }
    }
    
    onMounted(() => {
      loadRecords()
    })
    
    return {
      loading, records, page, pageSize, total, totalPages,
      passCount, highestScore, avgScore,
      formatDate, getScoreClass, getProgressWidth, getProgressPercent, getProgressClass,
      loadRecords, changePage
    }
  }
}
</script>

<style scoped>
.stats-summary {
  display: flex;
  align-items: center;
  gap: 12px;
  background: white;
  border-radius: 12px;
  padding: 16px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.stats-icon {
  width: 44px;
  height: 44px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 1.2rem;
}

.stats-value {
  font-size: 1.25rem;
  font-weight: 700;
  color: #2d3748;
}

.stats-label {
  font-size: 0.8rem;
  color: #718096;
}

.exam-record-item {
  background: white;
  border: 1px solid #e2e8f0;
  border-radius: 12px;
  padding: 16px 20px;
  margin-bottom: 12px;
  transition: box-shadow 0.2s ease;
}

.exam-record-item:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
}

.record-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
  padding-bottom: 12px;
  border-bottom: 1px solid #f1f5f9;
}

.record-title {
  font-weight: 600;
  color: #2d3748;
}

.record-score {
  font-size: 1.75rem;
  font-weight: 700;
}

.record-score small {
  font-size: 1rem;
  font-weight: 400;
  color: #a0aec0;
}
</style>
