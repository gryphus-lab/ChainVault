import { describe, expect, it } from 'vitest'
import { logo } from './logo'

describe('logo', () => {
  it('exports icon path data', () => {
    expect(Array.isArray(logo)).toBe(true)
    expect(logo.length).toBeGreaterThan(0)
  })
})
