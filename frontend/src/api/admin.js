import request from '../utils/request'

// 获取用户列表
export function getUserList(params) {
  return request.get('/admin/users', { params })
}

// 更新用户状态
export function updateUserStatus(id, status) {
  return request.put(`/admin/users/${id}/status`, null, { params: { status } })
}

// 更新用户角色
export function updateUserRole(id, role) {
  return request.put(`/admin/users/${id}/role`, null, { params: { role } })
}

// 删除用户
export function deleteUser(id) {
  return request.delete(`/admin/users/${id}`)
}

// 获取分类列表
export function getCategoryList(params) {
  return request.get('/admin/categories', { params })
}

// 创建分类
export function createCategory(data) {
  return request.post('/admin/categories', data)
}

// 更新分类
export function updateCategory(id, data) {
  return request.put(`/admin/categories/${id}`, data)
}

// 删除分类
export function deleteCategory(id) {
  return request.delete(`/admin/categories/${id}`)
}

// 获取题目列表
export function getQuestionList(params) {
  return request.get('/admin/questions', { params })
}

// 获取所有题目
export function getAllQuestions(params) {
  return request.get('/admin/questions/all', { params })
}

// 获取题目详情
export function getQuestionById(id) {
  return request.get(`/admin/questions/${id}`)
}

// 创建题目
export function createQuestion(data) {
  return request.post('/admin/questions', data)
}

// 更新题目
export function updateQuestion(id, data) {
  return request.put(`/admin/questions/${id}`, data)
}

// 删除题目
export function deleteQuestion(id) {
  return request.delete(`/admin/questions/${id}`)
}

// 获取试卷列表
export function getExamList(params) {
  return request.get('/admin/exams', { params })
}

// 获取试卷详情
export function getExamById(id) {
  return request.get(`/admin/exams/${id}`)
}

// 创建试卷
export function createExam(data) {
  return request.post('/admin/exams', data)
}

// 更新试卷
export function updateExam(id, data) {
  return request.put(`/admin/exams/${id}`, data)
}

// 发布试卷
export function publishExam(id) {
  return request.put(`/admin/exams/${id}/publish`)
}

// 结束试卷
export function endExam(id) {
  return request.put(`/admin/exams/${id}/end`)
}

// 删除试卷
export function deleteExam(id) {
  return request.delete(`/admin/exams/${id}`)
}

// 获取考试记录列表
export function getExamRecordList(params) {
  return request.get('/admin/exam-records', { params })
}

// 获取管理员统计
export function getAdminStats() {
  return request.get('/admin/statistics')
}
