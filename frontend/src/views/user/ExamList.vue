<template>
  <div class="fade-in">
    <div class="card">
      <div class="card-header">
        <h5 class="mb-0">可参加的考试</h5>
      </div>
      <div class="card-body">
        <div v-if="loading" class="loading-spinner">
          <div class="spinner-border text-primary"></div>
        </div>
        
        <div v-else-if="exams.length === 0" class="empty-state">
          <i class="bi bi-file-earmark-x"></i>
          <h5>暂无可参加的考试</h5>
        </div>
        
        <div v-else class="row g-4">
          <div class="col-md-6 col-lg-4" v-for="exam in exams" :key="exam.id">
            <div class="card h-100 border">
              <div class="card-body">
                <h5 class="card-title">{{ exam.title }}</h5>
                <p class="card-text text-muted small">{{ exam.description || '暂无描述' }}</p>
                <ul class="list-unstyled small">
                  <li class="d-flex justify-content-between py-1">
                    <span class="text-muted">题目数量：</span>
                    <span>{{ exam.questionCount }} 道</span>
                  </li>
                  <li class="d-flex justify-content-between py-1">
                    <span class="text-muted">总分：</span>
                    <span>{{ exam.totalScore }} 分</span>
                  </li>
                  <li class="d-flex justify-content-between py-1">
                    <span class="text-muted">及格分：</span>
                    <span>{{ exam.passScore }} 分</span>
                  </li>
                  <li class="d-flex justify-content-between py-1">
                    <span class="text-muted">考试时长：</span>
                    <span>{{ exam.duration }} 分钟</span>
                  </li>
                </ul>
              </div>
              <div class="card-footer bg-transparent border-top-0">
                <button class="btn btn-primary w-100" @click="startExam(exam.id)">
                  <i class="bi bi-play-fill me-1"></i>开始考试
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, onMounted, inject } from 'vue'
import { useRouter } from 'vue-router'
import { getExamList, startExam as startExamApi } from '../../api/exam'

export default {
  name: 'ExamList',
  setup() {
    const router = useRouter()
    const showToast = inject('showToast')
    
    const loading = ref(false)
    const exams = ref([])
    
    const loadExams = async () => {
      loading.value = true
      try {
        const res = await getExamList()
        exams.value = res.data
      } catch (error) {
        console.error('加载考试列表失败', error)
      } finally {
        loading.value = false
      }
    }
    
    const startExam = async (examId) => {
      try {
        await startExamApi(examId)
        router.push(`/exam/do/${examId}`)
      } catch (error) {
        // 错误已处理
      }
    }
    
    onMounted(() => {
      loadExams()
    })
    
    return {
      loading,
      exams,
      startExam
    }
  }
}
</script>
