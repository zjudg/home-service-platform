import { getMapRuntimeConfig } from '@/api/map'

const TENCENT_MAP_SCRIPT = 'https://map.qq.com/api/gljs?v=1.exp&libraries=service'
const TIANDITU_MAP_SCRIPT = 'https://api.tianditu.gov.cn/api?v=4.0&tk='

let runtimeConfigPromise
const scriptPromises = new Map()

function loadScript (url, id) {
  if (scriptPromises.has(id)) return scriptPromises.get(id)
  const existing = document.getElementById(id)
  if (existing) {
    const promise = Promise.resolve()
    scriptPromises.set(id, promise)
    return promise
  }
  const promise = new Promise((resolve, reject) => {
    const script = document.createElement('script')
    script.id = id
    script.src = url
    script.async = true
    script.onload = resolve
    script.onerror = () => reject(new Error(`地图脚本加载失败：${id}`))
    document.head.appendChild(script)
  })
  scriptPromises.set(id, promise)
  return promise
}

export function getMapConfig () {
  if (!runtimeConfigPromise) {
    runtimeConfigPromise = getMapRuntimeConfig().then(response => response.data || response)
  }
  return runtimeConfigPromise
}

export async function loadTencentMap () {
  const config = await getMapConfig()
  if (!config.tencentKey) throw new Error('未配置腾讯地图 Key')
  if (!window.TMap) {
    await loadScript(`${TENCENT_MAP_SCRIPT}&key=${encodeURIComponent(config.tencentKey)}`, 'tencent-map-sdk')
  }
  return window.TMap
}

export async function loadTiandituMap () {
  const config = await getMapConfig()
  if (!config.tiandituKey) throw new Error('未配置天地图 Key')
  if (!window.T) {
    await loadScript(`${TIANDITU_MAP_SCRIPT}${encodeURIComponent(config.tiandituKey)}`, 'tianditu-map-sdk')
  }
  return window.T
}

export async function loadMapProvider (provider) {
  if (provider === 'tencent') return loadTencentMap()
  if (provider === 'tianditu') return loadTiandituMap()
  throw new Error(`不支持的地图平台：${provider}`)
}
