/*
 * Copyright (c) 2026. Gryphus Lab
 */
import { render, screen } from '@testing-library/react'
import { describe, it, expect, vi } from 'vitest'
import OverviewDataBox from './overviewDataBox'

// Mock StatBox to simplify the DOM and check props directly
vi.mock('@/components/StatBox', () => ({
  default: ({ title, subtitle }: { title: string; subtitle: string }) => (
    <div data-testid="stat-box">
      <span data-testid="stat-title">{title}</span>
      <span data-testid="stat-subtitle">{subtitle}</span>
    </div>
  ),
}))

describe('OverviewDataBox Component', () => {
  const mockColors = {
    primary: { 400: '#123' },
    greenAccent: { 600: '#0F0' },
    redAccent: { 600: '#F00' },
    orangeAccent: { 600: '#e67e00' },
  }

  it('renders all four stat boxes with correct titles', () => {
    const stats = {
      total: 100,
      success: 70,
      pending: 10,
      running: 10,
      failed: 10,
    }

    render(<OverviewDataBox colors={mockColors} stats={stats} />)

    expect(screen.getByText('Total Migrations')).toBeInTheDocument()
    expect(screen.getByText('100')).toBeInTheDocument()

    expect(screen.getByText('Successful')).toBeInTheDocument()
    expect(screen.getByText('70')).toBeInTheDocument()

    // Verification of the math: pending (5) + running (5) = 10
    expect(screen.getByText('In Progress')).toBeInTheDocument()
    expect(screen.getByText('20')).toBeInTheDocument()

    expect(screen.getByText('Failed')).toBeInTheDocument()
    expect(screen.getByText('10')).toBeInTheDocument()
  })

  it("defaults to '0' when stats are missing or undefined", () => {
    render(<OverviewDataBox colors={mockColors} stats={{}} />)

    const titles = screen.getAllByTestId('stat-title')
    titles.forEach((title) => {
      expect(title.textContent).toBe('0')
    })
  })

  it('correctly sums In Progress when only one value is provided', () => {
    const partialStats = { pending: 5 } // running is undefined
    render(<OverviewDataBox colors={mockColors} stats={partialStats} />)

    expect(screen.getByText('In Progress')).toBeInTheDocument()
    expect(screen.getByText('5')).toBeInTheDocument()
  })
})
