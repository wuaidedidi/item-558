import axios from 'axios'
import { getToken, logout } from './auth'
import router from '../router'

// 已显示的消息集合，用于去重
const recentMessages = new Set()

// 显示错误消息（去重，通过自定义事件触发App.vue中的toast）
const showError = (message) => {
  if (recentMessages.has(message)) return
  recentMessages.add(message)
  setTimeout(() => recentMessages.delete(message), 2000)
  
  // 通过自定义事件触发全局toast
  window.dispatchEvent(new CustomEvent('show-toast', {
    detail: { message, type: 'error' }
  }))
}

const request = axios.create({
  baseURL: '/api',
  timeout: 30000,
  headers: {
    'Content-Type': 'application/json'
  }
})

// 请求拦截器
request.interceptors.request.use(
  config => {
    const token = getToken()
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  error => {
    return Promise.reject(error)
  }
)

// 响应拦截器
request.interceptors.response.use(
  response => {
    const res = response.data
    
    // 业务错误处理
    if (res.code !== 200) {
      showError(res.message || '操作失败')
      const error = new Error(res.message)
      error._isBusinessError = true
      return Promise.reject(error)
    }
    
    return res
  },
  error => {
    // 已处理的业务错误，跳过
    if (error._isBusinessError) {
      return Promise.reject(error)
    }
    
    let message = '网络错误，请检查网络连接'
    
    if (error.response) {
      const status = error.response.status
      const data = error.response.data
      
      switch (status) {
        case 401:
          // 登录接口的401显示后端错误消息，其他接口的401跳转登录页
          if (error.config.url?.includes('/auth/login')) {
            message = data?.message || '用户名或密码错误'
          } else {
            message = '登录已过期，请重新登录'
            logout()
            router.push('/login')
          }
          break
        case 403:
          message = '没有权限访问该资源'
          break
        case 404:
          message = '请求的资源不存在'
          break
        case 500:
          message = data?.message || '服务器错误，请稍后重试'
          break
        default:
          message = data?.message || `请求失败 (${status})`
      }
    }
    
    showError(message)
    return Promise.reject(error)
  }
)

export default request
