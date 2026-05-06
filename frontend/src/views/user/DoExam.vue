<template>
  <div class="fade-in">
    <div v-if="loading" class="loading-spinner">
      <div class="spinner-border text-primary"></div>
    </div>
    
    <div v-else-if="!exam" class="empty-state">
      <i class="bi bi-exclamation-triangle"></i>
      <h5>考试不存在</h5>
      <router-link to="/exams" class="btn btn-primary mt-3">返回考试列表</router-link>
    </div>
    
    <div v-else>
      <div class="row">
        <div class="col-lg-9">
          <!-- 题目卡片 -->
          <div class="question-card" v-if="!submitted">
            <div class="question-header">
              <span class="question-type" :class="getTypeClass(currentQuestion.type)">
                {{ getTypeName(currentQuestion.type) }}
              </span>
              <span class="text-muted">第 {{ currentIndex + 1 }} 题 / 共 {{ questions.length }} 题</span>
            </div>
            
            <div class="question-content">{{ currentQuestion.content }}</div>
            
            <!-- 选择题选项 -->
            <div v-if="currentQuestion.type === 'SINGLE' || currentQuestion.type === 'MULTIPLE'" class="question-options">
              <div 
                v-for="option in currentQuestion.options" 
                :key="option.id"
                class="option-item"
                :class="{ selected: isSelected(option.optionLabel) }"
                @click="selectOption(option.optionLabel)"
              >
                <span class="option-label">{{ option.optionLabel }}</span>
                <span class="option-content">{{ option.optionContent }}</span>
              </div>
            </div>
            
            <!-- 判断题选项 -->
            <div v-else-if="currentQuestion.type === 'JUDGE'" class="question-options">
              <div 
                class="option-item"
                :class="{ selected: answers[currentIndex] === 'TRUE' }"
                @click="answers[currentIndex] = 'TRUE'"
              >
                <span class="option-label"><i class="bi bi-check-lg"></i></span>
                <span class="option-content">正确</span>
              </div>
              <div 
                class="option-item"
                :class="{ selected: answers[currentIndex] === 'FALSE' }"
                @click="answers[currentIndex] = 'FALSE'"
              >
                <span class="option-label"><i class="bi bi-x-lg"></i></span>
                <span class="option-content">错误</span>
              </div>
            </div>
            
            <!-- 填空题输入 -->
            <div v-else-if="currentQuestion.type === 'FILL'" class="mb-3">
              <label class="form-label">请输入答案</label>
              <input type="text" class="form-control" v-model="answers[currentIndex]" placeholder="多个答案用英文逗号分隔">
            </div>
            
            <!-- 问答题输入 -->
            <div v-else-if="currentQuestion.type === 'ESSAY'" class="mb-3">
              <label class="form-label">请输入答案</label>
              <textarea class="form-control" rows="5" v-model="answers[currentIndex]" placeholder="输入您的答案"></textarea>
            </div>
            
            <!-- 操作按钮 -->
            <div class="d-flex justify-content-between mt-4">
              <button class="btn btn-outline-secondary" @click="prevQuestion" :disabled="currentIndex === 0">
                <i class="bi bi-chevron-left me-1"></i>上一题
              </button>
              <button v-if="currentIndex < questions.length - 1" class="btn btn-primary" @click="nextQuestion">
                下一题<i class="bi bi-chevron-right ms-1"></i>
              </button>
              <button v-else class="btn btn-success" @click="confirmSubmit">
                <i class="bi bi-check2 me-1"></i>交卷
              </button>
            </div>
          </div>
          
          <!-- 结果页面 -->
          <div v-else class="card">
            <div class="card-body text-center py-5">
              <div class="mb-4">
                <div class="d-inline-flex align-items-center justify-content-center" 
                     style="width: 120px; height: 120px; border-radius: 50%; font-size: 2rem;"
                     :style="{ background: result.score >= exam.passScore ? 'linear-gradient(135deg, #48bb78 0%, #38a169 100%)' : 'linear-gradient(135deg, #f56565 0%, #e53e3e 100%)' }">
                  <div class="text-white text-center">
                    <div style="font-size: 2rem; font-weight: bold;">{{ result.score }}</div>
                    <div style="font-size: 0.875rem;">分</div>
                  </div>
                </div>
              </div>
              <h4 class="mb-2">
                {{ result.score >= exam.passScore ? '🎉 恭喜通过!' : '😢 未通过' }}
              </h4>
              <p class="text-muted mb-4">
                总分 {{ exam.totalScore }} 分，及格分 {{ exam.passScore }} 分
              </p>
              <div class="d-flex justify-content-center gap-3">
                <router-link to="/exams" class="btn btn-outline-primary">返回考试列表</router-link>
                <router-link to="/exam/records" class="btn btn-primary">查看考试记录</router-link>
              </div>
            </div>
          </div>
        </div>
        
        <!-- 侧边栏 -->
        <div class="col-lg-3" v-if="!submitted">
          <!-- 计时器 -->
          <div class="exam-timer mb-4">
            <div class="timer-icon">
              <i class="bi bi-clock"></i>
            </div>
            <div class="timer-text">
              <h4>{{ formatTime(remainingTime) }}</h4>
              <p>剩余时间</p>
            </div>
          </div>
          
          <!-- 答题卡 -->
          <div class="card">
            <div class="card-header">
              <h6 class="mb-0">答题卡</h6>
            </div>
            <div class="card-body">
              <div class="d-flex flex-wrap gap-2">
                <button 
                  v-for="(q, index) in questions" 
                  :key="index"
                  class="btn btn-sm"
                  :class="{
                    'btn-primary': index === currentIndex,
                    'btn-success': index !== currentIndex && answers[index],
                    'btn-outline-secondary': index !== currentIndex && !answers[index]
                  }"
                  style="width: 36px; height: 36px;"
                  @click="currentIndex = index"
                >
                  {{ index + 1 }}
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
    
    <!-- 确认交卷模态框 -->
    <div class="modal fade" id="submitModal" tabindex="-1">
      <div class="modal-dialog modal-dialog-centered">
        <div class="modal-content">
          <div class="modal-header">
            <h5 class="modal-title">确认交卷</h5>
            <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
          </div>
          <div class="modal-body">
            <p>您已完成 {{ answeredCount }} / {{ questions.length }} 道题目。</p>
            <p v-if="answeredCount < questions.length" class="text-warning">
              <i class="bi bi-exclamation-triangle me-1"></i>
              还有 {{ questions.length - answeredCount }} 道题目未作答，确定要交卷吗？
            </p>
            <p v-else class="text-success">
              <i class="bi bi-check-circle me-1"></i>
              所有题目已作答完毕，确定要交卷吗？
            </p>
          </div>
          <div class="modal-footer">
            <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">继续答题</button>
            <button type="button" class="btn btn-primary" @click="submitExam">确认交卷</button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, computed, onMounted, onUnmounted, inject } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getExamDetail, startExam, submitExam as submitExamApi } from '../../api/exam'
