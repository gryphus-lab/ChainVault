/*
 * Copyright (c) 2026. Gryphus Lab
 */
import React, { ReactNode } from 'react'
import {
  CButton,
  CCard,
  CCardBody,
  CCardFooter,
  CCardGroup,
  CCardHeader,
  CCardImage,
  CCardLink,
  CCardText,
  CCardTitle,
  CCol,
  CListGroup,
  CListGroupItem,
  CNav,
  CNavItem,
  CNavLink,
  CRow,
} from '@coreui/react'
import { DocsComponents, DocsExample } from '../../../components'
import ReactImg from '../../../assets/images/react.jpg'

/* ------------------ Types ------------------ */

interface BasicCardProps {
  title?: ReactNode
  text?: ReactNode
  footer?: ReactNode
  header?: ReactNode
  image?: boolean
  links?: string[]
  width?: string
}

interface ListCardProps {
  header?: ReactNode
  footer?: ReactNode
}

interface NavCardProps {
  variant: 'tabs' | 'pills'
}

interface ColorCardsProps {
  bordered?: boolean
  topBorder?: boolean
}

/* ------------------ Reusable Components ------------------ */

const BasicCard = ({
  title = 'Card title',
  text = 'Some quick example text',
  footer,
  header,
  image,
  links,
  width,
}: BasicCardProps) => (
  <CCard style={width ? { width } : undefined} className="mb-3">
    {image && <CCardImage orientation="top" src={ReactImg} />}
    {header && <CCardHeader>{header}</CCardHeader>}
    <CCardBody>
      {title && <CCardTitle>{title}</CCardTitle>}
      {text && <CCardText>{text}</CCardText>}
      {links?.map((l, i) => (
        <CCardLink key={`${l}-${i}`} href="#">
          {l}
        </CCardLink>
      ))}
    </CCardBody>
    {footer && <CCardFooter>{footer}</CCardFooter>}
  </CCard>
)

const ListCard = ({ header, footer }: ListCardProps) => (
  <CCard style={{ width: '18rem' }}>
    {header && <CCardHeader>{header}</CCardHeader>}
    <CListGroup flush>
      {['Cras justo odio', 'Dapibus ac facilisis in', 'Vestibulum at eros'].map((item, i) => (
        <CListGroupItem key={`${item}-${i}`}>{item}</CListGroupItem>
      ))}
    </CListGroup>
    {footer && <CCardFooter>{footer}</CCardFooter>}
  </CCard>
)

const NavCard = ({ variant }: NavCardProps) => (
  <CCard className="text-center mb-3">
    <CCardHeader>
      <CNav variant={variant}>
        {['Active', 'Link', 'Disabled'].map((item, i) => (
          <CNavItem key={`${item}-${i}`}>
            <CNavLink href="#" active={i === 0} disabled={i === 2}>
              {item}
            </CNavLink>
          </CNavItem>
        ))}
      </CNav>
    </CCardHeader>
    <CCardBody>
      <CCardTitle>Special title treatment</CCardTitle>
      <CCardText>With supporting text below.</CCardText>
      <CButton color="primary">Go somewhere</CButton>
    </CCardBody>
  </CCard>
)

// Explicitly typing the return to match CoreUI expectations
function getColorVariant(color: string): 'dark' | 'white' {
  return color === 'light' ? 'dark' : 'white'
}

const ColorCards = ({ bordered = false, topBorder = false }: ColorCardsProps) => {
  const variants = [
    'primary',
    'secondary',
    'success',
    'danger',
    'warning',
    'info',
    'light',
    'dark',
  ] as const

  return (
    <CRow>
      {variants.map((color, i) => {
        const borderedValue = `border-${color}`
        const topBorderedValue = `border-top-${color} border-top-3`
        return (
          <CCol lg={4} key={`${color}-${i}`}>
            <CCard
              color={!bordered && !topBorder ? color : undefined}
              textColor={bordered || topBorder ? ((bordered || topBorder) && color === 'light' ? getColorVariant(color) : (color as any)) : getColorVariant(color)}
              className={`mb-3 ${bordered ? borderedValue : ''} ${topBorder ? topBorderedValue : ''}`}
            >
              <CCardHeader>Header</CCardHeader>
              <CCardBody>
                <CCardTitle>{color.charAt(0).toUpperCase() + color.slice(1)} card</CCardTitle>
                <CCardText>Some quick example text.</CCardText>
              </CCardBody>
            </CCard>
          </CCol>
        )
      })}
    </CRow>
  )
}

/* ------------------ Main Component ------------------ */

const Cards = () => {
  const examplePath = 'components/card'

  return (
    <CRow>
      <CCol xs={12}>
        <DocsComponents href={`${examplePath}/`} />

        {/* Fixed TS2741 by adding required href to DocsExample */}
        <DocsExample href={examplePath}>
          <BasicCard image width="18rem" />
        </DocsExample>

        <DocsExample href={`${examplePath}/#body`}>
          <BasicCard title={null} text="This is some text within a card body." width="18rem" />
        </DocsExample>

        <DocsExample href={`${examplePath}/#titles-text-and-links`}>
          <BasicCard links={['Card link', 'Another link']} width="18rem" />
        </DocsExample>

        <DocsExample href={`${examplePath}/#list-groups`}>
          <ListCard />
        </DocsExample>

        <DocsExample href={`${examplePath}/#navigation`}>
          <NavCard variant="tabs" />
          <NavCard variant="pills" />
        </DocsExample>

        <DocsExample href={`${examplePath}/#background-variants`}>
          <ColorCards />
        </DocsExample>

        <DocsExample href={`${examplePath}/#border-variants`}>
          <ColorCards bordered />
        </DocsExample>

        <DocsExample href={`${examplePath}/#top-border-variants`}>
          <ColorCards topBorder />
        </DocsExample>

        <DocsExample href={`${examplePath}/#card-groups`}>
          <CCardGroup>
            {[1, 2, 3].map((i) => (
              <BasicCard key={i} image footer="Last updated 3 mins ago" width="18rem" />
            ))}
          </CCardGroup>
        </DocsExample>

        <DocsExample href={`${examplePath}/#grid-cards`}>
          <CRow xs={{ cols: 1 }} md={{ cols: 2 }}>
            {[1, 2, 3, 4].map((i) => (
              <CCol key={i}>
                <BasicCard image footer="Last updated" width="18rem" />
              </CCol>
            ))}
          </CRow>
        </DocsExample>
      </CCol>
    </CRow>
  )
}

export default Cards