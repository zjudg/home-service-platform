<script setup>
import { onLaunch } from '@dcloudio/uni-app'
import { useAppStore } from '@/stores/app'
import { useAuthStore } from '@/stores/auth'
import { useConfigStore } from '@/stores/config'

const appStore = useAppStore()
const authStore = useAuthStore()
const configStore = useConfigStore()

onLaunch(async () => {
  await Promise.allSettled([authStore.restore(), configStore.load()])
  appStore.markReady()
})
</script>

<style lang="scss">
@import './styles/common.scss';

page { background: $app-bg; color: $app-text; font-family: -apple-system, BlinkMacSystemFont, 'Helvetica Neue', sans-serif; }
view, text, button, input { box-sizing: border-box; }
button::after { border: 0; }
</style>
