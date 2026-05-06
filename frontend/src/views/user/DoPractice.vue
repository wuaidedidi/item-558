<template>
  <div class="fade-in">
    <div v-if="loading" class="loading-spinner py-5"><div class="spinner-border text-primary"></div></div>
    
    <div v-else-if="questions.length === 0" class="empty-state py-5">
      <i class="bi bi-emoji-frown"></i>
      <h5>该分类暂无题目</h5>
      <router-link to="/practice" class="btn btn-primary mt-3">返回选择分类</router-link>
    </div>
    
    <div v-else class="row">
      <!-- 左侧：题目区域 -->
      <div class="col-lg-9">
        <div class="question-card" v-if="!submitted">
          <div class="question-header">
            <div class="d-flex gap-2 align-items-center">
              <span class="question-type" :class="getTypeClass(currentQuestion.type)">
                {{ getTypeName(currentQuestion.type) }}
              </span>
              <span class="text-muted">第 {{ currentIndex + 1 }} / {{ questions.length }} 题</span>
              <span class="badge bg-light text-dark">{{ currentQuestion.score || 10 }} 分</span>
            </div>
            <button class="btn btn-sm btn-outline-info" @click="showAnswer" v-if="!showingAnswer">
              <i class="bi bi-eye me-1"></i>查看答案
            </button>
          </div>
          
          <div class="question-content">{{ currentQuestion.content }}</div>
          
          <!-- 选择题选项 -->
          <div v-if="currentQuestion.type === 'SINGLE' || currentQuestion.type === 'MULTIPLE'" class="question-options">
            <div v-for="option in currentQuestion.options" :key="option.optionLabel"
                 class="option-item" 
                 :class="getOptionClass(option)"
                 @click="!showingAnswer && selectOption(option.optionLabel)">
              <span class="option-label">{{ option.optionLabel }}</span>
              <span class="option-content">{{ option.optionContent }}</span>
              <i v-if="showingAnswer && option.isCorrect" class="bi bi-check-circle-fill text-success ms-auto"></i>
              <i v-if="showingAnswer && isSelected(option.optionLabel) && !option.isCorrect" class="bi bi-x-circle-fill text-danger ms-auto"></i>
            </div>
          </div>
          
          <!-- 判断题选项 -->
          <div v-else-if="currentQuestion.type === 'JUDGE'" class="question-options">
            <div class="option-item" 
                 :class="getJudgeClass('TRUE')"
                 @click="!showingAnswer && (answers[currentIndex] = 'TRUE')">
              <span class="option-label"><i class="bi bi-check-lg"></i></span>
              <span class="option-content">正确</span>
              <i v-if="showingAnswer && currentQuestion.answer === 'TRUE'" class="bi bi-check-circle-fill text-success ms-auto"></i>
            </div>
            <div class="option-item" 
                 :class="getJudgeClass('FALSE')"
                 @click="!showingAnswer && (answers[currentIndex] = 'FALSE')">
              <span class="option-label"><i class="bi bi-x-lg"></i></span>
              <span class="option-content">错误</span>
              <i v-if="showingAnswer && currentQuestion.answer === 'FALSE'" class="bi bi-check-circle-fill text-success ms-auto"></i>
            </div>
          </div>
          
          <!-- 填空题输入 -->
          <div v-else-if="currentQuestion.type === 'FILL'" class="mb-3">
            <label class="form-label">请输入答案（多个空格答案用英文逗号分隔）</label>
            <input type="text" class="form-control form-control-lg" v-model="answers[currentIndex]" 
                   :readonly="showingAnswer" placeholder="输入您的答案">
          </div>
          
          <!-- 问答题输入 -->
          <div v-else-if="currentQuestion.type === 'ESSAY'" class="mb-3">
            <label class="form-label">请输入您的答案</label>
            <textarea class="form-control" rows="5" v-model="answers[currentIndex]" 
                      :readonly="showingAnswer" placeholder="输入您的答案"></textarea>
          </div>
          
          <!-- 答案解析区域 -->
          <div v-if="showingAnswer" class="answer-feedback mt-4">
            <div class="row g-3">
              <div class="col-md-6">
                <div class="answer-box" :class="isCurrentCorrect ? 'correct' : 'wrong'">
                  <div class="answer-label">
                    <i :class="isCurrentCorrect ? 'bi bi-check-circle' : 'bi bi-x-circle'" class="me-1"></i>
                    {{ isCurrentCorrect ? '回答正确' : '回答错误' }}
                  </div>
                  <div class="answer-content">你的答案：{{ answers[currentIndex] || '未作答' }}</div>
                </div>
              </div>
              <div class="col-md-6">
                <div class="answer-box correct">
                  <div class="answer-label"><i class="bi bi-check-circle me-1"></i>正确答案</div>
                  <div class="answer-content">{{ currentQuestion.answer }}</div>
                </div>
              </div>
            </div>
            <div v-if="currentQuestion.analysis" class="analysis-box mt-3">
              <div class="analysis-label"><i class="bi bi-lightbulb me-1"></i>题目解析</div>
              <div class="analysis-content">{{ currentQuestion.analysis }}</div>
            </div>
          </div>
          
          <!-- 操作按钮 -->
          <div class="d-flex justify-content-between mt-4">
            <button class="btn btn-outline-secondary" @click="prevQuestion" :disabled="currentIndex === 0">
              <i class="bi bi-chevron-left me-1"></i>上一题
            </button>
            <div class="d-flex gap-2">
              <button v-if="currentIndex < questions.length - 1" class="btn btn-primary" @click="nextQuestion">
                下一题<i class="bi bi-chevron-right ms-1"></i>
              </button>
              <button v-if="currentIndex === questions.length - 1" class="btn btn-success" @click="showSubmitConfirm">
                <i class="bi bi-check2 me-1"></i>提交答案
              </button>
            </div>
          </div>
        </div>
        
        <!-- 结果页面 -->
        <div v-else class="card">
          <div class="card-body text-center py-5">
            <div class="mb-4">
              <div class="d-inline-flex align-items-center justify-content-center" 
                   style="width: 120px; height: 120px; border-radius: 50%; font-size: 2rem;"
                   :style="{ background: result.correctCount >= questions.length / 2 ? 'linear-gradient(135deg, #48bb78 0%, #38a169 100%)' : 'linear-gradient(135deg, #f56565 0%, #e53e3e 100%)' }">
                <div class="text-white text-center">
                  <div style="font-size: 2rem; font-weight: bold;">{{ Math.round(result.correctCount / questions.length * 100) }}%</div>
                </div>
              </div>
            </div>
            <h4 class="mb-3">练习完成！</h4>
            <p class="text-muted mb-2">共 {{ questions.length }} 题，答对 {{ result.correctCount }} 题</p>
            <p class="mb-4"><span class="badge bg-primary fs-6">得分：{{ result.score }} 分</span></p>
            <div class="d-flex justify-content-center gap-3">
              <router-link to="/practice" class="btn btn-outline-primary">继续练习</router-link>
              <router-link to="/wrong-book" class="btn btn-outline-danger">查看错题</router-link>
              <router-link to="/practice/records" class="btn btn-primary">练习记录</router-link>
            </div>
          </div>
        </div>
      </div>
      
      <!-- 右侧：答题卡 -->
      <div class="col-lg-3" v-if="!submitted">
        <div class="card answer-card-panel">
          <div class="card-header">
            <h6 class="mb-0"><i class="bi bi-grid-3x3 me-2"></i>答题卡</h6>
          </div>
          <div class="card-body">
            <div class="d-flex flex-wrap gap-2">
              <button v-for="(q, index) in questions" :key="index"
                      class="answer-card-btn"
                      :class="getCardClass(index)"
                      @click="goToQuestion(index)">
                {{ index + 1 }}
              </button>
            </div>
            <div class="mt-3 pt-3 border-top">
              <div class="d-flex justify-content-between text-sm text-muted">
                <span><span class="dot answered"></span> 已答 {{ answeredCount }}</span>
                <span><span class="dot unanswered"></span> 未答 {{ questions.length - answeredCount }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
    
    <!-- 确认提交对话框 -->
    <ConfirmModal 
      v-model:visible="confirmVisible"
      title="提交答案"
      :message="confirmMessage"
      type="info"
      confirmText="提交"
      @confirm="submitPractice"
    />
  </div>
</template>

<script>
import { ref, computed, onMounted, onBeforeUnmount, inject } from 'vue'
import { useRoute } from 'vue-router'
import { getPracticeQuestions, submitPractice as submitPracticeApi } from '../../api/exam'
import ConfirmModal from '../../components/ConfirmModal.vue'

export default {
  name: 'DoPractice',
  components: { ConfirmModal },
  setup() {
    const route = useRoute()
    const showToast = inject('showToast')
    
    const loading = ref(false)
    const submitting = ref(false)
    const questions = ref([])
    const currentIndex = ref(0)
    const answers = ref([])
    const submitted = ref(false)
    const result = ref({})
    const showingAnswer = ref(false)
    const confirmVisible = ref(false)
    
    const categoryId = route.params.categoryId
    const count = route.query.count || 10
    
    const STORAGE_KEY = `practice_${categoryId}_answers`
    
    const currentQuestion = computed(() => questions.value[currentIndex.value] || {})
    const answeredCount = computed(() => answers.value.filter(a => a).length)
    
    const confirmMessage = computed(() => {
      const unanswered = questions.value.length - answeredCount.value
      if (unanswered > 0) {
        return `您还有 ${unanswered} 道题未作答，确定要提交吗？`
      }
      return '确定要提交答案吗？'
    })
    
    const isCurrentCorrect = computed(() => {
      const q = currentQuestion.value
      const a = answers.value[currentIndex.value] || ''
      if (!a) return false
      return a.toUpperCase().trim() === q.answer?.toUpperCase().trim()
    })
    
    const getTypeName = (type) => ({ SINGLE: '单选题', MULTIPLE: '多选题', JUDGE: '判断题', FILL: '填空题', ESSAY: '问答题' }[type] || type)
    const getTypeClass = (type) => ({ SINGLE: 'single', MULTIPLE: 'multiple', JUDGE: 'judge', FILL: 'fill', ESSAY: 'essay' }[type] || '')
    
    const isSelected = (label) => {
      const answer = answers.value[currentIndex.value] || ''
      if (currentQuestion.value.type === 'MULTIPLE') {
        return answer.split(',').includes(label)
      }
      return answer === label
    }
    
    const getOptionClass = (option) => {
      const classes = []
      if (isSelected(option.optionLabel)) classes.push('selected')
      if (showingAnswer.value) {
        if (option.isCorrect) classes.push('correct-answer')
        else if (isSelected(option.optionLabel)) classes.push('wrong-answer')
      }
      return classes.join(' ')
    }
    
    const getJudgeClass = (value) => {
      const selected = answers.value[currentIndex.value] === value
      const classes = []
      if (selected) classes.push('selected')
      if (showingAnswer.value) {
        if (currentQuestion.value.answer === value) classes.push('correct-answer')
        else if (selected) classes.push('wrong-answer')
      }
      return classes.join(' ')
    }
    
    const getCardClass = (index) => {
      if (index === currentIndex.value) return 'current'
      if (answers.value[index]) return 'answered'
      return 'unanswered'
    }
    
    const selectOption = (label) => {
      if (currentQuestion.value.type === 'MULTIPLE') {
        let selected = (answers.value[currentIndex.value] || '').split(',').filter(Boolean)
        if (selected.includes(label)) {
          selected = selected.filter(l => l !== label)
        } else {
          selected.push(label)
        }
        selected.sort()
        answers.value[currentIndex.value] = selected.join(',')
      } else {
        answers.value[currentIndex.value] = label
      }
      saveProgress()
    }
    
    const prevQuestion = () => {
      if (currentIndex.value > 0) {
        currentIndex.value--
        showingAnswer.value = false
      }
    }
    
    const nextQuestion = () => {
      if (currentIndex.value < questions.value.length - 1) {
        currentIndex.value++
        showingAnswer.value = false
      }
    }
    
    const goToQuestion = (index) => {
      currentIndex.value = index
      showingAnswer.value = false
    }
    
    const showAnswer = () => {
      showingAnswer.value = true
    }
    
    const showSubmitConfirm = () => {
      confirmVisible.value = true
    }
    
    const submitPractice = async () => {
      submitting.value = true
      const answerList = questions.value.map((q, i) => ({
        questionId: q.id,
        answer: answers.value[i] || ''
      }))
      
      try {
        const res = await submitPracticeApi({
          categoryId: parseInt(categoryId),
          mode: 'NORMAL',
          answers: answerList
        })
        result.value = res.data
        submitted.value = true
        localStorage.removeItem(STORAGE_KEY)
        showToast('提交成功')
      } catch (error) {
        console.error('提交失败', error)
      }
      submitting.value = false
    }
    
    const saveProgress = () => {
      localStorage.setItem(STORAGE_KEY, JSON.stringify({
        answers: answers.value,
        currentIndex: currentIndex.value,
        timestamp: Date.now()
      }))
    }
    
    const loadProgress = () => {
      try {
        const saved = localStorage.getItem(STORAGE_KEY)
        if (saved) {
          const data = JSON.parse(saved)
          // 只恢复24小时内的进度
          if (Date.now() - data.timestamp < 24 * 60 * 60 * 1000) {
            if (data.answers && data.answers.length === questions.value.length) {
              answers.value = data.answers
              currentIndex.value = data.currentIndex || 0
              showToast('已恢复上次答题进度', 'info')
            }
          }
        }
      } catch (e) {}
    }
    
    const loadQuestions = async () => {
      loading.value = true
      try {
        const res = await getPracticeQuestions(categoryId, count)
        questions.value = res.data
        answers.value = new Array(res.data.length).fill('')
        loadProgress()
      } catch (error) {
        console.error('加载题目失败', error)
      }
      loading.value = false
    }
    
    onMounted(() => {
      loadQuestions()
    })
    
    onBeforeUnmount(() => {
      if (!submitted.value && answers.value.some(a => a)) {
        saveProgress()
      }
    })
    
    return {
      loading, submitting, questions, currentIndex, currentQuestion, answers, submitted, result,
      showingAnswer, confirmVisible, confirmMessage, answeredCount, isCurrentCorrect,
      getTypeName, getTypeClass, isSelected, getOptionClass, getJudgeClass, getCardClass,
      selectOption, prevQuestion, nextQuestion, goToQuestion, showAnswer, showSubmitConfirm, submitPractice
    }
  }
}
</script>

<style scoped>
.answer-card-panel {
  position: sticky;
  top: 20px;
}

.answer-card-btn {
  width: 40px;
  height: 40px;
  border: 2px solid #e2e8f0;
  border-radius: 8px;
  background: white;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s ease;
}

.answer-card-btn.current {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border-color: #667eea;
}

.answer-card-btn.answered {
  background: #48bb78;
  color: white;
  border-color: #48bb78;
}

.answer-card-btn.unanswered:hover {
  border-color: #667eea;
}

.dot {
  display: inline-block;
  width: 10px;
  height: 10px;
  border-radius: 50%;
  margin-right: 4px;
}

.dot.answered { background: #48bb78; }
.dot.unanswered { background: #e2e8f0; }

.option-item.correct-answer {
  background: #f0fff4 !important;
  border-color: #48bb78 !important;
}

.option-item.wrong-answer {
  background: #fff5f5 !important;
  border-color: #f56565 !important;
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
</style>
