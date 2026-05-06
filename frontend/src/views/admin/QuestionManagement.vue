<template>
  <div class="fade-in">
    <div class="card">
      <div class="card-header d-flex justify-content-between align-items-center">
        <h5 class="mb-0">题目管理</h5>
        <button class="btn btn-primary" @click="showModal()">
          <i class="bi bi-plus-lg me-1"></i>添加题目
        </button>
      </div>
      <div class="card-body">
        <!-- 筛选区域 -->
        <div class="row g-3 mb-4">
          <div class="col-md-3">
            <select class="form-select" v-model="filters.categoryId" @change="loadQuestions">
              <option value="">全部分类</option>
              <option v-for="c in categories" :key="c.id" :value="c.id">{{ c.name }}</option>
            </select>
          </div>
          <div class="col-md-3">
            <select class="form-select" v-model="filters.type" @change="loadQuestions">
              <option value="">全部题型</option>
              <option v-for="(name, key) in typeNames" :key="key" :value="key">{{ name }}</option>
            </select>
          </div>
          <div class="col-md-3">
            <select class="form-select" v-model="filters.difficulty" @change="loadQuestions">
              <option value="">全部难度</option>
              <option :value="1">简单</option>
              <option :value="2">中等</option>
              <option :value="3">困难</option>
            </select>
          </div>
          <div class="col-md-3">
            <div class="input-group">
              <span class="input-group-text bg-white"><i class="bi bi-search"></i></span>
              <input type="text" class="form-control border-start-0" v-model="filters.keyword" 
                     @keyup.enter="loadQuestions" placeholder="搜索题目内容...">
            </div>
          </div>
        </div>
        
        <!-- 表格 -->
        <div v-if="loading" class="loading-spinner py-5"><div class="spinner-border text-primary"></div></div>
        <div v-else-if="questions.length === 0" class="empty-state py-5">
          <i class="bi bi-question-circle"></i>
          <h5>暂无题目</h5>
          <p class="text-muted">点击上方按钮添加题目</p>
        </div>
        <div v-else class="table-responsive">
          <table class="table table-hover mb-0">
            <thead>
              <tr>
                <th class="ps-3" style="width: 60px;">ID</th>
                <th style="width: 100px;">分类</th>
                <th style="width: 80px;">题型</th>
                <th>题目内容</th>
                <th style="width: 80px;">难度</th>
                <th style="width: 60px;">分值</th>
                <th style="width: 140px;">操作</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="q in questions" :key="q.id">
                <td class="ps-3">{{ q.id }}</td>
                <td><span class="badge bg-light text-dark">{{ q.categoryName }}</span></td>
                <td><span class="badge" :class="typeClass[q.type]">{{ typeNames[q.type] }}</span></td>
                <td class="text-truncate" style="max-width: 300px;">{{ q.content }}</td>
                <td>
                  <span class="badge" :class="difficultyClass[q.difficulty]">
                    {{ difficultyNames[q.difficulty] }}
                  </span>
                </td>
                <td><span class="fw-medium">{{ q.score }}</span></td>
                <td>
                  <div class="btn-group btn-group-sm">
                    <button class="btn btn-outline-primary" @click="showModal(q)" title="编辑">
                      <i class="bi bi-pencil"></i>
                    </button>
                    <button class="btn btn-outline-info" @click="showPreview(q)" title="预览">
                      <i class="bi bi-eye"></i>
                    </button>
                    <button class="btn btn-outline-danger" @click="showDeleteConfirm(q)" title="删除">
                      <i class="bi bi-trash"></i>
                    </button>
                  </div>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>
    
    <!-- 添加/编辑题目模态框 -->
    <div class="modal fade" id="questionModal" tabindex="-1" data-bs-backdrop="static">
      <div class="modal-dialog modal-lg modal-dialog-centered modal-dialog-scrollable">
        <div class="modal-content">
          <div class="modal-header">
            <h5 class="modal-title">
              <i :class="editId ? 'bi bi-pencil' : 'bi bi-plus-circle'" class="me-2"></i>
              {{ editId ? '编辑题目' : '添加题目' }}
            </h5>
            <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
          </div>
          <div class="modal-body">
            <!-- 基本信息 -->
            <div class="row g-3 mb-4">
              <div class="col-md-4">
                <label class="form-label">题目分类 <span class="text-danger">*</span></label>
                <select class="form-select" v-model="form.categoryId">
                  <option value="">请选择分类</option>
                  <option v-for="c in categories" :key="c.id" :value="c.id">{{ c.name }}</option>
                </select>
              </div>
              <div class="col-md-4">
                <label class="form-label">题目类型 <span class="text-danger">*</span></label>
                <select class="form-select" v-model="form.type" @change="onTypeChange">
                  <option v-for="(name, key) in typeNames" :key="key" :value="key">{{ name }}</option>
                </select>
              </div>
              <div class="col-md-2">
                <label class="form-label">难度</label>
                <select class="form-select" v-model="form.difficulty">
                  <option :value="1">简单</option>
                  <option :value="2">中等</option>
                  <option :value="3">困难</option>
                </select>
              </div>
              <div class="col-md-2">
                <label class="form-label">分值</label>
                <input type="number" class="form-control" v-model.number="form.score" min="1" max="100">
              </div>
            </div>
            
            <!-- 题目内容 -->
            <div class="mb-4">
              <label class="form-label">题目内容 <span class="text-danger">*</span></label>
              <textarea class="form-control" v-model="form.content" rows="3" 
                        placeholder="请输入题目内容"></textarea>
            </div>
            
            <!-- 选择题选项 -->
            <div v-if="form.type === 'SINGLE' || form.type === 'MULTIPLE'" class="mb-4">
              <div class="d-flex justify-content-between align-items-center mb-2">
                <label class="form-label mb-0">
                  选项设置 <span class="text-danger">*</span>
                  <small class="text-muted ms-2">
                    ({{ form.type === 'SINGLE' ? '单选题请勾选1个正确答案' : '多选题请勾选至少2个正确答案' }})
                  </small>
                </label>
                <button type="button" class="btn btn-outline-primary btn-sm" @click="addOption" 
                        :disabled="form.options.length >= 8">
                  <i class="bi bi-plus me-1"></i>添加选项
                </button>
              </div>
              <div class="option-list">
                <div v-for="(opt, idx) in form.options" :key="idx" 
                     class="option-item d-flex align-items-center gap-2 mb-2">
                  <span class="option-label-badge">{{ opt.optionLabel }}</span>
                  <input type="text" class="form-control" v-model="opt.optionContent" 
                         :placeholder="`请输入选项${opt.optionLabel}的内容`">
                  <div class="form-check">
                    <input type="checkbox" class="form-check-input" :id="'opt'+idx" 
                           v-model="opt.isCorrect" @change="onCorrectChange(idx)">
                    <label class="form-check-label text-nowrap" :for="'opt'+idx">正确</label>
                  </div>
                  <button type="button" class="btn btn-outline-danger btn-sm" 
                          @click="removeOption(idx)" :disabled="form.options.length <= 2">
                    <i class="bi bi-x-lg"></i>
                  </button>
                </div>
              </div>
            </div>
            
            <!-- 判断题答案 -->
            <div v-if="form.type === 'JUDGE'" class="mb-4">
              <label class="form-label">正确答案 <span class="text-danger">*</span></label>
              <div class="d-flex gap-4">
                <div class="form-check">
                  <input type="radio" class="form-check-input" id="judgeTrue" value="TRUE" v-model="form.answer">
                  <label class="form-check-label" for="judgeTrue">
                    <i class="bi bi-check-circle text-success me-1"></i>正确
                  </label>
                </div>
                <div class="form-check">
                  <input type="radio" class="form-check-input" id="judgeFalse" value="FALSE" v-model="form.answer">
                  <label class="form-check-label" for="judgeFalse">
                    <i class="bi bi-x-circle text-danger me-1"></i>错误
                  </label>
                </div>
              </div>
            </div>
            
            <!-- 填空题答案 -->
            <div v-if="form.type === 'FILL'" class="mb-4">
              <label class="form-label">
                参考答案 <span class="text-danger">*</span>
                <small class="text-muted ms-2">(多个空格答案用英文逗号分隔)</small>
              </label>
              <input type="text" class="form-control" v-model="form.answer" 
                     placeholder="例如：答案1,答案2">
            </div>
            
            <!-- 问答题参考答案 -->
            <div v-if="form.type === 'ESSAY'" class="mb-4">
              <label class="form-label">参考答案</label>
              <textarea class="form-control" v-model="form.answer" rows="4" 
                        placeholder="请输入问答题的参考答案"></textarea>
            </div>
            
            <!-- 解析 -->
            <div class="mb-3">
              <label class="form-label">题目解析</label>
              <textarea class="form-control" v-model="form.analysis" rows="2" 
                        placeholder="选填，帮助学生理解题目"></textarea>
            </div>
          </div>
          <div class="modal-footer">
            <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">取消</button>
            <button type="button" class="btn btn-primary" @click="saveQuestion" :disabled="saving">
              <span v-if="saving" class="spinner-border spinner-border-sm me-1"></span>
              <i v-else class="bi bi-check-lg me-1"></i>
              {{ saving ? '保存中...' : '保存' }}
            </button>
          </div>
        </div>
      </div>
    </div>
    
    <!-- 预览模态框 -->
    <div class="modal fade" id="previewModal" tabindex="-1">
      <div class="modal-dialog modal-dialog-centered">
        <div class="modal-content">
          <div class="modal-header">
            <h5 class="modal-title"><i class="bi bi-eye me-2"></i>题目预览</h5>
            <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
          </div>
          <div class="modal-body" v-if="previewQuestion">
            <div class="mb-3">
              <span class="badge me-2" :class="typeClass[previewQuestion.type]">{{ typeNames[previewQuestion.type] }}</span>
              <span class="badge" :class="difficultyClass[previewQuestion.difficulty]">{{ difficultyNames[previewQuestion.difficulty] }}</span>
              <span class="float-end text-muted">{{ previewQuestion.score }}分</span>
            </div>
            <div class="question-content mb-3">{{ previewQuestion.content }}</div>
            <div v-if="previewQuestion.options?.length" class="options-preview">
              <div v-for="opt in previewQuestion.options" :key="opt.id" 
                   class="option-preview-item" :class="{ correct: opt.isCorrect }">
                <span class="option-label-badge me-2">{{ opt.optionLabel }}</span>
                {{ opt.optionContent }}
                <i v-if="opt.isCorrect" class="bi bi-check-circle-fill text-success ms-2"></i>
              </div>
            </div>
            <div class="mt-3 p-3 bg-light rounded">
              <strong class="text-success">正确答案：</strong>{{ previewQuestion.answer }}
            </div>
            <div v-if="previewQuestion.analysis" class="mt-2 p-3 bg-info bg-opacity-10 rounded">
              <strong>解析：</strong>{{ previewQuestion.analysis }}
            </div>
          </div>
        </div>
      </div>
    </div>
    
    <!-- 确认删除对话框 -->
    <ConfirmModal 
      v-model:visible="confirmVisible"
      title="删除题目"
      :message="confirmMessage"
      type="danger"
      confirmText="删除"
      @confirm="handleDelete"
    />
  </div>
