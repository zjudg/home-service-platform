<template>
  <div class="material-image-field">
    <ImageUpload
      v-if="!pickerOnly"
      v-model="imageValue"
      :limit="limit"
      v-bind="$attrs"
    />
    <el-button
      class="material-image-field__trigger"
      link
      type="primary"
      @click="openPicker"
    >
      {{ compact ? '素材库' : '从素材库选择' }}
    </el-button>

    <el-dialog
      v-model="visible"
      title="从素材库选择图片"
      width="760px"
      append-to-body
    >
      <div class="material-image-field__toolbar">
        <span>选择的图片将替换当前内容</span>
        <el-input
          v-model="keyword"
          clearable
          placeholder="搜索素材名称"
          @keyup.enter="search"
          @clear="search"
        >
          <template #append>
            <el-button @click="search">
              搜索
            </el-button>
          </template>
        </el-input>
      </div>
      <div
        v-loading="loading"
        class="material-image-field__body"
      >
        <div
          v-if="materials.length"
          class="material-image-field__grid"
        >
          <button
            v-for="item in materials"
            :key="item.materialId"
            class="material-image-field__item"
            :class="{ 'is-selected': isSelected(item) }"
            type="button"
            @click="toggleMaterial(item)"
          >
            <el-image
              class="material-image-field__image"
              :src="assetUrl(item.materialUrl)"
              fit="cover"
            />
            <span :title="item.materialName">{{ item.materialName || '未命名素材' }}</span>
          </button>
        </div>
        <el-empty
          v-else-if="!loading"
          description="没有可选图片。先上传到素材中心，再回到这里搜索。"
        >
          <el-link
            :href="materialCenterHref"
            target="_blank"
            type="primary"
            :underline="false"
          >
            在新窗口打开素材中心
          </el-link>
        </el-empty>
      </div>
      <pagination
        v-show="total > 0"
        v-model:page="pageNum"
        v-model:limit="pageSize"
        :total="total"
        @pagination="loadMaterials"
      />
      <template #footer>
        <el-button @click="visible = false">
          取消
        </el-button>
        <el-button
          type="primary"
          :disabled="!selectedMaterials.length"
          @click="confirmSelection"
        >
          使用所选图片
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import ImageUpload from '@/components/ImageUpload'
import { listMaterial } from '@/api/zjudg/material'

defineOptions({ inheritAttrs: false })

const props = defineProps({
  modelValue: {
    type: [String, Object, Array],
    default: ''
  },
  limit: {
    type: Number,
    default: 5
  },
  pickerOnly: {
    type: Boolean,
    default: false
  },
  compact: {
    type: Boolean,
    default: false
  }
})
const emit = defineEmits(['update:modelValue'])

const router = useRouter()
const materialCenterHref = router.resolve('/zjudg/material').href
const visible = ref(false)
const loading = ref(false)
const keyword = ref('')
const materials = ref([])
const selectedMaterials = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(12)
const imageValue = computed({
  get: () => props.modelValue,
  set: value => emit('update:modelValue', value)
})

function assetUrl (url) {
  if (!url || /^https?:\/\//.test(url)) return url || ''
  return `${import.meta.env.VITE_APP_BASE_API || ''}${url}`
}

function isSelected (item) {
  return selectedMaterials.value.some(selected => selected.materialId === item.materialId)
}

function toggleMaterial (item) {
  const index = selectedMaterials.value.findIndex(selected => selected.materialId === item.materialId)
  if (index >= 0) {
    selectedMaterials.value.splice(index, 1)
    return
  }
  if (props.limit <= 1) {
    selectedMaterials.value = [item]
    return
  }
  if (selectedMaterials.value.length >= props.limit) {
    ElMessage.warning(`最多选择${props.limit}张图片`)
    return
  }
  selectedMaterials.value.push(item)
}

function openPicker () {
  keyword.value = ''
  pageNum.value = 1
  selectedMaterials.value = []
  visible.value = true
  loadMaterials()
}

async function loadMaterials () {
  loading.value = true
  try {
    const response = await listMaterial({
      pageNum: pageNum.value,
      pageSize: pageSize.value,
      materialType: 'image',
      materialName: keyword.value
    })
    materials.value = response.rows || response.data?.rows || []
    total.value = response.total || response.data?.total || 0
  } catch (error) {
    ElMessage.error(error?.message || '素材加载失败')
  } finally {
    loading.value = false
  }
}

function search () {
  pageNum.value = 1
  loadMaterials()
}

function confirmSelection () {
  if (!selectedMaterials.value.length) return
  const urls = selectedMaterials.value.map(item => item.materialUrl).filter(Boolean)
  if (urls.length) {
    emit('update:modelValue', props.limit === 1 ? urls[0] : urls.join(','))
    visible.value = false
    ElMessage.success('已使用素材库图片')
  }
}
</script>

<style scoped lang="scss">
.material-image-field__trigger {
  display: flex;
  margin-top: 4px;
}

.material-image-field__toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 16px;
  color: var(--el-text-color-secondary);
  font-size: 13px;
}

.material-image-field__toolbar .el-input {
  width: 280px;
}

.material-image-field__body {
  min-height: 250px;
}

.material-image-field__grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 12px;
  max-height: 430px;
  overflow-y: auto;
  padding: 2px;
}

.material-image-field__item {
  min-width: 0;
  padding: 0;
  overflow: hidden;
  border: 1px solid var(--el-border-color);
  border-radius: 6px;
  background: var(--el-bg-color);
  color: var(--el-text-color-primary);
  text-align: left;
  cursor: pointer;
}

.material-image-field__item:hover,
.material-image-field__item.is-selected {
  border-color: var(--el-color-primary);
  box-shadow: 0 0 0 1px var(--el-color-primary-light-7);
}

.material-image-field__image {
  display: block;
  width: 100%;
  height: 126px;
  background: var(--el-fill-color-light);
}

.material-image-field__item span {
  display: block;
  overflow: hidden;
  padding: 8px;
  font-size: 12px;
  text-overflow: ellipsis;
  white-space: nowrap;
}

@media (max-width: 760px) {
  .material-image-field__toolbar {
    align-items: stretch;
    flex-direction: column;
  }

  .material-image-field__toolbar .el-input {
    width: 100%;
  }

  .material-image-field__grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}
</style>
