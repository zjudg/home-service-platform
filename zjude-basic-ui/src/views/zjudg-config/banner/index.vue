<template>
  <div class="app-container">
    <el-form
      v-show="showSearch"
      ref="queryRef"
      :model="queryParams"
      :inline="true"
    >
      <el-form-item
        label="轮播标题"
        prop="bannerTitle"
      >
        <el-input
          v-model="queryParams.bannerTitle"
          placeholder="请输入轮播图标题"
          clearable
          style="width: 200px"
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item
        label="广告位置"
        prop="bannerPosition"
      >
        <el-select
          v-model="queryParams.bannerPosition"
          placeholder="请选择广告位置"
          clearable
          style="width: 200px"
        >
          <el-option
            v-for="dict in zjudg_banner_position"
            :key="dict.value"
            :label="dict.label"
            :value="dict.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item
        label="跳转类型"
        prop="linkType"
      >
        <el-select
          v-model="queryParams.linkType"
          placeholder="请选择跳转类型"
          clearable
          style="width: 200px"
        >
          <el-option
            v-for="dict in zjudg_banner_link_type"
            :key="dict.value"
            :label="dict.label"
            :value="dict.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item
        label="状态"
        prop="status"
      >
        <el-select
          v-model="queryParams.status"
          placeholder="请选择状态"
          clearable
          style="width: 200px"
        >
          <el-option
            v-for="dict in sys_normal_disable"
            :key="dict.value"
            :label="dict.label"
            :value="dict.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="创建时间">
        <el-date-picker
          v-model="dateRange"
          value-format="YYYY-MM-DD"
          type="daterange"
          range-separator="-"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
        />
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
          v-hasPerm="['zjudg:banner:add']"
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
          v-hasPerm="['zjudg:banner:edit']"
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
          v-hasPerm="['zjudg:banner:remove']"
          type="danger"
          plain
          icon="Delete"
          :disabled="multiple"
          @click="handleDelete"
        >
          删除
        </el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          v-hasPerm="['zjudg:banner:export']"
          type="warning"
          plain
          icon="Download"
          @click="handleExport"
        >
          导出
        </el-button>
      </el-col>
      <right-toolbar
        v-model:show-search="showSearch"
        @query-table="getList"
      ></right-toolbar>
    </el-row>

    <el-table
      v-loading="loading"
      :data="bannerList"
      @selection-change="handleSelectionChange"
    >
      <el-table-column
        type="selection"
        width="55"
        align="center"
      />
      <el-table-column
        label="轮播标题"
        align="center"
        prop="bannerTitle"
        :show-overflow-tooltip="true"
      />
      <el-table-column
        label="轮播图片"
        align="center"
        prop="imageUrl"
        width="120"
      >
        <template #default="scope">
          <image-preview
            :src="scope.row.imageUrl"
            :width="90"
            :height="45"
          />
        </template>
      </el-table-column>
      <el-table-column
        label="广告位置"
        align="center"
        prop="bannerPosition"
        width="100"
      >
        <template #default="scope">
          <dict-tag
            :options="zjudg_banner_position"
            :value="scope.row.bannerPosition"
          />
        </template>
      </el-table-column>
      <el-table-column
        label="跳转类型"
        align="center"
        prop="linkType"
        width="100"
      >
        <template #default="scope">
          <dict-tag
            :options="zjudg_banner_link_type"
            :value="scope.row.linkType"
          />
        </template>
      </el-table-column>
      <el-table-column
        label="跳转地址"
        align="center"
        prop="linkUrl"
        :show-overflow-tooltip="true"
      />
      <el-table-column
        label="显示顺序"
        align="center"
        prop="bannerSort"
        width="90"
      />
      <el-table-column
        label="状态"
        align="center"
        prop="status"
        width="90"
      >
        <template #default="scope">
          <dict-tag
            :options="sys_normal_disable"
            :value="scope.row.status"
          />
        </template>
      </el-table-column>
      <el-table-column
        label="生效时间"
        align="center"
        width="320"
      >
        <template #default="scope">
          <span v-if="scope.row.startTime && scope.row.endTime">
            {{ scope.row.startTime }} ~ {{ scope.row.endTime }}
          </span>
          <span v-else>长期有效</span>
        </template>
      </el-table-column>
      <el-table-column
        label="创建时间"
        align="center"
        prop="createTime"
        width="180"
      >
        <template #default="scope">
          <span>{{ parseTime(scope.row.createTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column
        label="操作"
        width="180"
        align="center"
        class-name="small-padding fixed-width"
      >
        <template #default="scope">
          <el-button
            v-hasPerm="['zjudg:banner:edit']"
            link
            type="primary"
            icon="Edit"
            @click="handleUpdate(scope.row)"
          >
            修改
          </el-button>
          <el-button
            v-hasPerm="['zjudg:banner:remove']"
            link
            type="primary"
            icon="Delete"
            @click="handleDelete(scope.row)"
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

    <!-- 添加或修改轮播图对话框 -->
    <el-dialog
      v-model="open"
      :title="title"
      width="680px"
      append-to-body
    >
      <el-form
        ref="bannerRef"
        :model="form"
        :rules="rules"
        label-width="100px"
      >
        <el-form-item
          label="轮播标题"
          prop="bannerTitle"
        >
          <el-input
            v-model="form.bannerTitle"
            placeholder="请输入轮播图标题"
            maxlength="100"
          />
        </el-form-item>
        <el-form-item
          label="轮播图片"
          prop="imageUrl"
        >
          <MaterialImageField
            v-model="form.imageUrl"
            :limit="1"
            :file-size="5"
            :file-type="['png', 'jpg', 'jpeg', 'gif']"
          />
        </el-form-item>
        <el-form-item
          label="广告位置"
          prop="bannerPosition"
        >
          <el-select
            v-model="form.bannerPosition"
            placeholder="请选择广告位置"
            style="width: 100%"
          >
            <el-option
              v-for="dict in zjudg_banner_position"
              :key="dict.value"
              :label="dict.label"
              :value="dict.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item
          label="跳转类型"
          prop="linkType"
        >
          <el-radio-group
            v-model="form.linkType"
            @change="handleLinkTypeChange"
          >
            <el-radio
              v-for="dict in zjudg_banner_link_type"
              :key="dict.value"
              :label="dict.value"
            >
              {{ dict.label }}
            </el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item
          v-if="form.linkType !== '0'"
          label="跳转地址"
          prop="linkUrl"
        >
          <el-input
            v-model="form.linkUrl"
            :placeholder="form.linkType === '2' ? '请输入以 http(s) 开头的完整链接' : '请输入内部页面路径，如 /pages/goods/detail'"
            maxlength="500"
          />
        </el-form-item>
        <el-form-item
          label="显示顺序"
          prop="bannerSort"
        >
          <el-input-number
            v-model="form.bannerSort"
            controls-position="right"
            :min="0"
          />
        </el-form-item>
        <el-form-item
          label="生效时间"
        >
          <el-date-picker
            v-model="effectiveRange"
            type="datetimerange"
            value-format="YYYY-MM-DD HH:mm:ss"
            range-separator="至"
            start-placeholder="开始时间"
            end-placeholder="结束时间"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item
          label="状态"
          prop="status"
        >
          <el-radio-group v-model="form.status">
            <el-radio
              v-for="dict in sys_normal_disable"
              :key="dict.value"
              :label="dict.value"
            >
              {{ dict.label }}
            </el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item
          label="备注"
          prop="remark"
        >
          <el-input
            v-model="form.remark"
            type="textarea"
            placeholder="请输入内容"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button
            type="primary"
            @click="submitForm"
          >
            确 定
          </el-button>
          <el-button @click="cancel">
            取 消
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="Banner">
import MaterialImageField from '@/components/MaterialImageField'
import { listBanner, getBanner, addBanner, updateBanner, delBanner } from '@/api/zjudg-config/banner'

const { proxy } = getCurrentInstance()
const { zjudg_banner_link_type, zjudg_banner_position, sys_normal_disable } = proxy.useDict('zjudg_banner_link_type', 'zjudg_banner_position', 'sys_normal_disable')

const bannerList = ref([])
const open = ref(false)
const loading = ref(true)
const showSearch = ref(true)
const ids = ref([])
const single = ref(true)
const multiple = ref(true)
const total = ref(0)
const title = ref('')
const dateRange = ref([])
const effectiveRange = ref([])

const data = reactive({
  form: {},
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    bannerTitle: undefined,
    bannerPosition: undefined,
    linkType: undefined,
    status: undefined
  },
  rules: {
    bannerTitle: [{ required: true, message: '轮播图标题不能为空', trigger: 'blur' }],
    imageUrl: [{ required: true, message: '轮播图片不能为空', trigger: 'change' }],
    bannerPosition: [{ required: true, message: '广告位置不能为空', trigger: 'change' }],
    linkType: [{ required: true, message: '跳转类型不能为空', trigger: 'change' }],
    bannerSort: [{ required: true, message: '显示顺序不能为空', trigger: 'blur' }],
    status: [{ required: true, message: '状态不能为空', trigger: 'change' }],
    linkUrl: [{ validator: validateLinkUrl, trigger: 'blur' }]
  }
})

const { queryParams, form, rules } = toRefs(data)

/** 跳转地址联动校验 */
function validateLinkUrl (rule, value, callback) {
  if (form.value.linkType !== '0' && !value) {
    callback(new Error('跳转地址不能为空'))
    return
  }
  if (form.value.linkType === '2' && value && !/^https?:\/\//.test(value)) {
    callback(new Error('外部链接必须以 http 或 https 开头'))
    return
  }
  callback()
}

/** 查询轮播图列表 */
function getList () {
  loading.value = true
  listBanner(proxy.addDateRange(queryParams.value, dateRange.value)).then(response => {
    bannerList.value = response.rows
    total.value = response.total
    loading.value = false
  })
}

/** 取消按钮 */
function cancel () {
  open.value = false
  reset()
}

/** 表单重置 */
function reset () {
  form.value = {
    bannerId: undefined,
    bannerTitle: undefined,
    imageUrl: undefined,
    linkType: '0',
    linkUrl: undefined,
    bannerPosition: 'index',
    bannerSort: 0,
    status: '0',
    startTime: undefined,
    endTime: undefined,
    remark: undefined
  }
  effectiveRange.value = []
  proxy.resetForm('bannerRef')
}

/** 搜索按钮操作 */
function handleQuery () {
  queryParams.value.pageNum = 1
  getList()
}

/** 重置按钮操作 */
function resetQuery () {
  dateRange.value = []
  proxy.resetForm('queryRef')
  handleQuery()
}

/** 多选框选中数据 */
function handleSelectionChange (selection) {
  ids.value = selection.map(item => item.bannerId)
  single.value = selection.length !== 1
  multiple.value = !selection.length
}

/** 跳转类型变更时联动清空跳转地址 */
function handleLinkTypeChange () {
  if (form.value.linkType === '0') {
    form.value.linkUrl = ''
  }
  proxy.$refs.bannerRef?.clearValidate('linkUrl')
}

/** 新增按钮操作 */
function handleAdd () {
  reset()
  open.value = true
  title.value = '添加轮播图'
}

/** 修改按钮操作 */
function handleUpdate (row) {
  reset()
  const bannerId = row.bannerId || ids.value
  getBanner(bannerId).then(response => {
    form.value = response.data
    effectiveRange.value = response.data.startTime && response.data.endTime
      ? [response.data.startTime, response.data.endTime]
      : []
    open.value = true
    title.value = '修改轮播图'
  })
}

/** 提交按钮 */
function submitForm () {
  proxy.$refs.bannerRef.validate(valid => {
    if (valid) {
      const range = effectiveRange.value || []
      form.value.startTime = range[0] || null
      form.value.endTime = range[1] || null
      if (form.value.bannerId !== undefined) {
        updateBanner(form.value).then(() => {
          proxy.$modal.msgSuccess('修改成功')
          open.value = false
          getList()
        })
      } else {
        addBanner(form.value).then(() => {
          proxy.$modal.msgSuccess('新增成功')
          open.value = false
          getList()
        })
      }
    }
  })
}

/** 删除按钮操作 */
function handleDelete (row) {
  const bannerIds = row.bannerId || ids.value
  proxy.$modal.confirm('是否确认删除轮播图编号为"' + bannerIds + '"的数据项？').then(function () {
    return delBanner(bannerIds)
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess('删除成功')
  }).catch(() => {})
}

/** 导出按钮操作 */
function handleExport () {
  proxy.download('zjudg/banner/export', {
    ...proxy.addDateRange(queryParams.value, dateRange.value)
  }, `banner_${new Date().getTime()}.xlsx`)
}

getList()
</script>
