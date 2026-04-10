/*
 * Copyright (c) 2026. Gryphus Lab
 */
import React, { FC, useMemo } from 'react'
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
import { DocsExample } from '../../components'

import WidgetsBrand from './WidgetsBrand'
import WidgetsDropdown from './WidgetsDropdown'
import secureRandomInt from '../../lib/utils'

/* -------------------------------------------------------------------------- */
/*                                   TYPES                                    */
/* -------------------------------------------------------------------------- */

type Range = { min: number; max: number }

type ChartVariant = 'bar' | 'line'

interface MiniChartProps {
  color: string
  variant?: ChartVariant
}

/* -------------------------------------------------------------------------- */
/*                                  HELPERS                                   */
/* -------------------------------------------------------------------------- */

const DAYS = ['M', 'T', 'W', 'T', 'F', 'S', 'S']

const generateSeries = ({ min, max }: Range, length = 15): number[] =>
  Array.from({ length }, () => secureRandomInt(max - min + 1) + min)

const baseChartOptions = {
  maintainAspectRatio: false,
  plugins: { legend: { display: false } },
  scales: {
    x: { display: false },
    y: { display: false },
  },
}

const lineExtras = {
  elements: {
    line: { tension: 0.4 },
    point: { radius: 0 },
  },
}

/* -------------------------------------------------------------------------- */
/*                               MINI CHART WIDGET                            */
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
          data: generateSeries({ min: 40, max: 100 }, DAYS.length * 3),
        },
      ],
    }),
    [color, variant],
  )

  const style = { height: '40px', width: '80px' }

  return variant === 'bar' ? (
    <CChartBar className="mx-auto" style={style} data={data} options={baseChartOptions} />
  ) : (
    <CChartLine
      className="mx-auto"
      style={style}
      data={data}
      options={{ ...baseChartOptions, ...lineExtras }}
    />
  )
}

/* -------------------------------------------------------------------------- */
/*                                MAIN COMPONENT                              */
/* -------------------------------------------------------------------------- */

