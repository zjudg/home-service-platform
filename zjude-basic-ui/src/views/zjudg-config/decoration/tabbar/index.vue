<template>
  <div class="app-container tabbar-decoration">
    <NoticePanel><div>设置移动端底部导航图标和名称</div></NoticePanel>
    <el-tabs v-model="activeTab">
      <el-tab-pane
        label="菜单按钮设置"
        name="items"
      >
        <el-table
          :data="form.items"
          border
        >
          <el-table-column
            label="导航名称"
            prop="name"
            min-width="180"
            align="center"
          />
          <el-table-column
            label="选中图标"
            min-width="150"
            align="center"
          >
            <template #default="{ row }">
              <el-image
                v-if="row.activeIcon"
                :src="imageUrl(row.activeIcon)"
                class="tab-icon"
                fit="contain"
              /><span v-else>-</span>
            </template>
          </el-table-column>
          <el-table-column
            label="未选中图标"
            min-width="150"
            align="center"
          >
            <template #default="{ row }">
              <el-image
                v-if="row.icon"
                :src="imageUrl(row.icon)"
                class="tab-icon"
                fit="contain"
              /><span v-else>-</span>
            </template>
          </el-table-column>
          <el-table-column
            label="操作"
            width="140"
            align="center"
          >
            <template #default="{ $index }">
              <el-button
                link
                type="primary"
                @click="openDialog($index)"
              >
                编辑
              </el-button>
            </template>
          </el-table-column>
        </el-table>
        <div class="save-row">
          <el-button
            type="primary"
            :loading="saving"
            @click="saveForm"
          >
            确定
          </el-button>
        </div>
      </el-tab-pane>
      <el-tab-pane
        label="其它设置"
        name="other"
      >
        <el-form
          label-width="130px"
          class="color-form"
        >
          <el-form-item label="文本颜色">
            <el-color-picker v-model="form.textColor" />
            <div class="setting-tip">
              导航文本未选中时的颜色
            </div>
          </el-form-item>
          <el-form-item label="选中颜色">
            <el-color-picker v-model="form.activeColor" />
            <div class="setting-tip">
              导航文本选中时的颜色
            </div>
          </el-form-item>
          <el-form-item label="背景颜色">
            <el-color-picker v-model="form.backgroundColor" />
          </el-form-item>
          <el-form-item label="顶部边框">
            <el-select
              v-model="form.borderStyle"
              style="width: 180px"
            >
              <el-option
                label="黑色"
                value="black"
              />
              <el-option
                label="白色"
                value="white"
              />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button
              type="primary"
              :loading="saving"
              @click="saveForm"
            >
              确定
            </el-button>
          </el-form-item>
        </el-form>
        <div class="tabbar-preview">
          <div
            v-for="item in form.items"
            :key="item.key"
            :style="{ color: item.key === 'home' ? form.activeColor : form.textColor }"
          >
            <img
              v-if="item.key === 'home' ? item.activeIcon : item.icon"
              :src="imageUrl(item.key === 'home' ? item.activeIcon : item.icon)"
              alt=""
            >
            <span>{{ item.name }}</span>
          </div>
        </div>
      </el-tab-pane>
    </el-tabs>

    <el-dialog
      v-model="dialogVisible"
      title="编辑导航按钮"
      width="620px"
      append-to-body
      destroy-on-close
    >
      <el-form
        :model="itemForm"
        label-width="120px"
      >
        <el-form-item label="导航名称">
          <el-input
            v-model="itemForm.name"
            maxlength="12"
          />
        </el-form-item>
        <el-form-item label="选中图标">
          <MaterialImageField
            v-model="itemForm.activeIcon"
            :limit="1"
            :is-show-tip="false"
            compact
          />
        </el-form-item>
        <el-form-item label="未选中图标">
          <MaterialImageField
            v-model="itemForm.icon"
            :limit="1"
            :is-show-tip="false"
            compact
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button
          type="primary"
          @click="submitItem"
        >
          确定
        </el-button>
        <el-button @click="dialogVisible = false">
          取消
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="DecorationTabbar">
import NoticePanel from '@/components/Settings/NoticePanel'
import MaterialImageField from '@/components/MaterialImageField'
import { tabbarDecorationApi } from '@/api/settings/decoration'

