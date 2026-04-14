/*
 * Copyright (c) 2026. Gryphus Lab
 */
import { useEffect, useMemo, useState } from 'react'
import { Link } from 'react-router-dom'
import {
  CCol,
  CPagination,
  CPaginationItem,
  CRow,
  CSpinner,
  CTable,
  CTableBody,
  CTableDataCell,
  CTableHead,
  CTableHeaderCell,
  CTableRow,
  CWidgetStatsB,
} from '@coreui/react'
import { ChevronDown, ChevronsUpDown, ChevronUp } from 'lucide-react'
import { getMigrations, getMigrationStats } from '../../lib/api'
import { Migration, MigrationStats } from '../../types'
import { safeFormat } from '../../lib/utils'

type SortDirection = 'asc' | 'desc' | null

function getBadgeColor(m: Migration) {
  switch (m.status) {
    case 'SUCCESS':
      return 'bg-success-light text-success'
    case 'FAILED':
      return 'bg-danger-light text-danger'
    case 'PENDING':
      return 'bg-warning-light text-warning'
    case 'RUNNING':
      return 'bg-info-light text-info'
    default:
      return 'bg-light text-dark'
  }
}

function getTableRows(currentMigrations: Migration[]) {
  return currentMigrations.length === 0 ? (
    <CTableRow>
      <CTableDataCell colSpan={6} className="text-center py-5 text-muted">
        No migration data found.
      </CTableDataCell>
    </CTableRow>
  ) : (
    currentMigrations.map((m) => (
      <CTableRow key={m.id}>
        <CTableDataCell className="small text-muted">{m.id}</CTableDataCell>
        <CTableDataCell className="fw-semibold">{m.docId}</CTableDataCell>
        <CTableDataCell>
          <span className={`badge ${getBadgeColor(m)}`}>{m.status}</span>
        </CTableDataCell>
        <CTableDataCell>{safeFormat(m.createdAt)}</CTableDataCell>
        <CTableDataCell>{safeFormat(m.updatedAt)}</CTableDataCell>
        <CTableDataCell className="text-center">
          <Link to={`/migration/${m.id}`} className="btn btn-sm btn-outline-primary px-3">
            View Details
          </Link>
        </CTableDataCell>
      </CTableRow>
    ))
  )
}

function getSortOrder(sortDir: 'asc' | 'desc' | null) {
  return sortDir === 'asc' ? 'ascending' : 'descending'
}

function computePaginationPages(
  totalPages: number,
  currentPage: number,
  maxPagesToShow: number,
): (number | string)[] {
  const pages: (number | string)[] = []

  if (totalPages <= maxPagesToShow) {
    // Show all pages if total is small
    for (let i = 1; i <= totalPages; i++) {
      pages.push(i)
    }
  } else {
    // Always show first page
    pages.push(1)

    // Calculate range around current page
    const leftBound = Math.max(2, currentPage - 1)
    const rightBound = Math.min(totalPages - 1, currentPage + 1)

    // Add left ellipsis if needed
    if (leftBound > 2) {
      pages.push('ellipsis-left')
    }

    // Add pages around current page
    for (let i = leftBound; i <= rightBound; i++) {
      pages.push(i)
    }

    // Add right ellipsis if needed
    if (rightBound < totalPages - 1) {
      pages.push('ellipsis-right')
    }

    // Always show last page
    pages.push(totalPages)
  }

  return pages
}

