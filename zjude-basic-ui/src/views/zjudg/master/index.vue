<template>
  <div class="app-container">
    <el-form
      :inline="true"
      :model="query"
    >
      <el-form-item label="师傅信息">
        <el-input
          v-model="query.keyword"
          clearable
          placeholder="师傅姓名/手机号"
        />
      </el-form-item><el-form-item label="用户信息">
        <el-input
          v-model="query.userKeyword"
          clearable
          placeholder="用户昵称/用户账号"
        />
      </el-form-item><el-form-item label="所在地区">
        <el-select
          v-model="query.region"
          clearable
          placeholder="请选择"
        >
          <el-option
            label="北京市"
            value="北京市"
          /><el-option
            label="上海市"
            value="上海市"
          />
        </el-select>
      </el-form-item><el-form-item label="首页推荐">
        <el-select
          v-model="query.recommend"
          clearable
        >
          <el-option
            label="全部"
            value=""
          /><el-option
            label="推荐"
            value="1"
          /><el-option
            label="不推荐"
            value="0"
          />
        </el-select>
      </el-form-item><el-form-item label="师傅状态">
        <el-select
          v-model="query.status"
          clearable
        >
          <el-option
            label="正常"
            value="0"
          /><el-option
            label="停用"
            value="1"
          />
        </el-select>
      </el-form-item><el-form-item>
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
    <div class="mb8">
      <el-button
        type="primary"
        @click="openForm"
      >
        添加师傅
      </el-button>
    </div><el-table :data="rows">
      <el-table-column
        label="用户昵称"
        prop="userNickname"
      /><el-table-column
        label="账号"
        prop="account"
      /><el-table-column
        label="师傅姓名"
        prop="masterName"
      /><el-table-column
        label="手机号码"
        prop="phone"
      /><el-table-column
        label="所在地区"
        prop="region"
      /><el-table-column label="状态">
        <template #default="{ row }">
          <el-tag :type="row.status === '0' ? 'success' : 'info'">
            {{ row.status === '0' ? '正常' : '停用' }}
          </el-tag>
        </template>
      </el-table-column><el-table-column label="首页推荐">
        <template #default="{ row }">
          {{ row.recommend === '1' ? '推荐' : '不推荐' }}
        </template>
      </el-table-column><el-table-column
        label="添加时间"
        prop="createTime"
      /><el-table-column label="操作">
        <template #default="{ row }">
          <el-button
            link
            type="primary"
            @click="openForm(row)"
          >
            编辑
          </el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-dialog
      v-model="visible"
      :title="form.masterId ? '编辑师傅' : '添加师傅'"
      width="560px"
    >
      <el-form
        :model="form"
        label-width="95px"
      >
        <el-form-item label="师傅姓名">
          <el-input v-model="form.masterName" />
        </el-form-item><el-form-item label="账号">
          <el-input v-model="form.account" />
        </el-form-item><el-form-item label="手机号">
          <el-input v-model="form.phone" />
        </el-form-item><el-form-item label="所在地区">
          <el-input v-model="form.region" />
        </el-form-item><el-form-item label="首页推荐">
          <el-switch
            v-model="form.recommend"
            active-value="1"
            inactive-value="0"
          />
        </el-form-item><el-form-item label="状态">
          <el-switch
            v-model="form.status"
            active-value="0"
            inactive-value="1"
          />
        </el-form-item>
      </el-form><template #footer>
        <el-button @click="visible = false">
          取消
        </el-button><el-button
          type="primary"
          @click="save"
        >
          确定
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>
<script setup>
import { reactive, ref } from 'vue'; import { ElMessage } from 'element-plus'
const visible = ref(false); const query = reactive({ keyword: '', userKeyword: '', region: '', recommend: '', status: '' }); const form = reactive({}); const rows = ref([])
function search () { rows.value = rows.value.filter(row => (!query.keyword || `${row.masterName}${row.phone}`.includes(query.keyword)) && (!query.region || row.region === query.region)) }
function reset () { Object.assign(query, { keyword: '', userKeyword: '', region: '', recommend: '', status: '' }) }
function openForm (row) { Object.keys(form).forEach(key => delete form[key]); Object.assign(form, row || { masterName: '', account: '', phone: '', region: '', recommend: '0', status: '0' }); visible.value = true }
function save () { if (!form.masterName) return ElMessage.warning('请输入师傅姓名'); if (!form.masterId) form.masterId = `M_${Date.now()}`; const index = rows.value.findIndex(row => row.masterId === form.masterId); if (index >= 0) rows.value[index] = { ...form }; else rows.value.push({ ...form, userNickname: '-', createTime: new Date().toLocaleString() }); visible.value = false; ElMessage.success('保存成功') }
</script><style scoped>.mb8{margin-bottom:8px}</style>
