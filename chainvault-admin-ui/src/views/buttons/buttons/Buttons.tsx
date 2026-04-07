/*
 * Copyright (c) 2026. Gryphus Lab
 */
import React from 'react'
import { CButton, CCard, CCardBody, CCardHeader, CCol, CRow } from '@coreui/react'
import CIcon from '@coreui/icons-react'
import { cilBell } from '@coreui/icons'
import { DocsComponents, DocsExample } from '../../../components'

const ButtonCard = ({ title, subtitle, children, description }: any) => (
  <CCol xs={12}>
    <CCard className="mb-4">
      <CCardHeader>
        <strong>{title}</strong> {subtitle && <small>{subtitle}</small>}
      </CCardHeader>
      <CCardBody>
        {description && <p className="text-body-secondary small">{description}</p>}
        {children}
      </CCardBody>
    </CCard>
  </CCol>
)

const ButtonStateGrid = ({ variant, showIcon, href }: any) => {
  const states = ['normal', 'active', 'disabled']
  const colors = ['primary', 'secondary', 'success', 'danger', 'warning', 'info']

  return (
    <DocsExample href={href}>
      {states.map((state) => (
        <CRow className="align-items-center mb-3" key={state}>
          <CCol xs={12} xl={2} className="mb-3 mb-xl-0">
            {state.charAt(0).toUpperCase() + state.slice(1)}
          </CCol>
          <CCol xs>
            {colors.map((color) => (
              <CButton
                color={color}
                key={color}
                variant={variant}
                active={state === 'active'}
                disabled={state === 'disabled'}
              >
                {showIcon && <CIcon icon={cilBell} className="me-2" />}
                {color.charAt(0).toUpperCase() + color.slice(1)}
              </CButton>
            ))}
            {!variant && (
              <CButton color="link">
                {showIcon && <CIcon icon={cilBell} className="me-2" />}
                Link
              </CButton>
            )}
          </CCol>
        </CRow>
      ))}
    </DocsExample>
  )
}

const Buttons = () => {
  const baseColors = [
    'primary',
    'secondary',
    'success',
    'danger',
    'warning',
    'info',
    'light',
    'dark',
  ]

  return (
    <CRow>
      <CCol xs={12}>
        <DocsComponents href="components/buttons/" />
      </CCol>

      <ButtonCard
        title="React Button"
        description="CoreUI includes a bunch of predefined buttons components, each serving its own semantic purpose..."
      >
        <ButtonStateGrid href="components/buttons" />
      </ButtonCard>

      <ButtonCard
        title="React Button"
        subtitle="with icons"
        description={
          <>
            You can combine button with our <a href="https://coreui.io/icons/">CoreUI Icons</a>.
          </>
        }
      >
        <ButtonStateGrid href="components/buttons" showIcon />
      </ButtonCard>

      <ButtonCard title="React Button" subtitle="Button components">
        <p className="text-body-secondary small">
          The <code>&lt;CButton&gt;</code> component is designed for <code>&lt;button&gt;</code>,{' '}
          <code>&lt;a&gt;</code> or <code>&lt;input&gt;</code>.
        </p>
        <DocsExample href="components/buttons#button-components">
          <CButton as="a" color="primary" href="#" role="button">
            Link
          </CButton>
          <CButton type="submit" color="primary">
            Button
          </CButton>
          {(['button', 'submit', 'reset'] as const).map((type) => (
            <CButton
              key={type}
              as="input"
              type={type}
              color="primary"
              value={type.charAt(0).toUpperCase() + type.slice(1)}
            />
          ))}
        </DocsExample>
      </ButtonCard>

      <ButtonCard
        title="React Button"
        subtitle="outline"
        description="Set variant='outline' prop to remove all background colors."
      >
        <ButtonStateGrid href="components/buttons#outline-buttons" variant="outline" />
      </ButtonCard>

      <ButtonCard
        title="React Button"
        subtitle="ghost"
        description="Set variant='ghost' prop to remove all background colors."
      >
        <ButtonStateGrid href="components/buttons#ghost-buttons" variant="ghost" />
      </ButtonCard>

      <ButtonCard
        title="React Button"
        subtitle="Sizes"
        description="Add size='lg' or size='sm' for additional sizes."
      >
        {(['lg', 'sm'] as const).map((size) => (
          <DocsExample key={size} href="components/buttons#sizes">
            <CButton color="primary" size={size}>
              {size === 'lg' ? 'Large' : 'Small'} button
            </CButton>
            <CButton color="secondary" size={size}>
              {size === 'lg' ? 'Large' : 'Small'} button
            </CButton>
          </DocsExample>
        ))}
      </ButtonCard>

      <ButtonCard title="React Button" subtitle="Pill">
        <DocsExample href="components/buttons#pill-buttons">
          {baseColors.map((color) => (
            <CButton color={color} shape="rounded-pill" key={color}>
              {color.charAt(0).toUpperCase() + color.slice(1)}
            </CButton>
          ))}
        </DocsExample>
      </ButtonCard>

      <ButtonCard title="React Button" subtitle="Square">
        <DocsExample href="components/buttons#square">
          {baseColors.map((color) => (
            <CButton color={color} shape="rounded-0" key={color}>
              {color.charAt(0).toUpperCase() + color.slice(1)}
            </CButton>
          ))}
        </DocsExample>
      </ButtonCard>

      {/* Block Buttons and Disabled State sections can be handled similarly with ButtonCard */}
    </CRow>
  )
}

export default Buttons
