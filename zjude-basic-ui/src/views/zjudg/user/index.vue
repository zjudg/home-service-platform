<template>
  <div class="app-container">
    <el-form
      :inline="true"
      :model="query"
      class="query-form"
    >
      <el-form-item label="用户信息">
        <el-input
          v-model="query.keyword"
          clearable
          placeholder="昵称/手机号"
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="注册时间">
        <el-date-picker
          v-model="query.dateRange"
          type="daterange"
          value-format="YYYY-MM-DD"
          range-separator="-"
          start-placeholder="开始时间"
          end-placeholder="结束时间"
        />
      </el-form-item>
      <!-- 当前只存在小程序注册用户，来源为固定值，不再提供会产生空结果的筛选项 -->
      <el-form-item label="注册来源">
        <el-select
          v-model="query.source"
          clearable
          placeholder="全部"
        >
          <el-option
            label="微信小程序"
            value="mini_program"
          />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button
          type="primary"
          @click="handleQuery"
        >
          查询
        </el-button>
        <el-button @click="resetQuery">
          重置
        </el-button>
      </el-form-item>
    </el-form>

    <el-table
      v-loading="loading"
      :data="rows"
      empty-text="暂无用户"
    >
      <el-table-column
        label="头像"
        width="90"
      >
        <template #default="{ row }">
          <el-avatar :src="assetUrl(row.avatar) || undefined">
            {{ (row.nickName || '?').slice(0, 1) }}
          </el-avatar>
        </template>
      </el-table-column>
      <el-table-column
        label="用户昵称"
        prop="nickName"
        show-overflow-tooltip
      />
      <el-table-column
        label="手机号码"
        width="140"
      >
        <template #default="{ row }">
          {{ row.phoneNumber || '-' }}
        </template>
      </el-table-column>
      <el-table-column
        label="默认地址"
        min-width="220"
        show-overflow-tooltip
      >
        <template #default="{ row }">
          {{ row.address || '-' }}
        </template>
      </el-table-column>
      <el-table-column
        label="用户来源"
        prop="sourceLabel"
        width="120"
      />
      <el-table-column
        label="注册时间"
        prop="createTime"
        width="180"
      />
      <el-table-column
        label="操作"
        width="100"
      >
        <template #default="{ row }">
          <el-button
            link
            type="primary"
            @click="detail(row)"
          >
            详情
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination
      v-show="total > 0"
      v-model:page="query.pageNum"
      v-model:limit="query.pageSize"
      :total="total"
      @pagination="getList"
    />

    <el-drawer
      v-model="drawerVisible"
      title="用户详情"
      size="420px"
    >
      <el-descriptions
        :column="1"
        border
      >
        <el-descriptions-item label="用户昵称">
          {{ current.nickName || '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="手机号">
          {{ current.phoneNumber || '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="默认地址">
          {{ current.address || '未设置' }}
        </el-descriptions-item>
        <el-descriptions-item label="地址联系人">
          {{ current.addressContactName || '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="地址联系电话">
          {{ current.addressContactPhone || '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="注册来源">
          {{ current.sourceLabel || '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="注册时间">
          {{ current.createTime || '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="当前状态">
          {{ current.status === '0' ? '正常' : '停用' }}
        </el-descriptions-item>
        <el-descriptions-item
          v-hasRole="['admin']"
          label="小程序管理员"
        >
          <el-switch
            v-model="current.miniAdmin"
            active-text="可发起分享"
            inactive-text="普通用户"
            @change="changeMiniAdmin"
          />
        </el-descriptions-item>
      </el-descriptions>
    </el-drawer>
  </div>
</template>

<script setup name="ZjudgUser">
import { reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { listZjudgUsers, setZjudgMiniAdmin } from '@/api/zjudg/user'

function assetUrl (value) {
  if (!value) return ''
  if (/^https?:\/\//i.test(value)) return value
  const base = import.meta.env.VITE_APP_BASE_API || ''
  return `${base}${value.startsWith('/') ? value : `/${value}`}`
}

const loading = ref(false)
const rows = ref([])
const total = ref(0)
const drawerVisible = ref(false)
const current = ref({})

const query = reactive({
  keyword: '',
  source: '',
  dateRange: [],
  pageNum: 1,
  pageSize: 10
})

function getList () {
  loading.value = true
  const [beginTime, endTime] = query.dateRange || []
  listZjudgUsers({
    keyword: query.keyword,
    source: query.source,
    beginTime,
    endTime,
    pageNum: query.pageNum,
    pageSize: query.pageSize
  })
    .then(response => {
      rows.value = response.rows || []
      total.value = response.total || 0
    })
    .catch(e => {
      ElMessage.error(e?.message || '用户列表加载失败')
    })
    .finally(() => {
      loading.value = false
    })
}

function handleQuery () {
  query.pageNum = 1
  getList()
}

function resetQuery () {
  Object.assign(query, { keyword: '', source: '', dateRange: [], pageNum: 1, pageSize: 10 })
  getList()
}

function detail (row) {
  current.value = { ...row }
  drawerVisible.value = true
}

function changeMiniAdmin (enabled) {
  setZjudgMiniAdmin(current.value.userId, enabled)
    .then(() => {
      const target = rows.value.find(item => item.userId === current.value.userId)
      if (target) target.miniAdmin = enabled
      ElMessage.success(enabled ? '已设置为小程序管理员' : '已取消小程序管理员')
    })
    .catch(() => {
      current.value.miniAdmin = !enabled
    })
}

getList()
</script>
