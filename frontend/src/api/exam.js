import request from '../utils/request'

// 获取分类列表
export function getCategories() {
  return request.get('/categories')
}

// 获取练习题目
export function getPracticeQuestions(categoryId, count = 10) {
  return request.get('/practice/questions', { params: { categoryId, count } })
}

// 提交练习
export function submitPractice(data) {
  return request.post('/practice/submit', data)
}

// 获取练习记录
export function getPracticeRecords(params) {
  return request.get('/practice/records', { params })
}

// 获取练习记录详情
export function getPracticeRecordDetail(id) {
  return request.get(`/practice/records/${id}`)
}

// 获取试卷列表
export function getExamList() {
  return request.get('/exams')
}

// 获取试卷详情
export function getExamDetail(id) {
  return request.get(`/exams/${id}`)
}

// 开始考试
export function startExam(examId) {
  return request.post('/exam/start', null, { params: { examId } })
}

// 提交考试
export function submitExam(data) {
  return request.post('/exam/submit', data)
}

// 获取考试记录
export function getExamRecords(params) {
  return request.get('/exam/records', { params })
}

// 获取考试记录详情
export function getExamRecordDetail(id) {
  return request.get(`/exam/records/${id}`)
}

// 获取错题列表
export function getWrongQuestions(params) {
  return request.get('/wrong/list', { params })
}

// 获取错题练习题目
export function getWrongPracticeQuestions(count = 10) {
  return request.get('/wrong/practice', { params: { count } })
}

// 提交错题练习
export function submitWrongPractice(answers) {
  return request.post('/wrong/practice/submit', answers)
}

// 获取错题数量
export function getWrongQuestionCount() {
  return request.get('/wrong/count')
}

// 删除错题
export function removeWrongQuestion(questionId) {
  return request.delete(`/wrong/${questionId}`)
}

// 清空错题本
export function clearWrongQuestions() {
  return request.delete('/wrong/clear')
}
