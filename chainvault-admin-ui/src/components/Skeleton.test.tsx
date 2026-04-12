/*
 * Copyright (c) 2026. Gryphus Lab
 */
import { render, screen } from '@testing-library/react'
import { describe, expect, it } from 'vitest'
import { Skeleton, SkeletonCard, SkeletonText } from './Skeleton'

describe('Skeleton Components', () => {
  describe('Skeleton', () => {
    it('renders as a div with base animation and styling', () => {
      render(<Skeleton data-testid="skeleton" />)
      const skeleton = screen.getByTestId('skeleton')

      // Check for div tag and ARIA attributes for accessibility
      expect(skeleton.tagName).toBe('DIV')
      expect(skeleton).toHaveAttribute('role', 'status')
      expect(skeleton).toHaveAttribute('aria-label', 'Loading')
      expect(skeleton).toHaveClass('animate-pulse', 'bg-gray-200', 'rounded-md')
    })

    it('applies dynamic width and height classes correctly', () => {
      const { container, rerender } = render(<Skeleton width="w-48" height="h-6" />)
      let skeleton = container.querySelector('div')
      expect(skeleton).toHaveClass('w-48', 'h-6')

      rerender(<Skeleton width="w-full" />)
      skeleton = container.querySelector('div')
      expect(skeleton).toHaveClass('w-full')
    })

    it('merges custom className without losing base animation', () => {
      const { container } = render(<Skeleton className="rounded-full bg-blue-100" />)
      const skeleton = container.querySelector('div')

      expect(skeleton).toHaveClass('animate-pulse', 'rounded-full', 'bg-blue-100')
    })
  })

  describe('SkeletonText', () => {
    it('renders the default single line', () => {
      const { container } = render(<SkeletonText />)
      // Works with any tag as long as it has the animation class
      const lines = container.querySelectorAll('.animate-pulse')
      expect(lines).toHaveLength(1)
    })

    it('renders the specified number of lines', () => {
      const { container } = render(<SkeletonText lines={5} />)
      const lines = container.querySelectorAll('.animate-pulse')
      expect(lines).toHaveLength(5)
    })

    it('applies space-y-2 for multiple lines', () => {
      const { container } = render(<SkeletonText lines={3} />)
      expect(container.firstChild).toHaveClass('space-y-2')
    })
  })

  describe('SkeletonCard', () => {
    it('renders a card structure with a header and text lines', () => {
      const { container } = render(<SkeletonCard />)

      // Should have 1 header skeleton + 3 text skeletons
      const skeletons = container.querySelectorAll('.animate-pulse')
      expect(skeletons).toHaveLength(4)

      // Verify card container styling (this remains a div usually)
      expect(container.firstChild).toHaveClass('rounded-lg', 'border', 'bg-white', 'p-6')
    })

    it('accepts a custom className for the card container', () => {
      const { container } = render(<SkeletonCard className="custom-card" />)
      expect(container.firstChild).toHaveClass('custom-card')
    })
  })
})
