/*
 * Copyright (c) 2026. Gryphus Lab
 */
import { render, screen, waitFor } from '@testing-library/react'
import { describe, expect, it, vi, beforeEach } from 'vitest'
import { MemoryRouter, Route, Routes } from 'react-router-dom'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import MigrationDetailPage from '../views/pages/migration/MigrationDetailPage'
import * as api from '../lib/api'
import type { MigrationDetail } from '../types'

// Mock the API module
vi.mock('../lib/api', () => ({
  getMigrationDetail: vi.fn(),
}))

/**
 * Helper to create a fresh QueryClient for every test to prevent
 * cache bleeding between success and error states.
 */
const createTestQueryClient = () =>
  new QueryClient({
    defaultOptions: {
      queries: {
        retry: false,
        gcTime: 0,
      },
    },
  })

const mockMigrationData: MigrationDetail = {
  id: 'MIG-123',
  status: 'SUCCESS',
  docId: 'DOC-999',
  createdAt: '2026-01-01T10:00:00Z',
  updatedAt: '2026-01-01T11:00:00Z',
  processInstanceKey: 'proc-123',
  pageCount: 10,
  traceId: 'trace-abc-123',
  events: [
    {
      id: 'evt-1',
      migrationId: 'MIG-123',
      eventType: 'TASK_STARTED',
      taskType: 'Initialization',
      message: 'Starting migration...',
      createdAt: '2026-01-01T10:00:00Z',
      timestamp: '2026-01-01T10:00:00Z',
    },
    {
      id: 'evt-2',
      migrationId: 'MIG-123',
      eventType: 'TASK_COMPLETED',
      taskType: 'OCR Processing',
      message: 'OCR finished successfully',
      createdAt: '2026-01-01T10:05:00Z',
      timestamp: '2026-01-01T10:05:00Z',
    },
  ],
  pdfUrl: 'https://example.com/file.pdf',
  ocrAttempted: true,
  ocrSuccess: true,
  ocrPageCount: 5,
  ocrTotalTextLength: 1250,
}

const renderComponent = (id = 'MIG-123') => {
  return render(
    <QueryClientProvider client={createTestQueryClient()}>
      <MemoryRouter initialEntries={[`/migration/${id}`]}>
        <Routes>
          <Route path="/migration/:id" element={<MigrationDetailPage />} />
        </Routes>
      </MemoryRouter>
    </QueryClientProvider>,
  )
}

describe('MigrationDetailPage', () => {
  beforeEach(() => {
    vi.resetAllMocks()
  })

  it('renders loading state initially', () => {
    // Return a promise that never resolves to stay in loading state
    vi.mocked(api.getMigrationDetail).mockReturnValue(new Promise(() => {}))
    renderComponent()
    expect(screen.getByText(/loading migration details/i)).toBeInTheDocument()
  })

  it('renders error state when API fails', async () => {
    const consoleSpy = vi.spyOn(console, 'error').mockImplementation(() => {})

    // Use a specific error object
    vi.mocked(api.getMigrationDetail).mockRejectedValue(new Error('API Failure'))

    renderComponent('ERROR-ID')

    // Increase the timeout slightly and wait for the "Loading" to disappear
    // This gives the state machine time to move from Loading -> Error
    await waitFor(
      () => {
        const loadingElement = screen.queryByText(/loading/i)
        expect(loadingElement).toBeNull()
      },
      { timeout: 5000 },
    )

    // Now find the error message
    const errorMsg = await screen.findByText(/failed to load migration details/i)
    expect(errorMsg).toBeInTheDocument()

    consoleSpy.mockRestore()
  })

  it('renders full migration details and timeline successfully', async () => {
    // Resolve with the fully typed mockMigrationData
    vi.mocked(api.getMigrationDetail).mockResolvedValue(mockMigrationData)

    renderComponent()

    expect(await screen.findByText(/Migration: MIG-123/i)).toBeInTheDocument()

    const attemptedField = screen.getByText(/OCR Attempted:/i).parentElement
    expect(attemptedField).toHaveTextContent(/Yes/i)

    const successField = screen.getByText(/OCR Success:/i).parentElement
    expect(successField).toHaveTextContent(/✅ Yes/i)

    expect(screen.getByText('Initialization')).toBeInTheDocument()
    expect(screen.getByText('OCR Processing')).toBeInTheDocument()
    expect(screen.getByText('+300s')).toBeInTheDocument()
  })

  it('handles failed migration status with failure reasons', async () => {
    const failedMigration: MigrationDetail = {
      ...mockMigrationData,
      status: 'FAILED',
      ocrSuccess: false,
      failureReason: 'Engine Timeout Error',
    }

    vi.mocked(api.getMigrationDetail).mockResolvedValue(failedMigration)

    renderComponent()

    expect(await screen.findByText('FAILED')).toBeInTheDocument()
    expect(screen.getByText(/Engine Timeout Error/i)).toBeInTheDocument()
  })
})
