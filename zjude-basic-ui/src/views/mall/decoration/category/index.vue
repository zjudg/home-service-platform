<template>
  <div class="app-container category-decoration">
    <NoticePanel>
      <div>商品分类页可以选择布局，不同布局适应不同层级的商品分类。最多显示三级分类</div>
    </NoticePanel>
    <section
      class="layout-section"
      aria-labelledby="layout-heading"
    >
      <h2 id="layout-heading">
        商品分类页布局
      </h2>
      <div
        class="layout-options"
        role="radiogroup"
        aria-labelledby="layout-heading"
      >
        <button
          v-for="item in layouts"
          :key="item.id"
          type="button"
          class="layout-option"
          :class="{ 'is-selected': layout === item.id }"
          role="radio"
          :aria-checked="layout === item.id"
          :aria-label="`${item.title}，${item.description}`"
          @click="layout = item.id"
        >
          <div
            class="layout-preview"
            :class="`layout-preview--${item.id}`"
            aria-hidden="true"
          >
            <div class="preview-search" />
            <div class="preview-category-tabs">
              <i /><i /><i />
            </div>
            <div class="preview-content">
              <div class="preview-categories">
                <i
                  v-for="n in item.levels"
                  :key="n"
                />
              </div>
              <div class="preview-products">
                <i
                  v-for="n in item.productCount"
                  :key="n"
                ><b /><em /></i>
              </div>
            </div>
            <div class="preview-tabbar">
              <i /><i /><i /><i />
            </div>
          </div>
          <span class="layout-choice">
            <span class="layout-title">{{ item.title }}</span>
            <span class="layout-description">{{ item.description }}</span>
          </span>
          <span
            class="selection-mark"
            aria-hidden="true"
          />
        </button>
      </div>
      <div class="layout-actions">
        <el-button
          type="primary"
          :loading="saving"
          @click="saveForm"
        >
          确定
        </el-button>
      </div>
    </section>
    <section class="content-section">
      <h2>分类页内容</h2>
      <el-form
        label-width="130px"
        class="content-form"
      >
        <el-form-item label="顶部标题">
          <el-input
            v-model="form.bannerTitle"
            maxlength="50"
          />
        </el-form-item>
        <el-form-item label="顶部副标题">
          <el-input
            v-model="form.bannerSubtitle"
            maxlength="100"
          />
        </el-form-item>
        <el-form-item label="顶部背景">
          <el-input
            v-model="form.bannerBackground"
            placeholder="支持颜色或背景图片地址"
          />
        </el-form-item>
        <el-button
          type="primary"
          :loading="saving"
          @click="saveForm"
        >
          保存内容
        </el-button>
      </el-form>
    </section>
  </div>
</template>

<script setup name="DecorationCategory">
import NoticePanel from '@/components/Settings/NoticePanel'
import { categoryDecorationApi } from '@/api/settings/decoration'

const { proxy } = getCurrentInstance()
const saving = ref(false)
const layout = ref('1')
const form = reactive({ bannerTitle: '', bannerSubtitle: '', bannerBackground: '' })
const layouts = [
  { id: '1', levels: 5, productCount: 4, title: '一级布局', description: '分类较少，侧栏快速切换' },
  { id: '2', levels: 4, productCount: 4, title: '一级布局', description: '分类较少，顶部横向切换' },
  { id: '3', levels: 5, productCount: 3, title: '二级布局', description: '分类适中，层级清晰' },
  { id: '4', levels: 6, productCount: 4, title: '三级布局', description: '分类丰富，浏览更高效' }
]

function loadSettings () {
  categoryDecorationApi.listConfig({ pageNum: 1, pageSize: 20 }).then(response => {
    const rows = response.rows || []
    const record = rows.find(item => item.configKey === 'decorate.category.layout')
    if (record && ['1', '2', '3', '4'].includes(record.configValue)) layout.value = record.configValue
    for (const key of ['bannerTitle', 'bannerSubtitle', 'bannerBackground']) {
      const item = rows.find(row => row.configKey === `decorate.category.${key}`)
      if (item) form[key] = item.configValue
    }
  })
}

async function saveForm () {
  saving.value = true
  try {
    await categoryDecorationApi.saveConfig({
      'decorate.category.layout': layout.value,
      'decorate.category.bannerTitle': form.bannerTitle,
      'decorate.category.bannerSubtitle': form.bannerSubtitle,
      'decorate.category.bannerBackground': form.bannerBackground
    })
    proxy.$modal.msgSuccess('保存成功')
  } finally {
    saving.value = false
  }
}

loadSettings()
</script>

