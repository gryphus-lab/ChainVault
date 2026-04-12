/*
 * Copyright (c) 2026. Gryphus Lab
 */
import { cn } from '../lib/utils'
import * as React from 'react'

const MAX_SKELETON_LINES = 10

interface SkeletonProps extends React.HTMLAttributes<HTMLDivElement> {
  /** Tailwind width class, e.g. "w-32", "w-full", "w-3/4" */
  width?: string
  /** Tailwind height class, e.g. "h-6", "h-10" */
  height?: string
}

/**
 * Renders an animated skeleton placeholder as a `<div>` element.
 *
 * @param width - Optional Tailwind width class (e.g., "w-32", "w-full")
 * @param height - Optional Tailwind height class (e.g., "h-6", "h-10")
 * @param className - Additional CSS class names to merge onto the element
 * @param props - Additional props forwarded to the underlying `<div>` element
 * @returns The rendered `<div>` element acting as a skeleton loader
 */
export function Skeleton({ className, width, height, ...props }: Readonly<SkeletonProps>) {
  return (
    <div
      className={cn(
        'animate-pulse rounded-md bg-gray-200 dark:bg-gray-700 block',
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
  const cappedLines = Math.max(0, Math.min(Number(lines) || 0, MAX_SKELETON_LINES))
  return (
    <div className={cn('space-y-2', className)} aria-live="polite" aria-atomic="true">
      <span
        style={{
          position: 'absolute',
          width: '1px',
          height: '1px',
          padding: 0,
          margin: '-1px',
          overflow: 'hidden',
          clip: 'rect(0, 0, 0, 0)',
          whiteSpace: 'nowrap',
          border: 0,
        }}
      >
        Loading
      </span>
      {Array.from({ length: cappedLines }).map((_, i) => (
        <Skeleton key={`skeleton-${i}`} height="h-4" className="w-full" />
      ))}
    </div>
  )
}

export function SkeletonCard({ className }: Readonly<{ className?: string }>) {
  return (
    <div
      className={cn('rounded-lg border bg-white p-6 shadow', className)}
      aria-live="polite"
      aria-atomic="true"
    >
      <span
        style={{
          position: 'absolute',
          width: '1px',
          height: '1px',
          padding: 0,
          margin: '-1px',
          overflow: 'hidden',
          clip: 'rect(0, 0, 0, 0)',
          whiteSpace: 'nowrap',
          border: 0,
        }}
      >
        Loading
      </span>
      <Skeleton height="h-6" width="w-3/4" className="mb-4" />
      <SkeletonText lines={3} />
    </div>
  )
}
