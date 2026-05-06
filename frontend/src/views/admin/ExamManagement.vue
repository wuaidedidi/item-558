<template>
  <div class="fade-in">
    <div class="card">
      <div class="card-header d-flex justify-content-between align-items-center">
        <h5 class="mb-0">试卷管理</h5>
        <button class="btn btn-primary" @click="showModal()">
          <i class="bi bi-plus-lg me-1"></i>创建试卷
        </button>
      </div>
      <div class="card-body p-0">
        <div v-if="loading" class="loading-spinner py-5"><div class="spinner-border text-primary"></div></div>
        <div v-else-if="exams.length === 0" class="empty-state py-5">
          <i class="bi bi-file-ruled"></i>
          <h5>暂无试卷</h5>
          <p class="text-muted">点击上方按钮创建试卷</p>
        </div>
        <div v-else class="table-responsive">
          <table class="table table-hover mb-0">
            <thead>
              <tr>
                <th class="ps-4" style="width: 60px;">ID</th>
                <th>试卷标题</th>
                <th style="width: 80px;">总分</th>
                <th style="width: 80px;">及格分</th>
                <th style="width: 100px;">考试时长</th>
                <th style="width: 80px;">题目数</th>
                <th style="width: 100px;">状态</th>
                <th style="width: 200px;">操作</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="e in exams" :key="e.id">
                <td class="ps-4">{{ e.id }}</td>
                <td><span class="fw-medium">{{ e.title }}</span></td>
                <td>{{ e.totalScore }}分</td>
                <td>{{ e.passScore }}分</td>
                <td>{{ e.duration }}分钟</td>
                <td><span class="badge bg-light text-dark">{{ e.questionCount }}题</span></td>
                <td>
                  <span class="badge rounded-pill" :class="statusClass[e.status]">
                    <i :class="statusIcon[e.status]" class="me-1"></i>
                    {{ statusNames[e.status] }}
                  </span>
                </td>
                <td>
                  <div class="btn-group btn-group-sm">
                    <button v-if="e.status === 0" class="btn btn-outline-success" @click="showPublishConfirm(e)">
                      <i class="bi bi-send me-1"></i>发布
                    </button>
                    <button v-if="e.status === 1" class="btn btn-outline-warning" @click="showEndConfirm(e)">
                      <i class="bi bi-stop-circle me-1"></i>结束
                    </button>
                    <button v-if="e.status === 0" class="btn btn-outline-primary" @click="showModal(e)">
                      <i class="bi bi-pencil"></i>
                    </button>
                    <button v-if="e.status !== 1" class="btn btn-outline-danger" @click="showDeleteConfirm(e)">
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
    
    <!-- 创建/编辑试卷模态框 -->
    <div class="modal fade" id="examModal" tabindex="-1" data-bs-backdrop="static">
      <div class="modal-dialog modal-xl modal-dialog-centered modal-dialog-scrollable">
        <div class="modal-content">
          <div class="modal-header">
            <h5 class="modal-title">
              <i :class="editId ? 'bi bi-pencil' : 'bi bi-plus-circle'" class="me-2"></i>
              {{ editId ? '编辑试卷' : '创建试卷' }}
            </h5>
            <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
          </div>
          <div class="modal-body">
            <!-- 基本信息 -->
            <div class="row g-3 mb-4">
              <div class="col-md-6">
                <label class="form-label">试卷标题 <span class="text-danger">*</span></label>
                <input type="text" class="form-control" v-model="form.title" placeholder="请输入试卷标题">
              </div>
              <div class="col-md-3">
                <label class="form-label">及格分数 <span class="text-danger">*</span></label>
                <div class="input-group">
                  <input type="number" class="form-control" v-model.number="form.passScore" min="0">
                  <span class="input-group-text">分</span>
                </div>
              </div>
              <div class="col-md-3">
                <label class="form-label">考试时长 <span class="text-danger">*</span></label>
                <div class="input-group">
                  <input type="number" class="form-control" v-model.number="form.duration" min="1">
                  <span class="input-group-text">分钟</span>
                </div>
              </div>
            </div>
            
            <div class="mb-4">
              <label class="form-label">试卷描述</label>
              <textarea class="form-control" v-model="form.description" rows="2" placeholder="选填"></textarea>
            </div>
            
            <!-- 题目选择 -->
            <div class="mb-3">
              <div class="d-flex justify-content-between align-items-center mb-2">
                <label class="form-label mb-0">
                  选择题目 <span class="text-danger">*</span>
                  <span class="badge bg-primary ms-2">已选 {{ selectedQuestions.length }} 题</span>
                  <span class="badge bg-success ms-1">总分 {{ totalScore }} 分</span>
                </label>
                <div class="btn-group btn-group-sm">
                  <button type="button" class="btn btn-outline-primary" @click="selectAll">全选</button>
                  <button type="button" class="btn btn-outline-secondary" @click="clearSelection">清空</button>
                </div>
              </div>
              <div class="question-select-list border rounded">
                <div v-if="allQuestions.length === 0" class="text-center text-muted py-4">
                  <i class="bi bi-inbox fs-3"></i>
                  <p class="mb-0 mt-2">暂无题目，请先添加题目</p>
                </div>
                <div v-else v-for="q in allQuestions" :key="q.id" 
                     class="question-select-item" :class="{ selected: selectedQuestions.includes(q.id) }"
                     @click="toggleQuestion(q.id)">
                  <div class="form-check mb-0">
                    <input type="checkbox" class="form-check-input" :checked="selectedQuestions.includes(q.id)" @click.stop>
                    <label class="form-check-label d-block">
                      <span class="badge bg-light text-dark me-2">{{ q.categoryName }}</span>
                      <span class="badge me-2" :class="typeClass[q.type]">{{ typeNames[q.type] }}</span>
                      <span class="text-truncate">{{ q.content }}</span>
                      <span class="float-end text-muted">{{ q.score }}分</span>
                    </label>
                  </div>
                </div>
              </div>
            </div>
          </div>
          <div class="modal-footer">
            <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">取消</button>
            <button type="button" class="btn btn-primary" @click="saveExam" :disabled="saving">
              <span v-if="saving" class="spinner-border spinner-border-sm me-1"></span>
              <i v-else class="bi bi-check-lg me-1"></i>
              {{ saving ? '保存中...' : '保存' }}
            </button>
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
import { getExamList, getExamById, getAllQuestions, createExam, updateExam, publishExam as publishApi, endExam as endApi, deleteExam as deleteApi } from '../../api/admin'
import { Modal } from 'bootstrap'
import ConfirmModal from '../../components/ConfirmModal.vue'

