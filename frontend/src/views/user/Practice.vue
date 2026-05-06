<template>
  <div class="fade-in">
    <div class="card">
      <div class="card-header d-flex justify-content-between align-items-center">
        <h5 class="mb-0">选择分类开始练习</h5>
        <router-link to="/practice/records" class="btn btn-outline-primary btn-sm">
          <i class="bi bi-clock-history me-1"></i>练习记录
        </router-link>
      </div>
      <div class="card-body">
        <div v-if="loading" class="loading-spinner">
          <div class="spinner-border text-primary"></div>
        </div>
        
        <div v-else-if="categories.length === 0" class="empty-state">
          <i class="bi bi-folder-x"></i>
          <h5>暂无分类</h5>
          <p>请联系管理员添加题目分类</p>
        </div>
        
        <div v-else class="row g-4">
          <div class="col-md-6 col-lg-4" v-for="category in categories" :key="category.id">
            <div class="card h-100 border hover-shadow" style="cursor: pointer;" 
                 @click="startPractice(category.id)">
              <div class="card-body">
                <div class="d-flex align-items-center mb-3">
                  <div class="stat-icon primary me-3" style="width: 48px; height: 48px; font-size: 1.25rem;">
                    <i class="bi bi-folder2-open"></i>
                  </div>
                  <div>
                    <h5 class="mb-1">{{ category.name }}</h5>
                    <small class="text-muted">{{ category.questionCount || 0 }} 道题目</small>
                  </div>
                </div>
                <p class="text-muted small mb-3">{{ category.description || '暂无描述' }}</p>
                <div class="d-flex justify-content-between align-items-center">
                  <span class="badge bg-light text-dark">
                    <i class="bi bi-question-circle me-1"></i>{{ category.questionCount || 0 }}题
                  </span>
                  <span class="text-primary small">
                    开始练习 <i class="bi bi-arrow-right"></i>
                  </span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
    
    <!-- 练习数量设置模态框 -->
    <div class="modal fade" id="practiceModal" tabindex="-1">
      <div class="modal-dialog modal-dialog-centered">
        <div class="modal-content">
          <div class="modal-header">
            <h5 class="modal-title">练习设置</h5>
            <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
          </div>
          <div class="modal-body">
            <div class="mb-3">
              <label class="form-label">选择题目数量</label>
              <select class="form-select" v-model="questionCount">
                <option v-for="option in questionCountOptions" :key="option" :value="option">
                  {{ option }} 道题
                </option>
              </select>
              <small class="text-muted mt-1 d-block">
                该分类共有 {{ selectedCategoryQuestionCount }} 道题目
              </small>
            </div>
          </div>
          <div class="modal-footer">
            <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">取消</button>
            <button type="button" class="btn btn-primary" @click="confirmStart">开始练习</button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getCategories } from '../../api/exam'
import { Modal } from 'bootstrap'

export default {
  name: 'Practice',
  setup() {
    const router = useRouter()
    const loading = ref(false)
    const categories = ref([])
    const selectedCategoryId = ref(null)
    const questionCount = ref(5)
    let modal = null
    
    // 获取选中分类的题目数量
    const selectedCategoryQuestionCount = computed(() => {
      if (!selectedCategoryId.value) return 0
      const category = categories.value.find(c => c.id === selectedCategoryId.value)
      return category?.questionCount || 0
    })
    
    // 根据题目数量动态生成选项
    const questionCountOptions = computed(() => {
      const total = selectedCategoryQuestionCount.value
      const options = []
      const standardOptions = [5, 10, 20]
      
      // 添加小于等于总数的标准选项
      for (const opt of standardOptions) {
        if (opt <= total) {
          options.push(opt)
        }
      }
      
      // 如果总数不在标准选项中且大于0，添加总数作为选项
      if (total > 0 && !options.includes(total)) {
        options.push(total)
      }
      
      // 如果没有任何选项且有题目，添加总数
      if (options.length === 0 && total > 0) {
        options.push(total)
      }
      
      return options.sort((a, b) => a - b)
    })
    
    const loadCategories = async () => {
      loading.value = true
      try {
        const res = await getCategories()
        categories.value = res.data
      } catch (error) {
        console.error('加载分类失败', error)
      } finally {
        loading.value = false
      }
    }
    
    const startPractice = (categoryId) => {
      selectedCategoryId.value = categoryId
      // 重置选择为第一个可用选项
      if (questionCountOptions.value.length > 0) {
        questionCount.value = questionCountOptions.value[0]
      }
      modal = new Modal(document.getElementById('practiceModal'))
      modal.show()
    }
    
    const confirmStart = () => {
      modal.hide()
      router.push({
        path: `/practice/do/${selectedCategoryId.value}`,
        query: { count: questionCount.value }
      })
    }
    
    onMounted(() => {
      loadCategories()
    })
    
    return {
      loading,
      categories,
      questionCount,
      questionCountOptions,
      selectedCategoryQuestionCount,
      startPractice,
      confirmStart
    }
  }
}
</script>

<style scoped>
.hover-shadow:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  transform: translateY(-2px);
  transition: all 0.3s ease;
}
</style>
