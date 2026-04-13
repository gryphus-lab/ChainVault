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
import { getMigrationStats } from '../../lib/api'
import { MigrationStats } from '../../types'

const Dashboard = () => {
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

    fetchStats()
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
          {/* TODO: Populate with actual data from getMigrations */}
          <CTableRow>
            <CTableDataCell colSpan={7} className="text-center text-muted py-4">
              No documents available
            </CTableDataCell>
          </CTableRow>
        </CTableBody>
      </CTable>
    </>
  )
}

export default Dashboard