import { describe, expect, it } from 'vitest'
import { getPlatform } from '@/utils/platform'

describe('platform adapter', () => {
  it('falls back to h5 outside native targets', () => {
    expect(getPlatform()).toBe('h5')
  })
})
