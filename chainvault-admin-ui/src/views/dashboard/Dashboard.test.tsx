/*
 * Copyright (c) 2026. Gryphus Lab
 */
import { render, screen, waitFor, fireEvent, within } from '@testing-library/react'
import { describe, it, expect, vi, beforeEach } from 'vitest'
import { MemoryRouter } from 'react-router-dom'
import Dashboard from './Dashboard'
import * as api from '../../lib/api'
import { Migration, MigrationStats } from '../../types'

// Mock API module
vi.mock('../../lib/api', () => ({
  getMigrations: vi.fn(),
  getMigrationStats: vi.fn(),
}))

const mockStats: {
  total: number
  success: number
  failed: number
  pending: number
  running: number
} = {
  total: 25, // Forces 3 pages with pageSize 10
  success: 15,
  failed: 5,
  pending: 3,
  running: 2,
}

const mockMigrations: Migration[] = [
  {
    id: 'M-001',
    docId: 'DOC-ABC',
    status: 'SUCCESS',
    createdAt: '2026-01-01T10:00:00Z',
    updatedAt: '2026-01-01T11:00:00Z',
    processInstanceKey: '',
    pageCount: 0,
    ocrAttempted: false,
  },
  {
    id: 'M-002',
    docId: 'DOC-XYZ',
    status: 'FAILED',
    createdAt: '2026-01-02T10:00:00Z',
    updatedAt: '2026-01-02T11:00:00Z',
    processInstanceKey: '',
    pageCount: 0,
    ocrAttempted: false,
  },
]

describe('Dashboard Component', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    // Default successful mocks
    vi.mocked(api.getMigrationStats).mockResolvedValue(mockStats as MigrationStats)
    vi.mocked(api.getMigrations).mockResolvedValue(mockMigrations)
  })

  const renderDashboard = () =>
    render(
      <MemoryRouter>
        <Dashboard />
      </MemoryRouter>,
    )

  it('displays loading state and then renders statistics', async () => {
    renderDashboard()

    expect(screen.getByText(/loading migration records/i)).toBeInTheDocument()

    await waitFor(() => {
      expect(screen.getAllByText('25')).toHaveLength(2) // Total
      expect(screen.getAllByText('5')).toHaveLength(2) // In Progress (3+2)
    })
  })

  it('renders table data with correct status badges', async () => {
    renderDashboard()

    const row = await screen.findByRole('row', { name: /DOC-ABC/i })
    expect(within(row).getByText('SUCCESS')).toHaveClass('bg-success-light')
    expect(screen.getByText('DOC-XYZ')).toBeInTheDocument()
  })

  it('handles independent API failures using allSettled logic', async () => {
    // Stats fail, but Migrations succeed
    vi.mocked(api.getMigrationStats).mockRejectedValue(new Error('Stats Failed'))
    vi.mocked(api.getMigrations).mockResolvedValue(mockMigrations)

    renderDashboard()

    // Should see stats error alert
    expect(await screen.findByText(/failed to load migration statistics/i)).toBeInTheDocument()
    // Stats widgets should fallback to "Unavailable" per getDisplayValue
    expect(screen.getAllByText('Unavailable')).toHaveLength(4)
    // Table should still render successfully
    expect(screen.getByText('DOC-ABC')).toBeInTheDocument()
  })

  it('triggers server-side pagination refetch on page change', async () => {
    renderDashboard()

    await screen.findByText('DOC-ABC')

    // Find the pagination item with the text "2"
    const page2 = screen.getByText('2')
    fireEvent.click(page2)

    // Verify API called with offset 10 (Page 2)
    expect(api.getMigrations).toHaveBeenCalledWith({ limit: 10, offset: 10 })
  })

  it('cycles through sort states and updates ARIA attributes', async () => {
    renderDashboard()

    // Find the header button for Doc ID
    const sortBtn = await screen.findByLabelText(/sort by document id/i)
    const headerCell = sortBtn.closest('th')

    // Initial state (from code: default is createdAt desc, so docId is 'none')
    expect(headerCell).toHaveAttribute('aria-sort', 'none')

    // 1st Click: Ascending
    fireEvent.click(sortBtn)
    expect(headerCell).toHaveAttribute('aria-sort', 'ascending')

    // 2nd Click: Descending
    fireEvent.click(sortBtn)
    expect(headerCell).toHaveAttribute('aria-sort', 'descending')

    // Check sorting logic: XYZ should now be above ABC
    const rows = screen.getAllByRole('row')
    expect(rows[1]).toHaveTextContent('DOC-XYZ')
    expect(rows[2]).toHaveTextContent('DOC-ABC')
  })

  it('renders ellipsis in pagination when many pages exist', async () => {
    // Mock high total to trigger ellipsis logic (maxPagesToShow = 7)
    vi.mocked(api.getMigrationStats).mockResolvedValue({
      ...mockStats,
      total: 200,
      last24h: 0,
    })

    renderDashboard()

    await waitFor(() => {
      // Find the disabled ellipsis items
      const ellipses = screen.getAllByText('...')
      expect(ellipses.length).toBeGreaterThan(0)
    })
  })

  it('displays fallback message for empty result sets', async () => {
    vi.mocked(api.getMigrations).mockResolvedValue([])

    renderDashboard()

    expect(await screen.findByText(/no migration data found/i)).toBeInTheDocument()
  })
})
