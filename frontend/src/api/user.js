import request from '../utils/request'

// 登录
export function login(data) {
  return request.post('/auth/login', data)
}

// 注册
export function register(data) {
  return request.post('/auth/register', data)
}

// 获取当前用户信息
export function getProfile() {
  return request.get('/user/profile')
}

// 更新用户信息
export function updateProfile(data) {
  return request.put('/user/profile', data)
}

// 修改密码
export function changePassword(data) {
  return request.put('/user/password', data)
}

// 获取用户统计
export function getUserStats() {
  return request.get('/user/statistics')
}
