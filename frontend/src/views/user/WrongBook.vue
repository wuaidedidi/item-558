<template>
  <div class="fade-in">
    <div class="card">
      <div class="card-header d-flex justify-content-between align-items-center">
        <h5 class="mb-0">
          <i class="bi bi-journal-x me-2"></i>我的错题本 
          <span class="badge bg-danger ms-2">{{ total }}</span>
        </h5>
        <div class="d-flex gap-2">
          <select class="form-select form-select-sm" style="width: 150px;" v-model="categoryFilter" @change="loadWrongQuestions">
            <option value="">全部分类</option>
            <option v-for="cat in categories" :key="cat.id" :value="cat.id">{{ cat.name }}</option>
          </select>
          <button class="btn btn-success btn-sm" @click="startPractice" :disabled="wrongQuestions.length === 0">
            <i class="bi bi-arrow-repeat me-1"></i>错题重练
          </button>
          <button class="btn btn-outline-danger btn-sm" @click="showClearConfirm" :disabled="wrongQuestions.length === 0">
            <i class="bi bi-trash me-1"></i>清空
          </button>
        </div>
      </div>
      <div class="card-body">
        <div v-if="loading" class="loading-spinner py-5"><div class="spinner-border text-primary"></div></div>
        
        <div v-else-if="wrongQuestions.length === 0" class="empty-state py-5">
          <i class="bi bi-emoji-smile"></i>
          <h5>错题本为空</h5>
          <p class="text-muted">太棒了！没有错题记录</p>
        </div>
        
        <div v-else class="wrong-question-list">
          <div class="wrong-question-item" v-for="item in wrongQuestions" :key="item.id">
            <div class="d-flex justify-content-between align-items-start mb-3">
              <div class="d-flex gap-2 flex-wrap">
                <span class="badge bg-light text-dark">{{ item.question?.categoryName || '未分类' }}</span>
                <span class="badge" :class="typeClass[item.question?.type]">{{ typeNames[item.question?.type] }}</span>
                <span class="badge bg-warning text-dark">
                  <i class="bi bi-x-circle me-1"></i>错误 {{ item.wrongCount }} 次
                </span>
              </div>
              <button class="btn btn-sm btn-outline-danger" @click="showDeleteConfirm(item)">
                <i class="bi bi-trash"></i>
              </button>
            </div>
            
            <div class="question-content mb-3">{{ item.question?.content }}</div>
            
            <!-- 选项展示 -->
            <div v-if="item.question?.options?.length" class="options-display mb-3">
              <div v-for="opt in item.question.options" :key="opt.optionLabel" 
                   class="option-display-item" :class="{ correct: opt.isCorrect }">
                <span class="option-label-sm">{{ opt.optionLabel }}</span>
                <span>{{ opt.optionContent }}</span>
                <i v-if="opt.isCorrect" class="bi bi-check-circle-fill text-success ms-2"></i>
              </div>
            </div>
            
            <!-- 答案对比 -->
            <div class="row g-3">
              <div class="col-md-6">
                <div class="answer-box wrong">
                  <div class="answer-label"><i class="bi bi-x-circle me-1"></i>你的答案</div>
                  <div class="answer-content">{{ item.lastAnswer || '未作答' }}</div>
                </div>
              </div>
              <div class="col-md-6">
                <div class="answer-box correct">
                  <div class="answer-label"><i class="bi bi-check-circle me-1"></i>正确答案</div>
                  <div class="answer-content">{{ item.question?.answer }}</div>
                </div>
              </div>
            </div>
            
            <!-- 解析 -->
            <div v-if="item.question?.analysis" class="analysis-box mt-3">
              <div class="analysis-label"><i class="bi bi-lightbulb me-1"></i>题目解析</div>
              <div class="analysis-content">{{ item.question.analysis }}</div>
            </div>
          </div>
        </div>
        
        <!-- 分页 -->
        <nav v-if="totalPages > 1" class="mt-4">
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
    
    <!-- 错题重练模态框 -->
    <div class="modal fade" id="practiceModal" tabindex="-1" data-bs-backdrop="static">
      <div class="modal-dialog modal-lg modal-dialog-centered">
        <div class="modal-content">
          <div class="modal-header">
            <h5 class="modal-title">
              <i class="bi bi-arrow-repeat me-2"></i>错题重练
              <span class="badge bg-primary ms-2">{{ practiceIndex + 1 }} / {{ practiceQuestions.length }}</span>
            </h5>
            <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
          </div>
          <div class="modal-body" v-if="practiceQuestions.length > 0 && !practiceFinished">
            <!-- 进度条 -->
            <div class="progress mb-4" style="height: 6px;">
              <div class="progress-bar" :style="{ width: ((practiceIndex + 1) / practiceQuestions.length * 100) + '%' }"></div>
            </div>
            
            <!-- 题目内容 -->
            <div class="question-content mb-4">{{ currentPracticeQuestion.content }}</div>
            
            <!-- 选择题选项 -->
            <div v-if="currentPracticeQuestion.type === 'SINGLE' || currentPracticeQuestion.type === 'MULTIPLE'" class="question-options">
              <div v-for="opt in currentPracticeQuestion.options" :key="opt.optionLabel"
                   class="option-item" :class="{ selected: isPracticeSelected(opt.optionLabel) }"
                   @click="selectPracticeOption(opt.optionLabel)">
                <span class="option-label">{{ opt.optionLabel }}</span>
                <span class="option-content">{{ opt.optionContent }}</span>
              </div>
            </div>
            
            <!-- 判断题 -->
            <div v-else-if="currentPracticeQuestion.type === 'JUDGE'" class="question-options">
              <div class="option-item" :class="{ selected: practiceAnswers[practiceIndex] === 'TRUE' }" 
                   @click="practiceAnswers[practiceIndex] = 'TRUE'">
                <span class="option-label"><i class="bi bi-check-lg"></i></span>
                <span class="option-content">正确</span>
              </div>
              <div class="option-item" :class="{ selected: practiceAnswers[practiceIndex] === 'FALSE' }" 
                   @click="practiceAnswers[practiceIndex] = 'FALSE'">
                <span class="option-label"><i class="bi bi-x-lg"></i></span>
                <span class="option-content">错误</span>
              </div>
            </div>
            
            <!-- 填空/问答 -->
            <div v-else class="mb-3">
              <input v-if="currentPracticeQuestion.type === 'FILL'" type="text" class="form-control" 
                     v-model="practiceAnswers[practiceIndex]" placeholder="请输入答案（多个答案用逗号分隔）">
              <textarea v-else class="form-control" rows="4" 
                        v-model="practiceAnswers[practiceIndex]" placeholder="请输入答案"></textarea>
            </div>
          </div>
          
          <!-- 完成结果 -->
          <div class="modal-body text-center py-5" v-if="practiceFinished">
            <div class="d-inline-flex align-items-center justify-content-center mb-4" 
                 style="width: 100px; height: 100px; border-radius: 50%; font-size: 2rem;"
                 :style="{ background: practiceResult.correctCount > 0 ? 'linear-gradient(135deg, #48bb78 0%, #38a169 100%)' : 'linear-gradient(135deg, #f56565 0%, #e53e3e 100%)' }">
              <span class="text-white">{{ Math.round(practiceResult.correctCount / practiceQuestions.length * 100) }}%</span>
            </div>
            <h5 class="mb-3">练习完成！</h5>
            <p class="text-muted">
              共 {{ practiceQuestions.length }} 题，答对 {{ practiceResult.correctCount }} 题
              <br>
              <span class="text-success" v-if="practiceResult.correctCount > 0">
                <i class="bi bi-check-circle me-1"></i>{{ practiceResult.correctCount }} 道题已从错题本移除
              </span>
            </p>
          </div>
          
          <div class="modal-footer" v-if="!practiceFinished">
            <button v-if="practiceIndex > 0" class="btn btn-outline-secondary" @click="practiceIndex--">
              <i class="bi bi-chevron-left me-1"></i>上一题
            </button>
            <button v-if="practiceIndex < practiceQuestions.length - 1" class="btn btn-primary" @click="practiceIndex++">
              下一题<i class="bi bi-chevron-right ms-1"></i>
            </button>
            <button v-else class="btn btn-success" @click="submitPractice" :disabled="submitting">
              <span v-if="submitting" class="spinner-border spinner-border-sm me-1"></span>
              <i v-else class="bi bi-check-lg me-1"></i>提交
            </button>
          </div>
          <div class="modal-footer" v-else>
            <button class="btn btn-secondary" data-bs-dismiss="modal">关闭</button>
            <button class="btn btn-primary" @click="startPractice" v-if="wrongQuestions.length > 0">继续练习</button>
          </div>
        </div>
      </div>
    </div>
    
    <!-- 确认对话框 -->
    <ConfirmModal 
      v-model:visible="confirmVisible"
      :title="confirmConfig.title"
      :message="confirmConfig.message"
      :type="confirmConfig.type"
      :confirmText="confirmConfig.confirmText"
      @confirm="confirmConfig.onConfirm"
    />
  </div>
