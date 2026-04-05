/*
 * Copyright (c) 2026. Gryphus Lab
 */
import { type ClassValue, clsx } from 'clsx'
import { twMerge } from 'tailwind-merge'
import { format, parseISO } from 'date-fns'

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
