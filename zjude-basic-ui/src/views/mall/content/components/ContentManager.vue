<template>
  <div class="app-container content-page">
    <NoticePanel v-if="notice.length">
      <div
        v-for="line in notice"
        :key="line"
      >
        {{ line }}
      </div>
    </NoticePanel>

    <el-tabs
      v-if="kind === 'ad'"
      v-model="query.platform"
      @tab-change="getList"
    >
      <el-tab-pane
        label="移动端商城"
        name="MOBILE"
      />
      <el-tab-pane
        label="PC端商城"
        name="PC"
      />
    </el-tabs>

    <el-form
      :inline="true"
      class="query-form"
    >
      <el-form-item :label="searchLabel">
        <el-input
          v-model="query.title"
          clearable
          :placeholder="`请输入${searchLabel}`"
          @keyup.enter="getList"
        />
      </el-form-item>
      <el-form-item
        v-if="isDocument"
        :label="kind === 'help' ? '帮助分类' : '文章分类'"
      >
        <el-select
          v-model="query.categoryId"
          clearable
          placeholder="全部"
        >
          <el-option
            v-for="item in categoryOptions"
            :key="item.contentId"
            :label="item.title"
            :value="item.contentId"
          />
        </el-select>
      </el-form-item>
      <el-form-item
        v-if="kind === 'ad'"
        label="广告位置"
      >
        <el-select
          v-model="query.slotId"
          clearable
          placeholder="全部"
        >
          <el-option
            v-for="item in slotOptions"
            :key="item.contentId"
            :label="item.title"
            :value="item.contentId"
          />
        </el-select>
      </el-form-item>
      <el-form-item
        v-if="kind === 'article'"
        label="商城公告"
      >
        <el-select
          v-model="query.isAnnouncement"
          clearable
          placeholder="全部"
        >
          <el-option
            label="是"
            value="1"
          />
          <el-option
            label="否"
            value="0"
          />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button
          type="primary"
          icon="Search"
          @click="getList"
        >
          查询
        </el-button>
        <el-button
          icon="Refresh"
          @click="resetQuery"
        >
          重置
        </el-button>
      </el-form-item>
    </el-form>

    <el-button
      v-hasPerm="['settings:content:add']"
      type="primary"
      icon="Plus"
      class="mb8"
      @click="openDialog()"
    >
      {{ addLabel }}
    </el-button>

    <el-table
      v-loading="loading"
      :data="rows"
      border
    >
      <el-table-column
        type="index"
        label="序号"
        width="68"
        align="center"
      />
      <el-table-column
        v-if="kind === 'ad'"
        label="终端"
        width="115"
        align="center"
      >
        <template #default="{ row }">
          {{ row.platform === 'PC' ? 'PC端商城' : '移动端商城' }}
        </template>
      </el-table-column>
      <el-table-column
        :label="titleLabel"
        prop="title"
        min-width="170"
        show-overflow-tooltip
      />
      <el-table-column
        v-if="kind === 'ad'"
        label="广告位置"
        prop="slotName"
        min-width="150"
      />
      <el-table-column
        v-if="isDocument"
        :label="kind === 'help' ? '帮助分类' : '文章分类'"
        prop="categoryName"
        min-width="130"
      />
      <el-table-column
        v-if="kind === 'slot'"
        label="建议图片尺寸"
        min-width="150"
        align="center"
      >
        <template #default="{ row }">
          {{ row.imageWidth || '-' }} × {{ row.imageHeight || '-' }}
        </template>
      </el-table-column>
      <el-table-column
        v-if="kind === 'ad' || isDocument"
        label="图片"
        width="130"
        align="center"
      >
        <template #default="{ row }">
          <el-image
            v-if="row.image"
            :src="imageUrl(row.image)"
            class="thumb"
            fit="cover"
          />
          <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column
        v-if="kind === 'article'"
        label="商城公告"
        width="100"
        align="center"
      >
        <template #default="{ row }">
          {{ row.isAnnouncement === '1' ? '是' : '否' }}
        </template>
      </el-table-column>
      <el-table-column
        v-if="isDocument"
        label="浏览量"
        prop="viewCount"
        width="90"
        align="center"
      />
      <el-table-column
        v-if="isDocument"
        label="点赞量"
        prop="likeCount"
        width="90"
        align="center"
      />
      <el-table-column
        v-if="isDocument || kind === 'ad'"
        label="排序"
        prop="sortNum"
        width="85"
        align="center"
      />
      <el-table-column
        label="状态"
        width="100"
        align="center"
      >
        <template #default="{ row }">
          <el-tag :type="row.status === '1' ? 'success' : 'info'">
            {{ row.status === '1' ? activeLabel : inactiveLabel }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column
        v-if="isDocument"
        label="创建时间"
        prop="createTime"
        width="170"
      />
      <el-table-column
        label="操作"
        width="185"
        fixed="right"
        align="center"
      >
        <template #default="{ row }">
          <el-button
            v-hasPerm="['settings:content:edit']"
            link
            type="primary"
            @click="openDialog(row)"
          >
            编辑
          </el-button>
          <el-button
            v-hasPerm="['settings:content:edit']"
            link
            :type="row.status === '1' ? 'warning' : 'success'"
            @click="toggleStatus(row)"
          >
            {{ row.status === '1' ? inactiveLabel : activeLabel }}
          </el-button>
          <el-button
            v-hasPerm="['settings:content:delete']"
            link
            type="danger"
            @click="removeRow(row)"
          >
            删除
          </el-button>
        </template>
      </el-table-column>
      <template #empty>
        <el-empty description="暂无数据" />
      </template>
    </el-table>
    <pagination
      v-show="total > 0"
      v-model:page="query.pageNum"
      v-model:limit="query.pageSize"
      :total="total"
      @pagination="getList"
    />

    <el-dialog
      v-model="dialogVisible"
      :title="`${form.contentId ? '编辑' : '新增'}${titleLabel}`"
      :width="isDocument ? '1050px' : '700px'"
      append-to-body
      destroy-on-close
    >
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="120px"
      >
        <template v-if="kind === 'slot'">
          <el-form-item
            label="广告位"
            prop="title"
          >
            <el-input
              v-model="form.title"
              maxlength="100"
            />
          </el-form-item>
          <el-form-item label="图片建议大小">
            <div class="dimension-fields">
              <el-input-number
                v-model="form.imageWidth"
                :min="1"
                :controls="false"
                placeholder="宽度"
              />
              <span>宽 ×</span>
              <el-input-number
                v-model="form.imageHeight"
                :min="1"
                :controls="false"
                placeholder="高度"
              />
              <span>高度</span>
            </div>
          </el-form-item>
          <el-form-item label="广告位状态">
            <el-radio-group v-model="form.status">
              <el-radio value="1">
                启用
              </el-radio><el-radio value="0">
                停用
              </el-radio>
            </el-radio-group>
          </el-form-item>
        </template>

        <template v-else-if="kind === 'ad'">
          <el-form-item
            label="广告标题"
            prop="title"
          >
            <el-input
              v-model="form.title"
              maxlength="100"
            />
          </el-form-item>
          <el-form-item
            label="广告位置"
            prop="slotId"
          >
            <el-select
              v-model="form.slotId"
              placeholder="请选择广告位置"
              class="wide-control"
            >
              <el-option
                v-for="item in slotOptions.filter(item => item.status === '1')"
                :key="item.contentId"
                :label="item.title"
                :value="item.contentId"
              />
            </el-select>
          </el-form-item>
          <el-form-item
            label="广告图片"
            prop="image"
          >
            <ImageUpload
              v-model="form.image"
              :limit="1"
              :is-show-tip="false"
            />
          </el-form-item>
          <el-form-item label="链接类型">
            <el-radio-group v-model="form.linkType">
              <el-radio value="PAGE">
                商城页面
              </el-radio><el-radio value="PRODUCT">
                商品页面
              </el-radio><el-radio value="CUSTOM">
                自定义链接
              </el-radio>
            </el-radio-group>
          </el-form-item>
          <el-form-item
            v-if="form.linkType"
            label="跳转链接"
          >
            <el-input
              v-model="form.link"
              :placeholder="form.linkType === 'CUSTOM' ? '请输入完整链接' : '请输入页面或商品标识'"
            />
          </el-form-item>
          <el-form-item label="广告状态">
            <el-radio-group v-model="form.status">
              <el-radio value="1">
                启用
              </el-radio><el-radio value="0">
                停用
              </el-radio>
            </el-radio-group>
          </el-form-item>
          <el-form-item label="排序">
            <el-input-number
              v-model="form.sortNum"
              :min="0"
              :max="999999"
            />
          </el-form-item>
        </template>

        <template v-else-if="isCategory">
          <el-form-item
            :label="titleLabel"
            prop="title"
          >
            <el-input
              v-model="form.title"
              maxlength="100"
            />
          </el-form-item>
          <el-form-item label="分类状态">
            <el-radio-group v-model="form.status">
              <el-radio value="1">
                启用
              </el-radio><el-radio value="0">
                停用
              </el-radio>
            </el-radio-group>
          </el-form-item>
        </template>

        <template v-else>
          <el-form-item
            :label="titleLabel"
            prop="title"
          >
            <el-input
              v-model="form.title"
              maxlength="100"
            />
          </el-form-item>
          <el-form-item
            :label="kind === 'help' ? '帮助分类' : '文章分类'"
            prop="categoryId"
          >
            <el-select
              v-model="form.categoryId"
              placeholder="请选择分类"
              class="wide-control"
            >
              <el-option
                v-for="item in categoryOptions.filter(item => item.status === '1')"
                :key="item.contentId"
                :label="item.title"
                :value="item.contentId"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="简介">
            <el-input
              v-model="form.intro"
              maxlength="500"
            />
          </el-form-item>
          <el-form-item :label="`${titleLabel}封面图`">
            <ImageUpload
              v-model="form.image"
              :limit="1"
              :is-show-tip="false"
            /><div class="form-tip">
              建议尺寸：500 × 500 像素
            </div>
          </el-form-item>
          <el-form-item
            v-if="kind === 'article'"
            label="商城公告"
          >
            <el-radio-group v-model="form.isAnnouncement">
              <el-radio value="1">
                是
              </el-radio><el-radio value="0">
                否
              </el-radio>
            </el-radio-group>
            <span class="form-tip inline-tip">商城公告会在商城首页新闻速递显示</span>
          </el-form-item>
          <el-form-item label="排序">
            <el-input-number
              v-model="form.sortNum"
              :min="0"
              :max="999999"
            />
          </el-form-item>
          <el-form-item :label="`${titleLabel}状态`">
            <el-radio-group v-model="form.status">
              <el-radio value="1">
                显示
              </el-radio><el-radio value="0">
                隐藏
              </el-radio>
            </el-radio-group>
          </el-form-item>
          <el-form-item
            :label="`${titleLabel}内容`"
            prop="content"
          >
            <Editor
              v-model="form.content"
              :height="360"
            />
          </el-form-item>
        </template>
      </el-form>
      <template #footer>
        <el-button
          type="primary"
          :loading="saving"
          @click="submit"
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

<script setup>
import NoticePanel from '@/components/Settings/NoticePanel'
import Editor from '@/components/Editor'
import { contentApis } from '@/api/mall/content'

const props = defineProps({ kind: { type: String, required: true } })
const { proxy } = getCurrentInstance()
const api = computed(() => contentApis[props.kind])
const typeLabels = { ad: '广告', slot: '广告位', help: '帮助', helpCategory: '帮助分类', article: '文章', articleCategory: '文章分类' }
const titleLabel = computed(() => typeLabels[props.kind])
const searchLabel = computed(() => props.kind === 'slot' ? '广告位' : titleLabel.value + (props.kind.endsWith('Category') ? '' : '标题'))
const addLabel = computed(() => `新增${titleLabel.value}`)
const isDocument = computed(() => ['help', 'article'].includes(props.kind))
const isCategory = computed(() => ['helpCategory', 'articleCategory'].includes(props.kind))
const activeLabel = computed(() => props.kind === 'ad' || props.kind === 'slot' ? '启用' : '显示')
const inactiveLabel = computed(() => props.kind === 'ad' || props.kind === 'slot' ? '停用' : '隐藏')
const notice = computed(() => ({
  ad: ['* 平台管理广告信息，系统默认了部分广告位，允许新建广告位。', '* 广告区分不同的终端，移动端商城（含H5、小程序、APP）和PC端商城。', '* 广告链接可以选择商城页面、商品页面、自定义链接。', '* 广告停用之后，商城不展示该广告。'],
  slot: ['* 平台维护广告位，允许新增自定义广告位。'],
  help: ['* 平台发布帮助内容，帮助排序值越小越靠前；排序值相同时新增文章在前。'],
  helpCategory: ['* 平台维护帮助分类，方便帮助分类整理。'],
  article: ['* 平台发布文章，可在商城新闻资讯栏目查看。', '* 设置文章为商城公告后，文章标题会在商城首页的新闻公告轮播显示。'],
  articleCategory: ['* 平台维护文章分类，方便文章分类整理。']
}[props.kind] || []))
const loading = ref(false)
const saving = ref(false)
const dialogVisible = ref(false)
const rows = ref([])
const total = ref(0)
const slotOptions = ref([])
const categoryOptions = ref([])
const formRef = ref()
const query = reactive({ pageNum: 1, pageSize: 10, title: '', categoryId: '', slotId: '', platform: 'MOBILE', isAnnouncement: '' })
const form = reactive(createForm())
const rules = computed(() => ({
  title: [{ required: true, message: `${titleLabel.value}名称不能为空`, trigger: 'blur' }],
  slotId: [{ required: true, message: '请选择广告位置', trigger: 'change' }],
  categoryId: [{ required: true, message: '请选择分类', trigger: 'change' }],
  image: [{ required: props.kind === 'ad', message: '请上传广告图片', trigger: 'change' }],
  content: [{ required: isDocument.value, message: '内容不能为空', trigger: 'blur' }]
}))

function createForm () {
  return { contentId: '', title: '', categoryId: '', slotId: '', platform: 'MOBILE', image: '', linkType: '', link: '', intro: '', content: '', isAnnouncement: '0', status: '1', sortNum: 50, imageWidth: undefined, imageHeight: undefined }
}

function getList () {
  loading.value = true
  api.value.list(query).then(response => {
    rows.value = response.rows || []
    total.value = response.total || 0
  }).finally(() => { loading.value = false })
}

function loadOptions () {
  const tasks = []
  if (props.kind === 'ad' || props.kind === 'slot') {
    tasks.push(contentApis.slot.list({ pageNum: 1, pageSize: 500 }).then(response => { slotOptions.value = response.rows || [] }))
  }
  if (props.kind === 'help' || props.kind === 'helpCategory') {
    tasks.push(contentApis.helpCategory.list({ pageNum: 1, pageSize: 500 }).then(response => { categoryOptions.value = response.rows || [] }))
  }
  if (props.kind === 'article' || props.kind === 'articleCategory') {
    tasks.push(contentApis.articleCategory.list({ pageNum: 1, pageSize: 500 }).then(response => { categoryOptions.value = response.rows || [] }))
  }
  return Promise.all(tasks)
}

function resetQuery () {
  Object.assign(query, { pageNum: 1, title: '', categoryId: '', slotId: '', isAnnouncement: '' })
  getList()
}

function openDialog (row) {
  Object.assign(form, createForm(), props.kind === 'ad' ? { platform: query.platform } : {}, row || {})
  formRef.value?.clearValidate()
  dialogVisible.value = true
}

function submit () {
  formRef.value.validate(valid => {
    if (!valid) return
    saving.value = true
    const action = form.contentId ? api.value.update : api.value.add
    action({ ...form }).then(() => {
      proxy.$modal.msgSuccess('保存成功')
      dialogVisible.value = false
      loadOptions().then(getList)
    }).finally(() => { saving.value = false })
  })
}

function toggleStatus (row) {
  api.value.update({ ...row, status: row.status === '1' ? '0' : '1' }).then(() => {
    proxy.$modal.msgSuccess('状态更新成功')
    getList()
  })
}

function removeRow (row) {
  proxy.$modal.confirm(`确认删除“${row.title}”吗？`).then(() => api.value.remove(row.contentId)).then(() => {
    proxy.$modal.msgSuccess('删除成功')
    loadOptions().then(getList)
  }).catch(() => {})
}

function imageUrl (value) {
  return value && value.startsWith('http') ? value : (import.meta.env.VITE_APP_BASE_API || '') + (value || '')
}

watch(() => props.kind, () => {
  Object.assign(query, { pageNum: 1, title: '', categoryId: '', slotId: '', isAnnouncement: '' })
  loadOptions().then(getList)
}, { immediate: true })
</script>

<style scoped lang="scss">
.content-page {
  .query-form { margin: 18px 0 12px; }
  .thumb { width: 80px; height: 64px; }
  .wide-control { width: 360px; max-width: 100%; }
  .dimension-fields { display: flex; align-items: center; gap: 10px; }
  .dimension-fields :deep(.el-input-number) { width: 150px; }
  .form-tip { color: var(--el-text-color-secondary); font-size: 12px; line-height: 1.5; }
  .inline-tip { margin-left: 14px; }
  :deep(.tox-tinymce) { width: 100%; }
}
</style>