</template>

<script>
import { ref, computed, onMounted, inject } from 'vue'
import { getWrongQuestions, getWrongPracticeQuestions, submitWrongPractice, removeWrongQuestion, clearWrongQuestions, getCategories } from '../../api/exam'
import { Modal } from 'bootstrap'
import ConfirmModal from '../../components/ConfirmModal.vue'

export default {
  name: 'WrongBook',
  components: { ConfirmModal },
  setup() {
    const showToast = inject('showToast')
    const loading = ref(false)
    const submitting = ref(false)
    const wrongQuestions = ref([])
    const categories = ref([])
    const categoryFilter = ref('')
    const page = ref(1)
    const pageSize = ref(10)
    const total = ref(0)
    
    const practiceQuestions = ref([])
    const practiceAnswers = ref([])
    const practiceIndex = ref(0)
    const practiceFinished = ref(false)
    const practiceResult = ref({})
    
    const confirmVisible = ref(false)
    const confirmConfig = ref({})
    
    let practiceModal = null
    
    const typeNames = { SINGLE: '单选题', MULTIPLE: '多选题', JUDGE: '判断题', FILL: '填空题', ESSAY: '问答题' }
    const typeClass = { SINGLE: 'bg-primary', MULTIPLE: 'bg-purple', JUDGE: 'bg-success', FILL: 'bg-warning text-dark', ESSAY: 'bg-info' }
    
    const totalPages = computed(() => Math.ceil(total.value / pageSize.value))
    const currentPracticeQuestion = computed(() => practiceQuestions.value[practiceIndex.value] || {})
    
    const isPracticeSelected = (label) => {
      const answer = practiceAnswers.value[practiceIndex.value] || ''
      if (currentPracticeQuestion.value.type === 'MULTIPLE') {
        return answer.split(',').includes(label)
      }
      return answer === label
    }
    
    const selectPracticeOption = (label) => {
      if (currentPracticeQuestion.value.type === 'MULTIPLE') {
        let selected = (practiceAnswers.value[practiceIndex.value] || '').split(',').filter(Boolean)
        if (selected.includes(label)) {
          selected = selected.filter(l => l !== label)
        } else {
          selected.push(label)
        }
        selected.sort()
        practiceAnswers.value[practiceIndex.value] = selected.join(',')
      } else {
        practiceAnswers.value[practiceIndex.value] = label
      }
    }
    
    const loadCategories = async () => {
      try {
        const res = await getCategories()
        categories.value = res.data
      } catch (e) {}
    }
    
    const loadWrongQuestions = async () => {
      loading.value = true
      try {
        const params = { page: page.value, size: pageSize.value }
        if (categoryFilter.value) params.categoryId = categoryFilter.value
        const res = await getWrongQuestions(params)
        wrongQuestions.value = res.data.records
        total.value = res.data.total
      } catch (e) {}
      loading.value = false
    }
    
    const showDeleteConfirm = (item) => {
      confirmConfig.value = {
        title: '移除错题',
        message: '确定要将这道题从错题本中移除吗？',
        type: 'warning',
        confirmText: '移除',
        onConfirm: () => removeWrong(item.questionId)
      }
      confirmVisible.value = true
    }
    
    const showClearConfirm = () => {
      confirmConfig.value = {
        title: '清空错题本',
        message: `确定要清空错题本中的全部 ${total.value} 道错题吗？此操作不可恢复！`,
        type: 'danger',
        confirmText: '清空',
        onConfirm: handleClear
      }
      confirmVisible.value = true
    }
    
    const removeWrong = async (questionId) => {
      try {
        await removeWrongQuestion(questionId)
        showToast('已移除')
        loadWrongQuestions()
      } catch (e) {}
    }
    
    const handleClear = async () => {
      try {
        await clearWrongQuestions()
        showToast('已清空错题本')
        loadWrongQuestions()
      } catch (e) {}
    }
    
    const startPractice = async () => {
      try {
        const res = await getWrongPracticeQuestions(10)
        if (res.data.length === 0) {
          showToast('暂无错题可练习', 'warning')
          return
        }
        practiceQuestions.value = res.data
        practiceAnswers.value = new Array(res.data.length).fill('')
        practiceIndex.value = 0
        practiceFinished.value = false
        practiceResult.value = {}
        practiceModal = new Modal(document.getElementById('practiceModal'))
        practiceModal.show()
      } catch (e) {}
    }
    
    const submitPractice = async () => {
      submitting.value = true
      const answers = practiceQuestions.value.map((q, i) => ({
        questionId: q.id,
        answer: practiceAnswers.value[i] || ''
      }))
      
      try {
        const res = await submitWrongPractice(answers)
        practiceResult.value = res.data
        practiceFinished.value = true
        loadWrongQuestions()
      } catch (e) {}
      submitting.value = false
    }
    
    const changePage = (p) => {
      if (p >= 1 && p <= totalPages.value) {
        page.value = p
        loadWrongQuestions()
      }
    }
    
    onMounted(() => {
      loadCategories()
      loadWrongQuestions()
    })
    
    return {
      loading, submitting, wrongQuestions, categories, categoryFilter, page, total, totalPages,
      typeNames, typeClass, confirmVisible, confirmConfig,
      practiceQuestions, practiceAnswers, practiceIndex, practiceFinished, practiceResult,
      currentPracticeQuestion, isPracticeSelected, selectPracticeOption,
      loadWrongQuestions, showDeleteConfirm, showClearConfirm, startPractice, submitPractice, changePage
    }
  }
}
</script>