export default {
  name: 'ExamManagement',
  components: { ConfirmModal },
  setup() {
    const showToast = inject('showToast')
    const loading = ref(false)
    const saving = ref(false)
    const exams = ref([])
    const allQuestions = ref([])
    const editId = ref(null)
    const selectedQuestions = ref([])
    const form = ref({ title: '', description: '', passScore: 60, duration: 60 })
    const confirmVisible = ref(false)
    const confirmConfig = ref({})
    
    const statusNames = { 0: '未发布', 1: '进行中', 2: '已结束' }
    const statusClass = { 0: 'bg-secondary', 1: 'bg-success', 2: 'bg-info' }
    const statusIcon = { 0: 'bi bi-circle', 1: 'bi bi-play-circle', 2: 'bi bi-check-circle' }
    const typeNames = { SINGLE: '单选', MULTIPLE: '多选', JUDGE: '判断', FILL: '填空', ESSAY: '问答' }
    const typeClass = { SINGLE: 'bg-primary', MULTIPLE: 'bg-purple', JUDGE: 'bg-success', FILL: 'bg-warning text-dark', ESSAY: 'bg-info' }
    
    let modal = null
    
    const totalScore = computed(() => {
      return allQuestions.value
        .filter(q => selectedQuestions.value.includes(q.id))
        .reduce((sum, q) => sum + (q.score || 0), 0)
    })
    
    const loadExams = async () => {
      loading.value = true
      try { 
        const res = await getExamList({ page: 1, size: 100 })
        exams.value = res.data.records 
      } catch (e) {}
      loading.value = false
    }
    
    const loadQuestions = async () => {
      try { 
        const res = await getAllQuestions({})
        allQuestions.value = res.data 
      } catch (e) {}
    }
    
    const showModal = async (e = null) => {
      editId.value = e?.id || null
      if (e?.id) {
        // 编辑时获取试卷详情
        try {
          const res = await getExamById(e.id)
          const examDetail = res.data
          form.value = { 
            title: examDetail.title, 
            description: examDetail.description || '', 
            passScore: examDetail.passScore, 
            duration: examDetail.duration 
          }
          selectedQuestions.value = examDetail.questions?.map(q => q.questionId) || []
        } catch (err) {
          form.value = { title: e.title, description: e.description || '', passScore: e.passScore, duration: e.duration }
          selectedQuestions.value = []
        }
      } else {
        form.value = { title: '', description: '', passScore: 60, duration: 60 }
        selectedQuestions.value = []
      }
      modal = new Modal(document.getElementById('examModal'))
      modal.show()
    }
    
    const toggleQuestion = (id) => {
      const idx = selectedQuestions.value.indexOf(id)
      if (idx > -1) {
        selectedQuestions.value.splice(idx, 1)
      } else {
        selectedQuestions.value.push(id)
      }
    }
    
    const selectAll = () => {
      selectedQuestions.value = allQuestions.value.map(q => q.id)
    }
    
    const clearSelection = () => {
      selectedQuestions.value = []
    }
    
    const saveExam = async () => {
      if (!form.value.title?.trim()) {
        showToast('请输入试卷标题', 'error')
        return
      }
      if (selectedQuestions.value.length === 0) {
        showToast('请至少选择一道题目', 'error')
        return
      }
      
      saving.value = true
      const data = { 
        ...form.value, 
        questions: selectedQuestions.value.map((qId, i) => {
          const q = allQuestions.value.find(x => x.id === qId)
          return { questionId: qId, score: q?.score || 10, sortOrder: i }
        })
      }
      try { 
        if (editId.value) await updateExam(editId.value, data)
        else await createExam(data)
        modal?.hide()
        showToast('保存成功')
        loadExams()
      } catch (e) {}
      saving.value = false
    }
    
    const showPublishConfirm = (e) => {
      confirmConfig.value = {
        title: '发布试卷',
        message: `确定要发布试卷「${e.title}」吗？发布后学生可以开始考试。`,
        type: 'success',
        confirmText: '发布',
        onConfirm: () => publishExam(e.id)
      }
      confirmVisible.value = true
    }
    
    const showEndConfirm = (e) => {
      confirmConfig.value = {
        title: '结束考试',
        message: `确定要结束「${e.title}」的考试吗？结束后学生将无法继续作答。`,
        type: 'warning',
        confirmText: '结束',
        onConfirm: () => endExam(e.id)
      }
      confirmVisible.value = true
    }
    
    const showDeleteConfirm = (e) => {
      confirmConfig.value = {
        title: '删除试卷',
        message: `确定要删除试卷「${e.title}」吗？此操作不可恢复！`,
        type: 'danger',
        confirmText: '删除',
        onConfirm: () => deleteExam(e.id)
      }
      confirmVisible.value = true
    }
    
    const publishExam = async (id) => {
      try { await publishApi(id); showToast('发布成功'); loadExams() } catch (e) {}
    }
    
    const endExam = async (id) => {
      try { await endApi(id); showToast('考试已结束'); loadExams() } catch (e) {}
    }
    
    const deleteExam = async (id) => {
      try { await deleteApi(id); showToast('删除成功'); loadExams() } catch (e) {}
    }
    
    onMounted(() => { loadExams(); loadQuestions() })
    
    return { 
      loading, saving, exams, allQuestions, editId, form, selectedQuestions, totalScore,
      statusNames, statusClass, statusIcon, typeNames, typeClass,
      confirmVisible, confirmConfig,
      showModal, toggleQuestion, selectAll, clearSelection, saveExam,
      showPublishConfirm, showEndConfirm, showDeleteConfirm
    }
  }
}
</script>

<style scoped>
.bg-purple {
  background-color: #805ad5 !important;
  color: white;
}

.question-select-list {
  max-height: 350px;
  overflow-y: auto;
}

.question-select-item {
  padding: 12px 16px;
  border-bottom: 1px solid #f1f5f9;
  cursor: pointer;
  transition: all 0.2s ease;
}

.question-select-item:hover {
  background: #f8fafc;
}

.question-select-item.selected {
  background: #ebf8ff;
  border-left: 3px solid #667eea;
}

.question-select-item:last-child {
  border-bottom: none;
}
</style>