</template>

<script>
import { ref, onMounted, inject } from 'vue'
import { getCategoryList, getQuestionList, createQuestion, updateQuestion, deleteQuestion as deleteApi } from '../../api/admin'
import { Modal } from 'bootstrap'
import ConfirmModal from '../../components/ConfirmModal.vue'

export default {
  name: 'QuestionManagement',
  components: { ConfirmModal },
  setup() {
    const showToast = inject('showToast')
    const loading = ref(false)
    const saving = ref(false)
    const questions = ref([])
    const categories = ref([])
    const filters = ref({ categoryId: '', type: '', difficulty: '', keyword: '' })
    const editId = ref(null)
    const previewQuestion = ref(null)
    const confirmVisible = ref(false)
    const confirmMessage = ref('')
    const deleteId = ref(null)
    
    const typeNames = { SINGLE: '单选题', MULTIPLE: '多选题', JUDGE: '判断题', FILL: '填空题', ESSAY: '问答题' }
    const typeClass = { SINGLE: 'bg-primary', MULTIPLE: 'bg-purple', JUDGE: 'bg-success', FILL: 'bg-warning text-dark', ESSAY: 'bg-info' }
    const difficultyNames = { 1: '简单', 2: '中等', 3: '困难' }
    const difficultyClass = { 1: 'bg-success', 2: 'bg-warning text-dark', 3: 'bg-danger' }
    
    const defaultOptions = () => [
      { optionLabel: 'A', optionContent: '', isCorrect: false },
      { optionLabel: 'B', optionContent: '', isCorrect: false },
      { optionLabel: 'C', optionContent: '', isCorrect: false },
      { optionLabel: 'D', optionContent: '', isCorrect: false }
    ]
    
    const getDefaultForm = () => ({
      categoryId: '', type: 'SINGLE', content: '', answer: '', 
      analysis: '', difficulty: 1, score: 10, options: defaultOptions()
    })
    
    const form = ref(getDefaultForm())
    let questionModal = null
    let previewModal = null
    
    const loadCategories = async () => {
      try { 
        const res = await getCategoryList({ page: 1, size: 100 })
        categories.value = res.data.records 
      } catch (e) {}
    }
    
    const loadQuestions = async () => {
      loading.value = true
      try { 
        const res = await getQuestionList({ page: 1, size: 100, ...filters.value })
        questions.value = res.data.records 
      } catch (e) {}
      loading.value = false
    }
    
    const onTypeChange = () => {
      // 切换题型时重置相关字段
      if (form.value.type === 'SINGLE' || form.value.type === 'MULTIPLE') {
        if (!form.value.options || form.value.options.length < 2) {
          form.value.options = defaultOptions()
        }
        form.value.answer = ''
      } else if (form.value.type === 'JUDGE') {
        form.value.answer = 'TRUE'
        form.value.options = []
      } else {
        form.value.answer = ''
        form.value.options = []
      }
    }
    
    const addOption = () => {
      if (form.value.options.length >= 8) return
      const labels = 'ABCDEFGH'
      form.value.options.push({
        optionLabel: labels[form.value.options.length],
        optionContent: '',
        isCorrect: false
      })
    }
    
    const removeOption = (idx) => {
      if (form.value.options.length <= 2) return
      form.value.options.splice(idx, 1)
      // 重新排列标签
      const labels = 'ABCDEFGH'
      form.value.options.forEach((opt, i) => opt.optionLabel = labels[i])
    }
    
    const onCorrectChange = (idx) => {
      // 单选题只能有一个正确答案
      if (form.value.type === 'SINGLE' && form.value.options[idx].isCorrect) {
        form.value.options.forEach((opt, i) => {
          if (i !== idx) opt.isCorrect = false
        })
      }
    }
    
    const validateForm = () => {
      if (!form.value.categoryId) {
        showToast('请选择题目分类', 'error')
        return false
      }
      if (!form.value.content?.trim()) {
        showToast('请输入题目内容', 'error')
        return false
      }
      
      // 选择题校验
      if (form.value.type === 'SINGLE' || form.value.type === 'MULTIPLE') {
        const filledOptions = form.value.options.filter(o => o.optionContent?.trim())
        if (filledOptions.length < 2) {
          showToast('选择题至少需要2个有效选项', 'error')
          return false
        }
        const correctCount = form.value.options.filter(o => o.isCorrect).length
        if (form.value.type === 'SINGLE') {
          if (correctCount !== 1) {
            showToast('单选题必须且只能有1个正确答案', 'error')
            return false
          }
        } else {
          if (correctCount < 2) {
            showToast('多选题至少需要2个正确答案', 'error')
            return false
          }
        }
        // 自动生成答案
        form.value.answer = form.value.options
          .filter(o => o.isCorrect)
          .map(o => o.optionLabel)
          .join(',')
      }
      
      // 判断题校验
      if (form.value.type === 'JUDGE') {
        if (!form.value.answer) {
          showToast('请选择正确答案', 'error')
          return false
        }
      }
      
      // 填空题校验
      if (form.value.type === 'FILL') {
        if (!form.value.answer?.trim()) {
          showToast('请输入填空题的参考答案', 'error')
          return false
        }
      }
      
      return true
    }
    
    const showModal = (q = null) => {
      editId.value = q?.id || null
      if (q) {
        form.value = {
          categoryId: q.categoryId,
          type: q.type,
          content: q.content,
          answer: q.answer,
          analysis: q.analysis || '',
          difficulty: q.difficulty,
          score: q.score,
          options: q.options?.length ? [...q.options] : defaultOptions()
        }
      } else {
        form.value = getDefaultForm()
        if (categories.value.length) {
          form.value.categoryId = categories.value[0].id
        }
      }
      questionModal = new Modal(document.getElementById('questionModal'))
      questionModal.show()
    }
    
    const showPreview = (q) => {
      previewQuestion.value = q
      previewModal = new Modal(document.getElementById('previewModal'))
      previewModal.show()
    }
    
    const saveQuestion = async () => {
      if (!validateForm()) return
      
      saving.value = true
      try {
        const data = { ...form.value }
        // 清理未填写的选项
        if (data.type === 'SINGLE' || data.type === 'MULTIPLE') {
          data.options = data.options.filter(o => o.optionContent?.trim())
        }
        
        if (editId.value) {
          await updateQuestion(editId.value, data)
        } else {
          await createQuestion(data)
        }
        questionModal?.hide()
        showToast('保存成功')
        loadQuestions()
      } catch (e) {}
      saving.value = false
    }
    
    const showDeleteConfirm = (q) => {
      deleteId.value = q.id
      confirmMessage.value = `确定要删除该题目吗？\n\n「${q.content.substring(0, 50)}...」`
      confirmVisible.value = true
    }
    
    const handleDelete = async () => {
      if (!deleteId.value) return
      try {
        await deleteApi(deleteId.value)
        showToast('删除成功')
        loadQuestions()
      } catch (e) {}
    }
    
    onMounted(() => {
      loadCategories()
      loadQuestions()
    })
    
    return {
      loading, saving, questions, categories, filters, editId, form,
      typeNames, typeClass, difficultyNames, difficultyClass,
      previewQuestion, confirmVisible, confirmMessage,
      loadQuestions, showModal, showPreview, saveQuestion,
      showDeleteConfirm, handleDelete,
      onTypeChange, addOption, removeOption, onCorrectChange
    }
  }
}
</script>

<style scoped>
.bg-purple {
  background-color: #805ad5 !important;
  color: white;
}

.option-label-badge {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border-radius: 50%;
  font-weight: 600;
  flex-shrink: 0;
}

.option-list .option-item {
  padding: 8px 12px;
  background: #f8fafc;
  border-radius: 8px;
}

.option-preview-item {
  padding: 10px 12px;
  background: #f8fafc;
  border-radius: 8px;
  margin-bottom: 8px;
  display: flex;
  align-items: center;
}

.option-preview-item.correct {
  background: #f0fff4;
  border: 1px solid #48bb78;
}

.question-content {
  font-size: 1.1rem;
  line-height: 1.7;
  color: #2d3748;
}
</style>
