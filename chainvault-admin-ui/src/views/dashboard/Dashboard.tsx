/*
 * Copyright (c) 2026. Gryphus Lab
 */
import { memo, useEffect, useMemo, useState } from 'react'
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
import { Migration, MigrationPage, MigrationStats } from '../../types'
import { safeFormat } from '../../lib/utils'

type SortDirection = 'asc' | 'desc' | null

/**
 * Selects the CSS class string for a migration status badge.
 *
 * @param m - The migration object whose `status` determines the badge color.
 * @returns The CSS class pair (background and text) corresponding to `m.status`: `bg-success-light text-success` for `SUCCESS`, `bg-danger-light text-danger` for `FAILED`, `bg-warning-light text-warning` for `PENDING`, `bg-info-light text-info` for `RUNNING`, and `bg-light text-dark` for any other status.
 */
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

/**
 * Produce table row elements for the migrations table.
 *
 * Renders a single centered empty-state row when `currentMigrations` is empty; otherwise returns a row per migration containing id, document id, status badge, formatted created/updated timestamps, and a "View Details" action.
 *
 * @param currentMigrations - List of migrations to render as table rows
 * @returns A JSX node: a single empty-state `CTableRow` when the list is empty, or an array of `CTableRow` elements for each migration
 */
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

/**
 * Convert a sort direction into the corresponding ARIA `aria-sort` value.
 *
 * @param sortDir - The sort direction: `'asc'`, `'desc'`, or `null` to indicate no sorting
 * @returns The ARIA sort order: `'none'` if `sortDir` is `null`, `'ascending'` for `'asc'`, or `'descending'` for `'desc'`
 */
function getSortOrder(sortDir: 'asc' | 'desc' | null) {
  if (sortDir === null) return 'none'
  return sortDir === 'asc' ? 'ascending' : 'descending'
}

/**
 * Produces an ordered list of page identifiers for pagination controls, including numeric pages and ellipsis markers where ranges are collapsed.
 *
 * @param totalPages - Total number of pages available
 * @param currentPage - Currently active page (1-based)
 * @param maxPagesToShow - Maximum number of numeric pages to display before inserting ellipses
 * @returns An array of page identifiers: page numbers and the strings `'ellipsis-left'` / `'ellipsis-right'` used to render collapsed ranges
 */
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

function getTableContent(migrationsError: string | null, currentMigrations: Migration[]) {
  return migrationsError ? null : getTableRows(currentMigrations)
}

interface SortableHeaderProps {
  sortKeyName: keyof Migration
  label: string
  style?: React.CSSProperties
  currentSortKey: keyof Migration | null
  sortDir: SortDirection
  onSort: (key: keyof Migration) => void
}

/**
 * Renders a sortable table header cell with appropriate aria-sort attributes and sort icons.
 *
 * @param sortKeyName - The migration property key this header controls
 * @param label - Display label for the header
 * @param style - Optional CSS styles for the header cell
 * @param currentSortKey - The currently active sort key
 * @param sortDir - The current sort direction
 * @param onSort - Callback to handle sort changes
 * @returns A memoized sortable header cell with click handler and sort indicator icon
 */
const SortableHeader = memo<SortableHeaderProps>(
  ({ sortKeyName, label, style, currentSortKey, sortDir, onSort }) => {
    const getSortIcon = () => {
      if (currentSortKey !== sortKeyName)
        return <ChevronsUpDown size={14} className="ms-1 text-muted opacity-50" />
      return sortDir === 'asc' ? (
        <ChevronUp size={14} className="ms-1 text-primary" />
      ) : (
        <ChevronDown size={14} className="ms-1 text-primary" />
      )
    }

    return (
      <CTableHeaderCell
        style={style}
        aria-sort={currentSortKey === sortKeyName ? getSortOrder(sortDir) : 'none'}
      >
        <button
          type="button"
          onClick={() => onSort(sortKeyName)}
          style={{ cursor: 'pointer', background: 'none', border: 'none', padding: 0 }}
          className="user-select-none"
          aria-label={`Sort by ${label}`}
        >
          {label} {getSortIcon()}
        </button>
      </CTableHeaderCell>
    )
  },
)
SortableHeader.displayName = 'SortableHeader'

