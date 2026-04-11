/*
 * Copyright (c) 2026. Gryphus Lab
 */
import React, { ComponentProps, useState } from 'react'
import {
  CButton,
  CCol,
  CRow,
  CModal,
  CModalHeader,
  CModalTitle,
  CModalBody,
  CModalFooter,
} from '@coreui/react'
import { DocsComponents, DocsExample } from '../../../components'

// =========================
// 🔁 TYPE DEFINITIONS
// =========================

interface AppModalProps extends Omit<ComponentProps<typeof CModal>, 'visible' | 'onClose'> {
  title: string
  trigger: React.ReactNode
  children: React.ReactNode
}

// =========================
// 🔁 REUSABLE COMPONENTS
// =========================

const AppModal = ({ title, trigger, children, ...props }: AppModalProps) => {
  const [visible, setVisible] = useState(false)

  return (
    <>
      <CButton onClick={() => setVisible(true)}>{trigger}</CButton>

      <CModal visible={visible} onClose={() => setVisible(false)} {...props}>
        <CModalHeader>
          <CModalTitle>{title}</CModalTitle>
        </CModalHeader>

        <CModalBody>{children}</CModalBody>

        <CModalFooter>
          <CButton color="secondary" onClick={() => setVisible(false)}>
            Close
          </CButton>
          <CButton color="primary">Save</CButton>
        </CModalFooter>
      </CModal>
    </>
  )
}

// =========================
// 🚀 MAIN COMPONENT
// =========================

const Modals = () => {
  return (
    <CRow>
      <CCol xs={12}>
        <DocsComponents href="components/modal/" />

        <DocsExample href="components/modal/">
          <AppModal title="Modal title" trigger="Launch demo modal">
            <p>
              Woohoo, you&apos;re reading this text in a modal! This demonstrates the basic modal
              functionality.
            </p>
          </AppModal>
        </DocsExample>

        <DocsExample href="components/modal/#vertically-centered">
          <AppModal title="Vertically centered modal" trigger="Vertically centered" alignment="center">
            <p>This modal is vertically centered on the page for better visual focus.</p>
          </AppModal>
        </DocsExample>

        <DocsExample href="components/modal/#scrollable">
          <AppModal title="Scrollable modal" trigger="Scrollable modal" scrollable>
            <p>
              This modal has a scrollable body when the content exceeds the viewport height. You can
              add as much content as needed and the modal body will scroll independently.
            </p>
            <p>
              Lorem ipsum dolor sit amet, consectetur adipiscing elit. Sed do eiusmod tempor
              incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud
              exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat.
            </p>
            <p>
              Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat
              nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui
              officia deserunt mollit anim id est laborum.
            </p>
          </AppModal>
        </DocsExample>

        <DocsExample href="components/modal/#fullscreen-modal">
          <AppModal title="Fullscreen modal" trigger="Fullscreen modal" fullscreen>
            <p>
              This modal takes up the entire viewport, providing maximum space for content. Great for
              complex forms or detailed information.
            </p>
          </AppModal>
        </DocsExample>
      </CCol>
    </CRow>
  )
}

export default Modals