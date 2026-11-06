import { defineConfig } from 'vite'
import uniModule from '@dcloudio/vite-plugin-uni'

const uni = typeof uniModule === 'function' ? uniModule : uniModule.default

// 接口地址统一在 config/index.js 维护，这里不再配置代理和环境变量
export default defineConfig({
  plugins: [uni()],
  server: {
    host: '0.0.0.0',
    port: 5174
  }
})
