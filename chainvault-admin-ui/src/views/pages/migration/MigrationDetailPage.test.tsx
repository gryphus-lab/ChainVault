/*
 * Copyright (c) 2026. Gryphus Lab
 */
import { render, screen } from '@testing-library/react'
import { describe, expect, it, vi } from 'vitest'
import { useQuery } from '@tanstack/react-query'
import { MemoryRouter } from 'react-router-dom'
import MigrationDetailPage from './MigrationDetailPage'
import type { MigrationDetail } from '../../../types'

// Mock useQuery directly so we control loading/error/success states without
// dealing with retry delays from the component's retry: 2 option.
vi.mock('@tanstack/react-query', async (importOriginal) => {
  const actual = await importOriginal<typeof import('@tanstack/react-query')>()
  return { ...actual, useQuery: vi.fn() }
})

vi.mock('react-router-dom', async (importOriginal) => {
  const actual = await importOriginal<typeof import('react-router-dom')>()
  return { ...actual, useParams: () => ({ id: 'test-123' }) }
})

const mockUseQuery = vi.mocked(useQuery)

const mockMigration: MigrationDetail = {
  id: 'test-123',
  docId: 'doc-abc',
  title: 'Test Migration',
  status: 'SUCCESS',
  createdAt: '2026-01-01T10:00:00Z',
  updatedAt: '2026-01-01T10:05:00Z',
  processInstanceKey: 'pik-1',
  traceId: 'trace-xyz',
  pageCount: 3,
  ocrAttempted: true,
  ocrSuccess: true,
  ocrPageCount: 3,
  ocrTotalTextLength: 1500,
  events: [
    {
      id: 'evt-1',
      migrationId: 'test-123',
      eventType: 'TASK_COMPLETED',
      stepName: 'SftpUpload',
      message: 'Upload complete',
      timestamp: '2026-01-01T10:05:00Z',
    },
  ],
}

function queryLoading() {
  mockUseQuery.mockReturnValue({ isLoading: true, isError: false, data: undefined } as any)
}

function queryError() {
  mockUseQuery.mockReturnValue({ isLoading: false, isError: true, data: undefined } as any)
}

function querySuccess(data: MigrationDetail = mockMigration) {
  mockUseQuery.mockReturnValue({ isLoading: false, isError: false, data } as any)
}

describe('MigrationDetailPage', () => {
  it('shows the loading state', () => {
    queryLoading()
    render(
      <MemoryRouter>
        <MigrationDetailPage />
      </MemoryRouter>,
    )
    expect(screen.getByText('Loading migration details...')).toBeInTheDocument()
  })

  it('shows the error state with a back link', () => {
    queryError()
    render(
      <MemoryRouter>
        <MigrationDetailPage />
      </MemoryRouter>,
    )
    expect(
      screen.getByText(/Failed to load migration details for ID: test-123/),
    ).toBeInTheDocument()
    const backLink = screen.getByRole('link', { name: /back to dashboard/i })
    expect(backLink).toHaveAttribute('href', '/')
  })

  describe('success state', () => {
    it('renders the migration id, title, and status badge', () => {
      querySuccess()
      render(
        <MemoryRouter>
          <MigrationDetailPage />
        </MemoryRouter>,
      )
      expect(screen.getByText('Migration test-123')).toBeInTheDocument()
      expect(screen.getByText('Test Migration')).toBeInTheDocument()
      expect(screen.getByText('SUCCESS')).toBeInTheDocument()
    })

    it('renders the stats grid with docId, created date, and traceId', () => {
      querySuccess()
      render(
        <MemoryRouter>
          <MigrationDetailPage />
        </MemoryRouter>,
      )
      expect(screen.getByText('doc-abc')).toBeInTheDocument()
      expect(screen.getByText('trace-xyz')).toBeInTheDocument()
      expect(screen.getByText('Document ID')).toBeInTheDocument()
      expect(screen.getByText('Created')).toBeInTheDocument()
    })

    it('renders the migration timeline with events', () => {
      querySuccess()
      render(
        <MemoryRouter>
          <MigrationDetailPage />
        </MemoryRouter>,
      )
      expect(screen.getByText('Migration Timeline')).toBeInTheDocument()
      expect(screen.getByText('SftpUpload')).toBeInTheDocument()
    })

    it('renders OCR details section', () => {
      querySuccess()
      render(
        <MemoryRouter>
          <MigrationDetailPage />
        </MemoryRouter>,
      )
      expect(screen.getByText('OCR & Processing')).toBeInTheDocument()
      expect(screen.getByText(/OCR Attempted/)).toBeInTheDocument()
      expect(screen.getByText(/Pages Processed/)).toBeInTheDocument()
      expect(screen.getByText(/Text Length/)).toBeInTheDocument()
    })

    it('renders failure reason when present', () => {
      querySuccess({ ...mockMigration, status: 'FAILED', failureReason: 'SFTP timeout' })
      render(
        <MemoryRouter>
          <MigrationDetailPage />
        </MemoryRouter>,
      )
      expect(screen.getByText(/SFTP timeout/)).toBeInTheDocument()
    })

    it('does not render failure reason section when absent', () => {
      querySuccess()
      render(
        <MemoryRouter>
          <MigrationDetailPage />
        </MemoryRouter>,
      )
      expect(screen.queryByText('Failure Reason')).not.toBeInTheDocument()
    })

    it('renders download links when both URLs are present', () => {
      querySuccess({
        ...mockMigration,
        chainZipUrl: 'http://example.com/chain.zip',
        pdfUrl: 'http://example.com/output.pdf',
      })
      render(
        <MemoryRouter>
          <MigrationDetailPage />
        </MemoryRouter>,
      )
      expect(screen.getByText('Downloads')).toBeInTheDocument()
      expect(screen.getByRole('link', { name: /chain zip/i })).toHaveAttribute(
        'href',
        'http://example.com/chain.zip',
      )
      expect(screen.getByRole('link', { name: /merged pdf/i })).toHaveAttribute(
        'href',
        'http://example.com/output.pdf',
      )
    })

    it('renders only the available download link when one URL is missing', () => {
      querySuccess({ ...mockMigration, chainZipUrl: 'http://example.com/chain.zip' })
      render(
        <MemoryRouter>
          <MigrationDetailPage />
        </MemoryRouter>,
      )
      expect(screen.getByRole('link', { name: /chain zip/i })).toBeInTheDocument()
      expect(screen.queryByRole('link', { name: /merged pdf/i })).not.toBeInTheDocument()
    })

    it('hides the downloads section when no URLs are present', () => {
      querySuccess()
      render(
        <MemoryRouter>
          <MigrationDetailPage />
        </MemoryRouter>,
      )
      expect(screen.queryByText('Downloads')).not.toBeInTheDocument()
    })
  })
})