const Widgets: FC = () => {
  return (
    <CCard className="mb-4">
      <CCardHeader>Widgets</CCardHeader>
      <CCardBody>
        {/* Dropdown */}
        <DocsExample href="components/widgets/#cwidgetstatsa">
          <WidgetsDropdown />
        </DocsExample>

        {/* Stats B */}
        <DocsExample href="components/widgets/#cwidgetstatsb">
          <CRow xs={{ gutter: 4 }}>
            {[
              { value: '89.9%', color: 'success' },
              { value: '12.124', color: 'info' },
              { value: '$98.111,00', color: 'warning' },
              { value: '2 TB', color: 'primary' },
            ].map((item, i) => (
              <CCol key={`${item.value}-${i}`} xs={12} sm={6} xl={4} xxl={3}>
                <CWidgetStatsB
                  value={item.value}
                  title="Widget title"
                  text="Lorem ipsum dolor sit amet enim."
                  progress={{ color: item.color, value: 89.9 }}
                />
              </CCol>
            ))}
          </CRow>
        </DocsExample>

        {/* Stats B Inverse */}
        <DocsExample href="components/widgets/#cwidgetstatsb">
          <CRow xs={{ gutter: 4 }}>
            {[
              { value: '89.9%', color: 'success' },
              { value: '12.124', color: 'info' },
              { value: '$98.111,00', color: 'warning' },
              { value: '2 TB', color: 'primary' },
            ].map((item, i) => (
              <CCol key={`${item.value}-${i}`} xs={12} sm={6} xl={4} xxl={3}>
                <CWidgetStatsB
                  inverse
                  color={item.color}
                  value={item.value}
                  title="Widget title"
                  text="Lorem ipsum dolor sit amet enim."
                  progress={{ value: 89.9 }}
                />
              </CCol>
            ))}
          </CRow>
        </DocsExample>

        {/* Stats E (Charts) */}
        <DocsExample href="components/widgets/#cwidgetstatse">
          <CRow xs={{ gutter: 4 }}>
            {(() => {
              const statsEItems: Array<{ color: string; variant: ChartVariant }> = [
                { color: '--cui-danger', variant: 'bar' },
                { color: '--cui-primary', variant: 'bar' },
                { color: '--cui-success', variant: 'bar' },
                { color: '--cui-danger', variant: 'line' },
                { color: '--cui-success', variant: 'line' },
                { color: '--cui-info', variant: 'line' },
              ]
              return statsEItems.map((item, i) => (
                <CCol key={`${item.variant}-${i}`} sm={4} md={3} xl={2}>
                  <CWidgetStatsE
                    chart={<MiniChart color={item.color} variant={item.variant} />}
                    title="title"
                    value="1,123"
                  />
                </CCol>
              ))
            })()}
          </CRow>
        </DocsExample>

        {/* Stats F */}
        <DocsExample href="components/widgets/#cwidgetstatsf">
          <CRow xs={{ gutter: 4 }}>
            {[
              { icon: cilSettings, color: 'primary' },
              { icon: cilUser, color: 'info' },
              { icon: cilMoon, color: 'warning' },
              { icon: cilBell, color: 'danger' },
            ].map((item, i) => (
              <CCol key={`${item.color}-${i}`} xs={12} sm={6} xl={4} xxl={3}>
                <CWidgetStatsF
                  icon={<CIcon width={24} icon={item.icon} />}
                  title="income"
                  value="$1.999,50"
                  color={item.color}
                />
              </CCol>
            ))}
          </CRow>
        </DocsExample>

        {/* Stats F with footer */}
        <DocsExample href="components/widgets/#cwidgetstatsf">
          <CRow xs={{ gutter: 4 }}>
            {[
              { icon: cilSettings, color: 'primary' },
              { icon: cilLaptop, color: 'info' },
              { icon: cilMoon, color: 'warning' },
              { icon: cilBell, color: 'danger' },
            ].map((item, i) => (
              <CCol key={`${item.color}-${i}`} xs={12} sm={6} xl={4} xxl={3}>
                <CWidgetStatsF
                  icon={<CIcon width={24} icon={item.icon} />}
                  title="income"
                  value="$1.999,50"
                  color={item.color}
                  footer={
                    <CLink
                      className="font-weight-bold font-xs text-body-secondary"
                      href="https://coreui.io/"
                      target="_blank"
                      rel="noopener noreferrer"
                    >
                      View more
                      <CIcon icon={cilArrowRight} className="float-end" width={16} />
                    </CLink>
                  }
                />
              </CCol>
            ))}
          </CRow>
        </DocsExample>

        {/* Brand */}
        <DocsExample href="components/widgets/#cwidgetstatsd">
          <WidgetsBrand />
        </DocsExample>

        <DocsExample href="components/widgets/#cwidgetstatsd">
          <WidgetsBrand withCharts />
        </DocsExample>

        {/* Stats C Group */}
        <DocsExample href="components/widgets/#cwidgetstatsc">
          <CCardGroup className="mb-4">
            {[
              { icon: cilPeople, value: '87.500', title: 'Visitors', color: 'info' },
              { icon: cilUserFollow, value: '385', title: 'New Clients', color: 'success' },
              { icon: cilBasket, value: '1238', title: 'Products sold', color: 'warning' },
              { icon: cilChartPie, value: '28%', title: 'Returning Visitors', color: 'primary' },
              { icon: cilSpeedometer, value: '5:34:11', title: 'Avg. Time', color: 'danger' },
            ].map((item, i) => (
              <CWidgetStatsC
                key={`${item.value}-${i}`}
                icon={<CIcon icon={item.icon} height={36} />}
                value={item.value}
                title={item.title}
                progress={{ color: item.color, value: 75 }}
              />
            ))}
          </CCardGroup>
        </DocsExample>

        {/* Stats C Grid */}
        <DocsExample href="components/widgets/#cwidgetstatsc">
          <CRow xs={{ gutter: 4 }}>
            {[
              { icon: cilPeople, value: '87.500', title: 'Visitors', color: 'info' },
              { icon: cilUserFollow, value: '385', title: 'New Clients', color: 'success' },
              { icon: cilBasket, value: '1238', title: 'Products sold', color: 'warning' },
              { icon: cilChartPie, value: '28%', title: 'Returning Visitors', color: 'primary' },
              { icon: cilSpeedometer, value: '5:34:11', title: 'Avg. Time', color: 'danger' },
              { icon: cilSpeech, value: '972', title: 'Comments', color: 'info' },
            ].map((item, i) => (
              <CCol key={`${item.value}-${i}`} xs={6} lg={4} xxl={2}>
                <CWidgetStatsC
                  icon={<CIcon icon={item.icon} height={36} />}
                  value={item.value}
                  title={item.title}
                  progress={{ color: item.color, value: 75 }}
                />
              </CCol>
            ))}
          </CRow>
        </DocsExample>
      </CCardBody>
    </CCard>
  )
}

export default Widgets