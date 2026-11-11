<template>
  <el-dialog
    :model-value="modelValue"
    :title="`编辑${method?.method || ''}`"
    width="720px"
    append-to-body
    destroy-on-close
    @update:model-value="$emit('update:modelValue', $event)"
  >
    <el-form
      v-if="method"
      label-width="120px"
      class="payment-form"
    >
      <el-form-item
        v-for="field in fields"
        :key="field.key"
        :label="field.label"
      >
        <el-input
          v-if="field.type === 'text'"
          v-model="form[field.key]"
          :placeholder="field.placeholder"
          :show-password="field.secret"
          clearable
        />
        <el-input
          v-else-if="field.type === 'textarea'"
          v-model="form[field.key]"
          type="textarea"
          :rows="5"
          :placeholder="field.placeholder"
          :show-password="field.secret"
          clearable
        />
        <el-input-number
          v-else-if="field.type === 'number'"
          v-model="form[field.key]"
          :min="0"
          :max="9999"
          controls-position="right"
          class="payment-number-input"
        />
        <el-radio-group
          v-else-if="field.type === 'status'"
          v-model="form[field.key]"
        >
          <el-radio value="1">
            启用
          </el-radio>
          <el-radio value="0">
            关闭
          </el-radio>
        </el-radio-group>
        <MaterialImageField
          v-else-if="field.type === 'image'"
          v-model="form[field.key]"
          :limit="1"
          :file-size="2"
          :is-show-tip="false"
        />
        <FileUpload
          v-else-if="field.type === 'file'"
          v-model="form[field.key]"
          :limit="1"
          :file-size="10"
          :file-type="['pem']"
          :is-show-tip="false"
        />
        <div
          v-if="field.tip"
          class="setting-tip"
        >
          {{ field.tip }}
        </div>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button
        type="primary"
        :loading="saving"
        @click="$emit('save')"
      >
        保存配置
      </el-button>
      <el-button
        :loading="saving"
        @click="$emit('validate')"
      >
        校验配置
      </el-button>
      <el-button @click="$emit('update:modelValue', false)">
        取消
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import MaterialImageField from '@/components/MaterialImageField'

defineProps({
  modelValue: {
    type: Boolean,
    default: false
  },
  method: {
    type: Object,
    default: null
  },
  fields: {
    type: Array,
    default: () => []
  },
  form: {
    type: Object,
    required: true
  },
  saving: {
    type: Boolean,
    default: false
  }
})

defineEmits(['update:modelValue', 'save', 'validate'])
</script>

<style scoped lang="scss">
.payment-form {
  padding: 4px 12px 0;
}

.payment-form :deep(.el-form-item) {
  align-items: flex-start;
  margin-bottom: 22px;
}

.payment-form :deep(.el-form-item__label) {
  line-height: 32px;
}

.payment-form :deep(.el-form-item__content) {
  display: block;
}

.payment-number-input {
  width: 100%;
}

.setting-tip {
  margin-top: 8px;
  color: var(--el-text-color-secondary);
  font-size: 12px;
  line-height: 18px;
}
</style>
