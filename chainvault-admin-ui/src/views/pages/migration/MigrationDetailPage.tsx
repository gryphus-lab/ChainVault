/*
 * Copyright (c) 2026. Gryphus Lab
 */
import { Link, useParams } from 'react-router-dom'
import { useQuery } from '@tanstack/react-query'
import { ArrowLeft, Download, FileText } from 'lucide-react'

import { getMigrationDetail } from '../../../lib/api'
import type { MigrationDetail } from '../../../types'

import Timeline from '../../../components/Timeline'
import { safeFormat } from '../../../lib/utils'
import {
  CBadge,
  CCard,
  CCardBody,
  CCardGroup,
  CCardHeader,
  CCol,
  CContainer,
  CRow,
} from '@coreui/react'

const STATUS_CLASSES: Record<string, string> = {
  SUCCESS: 'success',
  FAILED: 'danger',
  RUNNING: 'warning',
  PENDING: 'warning',
}

/**
 * Validates that a URL string uses a safe scheme and is properly formatted.
 * Only allows http:, https:, and blob: schemes to prevent XSS attacks via javascript: URLs.
 *
 * @param urlString - The URL string to validate
 * @returns `true` if the URL is safe, `false` otherwise
 */
function isSafeUrl(urlString: string | null | undefined): boolean {
  if (!urlString) {
    return false
  }

  try {
    const url = new URL(urlString, globalThis.location.origin)
    const safeSchemes = ['http:', 'https:', 'blob:']
    return safeSchemes.includes(url.protocol)
  } catch {
    return false
  }
}

/**
 * Provide a user-facing label that indicates whether OCR succeeded for a migration.
 *
 * @param migration - The migration record whose OCR result will be used
 * @returns `"✅ Yes"` when `migration.ocrSuccess` is `true`, `"❌ No"` otherwise
 */
function getOcrSuccessLabel(migration: MigrationDetail) {
  return migration.ocrSuccess ? '✅ Yes' : '❌ No'
}

/**
 * Display the migration detail page for the current route `id`, fetching migration data and showing either a loading state, an error message, or the detailed migration UI.
 *
 * @returns The React element representing the migration detail page.
 */