<style scoped lang="scss">
.category-decoration {
  .layout-section { padding: 18px 4px 4px; }
  h2 { margin: 0 0 18px; color: var(--el-text-color-primary); font-size: 15px; font-weight: 600; }
  .layout-options { display: grid; grid-template-columns: repeat(4, minmax(190px, 220px)); gap: 16px; }
  .layout-option { position: relative; display: flex; min-width: 0; flex-direction: column; align-items: center; padding: 14px; border: 1px solid var(--el-border-color); border-radius: 5px; background: var(--el-bg-color-overlay); color: var(--el-text-color-primary); cursor: pointer; text-align: left; transition: border-color 120ms ease, background-color 120ms ease, box-shadow 120ms ease; }
  .layout-option:hover { border-color: var(--el-color-primary); }
  .layout-option:focus-visible { outline: 2px solid var(--el-color-primary); outline-offset: 2px; }
  .layout-option.is-selected { border-color: var(--el-color-primary); background: color-mix(in srgb, var(--el-color-primary) 9%, var(--el-bg-color-overlay)); box-shadow: 0 0 0 1px var(--el-color-primary) inset; }
  .layout-preview { display: flex; position: relative; width: 150px; height: 220px; flex: none; flex-direction: column; gap: 7px; overflow: hidden; padding: 11px 8px 26px; border: 1px solid #dfe5ec; border-radius: 3px; background: #fff; }
  .preview-search { height: 16px; flex: none; border-radius: 9px; background: #edf1f5; }
  .preview-category-tabs { display: none; height: 14px; flex: none; justify-content: space-between; gap: 4px; overflow: hidden; }
  .preview-category-tabs i { width: 31%; border-radius: 2px; background: #e8edf4; }
  .preview-content { display: flex; min-height: 0; flex: 1; gap: 5px; overflow: hidden; }
  .preview-categories { display: flex; width: 26%; flex: none; flex-direction: column; gap: 5px; overflow: hidden; }
  .preview-categories i { min-height: 10px; flex: 1; border-radius: 2px; background: #e8edf4; }
  .preview-products { display: grid; min-width: 0; flex: 1; grid-template-columns: repeat(2, minmax(0, 1fr)); grid-auto-rows: minmax(0, 1fr); gap: 5px; overflow: hidden; }
  .preview-products i { display: flex; min-width: 0; flex-direction: column; gap: 3px; overflow: hidden; padding: 3px; border-radius: 3px; background: #f0f4f0; }
  .preview-products b { display: block; min-height: 0; flex: 1; border-radius: 2px; background: linear-gradient(135deg, #e2eee7, #f1eee4); }
  .preview-products em { display: block; width: 75%; height: 3px; flex: none; border-radius: 2px; background: #dce3e9; }
  .preview-tabbar { position: absolute; right: 0; bottom: 0; left: 0; display: flex; height: 22px; align-items: center; justify-content: space-around; border-top: 1px solid #e8ebef; background: #fff; }
  .preview-tabbar i { width: 9px; height: 9px; border-radius: 50%; background: #aeb7c2; }
  .layout-preview--2 .preview-category-tabs { display: flex; }
  .layout-preview--2 .preview-categories { display: none; }
  .layout-preview--2 .preview-products { grid-template-columns: repeat(2, minmax(0, 1fr)); }
  .layout-preview--3 .preview-categories { width: 34%; }
  .layout-preview--3 .preview-products { grid-template-columns: minmax(0, 1fr); }
  .layout-preview--4 .preview-categories { width: 27%; }
  .layout-preview--4 .preview-products { grid-template-columns: minmax(0, 1fr); }
  .layout-choice { display: flex; width: 100%; min-height: 50px; flex-direction: column; justify-content: center; gap: 4px; padding-top: 10px; }
  .layout-title { color: var(--el-text-color-primary); font-size: 14px; font-weight: 600; }
  .layout-description { color: var(--el-text-color-secondary); font-size: 12px; line-height: 1.4; }
  .selection-mark { position: absolute; top: 10px; right: 10px; width: 17px; height: 17px; border: 1px solid var(--el-border-color); border-radius: 50%; background: var(--el-bg-color); }
  .is-selected .selection-mark { border: 5px solid var(--el-color-primary); background: var(--el-bg-color); }
  .layout-actions { margin-top: 18px; }
  .content-section { margin-top: 28px; padding: 18px 4px 4px; border-top: 1px solid var(--el-border-color-lighter); }
  .content-form { max-width: 760px; }
  @media (max-width: 1120px) { .layout-options { grid-template-columns: repeat(2, minmax(190px, 220px)); } }
  @media (max-width: 560px) { .layout-options { grid-template-columns: minmax(0, 1fr); } .layout-option { width: 100%; } }
}
</style>
