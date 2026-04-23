/*
 * Copyright (c) 2026. Gryphus Lab
 */
import { describe, it, expect, vi } from 'vitest'
import secureRandomInt, { cn, safeFormat } from './utils'

describe('secureRandomInt', () => {
  it('returns a number within the specified range', () => {
    const max = 10
    const result = secureRandomInt(max)
    expect(Number.isInteger(result)).toBe(true)
    expect(result).toBeGreaterThanOrEqual(0)
    expect(result).toBeLessThan(max)
  })

  it('throws RangeError for invalid inputs', () => {
    expect(() => secureRandomInt(0)).toThrow(RangeError)
    expect(() => secureRandomInt(-5)).toThrow(RangeError)
    expect(() => secureRandomInt(1.5)).toThrow(RangeError)
  })

  it('uses crypto.getRandomValues', () => {
    const spy = vi.spyOn(crypto, 'getRandomValues')
    try {
      secureRandomInt(100)
      expect(spy).toHaveBeenCalled()
    } finally {
      spy.mockRestore()
    }
  })
})

describe('cn (class merging)', () => {
  it('merges standard classes', () => {
    expect(cn('base-class', 'extra-class')).toBe('base-class extra-class')
  })

  it('resolves Tailwind conflicts correctly', () => {
    // Tailwind-merge should ensure the last conflicting class wins
    expect(cn('px-2 py-2', 'p-5')).toBe('p-5')
    expect(cn('text-red-500', 'text-blue-500')).toBe('text-blue-500')
  })

  it('handles conditional classes from clsx', () => {
    expect(cn('flex', false, 'p-4')).toBe('flex p-4')
  })
})

describe('safeFormat', () => {
  const validIso = '2026-04-15T12:00:00Z'

  it('formats a valid ISO string with default pattern', () => {
    // Result depends on locale, but checking for a non-fallback string
    const result = safeFormat(validIso)
    expect(result).not.toBe('—')
    expect(typeof result).toBe('string')
  })

  it('applies a custom date pattern', () => {
    const result = safeFormat(validIso, 'yyyy-MM-dd')
    expect(result).toBe('2026-04-15')
  })

  it('returns fallback for null or undefined', () => {
    expect(safeFormat(null)).toBe('—')
    expect(safeFormat(undefined)).toBe('—')
  })

  it('returns custom fallback on error or empty string', () => {
    expect(safeFormat('', 'yyyy', 'N/A')).toBe('N/A')
    expect(safeFormat('not-a-date', 'yyyy', 'Invalid')).toBe('Invalid')
  })
})
