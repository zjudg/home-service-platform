<template>
  <div class="app-container">
    <el-form
      :inline="true"
      :model="query"
    >
      <el-form-item label="服务名称">
        <el-input
          v-model="query.serviceName"
          clearable
          placeholder="请输入服务名称"
        />
      </el-form-item>
      <el-form-item label="服务分类">
        <el-select
          v-model="query.categoryId"
          clearable
          placeholder="请选择"
        >
          <el-option
            v-for="item in categories"
            :key="item.categoryId"
            :label="item.categoryName"
            :value="item.categoryId"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="发布城市">
        <el-select
          v-model="query.cityId"
          clearable
          filterable
          placeholder="全部城市"
        >
          <el-option
            v-for="item in cityOptions"
            :key="item.cityId"
            :label="item.cityName"
            :value="item.cityId"
          />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button
          type="primary"
          @click="load"
        >
          查询
        </el-button><el-button @click="reset">
          重置
        </el-button>
      </el-form-item>
    </el-form>
    <el-tabs
      v-model="query.status"
      @tab-change="load"
    >
      <el-tab-pane
        label="全部"
        name=""
      /><el-tab-pane
        label="销售中"
        name="0"
      /><el-tab-pane
        label="仓库中"
        name="1"
      />
    </el-tabs>
    <div class="mb8">
      <el-button
        type="primary"
        @click="openForm()"
      >
        添加服务
      </el-button><el-button
        plain
        :disabled="!selected.length"
      >
        批量上架
      </el-button><el-button
        plain
        :disabled="!selected.length"
      >
        批量下架
      </el-button><el-button
        plain
        :disabled="!selected.length"
        @click="removeSelected"
      >
        批量删除
      </el-button>
    </div>
    <el-table
      v-loading="loading"
      :data="rows"
      @selection-change="selected = $event"
    >
      <el-table-column
        type="selection"
        width="50"
      />
      <el-table-column
        label="服务名称"
        prop="serviceName"
        min-width="180"
      />
      <el-table-column
        label="服务图片"
        width="100"
      >
        <template #default="{ row }">
          <image-preview
            v-if="getServiceImage(row.carouselImages)"
            :src="getServiceImage(row.carouselImages)"
            width="64"
            height="64"
          />
        </template>
      </el-table-column>
      <el-table-column
        label="服务分类"
        prop="categoryName"
      />
      <el-table-column
        label="价格"
        prop="price"
      />
      <el-table-column
        label="单位"
        prop="unitName"
      />
      <el-table-column
        label="预约人数"
        prop="reserveCount"
      />
      <el-table-column label="销售状态">
        <template #default="{ row }">
          <el-tag :type="row.status === '0' ? 'success' : 'info'">
            {{ row.status === '0' ? '销售中' : '仓库中' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column
        label="发布城市"
        width="110"
      >
        <template #default="{ row }">
          {{ row.cityName || '全国' }}
        </template>
      </el-table-column>
      <el-table-column
        label="排序"
        prop="sortNum"
      />
      <el-table-column
        label="添加时间"
        prop="createTime"
        width="170"
      />
      <el-table-column
        label="操作"
        width="150"
      >
        <template #default="{ row }">
          <el-button
            link
            type="primary"
            @click="openForm(row.serviceId)"
          >
            编辑
          </el-button>
          <el-button
            link
            type="danger"
            @click="remove([row.serviceId])"
          >
            删除
          </el-button>
        </template>
      </el-table-column>
    </el-table>
    <pagination
      v-show="total > 0"
      v-model:page="query.pageNum"
      v-model:limit="query.pageSize"
      :total="total"
      @pagination="load"
    />
    <el-dialog
      v-model="formVisible"
      :title="form.serviceId ? '编辑服务' : '新增服务'"
      width="760px"
    >
      <el-tabs v-model="activeTab">
        <el-tab-pane
          label="基础设置"
          name="base"
        >
          <el-form
            ref="formRef"
            :model="form"
            label-width="100px"
          >
            <el-form-item
              label="服务名称"
              required
            >
              <el-input
                v-model="form.serviceName"
                maxlength="64"
                show-word-limit
              />
            </el-form-item><el-form-item label="服务简介">
              <el-input v-model="form.serviceIntro" />
            </el-form-item><el-form-item
              label="服务分类"
              required
            >
              <el-select v-model="form.categoryId">
                <el-option
                  v-for="item in categories"
                  :key="item.categoryId"
                  :label="item.categoryName"
                  :value="item.categoryId"
                />
              </el-select>
            </el-form-item><el-form-item label="发布城市">
              <el-select
                v-model="form.cityId"
                clearable
                filterable
                placeholder="留空表示全国可用"
              >
                <el-option
                  v-for="item in cityOptions"
                  :key="item.cityId"
                  :label="`${item.provinceName} ${item.cityName}`"
                  :value="item.cityId"
                />
              </el-select>
              <div class="form-tip">
                留空时该服务对所有城市可见；选择城市后仅该城市可见
              </div>
            </el-form-item><el-form-item
              label="轮播图"
            >
              <image-upload
                v-model="form.carouselImages"
                :limit="10"
              />
              <div class="form-tip">
                建议尺寸：800×800，最多上传10张
              </div>
            </el-form-item><el-form-item
              label="价格"
              required
            >
              <el-input-number
                v-model="form.price"
                :min="0"
                :precision="2"
              />
            </el-form-item><el-form-item label="划线价">
              <el-input-number
                v-model="form.originalPrice"
                :min="0"
                :precision="2"
              />
            </el-form-item><el-form-item
              label="服务单位"
              required
            >
              <el-select v-model="form.unitId">
                <el-option
                  v-for="item in units"
                  :key="item.unitId"
                  :label="item.unitName"
                  :value="item.unitId"
                />
              </el-select>
            </el-form-item><el-form-item label="排序">
              <el-input-number
                v-model="form.sortNum"
                :min="0"
              />
            </el-form-item><el-form-item label="商品状态">
              <el-radio-group v-model="form.status">
                <el-radio value="0">
                  立即上架
                </el-radio><el-radio value="1">
                  放入仓库
                </el-radio>
              </el-radio-group>
            </el-form-item>
          </el-form>
        </el-tab-pane><el-tab-pane
          label="服务详情"
          name="detail"
          lazy
        >
          <editor
            v-model="form.detailContent"
            :min-height="350"
            :auto-focus="true"
          /><el-divider /><div
            class="detail-preview"
            v-html="form.detailContent"
          />
        </el-tab-pane>
      </el-tabs><template #footer>
        <el-button @click="formVisible = false">
          取消
        </el-button><el-button
          type="primary"
          @click="save"
        >
          保存
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { listService, getService, addService, updateService, deleteService } from '@/api/zjudg/service'
import { listCategory } from '@/api/zjudg/category'
import { listUnit } from '@/api/zjudg/unit'
import { listCity } from '@/api/zjudg/city'

const loading = ref(false); const rows = ref([]); const total = ref(0); const selected = ref([]); const categories = ref([]); const units = ref([]); const cityOptions = ref([]); const formVisible = ref(false); const activeTab = ref('base')
const query = reactive({ pageNum: 1, pageSize: 10, serviceName: '', categoryId: '', cityId: '', status: '' }); const form = reactive({})
function load () { loading.value = true; listService(query).then(res => { rows.value = res.rows || []; total.value = res.total || 0 }).finally(() => { loading.value = false }) }
function reset () { Object.assign(query, { pageNum: 1, serviceName: '', categoryId: '', cityId: '', status: '' }); load() }
function blank () { Object.keys(form).forEach(key => delete form[key]); Object.assign(form, { serviceName: '', serviceIntro: '', categoryId: '', cityId: '', carouselImages: '', price: 0, originalPrice: null, unitId: '', sortNum: 0, status: '1', detailContent: '' }) }
function getServiceImage (images) {
  if (Array.isArray(images)) return images[0] || ''
  if (typeof images !== 'string') return ''
  return images.split(',').map(image => image.trim()).find(Boolean) || ''
}
function openForm (id) { blank(); activeTab.value = 'base'; formVisible.value = true; if (id) getService(id).then(res => Object.assign(form, res.data)) }
function save () { const action = form.serviceId ? updateService : addService; action(form).then(() => { ElMessage.success('保存成功'); formVisible.value = false; load() }) }
function remove (ids) { ElMessageBox.confirm('确认删除选中的服务吗？', '提示', { type: 'warning' }).then(() => deleteService(ids.join(',')).then(() => { ElMessage.success('删除成功'); load() })) }
function removeSelected () { remove(selected.value.map(item => item.serviceId)) }
onMounted(() => { Promise.all([listCategory({ pageSize: 100 }), listUnit({ pageSize: 100 }), listCity({ pageSize: 500 })]).then(([c, u, ct]) => { categories.value = c.rows || []; units.value = u.rows || []; cityOptions.value = ct.rows || [] }); load() })
</script>

<style scoped>.detail-preview{border:1px solid #ebeef5;padding:16px;min-height:80px}.form-tip{color:#909399;font-size:12px}.mb8{margin-bottom:8px}</style>
