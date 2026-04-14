/*
 * Copyright (c) 2026. Gryphus Lab
 */
import { render, screen } from '@testing-library/react'
import { describe, expect, it, vi, beforeEach } from 'vitest'
import { MemoryRouter } from 'react-router-dom'
import Dashboard from './Dashboard'
import * as api from '../../lib/api'
import { MigrationStats } from '../../types'

// Mock API
vi.mock('../../lib/api', () => ({
  getMigrations: vi.fn(),
  getMigrationStats: vi.fn(),
}))

const mockStats = {
  total: 50,
  pending: 5,
  running: 5,
  success: 35,
  failed: 5,
}

const mockMigrations = [
  {
    id: 'uuid-1',
    docId: 'DOC-001',
    title: 'First Migration',
    status: 'success',
    createdAt: '2026-01-01T00:00:00Z',
    updatedAt: '2026-01-02T00:00:00Z',
  },
]

// Helper to render with Router context
const renderDashboard = () =>
  render(
    <MemoryRouter>
      <Dashboard />
    </MemoryRouter>,
  )

describe('Dashboard Component', () => {
  beforeEach(() => {
    vi.resetAllMocks()
  })

  it('shows loading state initially', () => {
    // Keep promises pending to check initial UI
    vi.mocked(api.getMigrationStats).mockReturnValue(new Promise(() => {}))
    vi.mocked(api.getMigrations).mockReturnValue(new Promise(() => {}))

    renderDashboard()

    expect(screen.getByText('Loading...')).toBeInTheDocument()
    const placeholders = screen.getAllByText('—')
    expect(placeholders).toHaveLength(4)
  })

  it('renders stats and migration table after successful fetch', async () => {
    vi.mocked(api.getMigrationStats).mockResolvedValue(mockStats as MigrationStats)
    vi.mocked(api.getMigrations).mockResolvedValue(mockMigrations)

    renderDashboard()

    // Using findBy... automatically wraps the check in act() and waits for the element
    const totalValue = await screen.findByText('50')
    const inProgressValue = await screen.findByText('10') // pending (5) + running (5)

    expect(totalValue).toBeInTheDocument()
    expect(inProgressValue).toBeInTheDocument()
    expect(screen.getByText('DOC-001')).toBeInTheDocument()
    expect(screen.getByText('First Migration')).toBeInTheDocument()

    // Check link path
    const link = screen.getByRole('link', { name: /view details/i })
    expect(link).toHaveAttribute('href', '/migration/uuid-1')
  })

  it('displays empty state message when migrations are empty', async () => {
    vi.mocked(api.getMigrationStats).mockResolvedValue(mockStats as MigrationStats)
    vi.mocked(api.getMigrations).mockResolvedValue([])

    renderDashboard()

    const emptyMessage = await screen.findByText('No documents available')
    expect(emptyMessage).toBeInTheDocument()
  })

  it('handles API errors gracefully', async () => {
    // Prevent console.error clutter in test output
    const consoleSpy = vi.spyOn(console, 'error').mockImplementation(() => {})

    vi.mocked(api.getMigrationStats).mockRejectedValue(new Error('Stats Failed'))
    vi.mocked(api.getMigrations).mockRejectedValue(new Error('Migrations Failed'))

    renderDashboard()

    // Widgets should show "Unavailable"
    const errorPlaceholders = await screen.findAllByText('Unavailable')
    expect(errorPlaceholders).toHaveLength(4)

    // Check console was called (optional)
    expect(consoleSpy).toHaveBeenCalled()

    consoleSpy.mockRestore()
  })

  it('correctly calculates index-based row numbers', async () => {
    vi.mocked(api.getMigrationStats).mockResolvedValue(mockStats as MigrationStats)
    vi.mocked(api.getMigrations).mockResolvedValue([
      { ...mockMigrations[0], id: '1', docId: 'D1' },
      { ...mockMigrations[0], id: '2', docId: 'D2' },
    ])

    renderDashboard()

    // Wait for data to load
    await screen.findByText('D1')

    // Check row numbers (index + 1)
    expect(screen.getByText('1')).toBeInTheDocument()
    expect(screen.getByText('2')).toBeInTheDocument()
  })
})
