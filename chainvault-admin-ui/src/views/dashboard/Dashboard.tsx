/*
 * Copyright (c) 2026. Gryphus Lab
 */
import { useEffect, useState } from 'react'
import {
  CCol,
  CRow,
  CTable,
  CTableBody,
  CTableDataCell,
  CTableHead,
  CTableHeaderCell,
  CTableRow,
  CWidgetStatsB,
} from '@coreui/react'
import { getMigrations, getMigrationStats } from '../../lib/api'
import { Migration, MigrationStats } from '../../types'
import { safeFormat } from '../../lib/utils'
import { Link } from 'react-router-dom'

const Dashboard = () => {
  const [migrations, setMigrations] = useState<Migration[] | null>(null)
  const [migrationStats, setMigrationStats] = useState<MigrationStats | null>(null)
  const [isLoading, setIsLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    const fetchStats = async () => {
      try {
        setIsLoading(true)
        setError(null)
        const stats = await getMigrationStats()
        setMigrationStats(stats)
      } catch (err) {
        console.error('Failed to fetch migration stats:', err)
        setError('Failed to load statistics')
      } finally {
        setIsLoading(false)
      }
    }

    const fetchMigrations = async () => {
      try {
        const data = await getMigrations()
        setMigrations(data)
      } catch (err) {
        console.error('Failed to fetch migrations:', err)
        setError('Failed to load migrations')
      }
    }

    fetchStats()
    fetchMigrations()
  }, [])

  const getDisplayValue = (value: number | undefined) => {
    if (isLoading) return '—'
    if (error) return 'Unavailable'
    return value?.toString() ?? '0'
  }

  const inProgress = (migrationStats?.pending ?? 0) + (migrationStats?.running ?? 0)

  return (
    <>
      <CRow>
        <CCol xs={6}>
          <CWidgetStatsB
            className="mb-3"
            color="primary"
            title="Total"
            value={getDisplayValue(migrationStats?.total)}
          />
        </CCol>
        <CCol xs={6}>
          <CWidgetStatsB
            className="mb-3"
            color="secondary"
            title="In Progress"
            value={getDisplayValue(inProgress)}
          />
        </CCol>
        <CCol xs={6}>
          <CWidgetStatsB
            className="mb-3"
            color="success"
            title="Success"
            value={getDisplayValue(migrationStats?.success)}
          />
        </CCol>
        <CCol xs={6}>
          <CWidgetStatsB
            className="mb-3"
            color="danger"
            title="Error"
            value={getDisplayValue(migrationStats?.failed)}
          />
        </CCol>
      </CRow>
      <CTable striped>
        <CTableHead>
          <CTableRow>
            <CTableHeaderCell scope="col">#</CTableHeaderCell>
            <CTableHeaderCell scope="col">DocId</CTableHeaderCell>
            <CTableHeaderCell scope="col">Title</CTableHeaderCell>
            <CTableHeaderCell scope="col">Status</CTableHeaderCell>
            <CTableHeaderCell scope="col">Created At</CTableHeaderCell>
            <CTableHeaderCell scope="col">Updated At</CTableHeaderCell>
            <CTableHeaderCell scope="col">View Details</CTableHeaderCell>
          </CTableRow>
        </CTableHead>
        <CTableBody>
          {migrations === null && (
            <CTableRow>
              <CTableDataCell colSpan={7} className="text-center text-muted">
                Loading...
              </CTableDataCell>
            </CTableRow>
          )}
          {migrations !== null && migrations.length === 0 && (
            <CTableRow>
              <CTableDataCell colSpan={7} className="text-center text-muted">
                No documents available
              </CTableDataCell>
            </CTableRow>
          )}
          {migrations?.map((migration, index) => (
            <CTableRow key={migration.id}>
              <CTableDataCell>{index + 1}</CTableDataCell>
              <CTableDataCell>{migration.docId}</CTableDataCell>
              <CTableDataCell>{migration.title}</CTableDataCell>
              <CTableDataCell>{migration.status}</CTableDataCell>
              <CTableDataCell>{safeFormat(migration.createdAt)}</CTableDataCell>
              <CTableDataCell>{safeFormat(migration.updatedAt)}</CTableDataCell>
              <CTableDataCell>
                <Link
                  to={`/migration/${migration.id}`}
                  className="btn btn-link"
                  aria-label={`View details for migration ${migration.docId}`}
                >
                  View Details
                </Link>
              </CTableDataCell>
            </CTableRow>
          ))}
        </CTableBody>
      </CTable>
    </>
  )
}

export default Dashboard
