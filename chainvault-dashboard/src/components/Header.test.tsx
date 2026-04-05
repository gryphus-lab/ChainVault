/*
 * Copyright (c) 2026. Gryphus Lab
 */
import { render, screen } from '@testing-library/react'
import { describe, it, expect, vi } from 'vitest'
import Header from './Header'

// Mock the tokens function to return predictable colors for testing
vi.mock('@/theme', () => ({
  tokens: () => ({
    gray: { 100: 'rgb(255, 255, 255)' },
    greenAccent: { 400: 'rgb(0, 255, 0)' },
  }),
}))

describe('Header Component', () => {
  it('renders the title and subtitle correctly', () => {
    render(<Header title="Test Title" subtitle="Test Subtitle" />)

    expect(screen.getByText('Test Title')).toBeInTheDocument()
    expect(screen.getByText('Test Subtitle')).toBeInTheDocument()
  })

  it('applies the correct theme colors from tokens', () => {
    render(<Header title="Colored Title" subtitle="Colored Subtitle" />)

    const titleElement = screen.getByText('Colored Title')
    const subtitleElement = screen.getByText('Colored Subtitle')

    // Checking computed styles (matching the mocked hex/rgb values)
    expect(titleElement).toHaveStyle({ color: 'rgb(255, 255, 255)' })
    expect(subtitleElement).toHaveStyle({ color: 'rgb(0, 255, 0)' })
  })

  it('renders empty strings when no props are provided', () => {
    const { container } = render(<Header />)

    // Check that Typography components exist but are empty
    const typographies = container.querySelectorAll('h2, h5')
    expect(typographies[0].textContent).toBe('')
    expect(typographies[1].textContent).toBe('')
  })
})
