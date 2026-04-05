/*
 * Copyright (c) 2026. Gryphus Lab
 */
import { render, screen } from '@testing-library/react'
import { describe, it, expect } from 'vitest'
import { Card, CardHeader, CardTitle, CardContent } from './Card'

describe('Card Components', () => {
  describe('Card', () => {
    it('renders children and base styles', () => {
      render(<Card>Card Content</Card>)
      const card = screen.getByText('Card Content')
      expect(card).toHaveClass('bg-white', 'shadow', 'rounded-lg')
    })

    it('merges custom className', () => {
      render(<Card className="custom-card">Content</Card>)
      expect(screen.getByText('Content')).toHaveClass('custom-card')
    })
  })

  describe('CardHeader', () => {
    it('renders with border-b and padding', () => {
      render(<CardHeader>Header</CardHeader>)
      const header = screen.getByText('Header')
      expect(header).toHaveClass('border-b', 'border-gray-200', 'px-6')
    })
  })

  describe('CardTitle', () => {
    it('renders with correct typography classes', () => {
      render(<CardTitle>Title</CardTitle>)
      const title = screen.getByText('Title')
      expect(title).toHaveClass('text-lg', 'font-medium', 'text-gray-900')
    })
  })

  describe('CardContent', () => {
    it('renders with standard padding', () => {
      render(<CardContent>Body</CardContent>)
      const content = screen.getByText('Body')
      expect(content).toHaveClass('px-6', 'py-5')
    })
  })

  it('composition works correctly', () => {
    render(
      <Card data-testid="full-card">
        <CardHeader>
          <CardTitle>Migration Stats</CardTitle>
        </CardHeader>
        <CardContent>
          <p>Successful: 10</p>
        </CardContent>
      </Card>,
    )

    expect(screen.getByText('Migration Stats')).toBeInTheDocument()
    expect(screen.getByText('Successful: 10')).toBeInTheDocument()

    // Verify nesting structure
    const card = screen.getByTestId('full-card')
    expect(card.querySelector('.border-b')).toBeInTheDocument() // Header check
    expect(card.querySelectorAll('.px-6').length).toBe(2) // Header + Content padding
  })

  it('forwards attributes to the underlying div', () => {
    render(
      <Card id="main-card" role="region">
        Content
      </Card>,
    )
    const card = screen.getByText('Content')
    expect(card).toHaveAttribute('id', 'main-card')
    expect(card).toHaveAttribute('role', 'region')
  })
})