const Dashboard = () => {
  // Data State
  const [migrations, setMigrations] = useState<MigrationPage | null>(null)
  const [migrationStats, setMigrationStats] = useState<MigrationStats | null>(null)
  const [isStatsLoading, setIsStatsLoading] = useState(true)
  const [isMigrationsLoading, setIsMigrationsLoading] = useState(true)
  const [statsError, setStatsError] = useState<string | null>(null)
  const [migrationsError, setMigrationsError] = useState<string | null>(null)

  // Pagination & Sort State
  const [currentPage, setCurrentPage] = useState(1)
  const [sortKey, setSortKey] = useState<keyof Migration | null>('createdAt')
  const [sortDir, setSortDir] = useState<SortDirection>('desc')
  const pageSize = 10

  // Effect 1: Fetch migration stats (independent of pagination/sorting)
  useEffect(() => {
    let isActive = true

    const fetchStats = async () => {
      if (isActive) {
        setIsStatsLoading(true)
        setStatsError(null)
      }

      try {
        const stats = await getMigrationStats()
        if (isActive) {
          setMigrationStats(stats)
        }
      } catch (error) {
        if (isActive) {
          console.error('Migration stats fetch error:', error)
          setStatsError('Failed to load migration statistics.')
        }
      } finally {
        if (isActive) {
          setIsStatsLoading(false)
        }
      }
    }

    fetchStats()

    return () => {
      isActive = false
    }
  }, [])

  // Effect 2: Fetch migrations (depends on pagination/sorting)
  useEffect(() => {
    let isActive = true

    const fetchMigrations = async () => {
      if (isActive) {
        setIsMigrationsLoading(true)
        setMigrationsError(null)
      }

      try {
        const data = await getMigrations({
          limit: pageSize,
          page: currentPage - 1,
          sortKey: sortKey ?? undefined,
          sortDir: sortDir ?? undefined,
        })
        if (isActive) {
          setMigrations(data)
        }
      } catch (error) {
        if (isActive) {
          console.error('Migrations fetch error:', error)
          setMigrationsError('Failed to load migration records.')
        }
      } finally {
        if (isActive) {
          setIsMigrationsLoading(false)
        }
      }
    }

    fetchMigrations()

    return () => {
      isActive = false
    }
  }, [currentPage, pageSize, sortKey, sortDir])

  const getDisplayValue = (value: number | undefined) => {
    if (isStatsLoading) return '—'
    if (statsError) return 'Unavailable'
    return value?.toString() ?? '0'
  }

  const sortedMigrations = useMemo(() => {
    // Server-side sorting: return migrations as-is from the API
    return migrations?.items ?? []
  }, [migrations])

  const totalMigrations = migrations?.total ?? migrationStats?.total ?? sortedMigrations.length
  const totalPages = Math.ceil(totalMigrations / pageSize)

  // Handlers
  const handleSort = (key: keyof Migration) => {
    if (sortKey === key) {
      // Toggle between asc and desc only
      setSortDir(sortDir === 'asc' ? 'desc' : 'asc')
    } else {
      setSortKey(key)
      setSortDir('asc')
    }
    setCurrentPage(1) // Reset to first page on new sort
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

        <CTable align="middle" responsive hover striped className="mb-0">
          <CTableHead>
            <CTableRow>
              <SortableHeader
                sortKeyName="id"
                label="ID"
                style={{ width: '10%' }}
                currentSortKey={sortKey}
                sortDir={sortDir}
                onSort={handleSort}
              />
              <SortableHeader
                sortKeyName="docId"
                label="Doc ID"
                currentSortKey={sortKey}
                sortDir={sortDir}
                onSort={handleSort}
              />
              <SortableHeader
                sortKeyName="status"
                label="Status"
                currentSortKey={sortKey}
                sortDir={sortDir}
                onSort={handleSort}
              />
              <SortableHeader
                sortKeyName="createdAt"
                label="Created At"
                currentSortKey={sortKey}
                sortDir={sortDir}
                onSort={handleSort}
              />
              <SortableHeader
                sortKeyName="updatedAt"
                label="Updated At"
                currentSortKey={sortKey}
                sortDir={sortDir}
                onSort={handleSort}
              />
              <CTableHeaderCell className="text-center">Action</CTableHeaderCell>
            </CTableRow>
          </CTableHead>
          <CTableBody>
            {isMigrationsLoading ? (
              <CTableRow>
                <CTableDataCell colSpan={6} className="text-center py-5">
                  <CSpinner color="primary" size="sm" className="me-2" />
                  Loading migration records...
                </CTableDataCell>
              </CTableRow>
            ) : (
              getTableContent(migrationsError, sortedMigrations)
            )}
          </CTableBody>
        </CTable>

        {/* Pagination Footer */}
        {!isMigrationsLoading && totalPages > 1 && (
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
