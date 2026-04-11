/*
 * Copyright (c) 2026. Gryphus Lab
 */
import React, { FC, useMemo, ReactNode } from 'react'
import {
  CCard,
  CCardBody,
  CCardGroup,
  CCardHeader,
  CCol,
  CLink,
  CRow,
  CWidgetStatsB,
  CWidgetStatsC,
  CWidgetStatsE,
  CWidgetStatsF,
} from '@coreui/react'
import { getStyle } from '@coreui/utils'
import CIcon from '@coreui/icons-react'
import {
  cilArrowRight,
  cilBasket,
  cilBell,
  cilChartPie,
  cilLaptop,
  cilMoon,
  cilPeople,
  cilSettings,
  cilSpeech,
  cilSpeedometer,
  cilUser,
  cilUserFollow,
} from '@coreui/icons'
import { CChartBar, CChartLine } from '@coreui/react-chartjs'
import type { ChartOptions } from 'chart.js'
import { DocsExample } from '../../components'

import WidgetsBrand from './WidgetsBrand'
import WidgetsDropdown from './WidgetsDropdown'
import secureRandomInt from '../../lib/utils'

/* -------------------------------------------------------------------------- */
/* TYPES                                    */
/* -------------------------------------------------------------------------- */

type ChartVariant = 'bar' | 'line'

interface MiniChartProps {
  color: string
  variant?: ChartVariant
}

/* -------------------------------------------------------------------------- */
/* HELPERS                                   */
/* -------------------------------------------------------------------------- */

const DAYS = ['M', 'T', 'W', 'T', 'F', 'S', 'S']

const generateSeries = (min: number, max: number, length = 15): number[] =>
  Array.from({ length }, () => secureRandomInt(max - min + 1) + min)

// Shared base options for both 'bar' and 'line' chart variants used in MiniChart.
// `satisfies` validates the shape against ChartOptions<'line'> at compile time
// while keeping the inferred literal type for reuse across chart variants.
const baseChartOptions = {
  maintainAspectRatio: false,
  plugins: { legend: { display: false } },
  scales: { x: { display: false }, y: { display: false } },
} satisfies ChartOptions<'line'>

const lineExtras = {
  elements: { line: { tension: 0.4 }, point: { radius: 0 } },
} as const

const WidgetGrid = ({
  children,
  href,
  gutter = 4,
}: {
  children: ReactNode
  href: string
  gutter?: number
}) => (
  <DocsExample href={href}>
    <CRow xs={{ gutter }}>{children}</CRow>
  </DocsExample>
)

const WidgetFooter = () => (
  <CLink
    className="font-weight-bold font-xs text-body-secondary"
    href="https://coreui.io/"
    target="_blank"
    rel="noopener noreferrer"
  >
    View more
    <CIcon icon={cilArrowRight} className="float-end" width={16} />
  </CLink>
)

/* -------------------------------------------------------------------------- */
/* MINI CHART WIDGET                            */
/* -------------------------------------------------------------------------- */

const MiniChart: FC<MiniChartProps> = ({ color, variant = 'bar' }) => {
  const data = useMemo(
    () => ({
      labels: new Array(3).fill(DAYS).flat(),
      datasets: [
        {
          backgroundColor: variant === 'bar' ? getStyle(color) : 'transparent',
          borderColor: variant === 'line' ? getStyle(color) : 'transparent',
          borderWidth: variant === 'line' ? 2 : 1,
          data: generateSeries(40, 100, DAYS.length * 3),
        },
      ],
    }),
    [color, variant],
  )

  const style = { height: '40px', width: '80px' }

  if (variant === 'bar') {
    return <CChartBar className="mx-auto" style={style} data={data} options={baseChartOptions} />
  }
  return (
    <CChartLine
      className="mx-auto"
      style={style}
      data={data}
      options={{ ...baseChartOptions, ...lineExtras }}
    />
  )
}

/* -------------------------------------------------------------------------- */
/* MAIN COMPONENT                              */
/* -------------------------------------------------------------------------- */

const statsBData = [
  { value: '89.9%', color: 'success' as const },
  { value: '12.124', color: 'info' as const },
  { value: '$98.111,00', color: 'warning' as const },
  { value: '2 TB', color: 'primary' as const },
]

const statsFData = [
  { icon: cilSettings, color: 'primary' as const, title: 'settings' },
  { icon: cilUser, color: 'info' as const, title: 'users' },
  { icon: cilMoon, color: 'warning' as const, title: 'dark mode' },
  { icon: cilBell, color: 'danger' as const, title: 'alerts' },
]

