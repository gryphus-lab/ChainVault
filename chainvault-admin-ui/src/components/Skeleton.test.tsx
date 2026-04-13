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

      expect(skeleton.tagName).toBe('DIV')
      expect(skeleton).toHaveClass('animate-pulse', 'bg-gray-200', 'rounded-md', 'block')
    })

    it('applies dynamic width and height classes correctly', () => {
      const { rerender } = render(<Skeleton data-testid="skeleton" width="w-48" height="h-6" />)
      let skeleton = screen.getByTestId('skeleton')
      expect(skeleton).toHaveClass('w-48', 'h-6')

      rerender(<Skeleton data-testid="skeleton" width="w-full" />)
      skeleton = screen.getByTestId('skeleton')
      expect(skeleton).toHaveClass('w-full')
    })

    it('merges custom className without losing base animation', () => {
      render(<Skeleton data-testid="skeleton" className="rounded-full bg-blue-100" />)
      const skeleton = screen.getByTestId('skeleton')

      expect(skeleton).toHaveClass('animate-pulse', 'rounded-full', 'bg-blue-100')
    })
  })

  describe('SkeletonText', () => {
    it('renders with accessibility attributes and hidden text', () => {
      const { container } = render(<SkeletonText />)
      const wrapper = container.firstChild as HTMLElement

      expect(wrapper).toHaveAttribute('aria-live', 'polite')
      expect(wrapper).toHaveAttribute('aria-atomic', 'true')
      expect(screen.getByText('Loading')).toBeInTheDocument()
    })

    it('renders the default single line', () => {
      const { container } = render(<SkeletonText />)
      // The wrapper div + the internal skeleton div
      const skeletons = container.querySelectorAll('.animate-pulse')
      expect(skeletons).toHaveLength(1)
    })

    it('renders the specified number of lines up to MAX_SKELETON_LINES', () => {
      const { container } = render(<SkeletonText lines={5} />)
      const skeletons = container.querySelectorAll('.animate-pulse')
      expect(skeletons).toHaveLength(5)
    })

    it('caps the number of lines at 10', () => {
      const { container } = render(<SkeletonText lines={20} />)
      const skeletons = container.querySelectorAll('.animate-pulse')
      expect(skeletons).toHaveLength(10)
    })
  })

  describe('SkeletonCard', () => {
    it('renders a card structure with a header and text lines', () => {
      const { container } = render(<SkeletonCard />)

      // Should have 1 header skeleton + 3 text skeletons
      const skeletons = container.querySelectorAll('.animate-pulse')
      expect(skeletons).toHaveLength(4)

      // Verify accessibility and container styling
      const wrapper = container.firstChild as HTMLElement
      expect(wrapper).toHaveAttribute('aria-live', 'polite')
      expect(wrapper).toHaveClass('rounded-lg', 'border', 'bg-white', 'p-6')
      expect(screen.getAllByText('Loading')).toHaveLength(2)
    })

    it('accepts a custom className for the card container', () => {
      const { container } = render(<SkeletonCard className="custom-card" />)
      expect(container.firstChild).toHaveClass('custom-card')
    })
  })
})
