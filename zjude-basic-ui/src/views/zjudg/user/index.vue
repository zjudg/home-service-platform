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
          placeholder="昵称/手机号/用户账号"
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
      <el-form-item label="注册来源">
        <el-select
          v-model="query.source"
          clearable
          placeholder="全部"
        >
          <el-option
            label="微信小程序"
            value="mini_program"
          /><el-option
            label="H5"
            value="h5"
          />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button
          type="primary"
          @click="search"
        >
          查询
        </el-button><el-button @click="reset">
          重置
        </el-button>
      </el-form-item>
    </el-form>
    <el-table
      v-loading="loading"
      :data="filteredRows"
    >
      <el-table-column
        label="头像"
        width="90"
      >
        <template #default="{ row }">
          <el-avatar :src="row.avatarUrl">
            {{ row.userNickname?.slice(0, 1) }}
          </el-avatar>
        </template>
      </el-table-column><el-table-column
        label="用户昵称"
        prop="userNickname"
      /><el-table-column
        label="账号"
        prop="account"
      /><el-table-column
        label="手机号码"
        prop="phone"
      /><el-table-column
        label="用户来源"
        prop="sourceLabel"
      /><el-table-column
        label="注册时间"
        prop="createTime"
      /><el-table-column label="操作">
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
    </el-table><pagination
      v-show="filteredRows.length"
      :total="filteredRows.length"
      :page-size="10"
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
          {{ current.userNickname }}
        </el-descriptions-item><el-descriptions-item label="账号">
          {{ current.account }}
        </el-descriptions-item><el-descriptions-item label="手机号">
          {{ current.phone || '-' }}
        </el-descriptions-item><el-descriptions-item label="注册来源">
          {{ current.sourceLabel }}
        </el-descriptions-item><el-descriptions-item label="注册时间">
          {{ current.createTime }}
        </el-descriptions-item>
      </el-descriptions>
    </el-drawer>
  </div>
</template>
<script setup>
import { computed, reactive, ref } from 'vue'
const loading = ref(false); const drawerVisible = ref(false); const current = ref({}); const query = reactive({ keyword: '', source: '', dateRange: [] })
const rows = ref([{ userId: 'U001', userNickname: 'NaBH4', account: 'u15561311', phone: '-', source: 'mini_program', sourceLabel: '微信小程序', createTime: '2023-05-05 16:32:06' }])
const filteredRows = computed(() => rows.value.filter(row => (!query.keyword || `${row.userNickname}${row.account}${row.phone}`.includes(query.keyword)) && (!query.source || row.source === query.source)))
function search () { loading.value = true; setTimeout(() => { loading.value = false }, 180) }
function reset () { Object.assign(query, { keyword: '', source: '', dateRange: [] }) }
function detail (row) { current.value = row; drawerVisible.value = true }
</script>
