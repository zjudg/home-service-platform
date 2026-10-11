<template>
  <el-dialog
    :model-value="modelValue"
    :title="`${channel?.name || ''}通知设置`"
    width="820px"
    append-to-body
    destroy-on-close
      @update:model-value="$emit('update:modelValue', $event)"
  >
    <el-form
      v-if="scene && channel"
      :model="form"
      label-width="120px"
      class="notification-form"
    >
      <el-form-item label="通知场景">
        <span class="readonly-value">{{ scene.name }}</span>
      </el-form-item>

      <template v-if="channel.key === 'system'">
        <el-form-item
          label="通知标题"
          required
        >
          <el-input
            v-model="form.title"
            clearable
          />
        </el-form-item>
        <VariableButtons
          :variables="scene.variables"
          @insert="insertVariable"
        />
        <el-form-item
          label="通知内容"
          required
        >
          <el-input
            v-model="form.content"
            type="textarea"
            :rows="4"
            resize="vertical"
          />
          <div class="setting-tip">
            支持嵌入场景变量，复制场景变量的值填入即可生效
          </div>
        </el-form-item>
        <el-form-item label="APP推送">
          <el-radio-group v-model="form.appPush">
            <el-radio value="1">
              开启
            </el-radio>
            <el-radio value="0">
              关闭
            </el-radio>
          </el-radio-group>
        </el-form-item>
      </template>

      <template v-else-if="channel.key === 'sms'">
        <el-form-item
          label="模板ID"
          required
        >
          <el-input
            v-model="form.templateId"
            clearable
          />
          <div class="setting-tip">
            需要与短信接口的模板ID一致
          </div>
        </el-form-item>
        <VariableButtons
          :variables="scene.variables"
          @insert="insertVariable"
        />
        <el-form-item
          label="短信内容"
          required
        >
          <el-input
            v-model="form.content"
            type="textarea"
            :rows="4"
            resize="vertical"
          />
        </el-form-item>
      </template>

      <template v-else>
        <el-form-item
          label="模板编号"
          required
        >
          <el-input
            v-model="form.templateNo"
            clearable
          />
        </el-form-item>
        <el-form-item
          label="模板名称"
          required
        >
          <el-input
            v-model="form.templateName"
            clearable
          />
        </el-form-item>
        <el-form-item
          label="模板ID"
          required
        >
          <el-input
            v-model="form.templateId"
            clearable
          />
        </el-form-item>
        <VariableButtons
          :variables="scene.variables"
          @insert="insertVariable"
        />
        <el-form-item
          label="模板内容"
          required
        >
          <div class="template-fields">
            <div
              v-for="(field, index) in form.fields"
              :key="field.id"
              class="template-field"
            >
              <el-input
                v-model="field.name"
                placeholder="字段名称"
              />
              <el-input
                v-model="field.key"
                placeholder="关键词"
              />
              <el-input
                v-model="field.value"
                placeholder="字段内容"
              />
              <el-button
                type="danger"
                @click="removeField(index)"
              >
                删除
              </el-button>
            </div>
            <el-button
              type="primary"
              plain
              @click="addField"
            >
              新增
            </el-button>
          </div>
        </el-form-item>
        <el-form-item label="模板预览">
          <el-input
            v-model="form.preview"
            type="textarea"
            :rows="5"
            resize="vertical"
          />
        </el-form-item>
        <el-form-item
          v-if="channel.key === 'wechat'"
          label="尾部描述"
        >
          <el-input
            v-model="form.footer"
            clearable
          />
        </el-form-item>
      </template>

      <el-form-item label="状态">
        <el-radio-group v-model="form.enabled">
          <el-radio value="1">
            开启
          </el-radio>
          <el-radio value="0">
            关闭
          </el-radio>
        </el-radio-group>
      </el-form-item>
    </el-form>

    <template #footer>
      <el-button
        type="primary"
        :loading="saving"
        @click="emit('save')"
      >
        确定
      </el-button>
      <el-button @click="emit('update:modelValue', false)">
        取消
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import VariableButtons from './VariableButtons'

const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false
  },
  scene: {
    type: Object,
    default: null
  },
  channel: {
    type: Object,
    default: null
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

const emit = defineEmits(['update:modelValue', 'save'])

function insertVariable (value) {
  props.form.content = `${props.form.content || ''}${value}`
}

function addField () {
  props.form.fields.push({
    id: `${Date.now()}-${props.form.fields.length}`,
    name: '',
    key: '',
    value: ''
  })
}

function removeField (index) {
  props.form.fields.splice(index, 1)
}
</script>

<style scoped lang="scss">
.notification-form {
  padding: 8px 20px 0 8px;
}

.notification-form :deep(.el-form-item) {
  align-items: flex-start;
  margin-bottom: 20px;
}

.notification-form :deep(.el-form-item__label) {
  line-height: 32px;
}

.notification-form :deep(.el-form-item__content) {
  display: block;
}

.readonly-value {
  line-height: 32px;
}

.template-fields {
  width: 100%;
}

.template-field {
  display: grid;
  grid-template-columns: 1fr 1fr 1.4fr auto;
  gap: 8px;
  margin-bottom: 10px;
}

.setting-tip {
  width: 100%;
  margin-top: 8px;
  color: var(--el-text-color-secondary);
  font-size: 13px;
  line-height: 20px;
}

@media (max-width: 900px) {
  .template-field {
    grid-template-columns: 1fr;
  }
}
</style>
