<template>
  <div class="app-container material-page">
    <div class="material-heading">
      <div>
        <h2>素材中心</h2>
        <p>统一管理家政服务使用的图片和视频素材</p>
      </div>
      <div class="material-total">
        <strong>{{ filteredMaterials.length }}</strong>
        <span>{{ activeType === 'image' ? '张图片' : '个视频' }}</span>
      </div>
    </div>
    <el-tabs
      v-model="activeType"
      class="material-tabs"
    >
      <el-tab-pane
        label="图片"
        name="image"
      /><el-tab-pane
        label="视频"
        name="video"
      />
    </el-tabs>
    <div class="material-layout">
      <aside class="group-panel">
        <div class="group-heading">
          <span>素材分组</span>
          <el-tooltip content="添加分组">
            <el-button
              text
              circle
              aria-label="添加分组"
              @click="addGroup"
            >
              <el-icon><FolderAdd /></el-icon>
            </el-button>
          </el-tooltip>
        </div>
        <div
          v-for="group in groups"
          :key="group.id"
          class="group-item"
          :class="{ active: activeGroup === group.id }"
          @click="activeGroup = group.id"
        >
          <el-icon class="group-icon">
            <Folder />
          </el-icon><span class="group-name">{{ group.name }}</span><span class="group-count">{{ groupCount(group.id) }}</span><el-dropdown
            v-if="group.id !== 'all'"
            trigger="click"
          >
            <el-icon><More /></el-icon><template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item @click="removeGroup(group.id)">
                  删除分组
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div><el-button
          class="add-group"
          plain
          @click="addGroup"
        >
          <el-icon><FolderAdd /></el-icon>
          添加分组
        </el-button>
      </aside>
      <main class="material-content">
        <div class="toolbar">
          <div class="toolbar-actions">
            <el-button
              type="primary"
              @click="uploadVisible = true"
            >
              <el-icon><Upload /></el-icon>
              本地上传
            </el-button><el-button
              plain
              :disabled="!selected.length"
              @click="removeSelected"
            >
              <el-icon><Delete /></el-icon>
              删除
            </el-button><el-button
              plain
              :disabled="!selected.length"
            >
              <el-icon><Rank /></el-icon>
              移动
            </el-button>
          </div><div class="toolbar-tools">
            <span class="selection-hint">已选 {{ selected.length }} 项</span>
            <el-input
              v-model="keyword"
              class="search"
              placeholder="搜索素材名称"
              clearable
            >
              <template #prefix>
                <el-icon><Search /></el-icon>
              </template>
            </el-input>
            <div class="view-switcher">
              <el-tooltip content="列表视图">
                <el-button
                  text
                  :class="{ active: viewMode === 'list' }"
                  aria-label="列表视图"
                  @click="viewMode = 'list'"
                >
                  <el-icon><List /></el-icon>
                </el-button>
              </el-tooltip>
              <el-tooltip content="网格视图">
                <el-button
                  text
                  :class="{ active: viewMode === 'grid' }"
                  aria-label="网格视图"
                  @click="viewMode = 'grid'"
                >
                  <el-icon><Grid /></el-icon>
                </el-button>
              </el-tooltip>
            </div>
          </div>
        </div>
        <div class="selection-bar">
          <el-checkbox
            v-model="allChecked"
            :disabled="!filteredMaterials.length"
            @change="toggleAll"
          >
            当前页全选
          </el-checkbox>
          <span class="result-count">共 {{ filteredMaterials.length }} 项</span>
        </div>
        <div
          v-if="filteredMaterials.length"
          class="material-grid"
          :class="{ 'is-list': viewMode === 'list' }"
        >
          <div
            v-for="item in filteredMaterials"
            :key="item.id"
            class="material-card"
            :class="{ selected: selected.includes(item.id) }"
          >
            <el-checkbox
              v-model="selected"
              class="card-checkbox"
              :value="item.id"
              aria-label="选择素材"
            /><div class="preview-box">
              <el-image
                v-if="item.type === 'image'"
                :src="item.url"
                fit="cover"
              /><video
                v-else
                :src="item.url"
                controls
              />
              <span class="type-badge">{{ item.type === 'image' ? '图片' : '视频' }}</span>
            </div><div class="material-meta">
              <div
                class="material-name"
                :title="item.name"
              >
                {{ item.name }}
              </div>
              <span class="material-group">{{ groupName(item.groupId) }}</span>
            </div>
          </div>
        </div><div
          v-else
          class="material-empty"
        >
          <el-empty
            :image-size="76"
            description="暂无素材"
          >
            <el-button
              type="primary"
              @click="uploadVisible = true"
            >
              <el-icon><Upload /></el-icon>
              上传{{ activeType === 'image' ? '图片' : '视频' }}
            </el-button>
          </el-empty>
        </div>
      </main>
    </div>
    <el-dialog
      v-model="uploadVisible"
      title="本地上传素材"
      width="520px"
    >
      <el-form label-width="80px">
        <el-form-item label="分组">
          <el-select v-model="uploadGroup">
            <el-option
              v-for="group in groups.slice(1)"
              :key="group.id"
              :label="group.name"
              :value="group.id"
            />
          </el-select>
        </el-form-item><el-form-item label="文件">
          <image-upload
            v-if="activeType === 'image'"
            v-model="uploadUrl"
            :limit="9"
          /><file-upload
            v-else
            v-model="uploadUrl"
            :limit="3"
          />
        </el-form-item>
      </el-form><template #footer>
        <el-button @click="uploadVisible = false">
          取消
        </el-button><el-button
          type="primary"
          @click="confirmUpload"
        >
          确定
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>
<script setup>
import { computed, ref } from 'vue'; import { ElMessage, ElMessageBox } from 'element-plus'; import { Delete, Folder, FolderAdd, Grid, List, More, Rank, Search, Upload } from '@element-plus/icons-vue'
const activeType = ref('image'); const activeGroup = ref('all'); const keyword = ref(''); const viewMode = ref('grid'); const selected = ref([]); const allChecked = ref(false); const uploadVisible = ref(false); const uploadUrl = ref(''); const uploadGroup = ref('ungrouped'); const groups = ref([{ id: 'all', name: '全部' }, { id: 'ungrouped', name: '未分组' }, { id: 'bottom', name: '底部导航' }]); const materials = ref([])
const filteredMaterials = computed(() => materials.value.filter(item => item.type === activeType.value && (!keyword.value || item.name.includes(keyword.value)) && (activeGroup.value === 'all' || item.groupId === activeGroup.value)))
function groupCount (groupId) { return materials.value.filter(item => item.type === activeType.value && (groupId === 'all' || item.groupId === groupId)).length }
function groupName (groupId) { return groups.value.find(group => group.id === groupId)?.name || '未分组' }
function toggleAll (value) { selected.value = value ? filteredMaterials.value.map(item => item.id) : [] }
function confirmUpload () { if (!uploadUrl.value) return ElMessage.warning('请选择素材文件'); const urls = uploadUrl.value.split(',').filter(Boolean); urls.forEach((url, index) => materials.value.push({ id: `${Date.now()}_${index}`, type: activeType.value, url, name: `本地素材${materials.value.length + 1}`, groupId: uploadGroup.value })); uploadUrl.value = ''; uploadVisible.value = false; ElMessage.success('上传成功') }
function removeSelected () { materials.value = materials.value.filter(item => !selected.value.includes(item.id)); selected.value = [] }
function addGroup () { ElMessageBox.prompt('请输入分组名称', '添加分组').then(({ value }) => { if (value) groups.value.push({ id: `group_${Date.now()}`, name: value }) }) }
function removeGroup (id) { groups.value = groups.value.filter(group => group.id !== id); activeGroup.value = 'all' }
</script>
<style scoped>
.material-page { min-height: calc(100vh - 112px); color: var(--el-text-color-primary); }
.material-heading { display: flex; align-items: center; justify-content: space-between; margin: 0 2px 14px; }
.material-heading h2 { margin: 0; font-size: 20px; font-weight: 600; letter-spacing: .2px; }
.material-heading p { margin: 6px 0 0; color: var(--el-text-color-secondary); font-size: 13px; }
.material-total { display: flex; align-items: baseline; gap: 7px; padding: 10px 14px; border: 1px solid var(--el-border-color-light); border-radius: 8px; background: var(--el-bg-color-overlay); }
.material-total strong { color: var(--el-color-primary); font-size: 20px; font-weight: 600; }
.material-total span { color: var(--el-text-color-secondary); font-size: 12px; }
.material-tabs { margin-bottom: 12px; }
.material-layout { display: flex; min-height: 610px; overflow: hidden; border: 1px solid var(--el-border-color-light); border-radius: 10px; background: var(--el-bg-color); box-shadow: 0 8px 24px rgba(0, 0, 0, .08); }
.group-panel { flex: 0 0 216px; padding: 18px 12px; border-right: 1px solid var(--el-border-color-light); background: var(--el-bg-color-overlay); }
.group-heading { display: flex; align-items: center; justify-content: space-between; padding: 0 8px 14px; color: var(--el-text-color-primary); font-size: 13px; font-weight: 600; }
.group-heading .el-button { color: var(--el-text-color-secondary); }
.group-heading .el-button:hover { color: var(--el-color-primary); }
.group-item { display: flex; align-items: center; gap: 9px; min-height: 42px; margin: 3px 0; padding: 0 10px; border-radius: 7px; color: var(--el-text-color-regular); cursor: pointer; transition: background-color .15s ease, color .15s ease; }
.group-item:hover { background: var(--el-fill-color-light); color: var(--el-text-color-primary); }
.group-item.active { color: var(--el-color-primary); background: color-mix(in srgb, var(--el-color-primary) 12%, transparent); }
.group-icon { flex: none; color: var(--el-color-primary); }
.group-name { overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.group-count { margin-left: auto; color: var(--el-text-color-secondary); font-size: 12px; }
.group-item .el-dropdown { display: flex; margin-left: 2px; opacity: 0; }
.group-item:hover .el-dropdown, .group-item.active .el-dropdown { opacity: 1; }
.add-group { width: 100%; margin-top: 20px; border-style: dashed; }
.material-content { flex: 1; min-width: 0; padding: 18px 22px 22px; }
.toolbar { display: flex; align-items: center; justify-content: space-between; gap: 18px; padding-bottom: 16px; border-bottom: 1px solid var(--el-border-color-light); }
.toolbar-actions, .toolbar-tools { display: flex; align-items: center; gap: 9px; }
.toolbar-tools { min-width: 0; }
.selection-hint, .result-count { color: var(--el-text-color-secondary); font-size: 12px; white-space: nowrap; }
.search { width: min(280px, 28vw); }
.view-switcher { display: flex; align-items: center; padding: 2px; border: 1px solid var(--el-border-color-light); border-radius: 6px; }
.view-switcher .el-button { width: 30px; height: 28px; margin: 0; color: var(--el-text-color-secondary); }
.view-switcher .el-button.active { color: var(--el-color-primary); background: color-mix(in srgb, var(--el-color-primary) 13%, transparent); }
.selection-bar { display: flex; align-items: center; justify-content: space-between; min-height: 44px; border-bottom: 1px solid var(--el-border-color-light); }
.material-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(205px, 1fr)); gap: 16px; padding-top: 18px; }
.material-card { position: relative; overflow: hidden; border: 1px solid var(--el-border-color-light); border-radius: 8px; background: var(--el-bg-color-overlay); transition: border-color .15s ease, transform .15s ease, box-shadow .15s ease; }
.material-card:hover, .material-card.selected { border-color: var(--el-color-primary); box-shadow: 0 6px 18px rgba(0, 0, 0, .12); transform: translateY(-1px); }
.card-checkbox { position: absolute; top: 10px; right: 10px; z-index: 2; }
.preview-box { position: relative; overflow: hidden; aspect-ratio: 16 / 10; background: var(--el-fill-color); }
.preview-box .el-image, .preview-box video { display: block; width: 100%; height: 100%; object-fit: cover; }
.type-badge { position: absolute; bottom: 8px; left: 8px; padding: 3px 7px; border: 1px solid rgba(255, 255, 255, .18); border-radius: 4px; background: rgba(0, 0, 0, .54); color: #fff; font-size: 11px; }
.material-meta { padding: 11px 12px 13px; }
.material-name { overflow: hidden; color: var(--el-text-color-primary); font-size: 13px; text-overflow: ellipsis; white-space: nowrap; }
.material-group { display: block; margin-top: 6px; color: var(--el-text-color-secondary); font-size: 12px; }
.material-empty { display: grid; min-height: 470px; place-items: center; }
.material-empty :deep(.el-empty) { padding: 22px 0; }
.material-empty :deep(.el-empty__description) { margin-top: 8px; color: var(--el-text-color-secondary); }
.material-grid.is-list { grid-template-columns: 1fr; }
.material-grid.is-list .material-card { display: grid; grid-template-columns: 220px 1fr; }
.material-grid.is-list .preview-box { aspect-ratio: auto; height: 132px; }
.material-grid.is-list .material-meta { display: flex; flex-direction: column; justify-content: center; padding: 16px 18px; }
@media (max-width: 960px) { .toolbar { align-items: flex-start; flex-direction: column; gap: 12px; } .toolbar-tools { width: 100%; } .search { width: min(320px, 100%); margin-left: auto; } }
@media (max-width: 680px) { .material-heading { align-items: flex-start; flex-direction: column; gap: 12px; } .material-layout { flex-direction: column; } .group-panel { flex-basis: auto; border-right: 0; border-bottom: 1px solid var(--el-border-color-light); } .group-item { display: inline-flex; width: auto; margin-right: 5px; } .add-group { width: auto; margin-top: 12px; } .material-content { padding: 16px; } .toolbar-tools { flex-wrap: wrap; } .search { order: -1; width: 100%; margin-left: 0; } .material-grid.is-list .material-card { grid-template-columns: 140px 1fr; } .material-grid.is-list .preview-box { height: 96px; } }
</style>
