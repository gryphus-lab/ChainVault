/*
 * Copyright (c) 2026. Gryphus Lab
 */
import { format, parseISO } from 'date-fns'
import clsx, { ClassValue } from 'clsx'
import { twMerge } from 'tailwind-merge'

/**
 * Generates a cryptographically secure random integer in the range [0, maxExclusive).
 *
 * @param maxExclusive - The exclusive upper bound; must be a positive integer.
 * @returns An integer greater than or equal to 0 and less than `maxExclusive`.
 * @throws RangeError if `maxExclusive` is not a positive integer
 */
export default function secureRandomInt(maxExclusive: number): number {
  if (!Number.isInteger(maxExclusive) || maxExclusive <= 0) {
    throw new RangeError('maxExclusive must be a positive integer')
  }
  const array = new Uint32Array(1)
  const limit = Math.floor(0x100000000 / maxExclusive) * maxExclusive
  do {
    crypto.getRandomValues(array)
  } while (array[0] >= limit)
  return array[0] % maxExclusive
}

/**
 * Merge multiple class-name inputs into a single class string and resolve Tailwind CSS class conflicts.
 *
 * @param inputs - Class value inputs (strings, arrays, objects, or other values accepted by class utilities) to be combined
 * @returns A single space-separated className string with conflicting Tailwind utility classes resolved
 */
export function cn(...inputs: ClassValue[]) {
  return twMerge(clsx(inputs))
}

export const safeFormat = (dateStr: string | undefined | null, fallback: string = '—') => {
  if (!dateStr) return fallback
  try {
    return format(parseISO(dateStr), 'PPp')
  } catch {
    return fallback
  }
}