export default function MigrationDetailPage() {
  const { id } = useParams<{ id: string }>()

  const {
    data: migration,
    isLoading,
    isError,
  } = useQuery<MigrationDetail, Error>({
    queryKey: ['migration-detail', id],
    queryFn: (): Promise<MigrationDetail> => getMigrationDetail(id!),
    enabled: !!id,
    staleTime: 30 * 1000,
    retry: 2,
  })

  if (isLoading) {
    return <div className="text-center py-20 text-gray-600">Loading migration details...</div>
  }

  if (isError || !migration) {
    return (
      <div className="text-center py-20">
        <p className="text-red-600 text-lg">Failed to load migration details for ID: {id}</p>
        <p className="text-gray-500 mt-2">
          The migration may not exist or there was a server error.
        </p>
        <Link to="/" className="text-blue-600 hover:underline mt-6 inline-block text-lg">
          ← Back to Dashboard
        </Link>
      </div>
    )
  }

  const textBgColorClass = STATUS_CLASSES[migration.status] ?? 'secondary'
  const tempoExploreUrlTemplate = import.meta.env.VITE_TEMPO_EXPLORE_URL
  const traceIDUrl =
    migration.traceId && tempoExploreUrlTemplate?.includes('{traceId}')
      ? tempoExploreUrlTemplate.replace('{traceId}', encodeURIComponent(migration.traceId))
      : null
  return (
    <CContainer>
      <CRow className="justify-content-start">
        <Link to="/" className="text-gray-500 hover:text-gray-900" aria-label="Back to dashboard">
          <ArrowLeft className="h-6 w-6" />
        </Link>
        <div>
          <h2>Migration: {migration.id}</h2>
          <CBadge textBgColor={textBgColorClass} shape="rounded-pill">
            {migration.status}
          </CBadge>
        </div>
      </CRow>
      <CRow className="justify-content-start">
        <CCol md={8}>
          <CCard className="mb-4">
            <CCardBody>
              <CCardGroup className="mb-4">
                <CCard>
                  <CCardHeader>DocId</CCardHeader>
                  <CCardBody>{migration.docId}</CCardBody>
                </CCard>
                <CCard>
                  <CCardHeader>Created At</CCardHeader>
                  <CCardBody>{safeFormat(migration.createdAt)}</CCardBody>
                </CCard>
                <CCard>
                  <CCardHeader>Updated At</CCardHeader>
                  <CCardBody>{safeFormat(migration.updatedAt)}</CCardBody>
                </CCard>
                <CCard>
                  <CCardHeader>Trace Id</CCardHeader>
                  <CCardBody>
                    {migration.traceId && traceIDUrl ? (
                      <a href={traceIDUrl} target="_blank" rel="noopener noreferrer">
                        {migration.traceId}
                      </a>
                    ) : (
                      'N/A'
                    )}
                  </CCardBody>
                </CCard>
              </CCardGroup>
              <CCardGroup className="mt-4">
                <CCard>
                  <CCardHeader>
                    <FileText className="inline-block mr-2" />
                    OCR Text Preview
                  </CCardHeader>
                  <CCardBody>
                    <p>{migration.ocrTextPreview ?? 'No OCR information available.'}</p>
                  </CCardBody>
                </CCard>
                <CCard>
                  <CCardHeader>OCR Details</CCardHeader>
                  <CCardBody>
                    <p>
                      <strong>OCR Attempted:</strong> {migration.ocrAttempted ? 'Yes' : 'No'}
                    </p>
                    <p>
                      <strong>OCR Success:</strong>{' '}
                      {migration.ocrAttempted ? getOcrSuccessLabel(migration) : 'N/A'}
                    </p>
                    {migration.ocrPageCount !== undefined && migration.ocrPageCount !== null && (
                      <p>
                        <strong>Pages Processed:</strong> {migration.ocrPageCount}
                      </p>
                    )}
                    {migration.ocrTotalTextLength !== undefined &&
                      migration.ocrTotalTextLength !== null && (
                        <p>
                          <strong>Text Length:</strong>{' '}
                          {migration.ocrTotalTextLength.toLocaleString()} chars
                        </p>
                      )}
                    {migration.failureReason && (
                      <p>
                        <strong>Failure Reason:</strong> {migration.failureReason}
                      </p>
                    )}
                  </CCardBody>
                </CCard>
                {(migration.pdfUrl || migration.chainZipUrl) && (
                  <CCard>
                    <CCardHeader>
                      <Download className="inline-block mr-2" />
                      Download
                    </CCardHeader>
                    <CCardBody>
                      {migration.pdfUrl && (
                        <p>
                          {isSafeUrl(migration.pdfUrl) ? (
                            <a
                              href={migration.pdfUrl}
                              target="_blank"
                              rel="noopener noreferrer"
                              className="btn btn-primary me-2"
                            >
                              Download PDF
                            </a>
                          ) : (
                            <button disabled className="btn btn-primary me-2">
                              Download PDF (Invalid URL)
                            </button>
                          )}
                        </p>
                      )}
                      {migration.chainZipUrl && (
                        <p>
                          {isSafeUrl(migration.chainZipUrl) ? (
                            <a
                              href={migration.chainZipUrl}
                              target="_blank"
                              rel="noopener noreferrer"
                              className="btn btn-primary"
                            >
                              Download ZIP
                            </a>
                          ) : (
                            <button disabled className="btn btn-primary">
                              Download ZIP (Invalid URL)
                            </button>
                          )}
                        </p>
                      )}
                    </CCardBody>
                  </CCard>
                )}
              </CCardGroup>
              <CCard className="mb-4 shadow-sm">
                <CCardHeader className="fw-bold">Timeline</CCardHeader>
                <CCardBody>
                  {migration.events && migration.events.length > 0 ? (
                    <Timeline events={migration.events} />
                  ) : (
                    <div className="text-center py-4 text-muted">
                      No events recorded for this migration.
                    </div>
                  )}
                </CCardBody>
              </CCard>
            </CCardBody>
          </CCard>
        </CCol>
      </CRow>
    </CContainer>
  )
}