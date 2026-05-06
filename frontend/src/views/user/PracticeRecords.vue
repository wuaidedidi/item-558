<template>
  <div class="fade-in">
    <div class="card">
      <div class="card-header d-flex justify-content-between align-items-center">
        <h5 class="mb-0">练习记录</h5>
        <router-link to="/practice" class="btn btn-primary btn-sm">
          <i class="bi bi-pencil-square me-1"></i>开始练习
        </router-link>
      </div>
      <div class="card-body">
        <div v-if="loading" class="loading-spinner">
          <div class="spinner-border text-primary"></div>
        </div>
        
        <div v-else-if="records.length === 0" class="empty-state">
          <i class="bi bi-clipboard-x"></i>
          <h5>暂无练习记录</h5>
          <p>快去练习吧！</p>
        </div>
        
        <div v-else class="table-responsive">
          <table class="table">
            <thead>
              <tr>
                <th>分类</th>
                <th>题目数量</th>
                <th>正确数量</th>
                <th>正确率</th>
                <th>得分</th>
                <th>练习时间</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="record in records" :key="record.id">
                <td>{{ record.categoryName || '未知分类' }}</td>
                <td>{{ record.totalCount }}</td>
                <td>{{ record.correctCount }}</td>
                <td>
                  <span class="badge" :class="getAccuracyClass(record.correctCount / record.totalCount)">
                    {{ Math.round(record.correctCount / record.totalCount * 100) }}%
                  </span>
                </td>
                <td>{{ record.score }}</td>
                <td>{{ formatDate(record.createdAt) }}</td>
              </tr>
            </tbody>
          </table>
        </div>
        
        <!-- 分页 -->
        <nav v-if="total > pageSize" class="mt-4">
          <ul class="pagination justify-content-center mb-0">
            <li class="page-item" :class="{ disabled: page === 1 }">
              <a class="page-link" href="#" @click.prevent="changePage(page - 1)">上一页</a>
            </li>
            <li class="page-item" v-for="p in totalPages" :key="p" :class="{ active: p === page }">
              <a class="page-link" href="#" @click.prevent="changePage(p)">{{ p }}</a>
            </li>
            <li class="page-item" :class="{ disabled: page === totalPages }">
              <a class="page-link" href="#" @click.prevent="changePage(page + 1)">下一页</a>
            </li>
          </ul>
        </nav>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, computed, onMounted } from 'vue'
import { getPracticeRecords } from '../../api/exam'

export default {
  name: 'PracticeRecords',
  setup() {
    const loading = ref(false)
    const records = ref([])
    const page = ref(1)
    const pageSize = ref(10)
    const total = ref(0)
    
    const totalPages = computed(() => Math.ceil(total.value / pageSize.value))
    
    const getAccuracyClass = (rate) => {
      if (rate >= 0.8) return 'bg-success'
      if (rate >= 0.6) return 'bg-warning'
      return 'bg-danger'
    }
    
    const formatDate = (dateStr) => {
      if (!dateStr) return ''
      const date = new Date(dateStr)
      return date.toLocaleString('zh-CN')
    }
    
    const loadRecords = async () => {
      loading.value = true
      try {
        const res = await getPracticeRecords({ page: page.value, size: pageSize.value })
        records.value = res.data.records
        total.value = res.data.total
      } catch (error) {
        console.error('加载记录失败', error)
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
      loading,
      records,
      page,
      pageSize,
      total,
      totalPages,
      getAccuracyClass,
      formatDate,
      changePage
    }
  }
}
</script>
