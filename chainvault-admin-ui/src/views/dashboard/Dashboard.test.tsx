/*
 * Copyright (c) 2026. Gryphus Lab
 */
import { render, screen, waitFor } from '@testing-library/react'
import { describe, expect, it, vi, beforeEach } from 'vitest'
import Dashboard from './Dashboard'
import * as api from '../../lib/api'
import { MigrationStats } from '../../types'

// Mock the API module
vi.mock('../../lib/api', () => ({
  getMigrations: vi.fn(),
  getMigrationStats: vi.fn(),
}))

const mockStats: MigrationStats = {
  total: 10,
  pending: 2,
  running: 1,
  success: 5,
  failed: 2,
  last24h: 7,
}

const mockMigrations = [
  {
    id: '550e8400-e29b-41d4-a716-446655440000',
    docId: 'DOC-101',
    title: 'Migration One',
    status: 'SUCCESS',
    createdAt: '2026-01-01T10:00:00Z',
    updatedAt: '2026-01-01T11:00:00Z',
    processInstanceKey: 'proc-123',
    pageCount: 5,
    ocrAttempted: true,
    ocrSuccess: true,
  },
]

describe('Dashboard Component', () => {
  beforeEach(() => {
    vi.resetAllMocks()
  })

  it('shows loading placeholders initially', async () => {
    // Delay the stats response to check loading state
    vi.mocked(api.getMigrationStats).mockReturnValue(new Promise(() => {}))
    vi.mocked(api.getMigrations).mockResolvedValue([])

    render(<Dashboard />)

    const totalWidgets = screen.getAllByText('—')
    expect(totalWidgets).toHaveLength(4)
  })

  it('renders stats and table data successfully', async () => {
    vi.mocked(api.getMigrationStats).mockResolvedValue(mockStats)
    vi.mocked(api.getMigrations).mockResolvedValue(mockMigrations)

    render(<Dashboard />)

    // Check Stats Widgets
    // Total: 10, In Progress: 2+1=3, Success: 5, Failed: 2
    await waitFor(() => {
      expect(screen.getByText('10')).toBeInTheDocument()
      expect(screen.getByText('3')).toBeInTheDocument()
      expect(screen.getByText('5')).toBeInTheDocument()
      expect(screen.getByText('2')).toBeInTheDocument()
    })

    // Check Table Data
    await waitFor(() => {
      expect(screen.getByText('1')).toBeInTheDocument() // Row index
      expect(screen.getByText('DOC-101')).toBeInTheDocument()
      expect(screen.getByText('Migration One')).toBeInTheDocument()
      expect(screen.getByText('SUCCESS')).toBeInTheDocument()
    })
  })

  it('renders empty state when no migrations are returned', async () => {
    vi.mocked(api.getMigrationStats).mockResolvedValue(mockStats)
    vi.mocked(api.getMigrations).mockResolvedValue([])

    render(<Dashboard />)

    await waitFor(() => {
      expect(screen.getByText('No documents available')).toBeInTheDocument()
    })
  })

  it('displays "Unavailable" in widgets when API fails', async () => {
    // Suppress console.error for this test
    const consoleSpy = vi.spyOn(console, 'error').mockImplementation(() => {})
    try {
      vi.mocked(api.getMigrationStats).mockRejectedValue(new Error('API Error'))
      vi.mocked(api.getMigrations).mockResolvedValue([])

      render(<Dashboard />)

      await waitFor(() => {
        const errorStates = screen.getAllByText('Unavailable')
        expect(errorStates.length).toBe(4)
      })
    } finally {
      consoleSpy.mockRestore()
    }
  })

  it('calculates "In Progress" as sum of pending and running', async () => {
    const customStats: MigrationStats = {
      total: 20,
      pending: 10,
      running: 5,
      success: 3,
      failed: 2,
      last24h: 12,
    }
    vi.mocked(api.getMigrationStats).mockResolvedValue(customStats)
    vi.mocked(api.getMigrations).mockResolvedValue([])

    render(<Dashboard />)

    await waitFor(() => {
      // 10 (pending) + 5 (running) = 15
      expect(screen.getByText('15')).toBeInTheDocument()
    })
  })
})
