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
 * Simple animated skeleton loader
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

// Convenience variants for common use cases
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
