import { describe, expect, it } from 'vitest'
import { sygnet } from './sygnet'

describe('sygnet', () => {
  it('exports icon path data', () => {
    expect(Array.isArray(sygnet)).toBe(true)
    expect(sygnet.length).toBeGreaterThan(0)
  })
})
