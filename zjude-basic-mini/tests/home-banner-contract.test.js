import { describe, expect, it } from 'vitest'
import { readFileSync } from 'node:fs'
import { join, resolve } from 'node:path'

const root = resolve(process.cwd())
const read = file => readFileSync(join(root, file), 'utf8')

describe('home banner contract', () => {
  it('loads banners from the zjudg_banner endpoint instead of only decoration config', () => {
    const api = read('api/mini.js')
    expect(api).toContain("url: '/mini/config/banners'")
    expect(api).toContain('export function getMiniBanners')

    const page = read('pages/home/index.vue')
    expect(page).toContain("import { getMiniBanners } from '@/api/mini'")
    expect(page).toContain("getMiniBanners('index')")
    // 后台未配置轮播图时回退到装修配置，首页不能因为轮播为空而变空
    expect(page).toMatch(/tableBanners\.value\.length[\s\S]*configStore\.home\?\.banners/)
  })

  it('does not navigate to external links inside the mini program', () => {
    const page = read('pages/home/index.vue')
    expect(page).toContain("uni.setClipboardData({ data: link })")
    expect(page).toContain('banner.link || banner.linkUrl')
  })
})
