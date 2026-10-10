<template>
  <div class="app-container channel-menu">
    <NoticePanel>配置微信公众号菜单，支持一级菜单和二级菜单；保存后可发布到微信公众号。</NoticePanel>
    <el-card shadow="never">
      <template #header>
        <div class="page-header">
          <strong>公众号菜单</strong><el-button
            type="primary"
            @click="openDialog()"
          >
            新增菜单
          </el-button>
        </div>
      </template>
      <div class="menu-workspace">
        <div class="phone-preview">
          <div class="phone-head">
            公众号
          </div><div class="phone-body">
            <span>9:38</span>
          </div>
          <div class="phone-nav">
            <button
              v-for="item in roots"
              :key="item.menuId"
              type="button"
              @click="selectedRoot = item.menuId"
            >
              {{ item.menuName }}
            </button><button
              type="button"
              class="add"
              @click="openDialog()"
            >
              ＋
            </button>
          </div>
        </div>
        <el-table
          :data="menus"
          border
          stripe
          class="menu-table"
        >
          <el-table-column
            prop="menuName"
            label="菜单名称"
            min-width="140"
          /><el-table-column
            prop="menuType"
            label="菜单类型"
            width="120"
          >
            <template #default="scope">
              {{ scope.row.menuType === 'LINK' ? '链接' : '关键字' }}
            </template>
          </el-table-column><el-table-column
            prop="keyword"
            label="关键字"
            min-width="160"
          /><el-table-column
            prop="link"
            label="链接地址"
            min-width="220"
            show-overflow-tooltip
          /><el-table-column
            prop="sortNum"
            label="排序"
            width="80"
          /><el-table-column
            label="状态"
            width="90"
          >
            <template #default="scope">
              <el-tag :type="scope.row.status === '1' ? 'success' : 'info'">
                {{ scope.row.status === '1' ? '启用' : '停用' }}
              </el-tag>
            </template>
          </el-table-column><el-table-column
            label="操作"
            width="180"
          >
            <template #default="scope">
              <el-button
                link
                type="primary"
                @click="openDialog(scope.row)"
              >
                编辑
              </el-button><el-button
                link
                type="danger"
                @click="remove(scope.row)"
              >
                删除
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </el-card>
    <el-dialog
      v-model="dialog.visible"
      :title="dialog.form.menuId ? '编辑菜单' : '新增菜单'"
      width="520px"
      append-to-body
    >
      <el-form
        ref="formRef"
        :model="dialog.form"
        :rules="rules"
        label-width="100px"
      >
        <el-form-item label="上级菜单">
          <el-select
            v-model="dialog.form.parentId"
            clearable
            placeholder="一级菜单"
          >
            <el-option
              label="一级菜单"
              value="0"
            /><el-option
              v-for="item in roots"
              :key="item.menuId"
              :label="item.menuName"
              :value="item.menuId"
            />
          </el-select>
        </el-form-item><el-form-item
          label="菜单名称"
          prop="menuName"
        >
          <el-input
            v-model="dialog.form.menuName"
            maxlength="8"
            show-word-limit
          />
        </el-form-item><el-form-item label="菜单类型">
          <el-radio-group v-model="dialog.form.menuType">
            <el-radio value="KEYWORD">
              关键字
            </el-radio><el-radio value="LINK">
              链接
            </el-radio>
          </el-radio-group>
        </el-form-item><el-form-item
          v-if="dialog.form.menuType === 'KEYWORD'"
          label="关键字"
        >
          <el-input
            v-model="dialog.form.keyword"
            placeholder="请输入回复关键字"
          />
        </el-form-item><el-form-item
          v-else
          label="链接地址"
        >
          <el-input
            v-model="dialog.form.link"
            placeholder="https://"
          />
        </el-form-item><el-form-item label="排序">
          <el-input-number
            v-model="dialog.form.sortNum"
            :min="0"
            :max="99"
          />
        </el-form-item><el-form-item label="状态">
          <el-switch
            v-model="dialog.form.status"
            active-value="1"
            inactive-value="0"
            active-text="启用"
            inactive-text="停用"
            inline-prompt
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialog.visible = false">
          取消
        </el-button><el-button
          type="primary"
          :loading="dialog.saving"
          @click="save"
        >
          确定
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="ChannelMenu">
import { channelApi } from '@/api/settings/channel'
import NoticePanel from '@/components/Settings/NoticePanel.vue'
const { proxy } = getCurrentInstance()
const menus = ref([]); const selectedRoot = ref(''); const formRef = ref()
const dialog = reactive({ visible: false, saving: false, form: {} })
const roots = computed(() => menus.value.filter(item => !item.parentId || item.parentId === '0'))
const rules = { menuName: [{ required: true, message: '请输入菜单名称', trigger: 'blur' }] }
function load () { channelApi.listMenus({ pageNum: 1, pageSize: 200 }).then(response => { menus.value = response.rows || [] }) }
function openDialog (row) { dialog.form = { parentId: '0', menuType: 'KEYWORD', status: '1', sortNum: 0, ...(row || {}) }; dialog.visible = true }
async function save () { await formRef.value.validate(); dialog.saving = true; try { await channelApi.saveMenu(dialog.form); dialog.visible = false; proxy.$modal.msgSuccess('保存成功'); load() } finally { dialog.saving = false } }
function remove (row) { proxy.$modal.confirm(`是否删除菜单“${row.menuName}”？`).then(() => channelApi.removeMenu(row.menuId)).then(() => { proxy.$modal.msgSuccess('删除成功'); load() }) }
load()
</script>

<style scoped lang="scss">
.channel-menu { .page-header { display:flex; justify-content:space-between; align-items:center; } .menu-workspace { display:grid; grid-template-columns:280px minmax(0,1fr); gap:24px; align-items:start; } .phone-preview { width:260px; height:460px; border:10px solid #20252d; border-radius:24px; overflow:hidden; background:#f5f7fa; box-shadow:0 8px 24px rgba(0,0,0,.1); } .phone-head { height:44px; display:flex; align-items:center; justify-content:center; color:#fff; background:#2c3036; font-weight:600; } .phone-body { height:350px; padding-top:10px; text-align:center; color:#999; } .phone-body span { background:#aaa; color:#fff; border-radius:3px; padding:2px 6px; font-size:11px; } .phone-nav { height:56px; display:flex; border-top:1px solid #ddd; background:#fff; } .phone-nav button { flex:1; min-width:0; border:0; border-right:1px solid #eee; background:#fff; color:#606266; cursor:pointer; overflow:hidden; text-overflow:ellipsis; white-space:nowrap; } .phone-nav .add { color:#409eff; font-size:22px; } .menu-table { min-width:0; } }
@media (max-width: 900px) { .channel-menu .menu-workspace { grid-template-columns:1fr; } .channel-menu .phone-preview { margin:0 auto; } }
</style>
