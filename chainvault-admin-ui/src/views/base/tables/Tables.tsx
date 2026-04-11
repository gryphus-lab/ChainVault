/*
 * Copyright (c) 2026. Gryphus Lab
 */
import React from 'react'
import {
  CCard,
  CCardBody,
  CCardHeader,
  CCol,
  CRow,
  CTable,
  CTableBody,
  CTableCaption,
  CTableDataCell,
  CTableHead,
  CTableHeaderCell,
  CTableRow,
} from '@coreui/react'
import { DocsComponents, DocsExample } from '../../../components'

/* -----------------------------
 * Types
 * ----------------------------- */
type TableCell = {
  value: React.ReactNode
  colSpan?: number
  active?: boolean
}

type TableRowData = {
  header?: React.ReactNode
  cells: TableCell[]
  color?: React.ComponentProps<typeof CTableRow>['color']
  active?: boolean
}

type TableConfig = {
  head?: TableRendererProps['head']
  rows: TableRendererProps['rows']
  caption?: TableRendererProps['caption']
  headColor?: TableRendererProps['headColor']
  props?: TableRendererProps['tableProps']
}

type TableSectionConfig = {
  title: string
  subtitle?: string
  description?: string
  href: string
  tables: TableConfig[]
}

/* -----------------------------
 * Shared Data
 * ----------------------------- */
const DEFAULT_HEAD = ['#', 'Class', 'Heading', 'Heading']

const DEFAULT_ROWS: TableRowData[] = [
  {
    header: 1,
    cells: [{ value: 'Mark' }, { value: 'Otto' }, { value: '@mdo' }],
  },
  {
    header: 2,
    cells: [{ value: 'Jacob' }, { value: 'Thornton' }, { value: '@fat' }],
  },
  {
    header: 3,
    cells: [{ value: 'Larry the Bird', colSpan: 2 }, { value: '@twitter' }],
  },
]

/* -----------------------------
 * Reusable Table Renderer
 * ----------------------------- */
type TableRendererProps = {
  head?: string[]
  rows: TableRowData[]
  caption?: string
  headColor?: React.ComponentProps<typeof CTableHead>['color']
  tableProps?: React.ComponentProps<typeof CTable>
}

const TableRenderer = ({
  head = DEFAULT_HEAD,
  rows,
  caption,
  headColor,
  tableProps,
}: TableRendererProps) => (
  <CTable {...tableProps}>
    {caption && <CTableCaption>{caption}</CTableCaption>}

    <CTableHead color={headColor}>
      <CTableRow>
        {head.map((h, i) => (
          <CTableHeaderCell key={`${h}-${i}`} scope="col">
            {h}
          </CTableHeaderCell>
        ))}
      </CTableRow>
    </CTableHead>

    <CTableBody>
      {rows.map((row, i) => (
        <CTableRow key={`${row}-${i}`} color={row.color} active={row.active}>
          {row.header !== undefined && (
            <CTableHeaderCell scope="row">{row.header}</CTableHeaderCell>
          )}

          {row.cells.map((cell, j) => (
            <CTableDataCell key={`${cell.value}-${j}`} colSpan={cell.colSpan} active={cell.active}>
              {cell.value}
            </CTableDataCell>
          ))}
        </CTableRow>
      ))}
    </CTableBody>
  </CTable>
)

/* -----------------------------
 * Section Renderer
 * ----------------------------- */
const TableSection = ({ title, subtitle, description, href, tables }: TableSectionConfig) => (
  <CCol xs={12}>
    <CCard className="mb-4">
      <CCardHeader>
        <strong>{title}</strong> {subtitle && <small>{subtitle}</small>}
      </CCardHeader>

      <CCardBody>
        {description && <p className="text-body-secondary small">{description}</p>}

        {tables.map((table, i) => (
          <DocsExample key={`${table}-${i}`} href={href}>
            <TableRenderer
              head={table.head}
              rows={table.rows}
              caption={table.caption}
              headColor={table.headColor}
              tableProps={table.props}
            />
          </DocsExample>
        ))}
      </CCardBody>
    </CCard>
  </CCol>
)

/* -----------------------------
 * Config (THIS replaces duplication)
 * ----------------------------- */
const SECTIONS: TableSectionConfig[] = [
  {
    title: 'React Table',
    subtitle: 'Basic example',
    href: 'components/table',
    description: 'Using the most basic table CoreUI, here’s how tables look.',
    tables: [
      {
        rows: DEFAULT_ROWS,
      },
    ],
  },
  {
    title: 'React Table',
    subtitle: 'Striped rows',
    href: 'components/table#striped-rows',
    tables: [
      { props: { striped: true }, rows: DEFAULT_ROWS },
      { props: { striped: true, color: 'dark' }, rows: DEFAULT_ROWS },
      { props: { striped: true, color: 'success' }, rows: DEFAULT_ROWS },
    ],
  },
  {
    title: 'React Table',
    subtitle: 'Hoverable rows',
    href: 'components/table#hoverable-rows',
    tables: [
      { props: { hover: true }, rows: DEFAULT_ROWS },
      { props: { hover: true, color: 'dark' }, rows: DEFAULT_ROWS },
      { props: { hover: true, striped: true }, rows: DEFAULT_ROWS },
    ],
  },
  {
    title: 'React Table',
    subtitle: 'Bordered',
    href: 'components/table#bordered-tables',
    tables: [
      { props: { bordered: true }, rows: DEFAULT_ROWS },
      {
        props: { bordered: true, borderColor: 'primary' },
        rows: DEFAULT_ROWS,
      },
    ],
  },
  {
    title: 'React Table',
    subtitle: 'Borderless',
    href: 'components/table#tables-without-borders',
    tables: [
      { props: { borderless: true }, rows: DEFAULT_ROWS },
      { props: { borderless: true, color: 'dark' }, rows: DEFAULT_ROWS },
    ],
  },
  {
    title: 'React Table',
    subtitle: 'Small',
    href: 'components/table#small-tables',
    tables: [{ props: { small: true }, rows: DEFAULT_ROWS }],
  },
  {
    title: 'React Table',
    subtitle: 'Captions',
    href: 'components/table#captions',
    tables: [
      {
        caption: 'List of users',
        rows: DEFAULT_ROWS,
      },
      {
        caption: 'List of users',
        props: { caption: 'top' },
        rows: DEFAULT_ROWS,
      },
    ],
  },
]

/* -----------------------------
 * Main Component
 * ----------------------------- */
const Tables: React.FC = () => {
  return (
    <CRow>
      <CCol xs={12}>
        <DocsComponents href="components/table/" />
      </CCol>

      {SECTIONS.map((section, index) => (
        <TableSection
          key={`${section.title}-${index}`}
          title={section.title}
          subtitle={section.subtitle}
          description={section.description}
          href={section.href}
          tables={section.tables}
        />
      ))}
    </CRow>
  )
}

export default Tables
