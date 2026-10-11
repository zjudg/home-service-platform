import { describe, expect, it } from 'vitest'
import { isExternal, isHttp, validEmail } from './validate'

describe('validation helpers', () => {
  it('recognizes HTTP and external links', () => {
    expect(isHttp('https://example.com')).toBe(true)
    expect(isHttp('/system/user')).toBe(false)
    expect(isExternal('mailto:test@example.com')).toBe(true)
    expect(isExternal('/system/user')).toBe(false)
  })

  it('validates common email boundaries', () => {
    expect(validEmail('user@example.com')).toBe(true)
    expect(validEmail('not-an-email')).toBe(false)
  })
})
