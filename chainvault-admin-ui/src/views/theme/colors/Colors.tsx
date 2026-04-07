/*
 * Copyright (c) 2026. Gryphus Lab
 */
import React, { useEffect, useState, useRef } from 'react'
import PropTypes from 'prop-types'
import classNames from 'classnames'
import { CRow, CCol, CCard, CCardHeader, CCardBody } from '@coreui/react'
import { rgbToHex } from '@coreui/utils'
import { DocsLink } from '../../../components'

const ThemeView = () => {
  const [color, setColor] = useState('rgb(255, 255, 255)')
  const ref = useRef<HTMLTableElement>(null)

  useEffect(() => {
    const table = ref.current
    const parent = table?.parentNode
    const el = parent?.firstChild
    if (!(el instanceof Element)) {
      return
    }
    const varColor = globalThis.getComputedStyle(el).getPropertyValue('background-color')
    setColor(varColor)
  }, [ref])

  return (
    <table className="table w-100" ref={ref}>
     <table className="table w-100" ref={ref}>
       <tbody>
         <tr>
           <td className="text-body-secondary">HEX:</td>
           <td className="font-weight-bold">{rgbToHex(color)}</td>
         </tr>
      <tbody>
        <tr>
          <td className="text-body-secondary">HEX:</td>
          <td className="font-weight-bold">{rgbToHex(color)}</td>
        </tr>
        <tr>
          <td className="text-body-secondary">RGB:</td>
          <td className="font-weight-bold">{color}</td>
        </tr>
      </tbody>
    </table>
  )
}

type ThemeColorProps = {
  className?: string
  children?: React.ReactNode
}

const ThemeColor = ({ className, children }: ThemeColorProps) => {
  const classes = classNames(className, 'theme-color w-75 rounded mb-3')
  return (
    <CCol xs={12} sm={6} md={4} xl={2} className="mb-4">
      <div className={classes} style={{ paddingTop: '75%' }}></div>
      {children}
      <ThemeView />
    </CCol>
  )
}

ThemeColor.propTypes = {
  children: PropTypes.node,
  className: PropTypes.string,
}

const Colors = () => {
  return (
    <CCard className="mb-4">
      <CCardHeader>
        Theme colors
        <DocsLink href="https://coreui.io/docs/utilities/colors/" />
      </CCardHeader>
      <CCardBody>
        <CRow>
          <ThemeColor className="bg-primary">
            <h6>Brand Primary Color</h6>
          </ThemeColor>
          <ThemeColor className="bg-secondary">
            <h6>Brand Secondary Color</h6>
          </ThemeColor>
          <ThemeColor className="bg-success">
            <h6>Brand Success Color</h6>
          </ThemeColor>
          <ThemeColor className="bg-danger">
            <h6>Brand Danger Color</h6>
          </ThemeColor>
          <ThemeColor className="bg-warning">
            <h6>Brand Warning Color</h6>
          </ThemeColor>
          <ThemeColor className="bg-info">
            <h6>Brand Info Color</h6>
          </ThemeColor>
          <ThemeColor className="bg-light">
            <h6>Brand Light Color</h6>
          </ThemeColor>
          <ThemeColor className="bg-dark">
            <h6>Brand Dark Color</h6>
          </ThemeColor>
        </CRow>
      </CCardBody>
    </CCard>
  )
}

export default Colors
