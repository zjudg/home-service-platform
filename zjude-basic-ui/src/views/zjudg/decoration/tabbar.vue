<template>
  <div class="app-container tabbar-page">
    <el-card
      v-loading="loading"
      shadow="never"
    >
      <template #header>
        <div class="page-header">
          <span>底部导航</span>
          <el-button
            type="primary"
            :loading="saving"
            @click="save"
          >
            保存设置
          </el-button>
        </div>
      </template>

      <el-alert
        type="info"
        :closable="false"
        show-icon
        title="导航项的顺序与跳转页面由代码固定，这里只调整文案、图标与配色"
        class="tip"
      />

      <el-form
        label-width="120px"
        class="tabbar-form"
      >
        <el-form-item
          v-for="(item, index) in items"
          :key="item.key"
          :label="item.label"
        >
          <div class="item-row">
            <el-input
              v-model="item.name"
              placeholder="显示文案"
              maxlength="8"
              class="item-name"
            />
            <MaterialImageField
              v-model="item.icon"
              :limit="1"
              :is-show-tip="false"
              compact
              class="item-icon"
            />
            <MaterialImageField
              v-model="item.activeIcon"
              :limit="1"
              :is-show-tip="false"
              compact
              class="item-icon"
            />
            <span class="item-hint">未上传图标时使用内置图标</span>
            <el-button
              link
              type="danger"
              @click="items.splice(index, 1)"
            >
              隐藏
            </el-button>
          </div>
        </el-form-item>

        <el-form-item label="显示文字颜色">
          <el-color-picker v-model="style.textColor" />
          <span class="item-hint">默认 #a39b95</span>
        </el-form-item>

        <el-form-item label="选中文字颜色">
          <el-color-picker v-model="style.activeColor" />
          <span class="item-hint">默认 #e06a3d</span>
        </el-form-item>

        <el-form-item label="背景颜色">
          <el-color-picker v-model="style.backgroundColor" />
          <span class="item-hint">留空使用默认白底毛玻璃</span>
        </el-form-item>
      </el-form>

      <el-button
        v-if="hiddenItems.length"
        @click="restoreHidden"
      >
        恢复被隐藏的导航项
      </el-button>
    </el-card>
  </div>
</template>

<script setup name="ZjudgTabbarDecoration">
import { computed, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import MaterialImageField from '@/components/MaterialImageField'
import { parseJsonConfig, tabbarDecorationApi, toConfigMap } from '@/api/settings/zjudgDecoration'

// key 与小程序 AppTabBar 的 routeMap 一一对应，页面跳转由代码控制
const ALL_ITEMS = [
  { key: 'home', label: '首页', name: '首页' },
  { key: 'category', label: '服务', name: '服务' },
  { key: 'cart', label: '订单', name: '订单' },
  { key: 'profile', label: '我的', name: '我的' }
]

const loading = ref(false)
const saving = ref(false)

const items = ref([])
const style = reactive({
  textColor: '',
  activeColor: '',
  backgroundColor: ''
})

const hiddenItems = computed(() => ALL_ITEMS.filter(base => !items.value.some(item => item.key === base.key)))

async function load () {
  loading.value = true
  try {
    const response = await tabbarDecorationApi.listConfig()
    const map = toConfigMap(response.rows || response.data?.rows)
    const configured = parseJsonConfig(map['decorate.tabbar.items'], [])
    // 以代码里的固定顺序为准，只取后台保存过的文案与图标
    const saved = Array.isArray(configured) ? configured : []
    items.value = saved
      .filter(item => ALL_ITEMS.some(base => base.key === item.key))
      .map(item => {
        const base = ALL_ITEMS.find(entry => entry.key === item.key)
        return {
          key: item.key,
          label: base.label,
          name: item.name || base.name,
          icon: item.icon || '',
          activeIcon: item.activeIcon || ''
        }
      })
    // 没有保存过时展示全部导航项，便于运营首次配置
    if (!items.value.length) {
      items.value = ALL_ITEMS.map(base => ({ ...base, icon: '', activeIcon: '' }))
    }
    style.textColor = map['decorate.tabbar.textColor'] || ''
    style.activeColor = map['decorate.tabbar.activeColor'] || ''
    style.backgroundColor = map['decorate.tabbar.backgroundColor'] || ''
  } catch (e) {
    ElMessage.error(e?.message || '底部导航配置加载失败')
  } finally {
    loading.value = false
  }
}

function restoreHidden () {
  hiddenItems.value.forEach(base => {
    items.value.push({ ...base, icon: '', activeIcon: '' })
  })
  // 按代码顺序重排，避免恢复后顺序错乱
  items.value.sort((a, b) => ALL_ITEMS.findIndex(base => base.key === a.key) - ALL_ITEMS.findIndex(base => base.key === b.key))
}

async function save () {
  if (!items.value.length) {
    ElMessage.warning('至少保留一个导航项')
    return
  }
  saving.value = true
  try {
    await tabbarDecorationApi.saveConfig({
      'decorate.tabbar.items': JSON.stringify(items.value.map(item => ({
        key: item.key,
        name: item.name,
        icon: item.icon,
        activeIcon: item.activeIcon
      }))),
      'decorate.tabbar.textColor': style.textColor || '',
      'decorate.tabbar.activeColor': style.activeColor || '',
      'decorate.tabbar.backgroundColor': style.backgroundColor || ''
    })
    ElMessage.success('保存成功')
  } catch (e) {
    ElMessage.error(e?.message || '保存失败')
  } finally {
    saving.value = false
  }
}

load()
</script>

<style scoped>
.page-header { display: flex; align-items: center; justify-content: space-between; }
.tip { margin-bottom: 18px; }
.tabbar-form { max-width: 900px; }
.item-row { display: flex; align-items: center; gap: 12px; width: 100%; }
.item-name { width: 160px; }
.item-icon { width: 90px; }
.item-hint { color: var(--el-text-color-secondary); font-size: 12px; }
</style>