import { Modal } from 'bootstrap'

export default {
  name: 'DoExam',
  setup() {
    const route = useRoute()
    const router = useRouter()
    const showToast = inject('showToast')
    
    const loading = ref(false)
    const exam = ref(null)
    const record = ref(null)
    const questions = ref([])
    const currentIndex = ref(0)
    const answers = ref([])
    const submitted = ref(false)
    const result = ref({})
    const remainingTime = ref(0)
    
    let timer = null
    let modal = null
    
    const examId = route.params.examId
    
    const currentQuestion = computed(() => {
      if (questions.value.length === 0) return {}
      const eq = questions.value[currentIndex.value]
      return eq?.question || {}
    })
    
    const answeredCount = computed(() => answers.value.filter(a => a).length)
    
    const getTypeName = (type) => {
      const types = {
        'SINGLE': '单选题',
        'MULTIPLE': '多选题',
        'JUDGE': '判断题',
        'FILL': '填空题',
        'ESSAY': '问答题'
      }
      return types[type] || type
    }
    
    const getTypeClass = (type) => {
      const classes = {
        'SINGLE': 'single',
        'MULTIPLE': 'multiple',
        'JUDGE': 'judge',
        'FILL': 'fill',
        'ESSAY': 'essay'
      }
      return classes[type] || ''
    }
    
    const isSelected = (label) => {
      const answer = answers.value[currentIndex.value] || ''
      if (currentQuestion.value.type === 'MULTIPLE') {
        return answer.split(',').includes(label)
      }
      return answer === label
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
    }
    
    const prevQuestion = () => {
      if (currentIndex.value > 0) currentIndex.value--
    }
    
    const nextQuestion = () => {
      if (currentIndex.value < questions.value.length - 1) currentIndex.value++
    }
    
    const formatTime = (seconds) => {
      const h = Math.floor(seconds / 3600)
      const m = Math.floor((seconds % 3600) / 60)
      const s = seconds % 60
      if (h > 0) {
        return `${h}:${m.toString().padStart(2, '0')}:${s.toString().padStart(2, '0')}`
      }
      return `${m.toString().padStart(2, '0')}:${s.toString().padStart(2, '0')}`
    }
    
    const confirmSubmit = () => {
      modal = new Modal(document.getElementById('submitModal'))
      modal.show()
    }
    
    const submitExam = async () => {
      modal?.hide()
      
      const answerList = questions.value.map((eq, i) => ({
        questionId: eq.questionId,
        answer: answers.value[i] || ''
      }))
      
      try {
        const res = await submitExamApi({
          examId: parseInt(examId),
          recordId: record.value.id,
          answers: answerList
        })
        result.value = res.data
        submitted.value = true
        if (timer) clearInterval(timer)
        showToast('交卷成功')
      } catch (error) {
        console.error('交卷失败', error)
      }
    }
    
    const loadExam = async () => {
      loading.value = true
      try {
        // 开始考试
        const startRes = await startExam(examId)
        record.value = startRes.data
        
        // 获取试卷详情
        const examRes = await getExamDetail(examId)
        exam.value = examRes.data
        questions.value = examRes.data.questions || []
        answers.value = new Array(questions.value.length).fill('')
        
        // 设置计时器
        remainingTime.value = exam.value.duration * 60
        timer = setInterval(() => {
          remainingTime.value--
          if (remainingTime.value <= 0) {
            clearInterval(timer)
            submitExam()
          }
        }, 1000)
      } catch (error) {
        console.error('加载考试失败', error)
      } finally {
        loading.value = false
      }
    }
    
    onMounted(() => {
      loadExam()
    })
    
    onUnmounted(() => {
      if (timer) clearInterval(timer)
    })
    
    return {
      loading,
      exam,
      questions,
      currentIndex,
      currentQuestion,
      answers,
      submitted,
      result,
      remainingTime,
      answeredCount,
      getTypeName,
      getTypeClass,
      isSelected,
      selectOption,
      prevQuestion,
      nextQuestion,
      formatTime,
      confirmSubmit,
      submitExam
    }
  }
}
</script>
