import { describe, expect, it } from 'vitest'
import nav from './_nav'

describe('_nav', () => {
  it('exports a non-empty navigation config', () => {
    expect(Array.isArray(nav)).toBe(true)
    expect(nav.length).toBeGreaterThan(0)
  })
})
