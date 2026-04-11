/*
 * Copyright (c) 2026. Gryphus Lab
 */
import React from 'react'
import {
  CButton,
  CButtonGroup,
  CCard,
  CCardBody,
  CCardHeader,
  CCol,
  CDropdown,
  CDropdownDivider,
  CDropdownItem,
  CDropdownMenu,
  CDropdownToggle,
  CRow,
} from '@coreui/react'
import { DocsComponents, DocsExample } from '../../../components'

// --- Helper Components ---

const DefaultMenu = ({ showDivider = true }: { showDivider?: boolean }) => (
  <CDropdownMenu>
    <CDropdownItem href="#">Action</CDropdownItem>
    <CDropdownItem href="#">Another action</CDropdownItem>
    <CDropdownItem href="#">Something else here</CDropdownItem>
    {showDivider && (
      <>
        <CDropdownDivider />
        <CDropdownItem href="#">Separated link</CDropdownItem>
      </>
    )}
  </CDropdownMenu>
)

const DropdownExample = ({ color = 'secondary', label, split = false, ...props }: any) => (
  <CDropdown variant="btn-group" {...props}>
    {split && <CButton color={color}>{label}</CButton>}
    <CDropdownToggle color={color} split={split}>
      {!split && label}
    </CDropdownToggle>
    <DefaultMenu />
  </CDropdown>
)

const DropdownCard = ({ title, subtitle, description, children }: any) => (
  <CCol xs={12}>
    <CCard className="mb-4">
      <CCardHeader>
        <strong>{title}</strong> <small>{subtitle}</small>
      </CCardHeader>
      <CCardBody>
        {description && <p className="text-body-secondary small">{description}</p>}
        {children}
      </CCardBody>
    </CCard>
  </CCol>
)

// --- Main Component ---

const Dropdowns = () => {
  const colors = ['primary', 'secondary', 'success', 'info', 'warning', 'danger'] as const

  return (
    <CRow>
      <CCol xs={12}>
        <DocsComponents href="components/dropdown/" />
      </CCol>

      <DropdownCard
        title="React Dropdown"
        subtitle="Single button"
        description="Here's how you can put them to work with either <button> elements:"
      >
        <DocsExample href="components/dropdown#single-button">
          <CDropdown>
            <CDropdownToggle color="secondary">Dropdown button</CDropdownToggle>
            <DefaultMenu showDivider={false} />
          </CDropdown>
        </DocsExample>
        <p className="text-body-secondary small">
          The best part is you can do this with any button variant, too:
        </p>
        <DocsExample href="components/dropdown#single-button">
          {colors.map((color) => (
            <DropdownExample key={color} color={color} label={color} />
          ))}
        </DocsExample>
      </DropdownCard>

      <DropdownCard
        title="React Dropdown"
        subtitle="Split button"
        description="Similarly, create split button dropdowns with virtually the same markup..."
      >
        <DocsExample href="components/dropdown#split-button">
          {colors.map((color) => (
            <DropdownExample key={color} color={color} label={color} split />
          ))}
        </DocsExample>
      </DropdownCard>

      <DropdownCard title="React Dropdown" subtitle="Sizing">
        {(['lg', 'sm'] as const).map((size) => (
          <DocsExample key={size} href="components/dropdown#sizing">
            <DropdownExample size={size} label={`${size === 'lg' ? 'Large' : 'Small'} button`} />
            <DropdownExample
              size={size}
              label={`${size === 'lg' ? 'Large' : 'Small'} split button`}
              split
            />
          </DocsExample>
        ))}
      </DropdownCard>

      <DropdownCard
        title="React Dropdown"
        subtitle="Dark dropdowns"
        description="Opt into darker dropdowns by setting dark property."
      >
        <DocsExample href="components/dropdown#dark-dropdowns">
          <CDropdown dark>
            <CDropdownToggle color="secondary">Dropdown button</CDropdownToggle>
            <DefaultMenu />
          </CDropdown>
        </DocsExample>
      </DropdownCard>

      {(['dropup', 'dropend'] as const).map((dir) => (
        <DropdownCard
          key={dir}
          title="React Dropdown"
          subtitle={dir.charAt(0).toUpperCase() + dir.slice(1)}
        >
          <DocsExample href={`components/dropdown#${dir}`}>
            <DropdownExample direction={dir} label="Dropdown" />
            <DropdownExample direction={dir} label="Small split button" split />
          </DocsExample>
        </DropdownCard>
      ))}

      <DropdownCard title="React Dropdown" subtitle="Dropleft">
        <DocsExample href="components/dropdown#dropleft">
          <CButtonGroup>
            <CDropdown variant="btn-group" direction="dropstart">
              <CDropdownToggle color="secondary" split />
              <DefaultMenu />
            </CDropdown>
            <CButton color="secondary">Small split button</CButton>
          </CButtonGroup>
        </DocsExample>
      </DropdownCard>
    </CRow>
  )
}

export default Dropdowns