const statsCData = [
  { icon: cilPeople, value: '87.500', title: 'Visitors', color: 'info' as const },
  { icon: cilUserFollow, value: '385', title: 'New Clients', color: 'success' as const },
  { icon: cilBasket, value: '1238', title: 'Products sold', color: 'warning' as const },
  { icon: cilChartPie, value: '28%', title: 'Returning Visitors', color: 'primary' as const },
  { icon: cilSpeedometer, value: '5:34:11', title: 'Avg. Time', color: 'danger' as const },
]

/**
 * Returns the appropriate icon for a widget, using cilLaptop for 'info' color when footer is present
 */
const getWidgetIcon = (hasFooter: boolean, color: string, defaultIcon: string | string[]) =>
  hasFooter && color === 'info' ? cilLaptop : defaultIcon

const Widgets: FC = () => {

  return (
    <CCard className="mb-4">
      <CCardHeader>Widgets</CCardHeader>
      <CCardBody>
        {/* Dropdown Widgets */}
        <DocsExample href="components/widgets/#cwidgetstatsa">
          <WidgetsDropdown />
        </DocsExample>

        {/* Stats B (Standard & Inverse) */}
        {([false, true] as const).map((isInverse) => (
          <WidgetGrid key={`stats-b-inverse-${isInverse}`} href="components/widgets/#cwidgetstatsb">
            {statsBData.map((item) => (
              <CCol key={item.value} xs={12} sm={6} xl={4} xxl={3}>
                <CWidgetStatsB
                  inverse={isInverse}
                  color={isInverse ? item.color : undefined}
                  value={item.value}
                  title="Widget title"
                  text="Lorem ipsum dolor sit amet enim."
                  progress={{ color: isInverse ? undefined : item.color, value: 89.9 }}
                />
              </CCol>
            ))}
          </WidgetGrid>
        ))}

        {/* Stats E (Mini Charts) */}
        <WidgetGrid href="components/widgets/#cwidgetstatse">
          {(
            [
              { color: '--cui-danger', variant: 'bar' },
              { color: '--cui-primary', variant: 'bar' },
              { color: '--cui-success', variant: 'bar' },
              { color: '--cui-danger', variant: 'line' },
              { color: '--cui-success', variant: 'line' },
              { color: '--cui-info', variant: 'line' },
            ] as const
          ).map((item) => (
            <CCol key={`${item.variant}-${item.color}`} sm={4} md={3} xl={2}>
              <CWidgetStatsE
                chart={<MiniChart color={item.color} variant={item.variant} />}
                title="title"
                value="1,123"
              />
            </CCol>
          ))}
        </WidgetGrid>

        {/* Stats F (Standard & Footer) */}
        {([false, true] as const).map((hasFooter) => (
          <WidgetGrid key={`stats-f-footer-${hasFooter}`} href="components/widgets/#cwidgetstatsf">
            {statsFData.map((item) => (
              <CCol key={item.color} xs={12} sm={6} xl={4} xxl={3}>
                <CWidgetStatsF
                  icon={
                    <CIcon width={24} icon={getWidgetIcon(hasFooter, item.color, item.icon)} />
                  }
                  title={item.title}
                  value="$1.999,50"
                  color={item.color}
                  footer={hasFooter ? <WidgetFooter /> : undefined}
                />
              </CCol>
            ))}
          </WidgetGrid>
        ))}

        {/* Brand Widgets */}
        <DocsExample href="components/widgets/#cwidgetstatsd">
          <WidgetsBrand />
        </DocsExample>
        <DocsExample href="components/widgets/#cwidgetstatsd">
          <WidgetsBrand withCharts />
        </DocsExample>

        {/* Stats C (Group) */}
        <DocsExample href="components/widgets/#cwidgetstatsc">
          <CCardGroup className="mb-4">
            {statsCData.map((item) => (
              <CWidgetStatsC
                key={item.title}
                icon={<CIcon icon={item.icon} height={36} />}
                value={item.value}
                title={item.title}
                progress={{ color: item.color, value: 75 }}
              />
            ))}
          </CCardGroup>
        </DocsExample>

        {/* Stats C (Grid) */}
        <WidgetGrid href="components/widgets/#cwidgetstatsc">
          {[
            ...statsCData,
            { icon: cilSpeech, value: '972', title: 'Comments', color: 'info' as const },
          ].map((item) => (
            <CCol key={item.title} xs={6} lg={4} xxl={2}>
              <CWidgetStatsC
                icon={<CIcon icon={item.icon} height={36} />}
                value={item.value}
                title={item.title}
                progress={{ color: item.color, value: 75 }}
              />
            </CCol>
          ))}
        </WidgetGrid>
      </CCardBody>
    </CCard>
  )
}

export default Widgets