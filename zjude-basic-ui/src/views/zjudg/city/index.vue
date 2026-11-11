<template>
  <div class="app-container">
    <el-form
      v-show="showSearch"
      ref="queryRef"
      :model="queryParams"
      :inline="true"
    >
      <el-form-item
        label="城市名称"
        prop="cityName"
      >
        <el-input
          v-model="queryParams.cityName"
          placeholder="请输入城市名称"
          clearable
          style="width: 200px"
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item
        label="所属省份"
        prop="provinceName"
      >
        <el-input
          v-model="queryParams.provinceName"
          placeholder="请输入省份"
          clearable
          style="width: 200px"
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item
        label="首字母"
        prop="initialLetter"
      >
        <el-input
          v-model="queryParams.initialLetter"
          placeholder="A-Z"
          clearable
          maxlength="1"
          style="width: 90px"
          @input="value => { queryParams.initialLetter = normalizeInitialLetter(value) }"
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="状态">
        <el-select
          v-model="queryParams.status"
          placeholder="全部"
          clearable
          style="width: 120px"
        >
          <el-option
            v-for="dict in sys_normal_disable"
            :key="dict.value"
            :label="dict.label"
            :value="dict.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button
          type="primary"
          icon="Search"
          @click="handleQuery"
        >
          搜索
        </el-button>
        <el-button
          icon="Refresh"
          @click="resetQuery"
        >
          重置
        </el-button>
      </el-form-item>
    </el-form>

    <el-row
      :gutter="10"
      class="mb8"
    >
      <el-col :span="1.5">
        <el-button
          v-hasPerm="['zjudg:city:add']"
          type="primary"
          plain
          icon="Plus"
          @click="handleAdd"
        >
          新增
        </el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          v-hasPerm="['zjudg:city:edit']"
          type="success"
          plain
          icon="Edit"
          :disabled="single"
          @click="handleUpdate"
        >
          修改
        </el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          v-hasPerm="['zjudg:city:remove']"
          type="danger"
          plain
          icon="Delete"
          :disabled="multiple"
          @click="handleDelete"
        >
          删除
        </el-button>
      </el-col>
      <right-toolbar
        v-model:show-search="showSearch"
        @query-table="getList"
      />
    </el-row>

    <el-table
      v-loading="loading"
      :data="cityList"
      @selection-change="handleSelectionChange"
    >
      <el-table-column
        type="selection"
        width="55"
        align="center"
      />
      <el-table-column
        label="首字母"
        prop="initialLetter"
        width="90"
        align="center"
      />
      <el-table-column
        label="城市名称"
        prop="cityName"
        min-width="160"
      />
      <el-table-column
        label="所属省份"
        prop="provinceName"
        min-width="160"
      />
      <el-table-column
        label="热门城市"
        width="100"
        align="center"
      >
        <template #default="{ row }">
          <el-tag :type="row.isHot === 'Y' ? 'warning' : 'info'">
            {{ row.isHot === 'Y' ? '是' : '否' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column
        label="状态"
        width="100"
        align="center"
      >
        <template #default="{ row }">
          <el-tag :type="row.status === '0' ? 'success' : 'info'">
            {{ row.status === '0' ? '正常' : '停用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column
        label="操作"
        width="150"
        align="center"
      >
        <template #default="{ row }">
          <el-button
            v-hasPerm="['zjudg:city:edit']"
            link
            type="primary"
            icon="Edit"
            @click="handleUpdate(row)"
          >
            修改
          </el-button>
          <el-button
            v-hasPerm="['zjudg:city:remove']"
            link
            type="danger"
            icon="Delete"
            @click="handleDelete(row)"
          >
            删除
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination
      v-show="total > 0"
      v-model:page="queryParams.pageNum"
      v-model:limit="queryParams.pageSize"
      :total="total"
      @pagination="getList"
    />

    <el-dialog
      v-model="open"
      :title="title"
      width="520px"
      append-to-body
    >
      <el-form
        ref="cityRef"
        :model="form"
        :rules="rules"
        label-width="90px"
      >
        <el-form-item
          label="城市ID"
          prop="cityId"
        >
          <el-input
            v-model="form.cityId"
            :disabled="!!form.cityId && isEdit"
            placeholder="如 6101，需与行政区划编码一致"
            maxlength="16"
          />
        </el-form-item>
        <el-form-item
          label="城市名称"
          prop="cityName"
        >
          <el-input
            v-model="form.cityName"
            placeholder="请输入城市名称"
            maxlength="64"
          />
        </el-form-item>
        <el-form-item
          label="所属省份"
          prop="provinceName"
        >
          <el-input
            v-model="form.provinceName"
            placeholder="请输入所属省份"
            maxlength="64"
          />
        </el-form-item>
        <el-form-item
          label="首字母"
          prop="initialLetter"
        >
          <el-input
            v-model="form.initialLetter"
            placeholder="A-Z，用于小程序城市分组"
            maxlength="1"
            style="width: 120px"
            @input="value => { form.initialLetter = normalizeInitialLetter(value) }"
          />
        </el-form-item>
        <el-form-item label="热门城市">
          <el-radio-group v-model="form.isHot">
            <el-radio value="Y">
              是
            </el-radio>
            <el-radio value="N">
              否
            </el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="form.status">
            <el-radio value="0">
              正常
            </el-radio>
            <el-radio value="1">
              停用
            </el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="open = false">
            取 消
          </el-button>
          <el-button
            type="primary"
            @click="submitForm"
          >
            确 定
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="ZjudgCity">
import { getCurrentInstance, onMounted, reactive, ref } from 'vue'
import { addCity, deleteCity, getCity, listCity, updateCity } from '@/api/zjudg/city'
import { useDict } from '@/utils/dict'
import {
  buildCityPayload,
  joinCityIds,
  normalizeInitialLetter,
  validateCityPayload
} from './cityForm'

const { proxy } = getCurrentInstance()
const { sys_normal_disable } = useDict('sys_normal_disable')

const cityList = ref([])
const open = ref(false)
const loading = ref(true)
const showSearch = ref(true)
const ids = ref([])
const single = ref(true)
const multiple = ref(true)
const total = ref(0)
const title = ref('')
const isEdit = ref(false)

const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  cityName: undefined,
  provinceName: undefined,
  initialLetter: undefined,
  status: undefined
})

const form = ref({})
const rules = {
  cityId: [
    { required: true, message: '城市ID不能为空', trigger: 'blur' },
    { max: 16, message: '城市ID长度不能超过16个字符', trigger: 'blur' }
  ],
  cityName: [{ required: true, message: '城市名称不能为空', trigger: 'blur' }],
  provinceName: [{ required: true, message: '所属省份不能为空', trigger: 'blur' }],
  initialLetter: [
    { required: true, message: '首字母不能为空', trigger: 'blur' },
    { pattern: /^[A-Za-z]$/, message: '首字母必须是单个英文字母', trigger: 'blur' }
  ]
}

function getList () {
  loading.value = true
  listCity(queryParams).then(res => {
    cityList.value = res.rows || []
    total.value = res.total || 0
  }).finally(() => {
    loading.value = false
  })
}

function handleQuery () {
  queryParams.pageNum = 1
  getList()
}

function resetQuery () {
  proxy.resetForm('queryRef')
  handleQuery()
}

function handleSelectionChange (selection) {
  ids.value = selection.map(item => item.cityId)
  single.value = selection.length !== 1
  multiple.value = !selection.length
}

function reset () {
  form.value = {
    cityId: undefined,
    cityName: undefined,
    provinceName: undefined,
    initialLetter: undefined,
    isHot: 'N',
    status: '0'
  }
  // 提交分支靠 isEdit 判断，必须在每次打开弹窗前复位：
  // 若上一次编辑请求失败而残留 true，紧接着的新增会被当成修改提交。
  isEdit.value = false
  proxy.resetForm('cityRef')
}

function handleAdd () {
  reset()
  open.value = true
  title.value = '新增城市'
}

function handleUpdate (row) {
  reset()
  const cityId = row.cityId || ids.value[0]
  getCity(cityId).then(res => {
    form.value = res.data
    isEdit.value = true
    open.value = true
    title.value = '修改城市'
  }).catch(() => {
    isEdit.value = false
  })
}

function submitForm () {
  proxy.$refs.cityRef.validate(valid => {
    if (!valid) return
    const payload = buildCityPayload(form.value)
    // 后端也有校验，但前端先拦能避免请求打出后才报「系统异常」
    const message = validateCityPayload(payload)
    if (message) {
      proxy.$modal.msgWarning(message)
      return
    }
    const isUpdate = isEdit.value && payload.cityId
    const submit = isUpdate ? updateCity : addCity
    submit(payload).then(() => {
      proxy.$modal.msgSuccess(isUpdate ? '修改成功' : '新增成功')
      open.value = false
      getList()
    })
  })
}

function handleDelete (row) {
  const cityIds = row?.cityId ? [row.cityId] : ids.value
  if (!cityIds.length) return
  proxy.$modal.confirm(`确认删除选中的 ${cityIds.length} 个城市吗？`).then(() => {
    return deleteCity(joinCityIds(cityIds))
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess('删除成功')
  }).catch(() => {})
}

onMounted(getList)

</script>
