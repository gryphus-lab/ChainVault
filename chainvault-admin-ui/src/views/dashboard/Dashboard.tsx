/*
 * Copyright (c) 2026. Gryphus Lab
 */
import {
  CTable,
  CTableBody,
  CTableDataCell,
  CTableHead,
  CTableHeaderCell,
  CTableRow,
} from '@coreui/react'

import WidgetsDropdown from '../widgets/WidgetsDropdown'

const Dashboard = () => {
  return (
    <>
      <WidgetsDropdown className="mb-4" />
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
          {/* TODO: Populate with actual data */}
          <CTableRow>
            <CTableHeaderCell scope="row">1</CTableHeaderCell>
            <CTableDataCell>doc123</CTableDataCell>
            <CTableDataCell>Sample Document</CTableDataCell>
            <CTableDataCell>
              <span className="badge bg-success">SUCCESS</span>
            </CTableDataCell>
            <CTableDataCell>2024-06-01 10:00:00</CTableDataCell>
            <CTableDataCell>2024-06-01 10:05:00</CTableDataCell>
            <CTableDataCell>
              <button className="btn btn-primary btn-sm">View Details</button>
            </CTableDataCell>
          </CTableRow>
        </CTableBody>
      </CTable>
    </>
  )
}

export default Dashboard
