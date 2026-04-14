/*
 * Copyright (c) 2026. Gryphus Lab
 */
import { describe, it, expect, vi, beforeEach } from 'vitest'
import { render, screen } from '@testing-library/react'
import MigrationDetailPage from './MigrationDetailPage'

// --- mocks ---

vi.mock('react-router-dom', () => ({
  Link: ({ children }: any) => <a>{children}</a>,
  useParams: vi.fn(),
}))

vi.mock('@tanstack/react-query', () => ({
  useQuery: vi.fn(),
}))

vi.mock('../../../components/Timeline', () => ({
  default: ({ events }: any) => <div>Timeline ({events.length})</div>,
}))

vi.mock('../../../lib/utils', () => ({
  safeFormat: vi.fn((d) => `formatted-${d}`),
}))

// Mock CoreUI
vi.mock('@coreui/react', () => ({
  CBadge: ({ children }: any) => <div>{children}</div>,
  CContainer: ({ children }: any) => <div>{children}</div>,
  CRow: ({ children }: any) => <div>{children}</div>,
  CCol: ({ children }: any) => <div>{children}</div>,
  CCard: ({ children }: any) => <div>{children}</div>,
  CCardHeader: ({ children }: any) => <div>{children}</div>,
  CCardBody: ({ children }: any) => <div>{children}</div>,
  CCardGroup: ({ children }: any) => <div>{children}</div>,
}))

import { useParams } from 'react-router-dom'
import { useQuery } from '@tanstack/react-query'

describe('MigrationDetailPage', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    ;(useParams as any).mockReturnValue({ id: '123' })
  })

  it('renders loading state', () => {
    ;(useQuery as any).mockReturnValue({
      isLoading: true,
    })

    render(<MigrationDetailPage />)

    expect(screen.getByText('Loading migration details...')).toBeInTheDocument()
  })

  it('renders error state', () => {
    ;(useQuery as any).mockReturnValue({
      isLoading: false,
      isError: true,
      data: undefined,
    })

    render(<MigrationDetailPage />)

    expect(screen.getByText(/Failed to load migration details/i)).toBeInTheDocument()

    expect(screen.getByText(/Back to Dashboard/i)).toBeInTheDocument()
  })

  it('renders migration details with no events', () => {
    ;(useQuery as any).mockReturnValue({
      isLoading: false,
      isError: false,
      data: {
        id: '123',
        status: 'SUCCESS',
        docId: 'DOC-1',
        createdAt: '2024-01-01',
        updatedAt: '2024-01-02',
        traceId: 'trace-123',
        events: [],
        ocrTextPreview: 'OCR TEXT',
        ocrAttempted: true,
        ocrSuccess: true,
        ocrPageCount: 10,
        ocrTotalTextLength: 5000,
        failureReason: null,
        pdfUrl: 'http://pdf',
        chainZipUrl: 'http://zip',
      },
    })

    render(<MigrationDetailPage />)

    expect(screen.getByText('Migration: 123')).toBeInTheDocument()

    expect(screen.getByText('DOC-1')).toBeInTheDocument()
    expect(screen.getByText('formatted-2024-01-01')).toBeInTheDocument()
    expect(screen.getByText('formatted-2024-01-02')).toBeInTheDocument()

    expect(screen.getByText('trace-123')).toBeInTheDocument()

    expect(screen.getByText('OCR TEXT')).toBeInTheDocument()
    expect(screen.getByText('Yes')).toBeInTheDocument()
    expect(screen.getByText('✅ Yes')).toBeInTheDocument()

    expect(screen.getByText('10')).toBeInTheDocument()
    expect(screen.getByText('5,000 chars')).toBeInTheDocument()

    expect(screen.getByText('Download PDF')).toBeInTheDocument()
    expect(screen.getByText('Download ZIP')).toBeInTheDocument()

    expect(screen.getByText('Timeline')).toBeInTheDocument()
    expect(screen.getByText('No events recorded for this migration.')).toBeInTheDocument()
  })

  it('handles missing optional fields', () => {
    ;(useQuery as any).mockReturnValue({
      isLoading: false,
      isError: false,
      data: {
        id: '123',
        title: 'Test Migration',
        status: 'FAILED',
        docId: 'DOC-1',
        createdAt: '2024-01-01',
        updatedAt: '2024-01-02',
        traceId: null,
        events: [],
        ocrTextPreview: null,
        ocrAttempted: false,
        ocrSuccess: false,
        ocrPageCount: null,
        ocrTotalTextLength: null,
        failureReason: 'Something broke',
        pdfUrl: null,
        chainZipUrl: null,
      },
    })

    render(<MigrationDetailPage />)

    expect(screen.getAllByText('N/A')).toHaveLength(2)
    expect(screen.getByText('No OCR information available.')).toBeInTheDocument()

    expect(screen.getByText('No')).toBeInTheDocument()

    expect(screen.getByText('Something broke')).toBeInTheDocument()

    // No downloads
    expect(screen.queryByText('Download PDF')).not.toBeInTheDocument()
    expect(screen.queryByText('Download ZIP')).not.toBeInTheDocument()
  })
})
