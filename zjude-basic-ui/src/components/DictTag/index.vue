<template>
  <div>
    <template v-for="(item, index) in options">
      <template v-if="values.includes(item.value)">
        <span
          v-if="(item.elTagType == 'default' || item.elTagType == '') && (item.elTagClass == '' || item.elTagClass == null)"
          :key="item.value"
          :index="index"
          :class="item.elTagClass"
        >{{ item.label + " " }}</span>
        <el-tag
          v-else
          :key="item.value + ''"
          :disable-transitions="true"
          :index="index"
          :type="item.elTagType === 'primary' ? '' : item.elTagType"
          :class="item.elTagClass"
        >
          {{ item.label + " " }}
        </el-tag>
      </template>
    </template>
    <template v-if="unmatch && showValue">
      {{ handleArray(unmatchArray) }}
    </template>
  </div>
</template>

<script setup>
const props = defineProps({
  // 数据
  options: {
    type: Array,
    default: () => [],
  },
  // 当前的值
  value: [Number, String, Array],
  // 当未找到匹配的数据时，显示value
  showValue: {
    type: Boolean,
    default: true,
  },
  separator: {
    type: String,
    default: ',',
  }
})

const values = computed(() => {
  if (props.value === null || typeof props.value === 'undefined' || props.value === '') return []
  return Array.isArray(props.value) ? props.value.map(item => '' + item) : String(props.value).split(props.separator)
})

const unmatchArray = computed(() => {
  // 没有value不显示
  if (props.value === null || typeof props.value === 'undefined' || props.value === '' || props.options.length === 0) return []
  return values.value.filter(item => !props.options.some(option => option.value === item))
})

const unmatch = computed(() => unmatchArray.value.length > 0)

function handleArray (array) {
  if (array.length === 0) return ''
  return array.reduce((pre, cur) => {
    return pre + ' ' + cur
  })
}
</script>

<style scoped>
.el-tag + .el-tag {
  margin-left: 10px;
}
</style>