const Dashboard = () => {
  // Data State
  const [migrations, setMigrations] = useState<Migration[] | null>(null)
  const [migrationStats, setMigrationStats] = useState<MigrationStats | null>(null)
  const [isLoading, setIsLoading] = useState(true)
  const [statsError, setStatsError] = useState<string | null>(null)
  const [migrationsError, setMigrationsError] = useState<string | null>(null)

  // Pagination & Sort State
  const [currentPage, setCurrentPage] = useState(1)
  const [sortKey, setSortKey] = useState<keyof Migration | null>('createdAt')
  const [sortDir, setSortDir] = useState<SortDirection>('desc')
  const pageSize = 10

  // Sortable header component
  const renderSortableHeader = (
    sortKeyName: keyof Migration,
    label: string,
    style?: React.CSSProperties,
  ) => {
    return (
      <CTableHeaderCell
        style={style}
        aria-sort={sortKey === sortKeyName ? getSortOrder(sortDir) : 'none'}
      >
        <button
          type="button"
          onClick={() => handleSort(sortKeyName)}
          style={{ cursor: 'pointer', background: 'none', border: 'none', padding: 0 }}
          className="user-select-none"
          aria-label={`Sort by ${label}`}
        >
          {label} {getSortIcon(sortKeyName)}
        </button>
      </CTableHeaderCell>
    )
  }

  useEffect(() => {
    const fetchData = async () => {
      setIsLoading(true)
      setStatsError(null)
      setMigrationsError(null)

      // Fetch endpoints independently so one failure doesn't discard the other result
      const results = await Promise.allSettled([
        getMigrationStats(),
        getMigrations({ limit: pageSize, offset: (currentPage - 1) * pageSize }),
      ])

      // Handle stats result
      if (results[0].status === 'fulfilled') {
        setMigrationStats(results[0].value)
      } else {
        console.error('Migration stats fetch error:', results[0].reason)
        setStatsError('Failed to load migration statistics.')
      }

      // Handle migrations result
      if (results[1].status === 'fulfilled') {
        setMigrations(results[1].value)
      } else {
        console.error('Migrations fetch error:', results[1].reason)
        setMigrationsError('Failed to load migration records.')
      }

      setIsLoading(false)
    }
    fetchData()
  }, [currentPage, pageSize])

  const getDisplayValue = (value: number | undefined) => {
    if (isLoading) return '—'
    if (statsError) return 'Unavailable'
    return value?.toString() ?? '0'
  }

  const sortedMigrations = useMemo(() => {
    if (!migrations) return []
    if (!sortKey || !sortDir) return migrations

    return [...migrations].sort((a, b) => {
      const aValue = a[sortKey] ?? ''
      const bValue = b[sortKey] ?? ''

      if (aValue < bValue) return sortDir === 'asc' ? -1 : 1
      if (aValue > bValue) return sortDir === 'asc' ? 1 : -1
      return 0
    })
  }, [migrations, sortKey, sortDir])

  const totalMigrations = migrationStats?.total ?? 0
  const totalPages = Math.ceil(totalMigrations / pageSize)
  const currentMigrations = useMemo(() => {
    // Server-side pagination: display the fetched page as-is after client-side sorting
    return sortedMigrations
  }, [sortedMigrations])

  // Handlers
  const handleSort = (key: keyof Migration) => {
    if (sortKey === key) {
      if (sortDir === 'asc') setSortDir('desc')
      else if (sortDir === 'desc') {
        setSortKey(null)
        setSortDir(null)
      }
    } else {
      setSortKey(key)
      setSortDir('asc')
    }
    setCurrentPage(1) // Reset to first page on new sort
  }

  const getSortIcon = (key: keyof Migration) => {
    if (sortKey !== key) return <ChevronsUpDown size={14} className="ms-1 text-muted opacity-50" />
    return sortDir === 'asc' ? (
      <ChevronUp size={14} className="ms-1 text-primary" />
    ) : (
      <ChevronDown size={14} className="ms-1 text-primary" />
    )
  }

  // Stats Calculations
  const inProgress = (migrationStats?.pending ?? 0) + (migrationStats?.running ?? 0)
  const getPercent = (val?: number) =>
    migrationStats?.total ? ((val ?? 0) / migrationStats.total) * 100 : 0

  return (
    <>
      <CRow className="mb-4">
        <CCol>
          <h2 className="h4 fw-bold">Migration Overview</h2>
        </CCol>
      </CRow>

      {/* Stats Widgets */}
      <CRow>
        <CCol sm={6} xl={3}>
          <CWidgetStatsB
            className="mb-4 shadow-sm"
            progress={{ color: 'primary', value: 100 }}
            title="Total Migrations"
            value={getDisplayValue(migrationStats?.total)}
          />
        </CCol>
        <CCol sm={6} xl={3}>
          <CWidgetStatsB
            className="mb-4 shadow-sm"
            progress={{ color: 'warning', value: getPercent(inProgress) }}
            title="In Progress"
            value={getDisplayValue(inProgress)}
          />
        </CCol>
        <CCol sm={6} xl={3}>
          <CWidgetStatsB
            className="mb-4 shadow-sm"
            progress={{ color: 'success', value: getPercent(migrationStats?.success) }}
            title="Successful"
            value={getDisplayValue(migrationStats?.success)}
          />
        </CCol>
        <CCol sm={6} xl={3}>
          <CWidgetStatsB
            className="mb-4 shadow-sm"
            progress={{ color: 'danger', value: getPercent(migrationStats?.failed) }}
            title="Failed"
            value={getDisplayValue(migrationStats?.failed)}
          />
        </CCol>
      </CRow>

      {/* Main Table Container */}
      <div>
        {statsError && (
          <div className="alert alert-danger mb-4" role="alert">
            {statsError}
          </div>
        )}
        {migrationsError && (
          <div className="alert alert-danger mb-4" role="alert">
            {migrationsError}
          </div>
        )}

        {sortKey && sortDir && totalPages > 1 && (
          <div className="alert alert-info mb-3 py-2 small" role="status">
            <strong>Note:</strong> Sorting applies to the current page only ({pageSize} records).
            Results are not sorted across all {totalMigrations} migrations.
          </div>
        )}

        <CTable align="middle" responsive hover striped className="mb-0">
          <CTableHead>
            <CTableRow>
              {renderSortableHeader('id', 'ID', { width: '10%' })}
              {renderSortableHeader('docId', 'Doc ID')}
              {renderSortableHeader('status', 'Status')}
              {renderSortableHeader('createdAt', 'Created At')}
              {renderSortableHeader('updatedAt', 'Updated At')}
              <CTableHeaderCell className="text-center">Action</CTableHeaderCell>
            </CTableRow>
          </CTableHead>
          <CTableBody>
            {isLoading ? (
              <CTableRow>
                <CTableDataCell colSpan={6} className="text-center py-5">
                  <CSpinner color="primary" size="sm" className="me-2" />
                  Loading migration records...
                </CTableDataCell>
              </CTableRow>
            ) : !migrationsError ? (
              getTableRows(currentMigrations)
            ) : null}
          </CTableBody>
        </CTable>

        {/* Pagination Footer */}
        {!isLoading && totalPages > 1 && (
          <div className="d-flex justify-content-between align-items-center mt-4">
            <div className="small text-muted">
              Showing <strong>{(currentPage - 1) * pageSize + 1}</strong> to{' '}
              <strong>{Math.min(currentPage * pageSize, totalMigrations)}</strong> of{' '}
              <strong>{totalMigrations}</strong> migrations
            </div>
            <CPagination className="mb-0">
              <CPaginationItem
                disabled={currentPage === 1}
                onClick={() => setCurrentPage(currentPage - 1)}
                style={{ cursor: currentPage === 1 ? 'default' : 'pointer' }}
              >
                Previous
              </CPaginationItem>

              {computePaginationPages(totalPages, currentPage, 7).map((page) => {
                if (typeof page === 'string') {
                  // Render ellipsis as non-clickable
                  return (
                    <CPaginationItem key={page} disabled>
                      ...
                    </CPaginationItem>
                  )
                }
                return (
                  <CPaginationItem
                    key={page}
                    active={currentPage === page}
                    onClick={() => setCurrentPage(page)}
                    style={{ cursor: 'pointer' }}
                  >
                    {page}
                  </CPaginationItem>
                )
              })}

              <CPaginationItem
                disabled={currentPage === totalPages}
                onClick={() => setCurrentPage(currentPage + 1)}
                style={{ cursor: currentPage === totalPages ? 'default' : 'pointer' }}
              >
                Next
              </CPaginationItem>
            </CPagination>
          </div>
        )}
      </div>
    </>
  )
}

export default Dashboard