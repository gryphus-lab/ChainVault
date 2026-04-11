/*
 * Copyright (c) 2026. Gryphus Lab
 */
import React, { FC } from 'react'
import {
  CCard,
  CCardBody,
  CCardHeader,
  CCol,
  CRow,
  CTab,
  CTabContent,
  CTabList,
  CTabPanel,
  CTabs,
} from '@coreui/react'
import { DocsComponents, DocsExample } from '../../../components'

/* =========================
 * Types
 * ========================= */

interface TabItem<K extends string | number> {
  key: K
  label: string
  content: string
  disabled?: boolean
}

interface TabsExampleProps<K extends string | number> {
  tabs: TabItem<K>[]
  activeKey: K
  variant?: 'tabs' | 'pills' | 'underline' | 'underline-border'
  panelClassName?: string
  exampleHref: string
}

/* =========================
 * Reusable Component
 * ========================= */

const TabsExample = <K extends string | number>({
  tabs,
  activeKey,
  variant,
  panelClassName = 'p-3',
  exampleHref,
}: TabsExampleProps<K>) => {
  return (
    <DocsExample href={exampleHref}>
      <CTabs activeItemKey={activeKey}>
        <CTabList {...(variant ? { variant } : {})}>
          {tabs.map(({ key, label, disabled }) => (
            <CTab
              key={key}
              id={`${key}-tab`}
              itemKey={key}
              disabled={disabled}
              aria-controls={`${key}-panel`}
            >
              {label}
            </CTab>
          ))}
        </CTabList>

        <CTabContent>
          {tabs.map(({ key, content }) => (
            <CTabPanel
              key={key}
              id={`${key}-panel`}
              itemKey={key}
              className={panelClassName}
              aria-labelledby={`${key}-tab`}
            >
              {content}
            </CTabPanel>
          ))}
        </CTabContent>
      </CTabs>
    </DocsExample>
  )
}

/* =========================
 * Data
 * ========================= */

const stringTabs: TabItem<string>[] = [
  { key: 'home', label: 'Home', content: 'Home tab content' },
  { key: 'profile', label: 'Profile', content: 'Profile tab content' },
  { key: 'contact', label: 'Contact', content: 'Contact tab content' },
  { key: 'disabled', label: 'Disabled', content: 'Disabled tab content', disabled: true },
]

const numberTabs: TabItem<number>[] = [
  { key: 1, label: 'Home', content: 'Home tab content' },
  { key: 2, label: 'Profile', content: 'Profile tab content' },
  { key: 3, label: 'Contact', content: 'Contact tab content' },
  { key: 4, label: 'Disabled', content: 'Disabled tab content', disabled: true },
]

/* =========================
 * Main Component
 * ========================= */

const Navs: FC = () => {
  return (
    <CRow>
      <CCol xs={12}>
        <DocsComponents href="components/tabs/" />

        {/* Tabs */}
        <CCard className="mb-4">
          <CCardHeader>
            <strong>React Tabs</strong>
          </CCardHeader>
          <CCardBody>
            <p className="text-body-secondary small">
              Basic tabs using <code>variant="tabs"</code>.
            </p>
            <TabsExample
              tabs={stringTabs}
              activeKey="profile"
              variant="tabs"
              exampleHref="components/tabs/#example"
            />
          </CCardBody>
        </CCard>

        {/* Unstyled */}
        <CCard className="mb-4">
          <CCardHeader>
            <strong>React Tabs</strong> <small>Unstyled</small>
          </CCardHeader>
          <CCardBody>
            <p className="text-body-secondary small">Default styling without specifying variant.</p>
            <TabsExample
              tabs={stringTabs}
              activeKey="profile"
              exampleHref="components/tabs/#unstyled"
            />
          </CCardBody>
        </CCard>

        {/* Pills */}
        <CCard className="mb-4">
          <CCardHeader>
            <strong>React Tabs</strong> <small>Pills</small>
          </CCardHeader>
          <CCardBody>
            <TabsExample
              tabs={numberTabs}
              activeKey={2}
              variant="pills"
              exampleHref="components/tabs/#pills"
            />
          </CCardBody>
        </CCard>

        {/* Underline */}
        <CCard className="mb-4">
          <CCardHeader>
            <strong>React Tabs</strong> <small>Underline</small>
          </CCardHeader>
          <CCardBody>
            <TabsExample
              tabs={numberTabs}
              activeKey={2}
              variant="underline"
              panelClassName="py-3"
              exampleHref="components/tabs/#underline"
            />
          </CCardBody>
        </CCard>

        {/* Underline Border */}
        <CCard className="mb-4">
          <CCardHeader>
            <strong>React Tabs</strong> <small>Underline border</small>
          </CCardHeader>
          <CCardBody>
            <TabsExample
              tabs={numberTabs}
              activeKey={2}
              variant="underline-border"
              panelClassName="py-3"
              exampleHref="components/tabs/#underline-border"
            />
          </CCardBody>
        </CCard>
      </CCol>
    </CRow>
  )
}

export default Navs