const { proxy } = getCurrentInstance()
const activeTab = ref('items')
const saving = ref(false)
const dialogVisible = ref(false)
const editingIndex = ref(-1)
const form = reactive({ items: defaultItems(), textColor: '#666666', activeColor: '#6DAA35', backgroundColor: '#FFFFFF', borderStyle: 'black' })
const itemForm = reactive({ key: '', name: '', icon: '', activeIcon: '' })

function defaultItems () {
  return [
    { key: 'home', name: '首页', icon: '', activeIcon: '' },
    { key: 'category', name: '分类', icon: '', activeIcon: '' },
    { key: 'cart', name: '购物车', icon: '', activeIcon: '' },
    { key: 'profile', name: '我的', icon: '', activeIcon: '' }
  ]
}

function loadSettings () {
  tabbarDecorationApi.listConfig({ pageNum: 1, pageSize: 20 }).then(response => {
    const rows = response.rows || []
    const getValue = key => rows.find(item => item.configKey === key)?.configValue
    try {
      const items = JSON.parse(getValue('decorate.tabbar.items') || 'null')
      if (Array.isArray(items)) form.items = items
    } catch {}
    if (getValue('decorate.tabbar.textColor')) form.textColor = getValue('decorate.tabbar.textColor')
    if (getValue('decorate.tabbar.activeColor')) form.activeColor = getValue('decorate.tabbar.activeColor')
    if (getValue('decorate.tabbar.backgroundColor')) form.backgroundColor = getValue('decorate.tabbar.backgroundColor')
    if (getValue('decorate.tabbar.borderStyle')) form.borderStyle = getValue('decorate.tabbar.borderStyle')
  })
}

function openDialog (index) {
  editingIndex.value = index
  Object.assign(itemForm, form.items[index])
  dialogVisible.value = true
}

function submitItem () {
  if (!itemForm.name.trim()) {
    proxy.$modal.msgError('导航名称不能为空')
    return
  }
  form.items.splice(editingIndex.value, 1, { ...itemForm })
  dialogVisible.value = false
}

async function saveForm () {
  saving.value = true
  try {
    await tabbarDecorationApi.saveConfig({
      'decorate.tabbar.items': JSON.stringify(form.items),
      'decorate.tabbar.textColor': form.textColor,
      'decorate.tabbar.activeColor': form.activeColor,
      'decorate.tabbar.backgroundColor': form.backgroundColor,
      'decorate.tabbar.borderStyle': form.borderStyle
    })
    proxy.$modal.msgSuccess('保存成功')
  } finally {
    saving.value = false
  }
}

function imageUrl (value) {
  return value && value.startsWith('http') ? value : (import.meta.env.VITE_APP_BASE_API || '') + (value || '')
}

loadSettings()
</script>

<style scoped lang="scss">
.tabbar-decoration {
  .tab-icon { width: 46px; height: 46px; }
  .save-row { margin-top: 16px; }
  .color-form { max-width: 700px; padding: 22px 10px; }
  .color-form :deep(.el-form-item__content) { display: block; }
  .setting-tip { margin-top: 8px; color: var(--el-text-color-secondary); font-size: 13px; }
  .tabbar-preview { display: flex; max-width: 480px; justify-content: space-around; padding: 14px 0; border-top: 1px solid var(--el-border-color); }
  .tabbar-preview div { display: flex; min-width: 60px; flex-direction: column; align-items: center; gap: 4px; font-size: 12px; }
  .tabbar-preview img { width: 24px; height: 24px; object-fit: contain; }
}
</style>
