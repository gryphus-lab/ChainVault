/*
 * Copyright (c) 2026. Gryphus Lab
 */
import { cn } from '../lib/utils'
import * as React from 'react'

interface SkeletonProps extends React.HTMLAttributes<HTMLDivElement> {
  /** Tailwind width class, e.g. "w-32", "w-full", "w-3/4" */
  width?: string
  /** Tailwind height class, e.g. "h-6", "h-10" */
  height?: string
}

/**
 * Renders an animated skeleton placeholder element.
 *
 * Applies the provided `width` and `height` values directly as Tailwind class strings on the element.
 *
 * @param width - Optional Tailwind width class (e.g., "w-32", "w-full")
 * @param height - Optional Tailwind height class (e.g., "h-6", "h-10")
 * @param className - Additional classes to apply to the element
 */
export function Skeleton({ className, width, height, ...props }: Readonly<SkeletonProps>) {
  return (
    <div
      className={cn(
        'animate-pulse rounded-md bg-gray-200 dark:bg-gray-700',
        width,
        height,
        className,
      )}
      {...props}
    />
  )
}

/**
 * Renders multiple skeleton text lines.
 *
 * Each line is rendered as a full-width skeleton bar stacked with vertical spacing.
 *
 * @param lines - Number of skeleton lines to render (default: `1`)
 * @param className - Additional CSS classes applied to the wrapper element
 * @returns A React element containing the requested number of skeleton lines
 */
export function SkeletonText({
  lines = 1,
  className,
}: Readonly<{
  lines?: number
  className?: string
}>) {
  return (
    <div className={cn('space-y-2', className)}>
      {Array.from({ length: lines }).map((_, i) => (
        <Skeleton key={i} height="h-4" className="w-full" />
      ))}
    </div>
  )
}

export function SkeletonCard({ className }: Readonly<{ className?: string }>) {
  return (
    <div className={cn('rounded-lg border bg-white p-6 shadow', className)}>
      <Skeleton height="h-6" width="w-3/4" className="mb-4" />
      <SkeletonText lines={3} />
    </div>
  )
}
