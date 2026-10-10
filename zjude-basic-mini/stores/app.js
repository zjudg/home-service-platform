import { computed, ref } from 'vue'
import { defineStore } from 'pinia'
import { getPlatform } from '@/utils/platform'

export const useAppStore = defineStore('app', () => {
  const platform = ref(getPlatform())
  const ready = ref(false)
  const appName = ref('Zjude Aden')
  const isReady = computed(() => ready.value)

  function markReady () {
    ready.value = true
  }

  return { platform, appName, ready, isReady, markReady }
})
