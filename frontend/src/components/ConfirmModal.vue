<template>
  <Teleport to="body">
    <div v-if="visible" class="modal-backdrop fade show"></div>
    <div v-if="visible" class="modal fade show d-block" tabindex="-1">
      <div class="modal-dialog modal-dialog-centered">
        <div class="modal-content confirm-modal">
          <div class="modal-header border-0 pb-0">
            <div class="modal-icon" :class="typeClass">
              <i :class="iconClass"></i>
            </div>
          </div>
          <div class="modal-body text-center pt-2">
            <h5 class="modal-title mb-2">{{ title }}</h5>
            <p class="text-muted mb-0">{{ message }}</p>
          </div>
          <div class="modal-footer border-0 justify-content-center gap-3 pt-0">
            <button type="button" class="btn btn-outline-secondary px-4" @click="handleCancel">
              {{ cancelText }}
            </button>
            <button type="button" class="btn px-4" :class="confirmBtnClass" @click="handleConfirm">
              {{ confirmText }}
            </button>
          </div>
        </div>
      </div>
    </div>
  </Teleport>
</template>

<script>
import { computed } from 'vue'

export default {
  name: 'ConfirmModal',
  props: {
    visible: { type: Boolean, default: false },
    title: { type: String, default: '确认操作' },
    message: { type: String, default: '确定要执行此操作吗？' },
    type: { type: String, default: 'warning' }, // warning, danger, info
    confirmText: { type: String, default: '确认' },
    cancelText: { type: String, default: '取消' }
  },
  emits: ['confirm', 'cancel', 'update:visible'],
  setup(props, { emit }) {
    const typeClass = computed(() => ({
      'warning': 'bg-warning-subtle text-warning',
      'danger': 'bg-danger-subtle text-danger',
      'info': 'bg-info-subtle text-info',
      'success': 'bg-success-subtle text-success'
    }[props.type] || 'bg-warning-subtle text-warning'))
    
    const iconClass = computed(() => ({
      'warning': 'bi bi-exclamation-triangle-fill',
      'danger': 'bi bi-trash-fill',
      'info': 'bi bi-info-circle-fill',
      'success': 'bi bi-check-circle-fill'
    }[props.type] || 'bi bi-exclamation-triangle-fill'))
    
    const confirmBtnClass = computed(() => ({
      'warning': 'btn-warning',
      'danger': 'btn-danger',
      'info': 'btn-primary',
      'success': 'btn-success'
    }[props.type] || 'btn-warning'))
    
    const handleConfirm = () => {
      emit('confirm')
      emit('update:visible', false)
    }
    
    const handleCancel = () => {
      emit('cancel')
      emit('update:visible', false)
    }
    
    return { typeClass, iconClass, confirmBtnClass, handleConfirm, handleCancel }
  }
}
</script>

<style scoped>
.confirm-modal {
  border: none;
  border-radius: 16px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.15);
  overflow: hidden;
}

.modal-icon {
  width: 64px;
  height: 64px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.75rem;
  margin: 0 auto;
}

.modal-title {
  font-weight: 600;
  color: #2d3748;
}

.modal-footer .btn {
  min-width: 100px;
  border-radius: 8px;
}
</style>
