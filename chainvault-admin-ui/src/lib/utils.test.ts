/*
 * Copyright (c) 2026. Gryphus Lab
 */
import { describe, it, expect, vi } from 'vitest'
import type { ClassValue } from 'clsx'
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

  it('extracts time components with HH:mm pattern', () => {
    const result = safeFormat('2026-04-15T09:05:00Z', 'HH:mm')
    expect(result).toMatch(/^\d{2}:\d{2}$/)
  })

  it('formats date-only ISO string (no time component)', () => {
    const result = safeFormat('2026-01-01', 'yyyy-MM-dd')
    expect(result).toBe('2026-01-01')
  })

  it('returns default fallback for whitespace-only string', () => {
    expect(safeFormat('   ')).toBe('—')
  })
})

describe('secureRandomInt (boundary cases)', () => {
  it('returns 0 for max of 1 (only valid value)', () => {
    const result = secureRandomInt(1)
    expect(result).toBe(0)
  })

  it('throws RangeError for non-integer float', () => {
    expect(() => secureRandomInt(2.9)).toThrow(RangeError)
  })

  it('throws RangeError for NaN', () => {
    expect(() => secureRandomInt(NaN)).toThrow(RangeError)
  })

  it('throws RangeError for Infinity', () => {
    expect(() => secureRandomInt(Infinity)).toThrow(RangeError)
  })

  it('returns a number strictly less than max across multiple calls', () => {
    const max = 5
    for (let i = 0; i < 50; i++) {
      const r = secureRandomInt(max)
      expect(r).toBeGreaterThanOrEqual(0)
      expect(r).toBeLessThan(max)
    }
  })
})

describe('cn (class merging) edge cases', () => {
  it('returns empty string when called with no arguments', () => {
    expect(cn()).toBe('')
  })

  it('ignores undefined and null values', () => {
    expect(cn('flex', undefined, null as unknown as ClassValue, 'gap-2')).toBe('flex gap-2')
  })

  it('handles object syntax for conditional classes', () => {
    expect(cn({ hidden: true, flex: false })).toBe('hidden')
    expect(cn({ hidden: false, flex: true })).toBe('flex')
  })

  it('deduplicates identical classes', () => {
    // tailwind-merge deduplicates conflicting utilities; same classes should not double up
    const result = cn('mt-2', 'mt-4')
    expect(result).toBe('mt-4')
  })
})