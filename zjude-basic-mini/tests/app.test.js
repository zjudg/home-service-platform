import { describe, expect, it } from 'vitest'
import { readdirSync, readFileSync, statSync } from 'node:fs'
import { resolve } from 'node:path'
import { getPlatform } from '@/utils/platform'

describe('platform adapter', () => {
  it('falls back to h5 outside native targets', () => {
    expect(getPlatform()).toBe('h5')
  })
})

describe('mobile theme guard', () => {
  it('does not reintroduce legacy brand colors', () => {
    const root = resolve(process.cwd())
    const files = []
    const visit = directory => {
      for (const entry of readdirSync(directory)) {
        const file = resolve(directory, entry)
        if (entry === 'node_modules' || entry === 'dist') continue
        if (statSync(file).isDirectory()) visit(file)
        else if (/\.(vue|scss|json)$/.test(entry)) files.push(file)
      }
    }
    visit(resolve(root, 'pages'))
    visit(resolve(root, 'components'))
    visit(resolve(root, 'styles'))
    files.push(resolve(root, 'uni.scss'), resolve(root, 'App.vue'), resolve(root, 'pages.json'))

    const legacyColors = [
      '#007aff', '#2f6fed', '#1f56c5', '#65a0ff', '#6a98f3',
      '#eaf2ff', '#e9f1ff', '#e8f0ff', '#4a84d8', '#9a6cc2', '#2C405A'
    ]
    const violations = files.flatMap(file => {
      const source = readFileSync(file, 'utf8').toLowerCase()
      return legacyColors.filter(color => source.includes(color.toLowerCase())).map(color => `${file}: ${color}`)
    })
    expect(violations).toEqual([])
  })
})