<style scoped>
.bg-purple { background-color: #805ad5 !important; color: white; }

.wrong-question-item {
  background: white;
  border: 1px solid #e2e8f0;
  border-radius: 12px;
  padding: 20px;
  margin-bottom: 16px;
  transition: box-shadow 0.2s ease;
}

.wrong-question-item:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
}

.option-display-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 8px 12px;
  background: #f8fafc;
  border-radius: 6px;
  margin-bottom: 6px;
}

.option-display-item.correct {
  background: #f0fff4;
  border: 1px solid #48bb78;
}

.option-label-sm {
  width: 24px;
  height: 24px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  background: #667eea;
  color: white;
  border-radius: 50%;
  font-size: 0.75rem;
  font-weight: 600;
}

.answer-box {
  padding: 12px;
  border-radius: 8px;
}

.answer-box.wrong {
  background: #fff5f5;
  border: 1px solid #fed7d7;
}

.answer-box.correct {
  background: #f0fff4;
  border: 1px solid #c6f6d5;
}

.answer-label {
  font-size: 0.75rem;
  font-weight: 600;
  margin-bottom: 4px;
}

.answer-box.wrong .answer-label { color: #e53e3e; }
.answer-box.correct .answer-label { color: #38a169; }

.answer-content {
  font-size: 0.95rem;
  color: #2d3748;
}

.analysis-box {
  background: #ebf8ff;
  border-radius: 8px;
  padding: 12px;
}

.analysis-label {
  font-size: 0.75rem;
  font-weight: 600;
  color: #3182ce;
  margin-bottom: 4px;
}

.analysis-content {
  font-size: 0.9rem;
  color: #2d3748;
}
</style>
