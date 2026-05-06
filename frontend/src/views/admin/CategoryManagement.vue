<template>
  <div class="fade-in">
    <div class="card">
      <div class="card-header d-flex justify-content-between align-items-center">
        <h5 class="mb-0">分类管理</h5>
        <button class="btn btn-primary" @click="showModal()">
          <i class="bi bi-plus-lg me-1"></i>添加分类
        </button>
      </div>
      <div class="card-body p-0">
        <div v-if="loading" class="loading-spinner py-5"><div class="spinner-border text-primary"></div></div>
        <div v-else-if="categories.length === 0" class="empty-state py-5">
          <i class="bi bi-folder"></i>
          <h5>暂无分类</h5>
          <p class="text-muted">点击上方按钮添加分类</p>
        </div>
        <div v-else class="table-responsive">
          <table class="table table-hover mb-0">
            <thead>
              <tr>
                <th class="ps-4" style="width: 80px;">ID</th>
                <th style="width: 150px;">名称</th>
                <th>描述</th>
                <th style="width: 100px;">题目数</th>
                <th style="width: 80px;">排序</th>
                <th style="width: 100px;">状态</th>
                <th style="width: 160px;">操作</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="cat in categories" :key="cat.id">
                <td class="ps-4">{{ cat.id }}</td>
                <td><span class="fw-medium">{{ cat.name }}</span></td>
                <td><span class="text-muted">{{ cat.description || '-' }}</span></td>
                <td><span class="badge bg-light text-dark">{{ cat.questionCount || 0 }} 题</span></td>
                <td>{{ cat.sortOrder }}</td>
                <td>
                  <span class="badge rounded-pill" :class="cat.status === 1 ? 'bg-success' : 'bg-secondary'">
                    {{ cat.status === 1 ? '启用' : '禁用' }}
                  </span>
                </td>
                <td>
                  <div class="btn-group btn-group-sm">
                    <button class="btn btn-outline-primary" @click="showModal(cat)">
                      <i class="bi bi-pencil me-1"></i>编辑
                    </button>
                    <button class="btn btn-outline-danger" @click="showDeleteConfirm(cat)">
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
    
    <!-- 编辑/添加模态框 -->
    <div class="modal fade" id="categoryModal" tabindex="-1">
      <div class="modal-dialog modal-dialog-centered">
        <div class="modal-content">
          <div class="modal-header">
            <h5 class="modal-title">
              <i :class="editId ? 'bi bi-pencil' : 'bi bi-plus-circle'" class="me-2"></i>
              {{ editId ? '编辑分类' : '添加分类' }}
            </h5>
            <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
          </div>
          <div class="modal-body">
            <div class="mb-3">
              <label class="form-label">名称 <span class="text-danger">*</span></label>
              <input type="text" class="form-control" v-model="form.name" placeholder="请输入分类名称">
            </div>
            <div class="mb-3">
              <label class="form-label">描述</label>
              <textarea class="form-control" v-model="form.description" rows="3" placeholder="请输入分类描述"></textarea>
            </div>
            <div class="row">
              <div class="col-md-6 mb-3">
                <label class="form-label">排序</label>
                <input type="number" class="form-control" v-model="form.sortOrder" placeholder="0">
              </div>
              <div class="col-md-6 mb-3">
                <label class="form-label">状态</label>
                <select class="form-select" v-model="form.status">
                  <option :value="1">启用</option>
                  <option :value="0">禁用</option>
                </select>
              </div>
            </div>
          </div>
          <div class="modal-footer">
            <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">取消</button>
            <button type="button" class="btn btn-primary" @click="saveCategory">
              <i class="bi bi-check-lg me-1"></i>保存
            </button>
          </div>
        </div>
      </div>
    </div>
    
    <!-- 确认删除对话框 -->
    <ConfirmModal 
      v-model:visible="confirmVisible"
      title="删除分类"
      :message="confirmMessage"
      type="danger"
      confirmText="删除"
      @confirm="handleDelete"
    />
  </div>
</template>

<script>
import { ref, onMounted, inject } from 'vue'
import { getCategoryList, createCategory, updateCategory, deleteCategory as deleteApi } from '../../api/admin'
import { Modal } from 'bootstrap'
import ConfirmModal from '../../components/ConfirmModal.vue'

export default {
  name: 'CategoryManagement',
  components: { ConfirmModal },
  setup() {
    const showToast = inject('showToast')
    const loading = ref(false)
    const categories = ref([])
    const editId = ref(null)
    const form = ref({ name: '', description: '', sortOrder: 0, status: 1 })
    const confirmVisible = ref(false)
    const confirmMessage = ref('')
    const deleteId = ref(null)
    let modal = null
    
    const loadCategories = async () => {
      loading.value = true
      try { 
        const res = await getCategoryList({ page: 1, size: 100 })
        categories.value = res.data.records 
      } catch (e) {}
      loading.value = false
    }
    
    const showModal = (cat = null) => {
      editId.value = cat?.id || null
      form.value = cat 
        ? { name: cat.name, description: cat.description || '', sortOrder: cat.sortOrder || 0, status: cat.status } 
        : { name: '', description: '', sortOrder: 0, status: 1 }
      modal = new Modal(document.getElementById('categoryModal'))
      modal.show()
    }
    
    const saveCategory = async () => {
      if (!form.value.name) {
        showToast('请输入分类名称', 'error')
        return
      }
      try {
        if (editId.value) await updateCategory(editId.value, form.value)
        else await createCategory(form.value)
        modal?.hide()
        showToast('保存成功')
        loadCategories()
      } catch (e) {}
    }
    
    const showDeleteConfirm = (cat) => {
      deleteId.value = cat.id
      confirmMessage.value = `确定要删除分类「${cat.name}」吗？该分类下的题目将无法归类。`
      confirmVisible.value = true
    }
    
    const handleDelete = async () => {
      if (!deleteId.value) return
      try { 
        await deleteApi(deleteId.value)
        showToast('删除成功')
        loadCategories() 
      } catch (e) {}
    }
    
    onMounted(() => loadCategories())
    
    return { 
      loading, categories, editId, form, showModal, saveCategory, 
      confirmVisible, confirmMessage, showDeleteConfirm, handleDelete 
    }
  }
}
</script>
