/*
 * Copyright (c) 2026. Gryphus Lab
 */
import { render, screen } from '@testing-library/react'
import { describe, it, expect, vi } from 'vitest'
import StatBox from './StatBox'

// Mock ProgressCircle to keep the test focused on StatBox props
vi.mock('./ProgressCircle', () => ({
  default: () => <div data-testid="mock-progress-circle" />,
}))

// Mock tokens for predictable color testing
vi.mock('@/theme', () => ({
  tokens: () => ({
    gray: { 100: 'rgb(200, 200, 200)' },
    redAccent: { 500: 'rgb(255, 0, 0)' },
    greenAccent: { 500: 'rgb(0, 255, 0)', 600: 'rgb(0, 200, 0)' },
  }),
}))

describe('StatBox Component', () => {
  const defaultProps = {
    title: '1,234',
    subtitle: 'Total Sales',
  }

  it('renders title and subtitle correctly', () => {
    render(<StatBox {...defaultProps} />)
    expect(screen.getByText('1,234')).toBeInTheDocument()
    expect(screen.getByText('Total Sales')).toBeInTheDocument()
  })

  it('renders the icon when provided', () => {
    render(<StatBox {...defaultProps} icon={<span data-testid="test-icon" />} />)
    expect(screen.getByTestId('test-icon')).toBeInTheDocument()
  })

  it('renders the increase text when provided', () => {
    render(<StatBox {...defaultProps} increase="+14%" />)
    expect(screen.getByText('+14%')).toBeInTheDocument()
  })

  it("applies red color if subtitle is 'System Alerts'", () => {
    render(<StatBox title="5" subtitle="System Alerts" />)
    const subtitleElement = screen.getByText('System Alerts')

    // Should match redAccent[500] from our mock
    expect(subtitleElement).toHaveStyle({ color: 'rgb(255, 0, 0)' })
  })

  it('applies green color for standard subtitles', () => {
    render(<StatBox {...defaultProps} />)
    const subtitleElement = screen.getByText('Total Sales')

    // Should match greenAccent[500] from our mock
    expect(subtitleElement).toHaveStyle({ color: 'rgb(0, 255, 0)' })
  })

  it('always renders the ProgressCircle', () => {
    render(<StatBox {...defaultProps} />)
    expect(screen.getByTestId('mock-progress-circle')).toBeInTheDocument()
  })
})
