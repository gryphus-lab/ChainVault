/*
 * Copyright (c) 2026. Gryphus Lab
 */
import { type ClassValue } from 'clsx'
import { cn, safeFormat } from './utils'
describe('TestUtils', () => {
  it('returns an empty string when no arguments are provided', () => {
    const inputs: ClassValue[] = []
    expect(cn(...inputs)).toBe('')
  })

  it('returns a valid formatted string when a valid ISO date is provided', () => {
    expect(safeFormat('2026-01-01 10:30:30')).toBe('Jan 1, 2026, 10:30 AM')
  })

  it('returns a dash when ISO date cannot be parsed to return fallback', () => {
    const fallback = '—'
    // check if date is not set
    expect(safeFormat(null)).toBe(fallback)

    // check for date not in ISO format i.e.
    expect(safeFormat('01.01.2010 00:00:00')).toBe(fallback)
  })
})
