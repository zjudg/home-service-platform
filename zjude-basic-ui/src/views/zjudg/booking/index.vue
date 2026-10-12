<template>
  <div class="app-container booking-page">
    <el-form
      :inline="true"
      class="advance-form"
    >
      <el-form-item label="可提前预约">
        <el-input-number
          v-model="advanceDays"
          :min="0"
          :max="90"
          controls-position="right"
        />
        <span class="unit-text">天</span>
      </el-form-item>
      <span class="form-tip">用户可以提前预约的天数，默认7天</span>
    </el-form>
    <div class="slot-heading">
      <span>时间段设置</span><el-button
        type="primary"
        @click="addSlot"
      >
        添加时间段
      </el-button><el-button
        plain
        :disabled="!selected.length"
        @click="removeSelected"
      >
        批量删除
      </el-button>
    </div>
    <div class="slot-layout">
      <el-table
        v-loading="loading"
        :data="rows"
        class="slot-table"
        @selection-change="selected = $event"
      >
        <el-table-column
          type="selection"
          width="55"
        />
        <el-table-column
          label="时间段"
          min-width="470"
        >
          <template #default="{ row }">
            <div class="time-range">
              <el-select
                v-model="row.startTime"
                @change="syncSlot(row)"
              >
                <el-option
                  v-for="time in timeOptions"
                  :key="`start-${time}`"
                  :label="time.slice(0, 5)"
                  :value="time"
                />
              </el-select><span>-</span><el-select
                v-model="row.endTime"
                @change="syncSlot(row)"
              >
                <el-option
                  v-for="time in timeOptions"
                  :key="`end-${time}`"
                  :label="time.slice(0, 5)"
                  :value="time"
                />
              </el-select>
            </div>
          </template>
        </el-table-column>
        <el-table-column
          label="排序"
          width="210"
        >
          <template #default="{ row }">
            <el-input-number
              v-model="row.sortNum"
              :min="0"
              :controls="false"
            />
          </template>
        </el-table-column>
        <el-table-column
          label="操作"
          width="100"
        >
          <template #default="{ $index }">
            <el-button
              link
              type="danger"
              @click="removeSlot($index)"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="preview-panel">
        <div class="preview-title">
          - 预览图 -
        </div><div class="preview-tags">
          <el-tag
            v-for="row in sortedRows"
            :key="row.slotId || row.slotName"
            effect="plain"
          >
            {{ row.slotName }}
          </el-tag>
        </div>
      </div>
    </div>
    <div class="save-bar">
      <el-button
        type="primary"
        :loading="saving"
        @click="saveAll"
      >
        保存
      </el-button>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { addBooking, deleteBooking, listBooking, updateBooking } from '@/api/zjudg/booking'

const loading = ref(false)
const saving = ref(false)
const rows = ref([])
const selected = ref([])
const removedIds = ref([])
const advanceDays = ref(3)
const timeOptions = []
for (let minutes = 8 * 60; minutes <= 19 * 60; minutes += 30) {
  const hour = String(Math.floor(minutes / 60)).padStart(2, '0')
  const minute = String(minutes % 60).padStart(2, '0')
  timeOptions.push(`${hour}:${minute}:00`)
}
const sortedRows = computed(() => [...rows.value].sort((a, b) => (a.sortNum || 0) - (b.sortNum || 0)))
function presetRows () { const result = []; for (let index = 0; index < timeOptions.length - 1; index += 1) { const startTime = timeOptions[index]; const endTime = timeOptions[index + 1]; result.push({ slotName: `${startTime.slice(0, 5)}-${endTime.slice(0, 5)}`, startTime, endTime, maxReserve: 0, sortNum: index + 1, status: '0' }) } return result }
function syncSlot (row) { row.slotName = `${row.startTime.slice(0, 5)}-${row.endTime.slice(0, 5)}` }
function load () { loading.value = true; listBooking({ pageNum: 1, pageSize: 100, status: '0' }).then((res) => { rows.value = res.rows && res.rows.length ? res.rows : presetRows(); removedIds.value = [] }).finally(() => { loading.value = false }) }
function addSlot () { const last = rows.value[rows.value.length - 1]; const startTime = last ? last.endTime : '08:00:00'; const startIndex = timeOptions.indexOf(startTime); const endTime = timeOptions[Math.min(startIndex + 1, timeOptions.length - 1)] || '08:30:00'; rows.value.push({ slotName: `${startTime.slice(0, 5)}-${endTime.slice(0, 5)}`, startTime, endTime, maxReserve: 0, sortNum: rows.value.length + 1, status: '0' }) }
function removeSlot (index) { const row = rows.value[index]; if (row.slotId) removedIds.value.push(row.slotId); rows.value.splice(index, 1) }
function removeSelected () { const ids = selected.value.filter(row => row.slotId).map(row => row.slotId); removedIds.value.push(...ids); rows.value = rows.value.filter(row => !selected.value.includes(row)); selected.value = [] }
function saveAll () { saving.value = true; const actions = rows.value.map((row, index) => { row.sortNum = index + 1; syncSlot(row); return row.slotId ? updateBooking(row) : addBooking(row) }); if (removedIds.value.length) actions.push(deleteBooking(removedIds.value.join(','))); Promise.all(actions).then(() => { ElMessage.success('保存成功'); load() }).finally(() => { saving.value = false }) }
onMounted(load)
</script>

<style scoped>
.advance-form { margin-bottom: 6px; }
.unit-text { display: inline-block; padding: 0 14px; line-height: 32px; border: 1px solid var(--el-border-color); border-left: 0; color: var(--el-text-color-regular); }
.form-tip { color: var(--el-text-color-secondary); font-size: 12px; margin-left: 12px; }
.slot-heading { display: flex; align-items: center; gap: 12px; margin: 12px 0 8px; }
.slot-heading > span { margin-right: 2px; }
.slot-layout { display: flex; align-items: flex-start; gap: 16px; }
.slot-table { flex: 1; }
.time-range { display: flex; align-items: center; gap: 8px; }
.time-range .el-select { width: 208px; }
.preview-panel { width: 280px; min-height: 460px; border: 1px solid var(--el-border-color); border-radius: 10px; padding: 12px 10px; }
.preview-title { text-align: center; color: var(--el-text-color-regular); margin-bottom: 12px; }
.preview-tags { display: grid; grid-template-columns: repeat(3, 1fr); gap: 8px 6px; }
.preview-tags .el-tag { overflow: hidden; padding: 0 5px; }
.save-bar { position: sticky; bottom: 0; z-index: 2; padding: 14px; margin: 16px -20px -20px; text-align: center; background: var(--el-bg-color); border-top: 1px solid var(--el-border-color-lighter); }
@media (max-width: 1100px) { .slot-layout { display: block; } .preview-panel { width: auto; margin-top: 16px; } }
</style>
