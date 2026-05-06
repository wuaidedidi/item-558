import { createRouter, createWebHistory } from 'vue-router'
import { getToken, getUser } from '../utils/auth'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/Login.vue'),
    meta: { guest: true }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('../views/Register.vue'),
    meta: { guest: true }
  },
  {
    path: '/',
    component: () => import('../views/Layout.vue'),
    meta: { requiresAuth: true },
    children: [
      {
        path: '',
        name: 'Dashboard',
        component: () => import('../views/Dashboard.vue')
      },
      // 用户端路由
      {
        path: 'practice',
        name: 'Practice',
        component: () => import('../views/user/Practice.vue')
      },
      {
        path: 'practice/do/:categoryId',
        name: 'DoPractice',
        component: () => import('../views/user/DoPractice.vue')
      },
      {
        path: 'practice/records',
        name: 'PracticeRecords',
        component: () => import('../views/user/PracticeRecords.vue')
      },
      {
        path: 'exams',
        name: 'ExamList',
        component: () => import('../views/user/ExamList.vue')
      },
      {
        path: 'exam/do/:examId',
        name: 'DoExam',
        component: () => import('../views/user/DoExam.vue')
      },
      {
        path: 'exam/records',
        name: 'ExamRecords',
        component: () => import('../views/user/ExamRecords.vue')
      },
      {
        path: 'wrong-book',
        name: 'WrongBook',
        component: () => import('../views/user/WrongBook.vue')
      },
      {
        path: 'profile',
        name: 'Profile',
        component: () => import('../views/user/Profile.vue')
      },
      // 管理员路由
      {
        path: 'admin/users',
        name: 'UserManagement',
        component: () => import('../views/admin/UserManagement.vue'),
        meta: { requiresAdmin: true }
      },
      {
        path: 'admin/categories',
        name: 'CategoryManagement',
        component: () => import('../views/admin/CategoryManagement.vue'),
        meta: { requiresAdmin: true }
      },
      {
        path: 'admin/questions',
        name: 'QuestionManagement',
        component: () => import('../views/admin/QuestionManagement.vue'),
        meta: { requiresAdmin: true }
      },
      {
        path: 'admin/exams',
        name: 'ExamManagement',
        component: () => import('../views/admin/ExamManagement.vue'),
        meta: { requiresAdmin: true }
      },
      {
        path: 'admin/exam-records',
        name: 'ExamRecordManagement',
        component: () => import('../views/admin/ExamRecordManagement.vue'),
        meta: { requiresAdmin: true }
      }
    ]
  },
  {
    path: '/:pathMatch(.*)*',
    redirect: '/'
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫
router.beforeEach((to, from, next) => {
  const token = getToken()
  const user = getUser()
  
  if (to.meta.requiresAuth && !token) {
    next('/login')
  } else if (to.meta.guest && token) {
    next('/')
  } else if (to.meta.requiresAdmin && (!user || user.role !== 'ADMIN')) {
    next('/')
  } else {
    next()
  }
})

export default router
