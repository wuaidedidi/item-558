<template>
  <div class="fade-in">
    <div class="card">
      <div class="card-header d-flex justify-content-between align-items-center">
        <h5 class="mb-0">用户管理</h5>
        <div class="d-flex gap-2">
          <div class="input-group" style="width: 280px;">
            <span class="input-group-text bg-white"><i class="bi bi-search"></i></span>
            <input type="text" class="form-control border-start-0" v-model="keyword" @input="loadUsers" placeholder="搜索用户名、昵称、邮箱...">
          </div>
        </div>
      </div>
      <div class="card-body p-0">
        <div v-if="loading" class="loading-spinner py-5"><div class="spinner-border text-primary"></div></div>
        <div v-else-if="users.length === 0" class="empty-state py-5">
          <i class="bi bi-people"></i>
          <h5>暂无用户数据</h5>
        </div>
        <div v-else class="table-responsive">
          <table class="table table-hover mb-0">
            <thead>
              <tr>
                <th class="ps-4" style="width: 80px;">ID</th>
                <th style="width: 120px;">用户名</th>
                <th style="width: 120px;">昵称</th>
                <th>邮箱</th>
                <th style="width: 100px;">角色</th>
                <th style="width: 100px;">状态</th>
                <th style="width: 180px;">操作</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="user in users" :key="user.id">
                <td class="ps-4">{{ user.id }}</td>
                <td><span class="fw-medium">{{ user.username }}</span></td>
                <td>{{ user.nickname || '-' }}</td>
                <td>{{ user.email || '-' }}</td>
                <td>
                  <span class="badge rounded-pill" :class="user.role === 'ADMIN' ? 'bg-danger' : 'bg-info'">
                    <i :class="user.role === 'ADMIN' ? 'bi bi-shield-check' : 'bi bi-person'" class="me-1"></i>
                    {{ user.role === 'ADMIN' ? '管理员' : '用户' }}
                  </span>
                </td>
                <td>
                  <span class="badge rounded-pill" :class="user.status === 1 ? 'bg-success' : 'bg-secondary'">
                    {{ user.status === 1 ? '正常' : '禁用' }}
                  </span>
                </td>
                <td>
                  <div class="btn-group btn-group-sm">
                    <button class="btn btn-outline-warning" @click="showToggleConfirm(user)">
                      <i :class="user.status === 1 ? 'bi bi-lock' : 'bi bi-unlock'" class="me-1"></i>
                      {{ user.status === 1 ? '禁用' : '启用' }}
                    </button>
                    <button class="btn btn-outline-danger" @click="showDeleteConfirm(user)">
                      <i class="bi bi-trash me-1"></i>删除
                    </button>
                  </div>
                </td>
              </tr>
            </tbody>
          </table>
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
import { ref, onMounted, inject } from 'vue'
import { getUserList, updateUserStatus, deleteUser as deleteApi } from '../../api/admin'
import { getUser } from '../../utils/auth'
import ConfirmModal from '../../components/ConfirmModal.vue'

export default {
  name: 'UserManagement',
  components: { ConfirmModal },
  setup() {
    const showToast = inject('showToast')
    const loading = ref(false)
    const users = ref([])
    const keyword = ref('')
    const confirmVisible = ref(false)
    const confirmConfig = ref({})
    const currentUser = getUser()
    
    const loadUsers = async () => {
      loading.value = true
      try {
        const res = await getUserList({ page: 1, size: 50, keyword: keyword.value })
        users.value = res.data.records
      } catch (e) {}
      loading.value = false
    }
    
    const showToggleConfirm = (user) => {
      // 检查是否是当前登录用户
      if (currentUser && currentUser.id === user.id) {
        showToast('不能禁用当前登录的账户', 'error')
        return
      }
      
      const action = user.status === 1 ? '禁用' : '启用'
      confirmConfig.value = {
        title: `${action}用户`,
        message: `确定要${action}用户「${user.username}」吗？`,
        type: user.status === 1 ? 'warning' : 'success',
        confirmText: action,
        onConfirm: () => toggleStatus(user)
      }
      confirmVisible.value = true
    }
    
    const showDeleteConfirm = (user) => {
      // 检查是否是当前登录用户
      if (currentUser && currentUser.id === user.id) {
        showToast('不能删除当前登录的账户', 'error')
        return
      }
      
      confirmConfig.value = {
        title: '删除用户',
        message: `确定要删除用户「${user.username}」吗？此操作不可恢复！`,
        type: 'danger',
        confirmText: '删除',
        onConfirm: () => deleteUser(user.id)
      }
      confirmVisible.value = true
    }
    
    const toggleStatus = async (user) => {
      try {
        await updateUserStatus(user.id, user.status === 1 ? 0 : 1)
        showToast('操作成功')
        loadUsers()
      } catch (e) {}
    }
    
    const deleteUser = async (id) => {
      try {
        await deleteApi(id)
        showToast('删除成功')
        loadUsers()
      } catch (e) {}
    }
    
    onMounted(() => loadUsers())
    
    return { loading, users, keyword, loadUsers, confirmVisible, confirmConfig, showToggleConfirm, showDeleteConfirm }
  }
}
</script>
