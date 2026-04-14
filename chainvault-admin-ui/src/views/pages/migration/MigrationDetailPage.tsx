/*
 * Copyright (c) 2026. Gryphus Lab
 */
import { Link, useParams } from 'react-router-dom'
import { useQuery } from '@tanstack/react-query'
import { ArrowLeft, Download, FileText } from 'lucide-react'

import { getMigrationDetail } from '../../../lib/api'
import type { MigrationDetail } from '../../../types'

import Timeline from '../../../components/Timeline'
import { Badge } from '../../../components/Badge'
import { safeFormat } from '../../../lib/utils'
import { CCard, CCardBody, CCardGroup, CCardHeader, CCol, CContainer, CRow } from '@coreui/react'

const STATUS_CLASSES: Record<string, string> = {
  SUCCESS: 'bg-green-100 text-green-800',
  FAILED: 'bg-red-100 text-red-800',
  RUNNING: 'bg-blue-100 text-blue-800',
  PENDING: 'bg-gray-100 text-gray-800',
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
 * Renders the migration detail page for the route `id`, loading migration data and showing a loading state, an error message,
 * or the full migration UI (header with status, stats grid, timeline, OCR/processing info, optional failure reason,
 * and optional download links).
 *
 * @returns The rendered React element for the migration detail page.
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

  const statusClass = STATUS_CLASSES[migration.status] ?? 'bg-gray-100 text-gray-800'
  const traceIDUrl =
    'http://localhost:3000/explore?schemaVersion=1&panes=%7B%224b3%22:%7B%22datasource%22:%22tempo%22,%22queries%22:%5B%7B%22query%22:%22' +
    migration.traceId +
    '%22,%22queryType%22:%22traceql%22,%22datasource%22:%7B%22type%22:%22tempo%22,%22uid%22:%22tempo%22%7D,%22refId%22:%22A%22,%22limit%22:20,' +
    '%22tableType%22:%22traces%22,%22metricsQueryType%22:%22range%22,%22serviceMapUseNativeHistograms%22:false%7D%5D,%22range%22:%7B%22from%22:' +
    '%22now-1h%22,%22to%22:%22now%22%7D,%22panelsState%22:%7B%22trace%22:%7B%22spanFilters%22:%7B%22spanNameOperator%22:%22%3D%22,%22' +
    'serviceNameOperator%22:%22%3D%22,%22fromOperator%22:%22%3E%22,%22toOperator%22:%22%3C%22,%22tags%22:%5B%7B%22id%22:%229d72bfd7-86e%22,' +
    '%22operator%22:%22%3D%22%7D%5D%7D%7D%7D,%22compact%22:false%7D%7D&orgId=1'
  return (
    <CContainer>
      <CRow className="justify-content-start">
        <Link to="/" className="text-gray-500 hover:text-gray-900" aria-label="Back to dashboard">
          <ArrowLeft className="h-6 w-6" />
        </Link>
        <div>
          <h2>Migration: {migration.id}</h2>
          <p>Title: {migration.title}</p>
        </div>
        <Badge className={statusClass}>{migration.status}</Badge>
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
                    <a href={traceIDUrl} target="_blank" rel="noopener noreferrer">
                      {migration.traceId || 'N/A'}
                    </a>
                  </CCardBody>
                </CCard>
              </CCardGroup>
              <Timeline events={migration.events} />
              <CCardGroup className="mt-4">
                <CCard>
                  <CCardHeader>
                    <FileText className="inline-block mr-2" />
                    OCR Text Preview
                  </CCardHeader>
                  <CCardBody>
                    <p>{migration.ocrTextPreview ?? 'No OCR information available.'}</p>{' '}
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
                          <a
                            href={migration.pdfUrl}
                            target="_blank"
                            rel="noopener noreferrer"
                            className="btn btn-primary me-2"
                          >
                            Download PDF
                          </a>
                        </p>
                      )}
                      {migration.chainZipUrl && (
                        <p>
                          <a
                            href={migration.chainZipUrl}
                            target="_blank"
                            rel="noopener noreferrer"
                            className="btn btn-primary"
                          >
                            Download ZIP
                          </a>
                        </p>
                      )}
                    </CCardBody>
                  </CCard>
                )}
              </CCardGroup>
            </CCardBody>
          </CCard>
        </CCol>
      </CRow>
    </CContainer>
  )
}
