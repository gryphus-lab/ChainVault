/*
 * Copyright (c) 2026. Gryphus Lab
 */
import React from 'react'
import {
  CAccordion,
  CAccordionBody,
  CAccordionHeader,
  CAccordionItem,
  CCard,
  CCardBody,
  CCardHeader,
  CCol,
  CRow,
} from '@coreui/react'
import { DocsComponents, DocsExample } from '../../../components'

type AccordionItemData = {
  key: number
  title: string
  content: string
}

type AccordionItemsProps = {
  items: AccordionItemData[]
}

type AccordionSectionConfig = {
  title: string
  subtitle?: string
  description?: string
  href: string
  props?: React.ComponentProps<typeof CAccordion>
}
/* -----------------------------
 * Data (single source of truth)
 * ----------------------------- */
const ACCORDION_ITEMS: AccordionItemData[] = [
  {
    key: 1,
    title: 'Accordion Item #1',
    content: "This is the first item's accordion body.",
  },
  {
    key: 2,
    title: 'Accordion Item #2',
    content: "This is the second item's accordion body.",
  },
  {
    key: 3,
    title: 'Accordion Item #3',
    content: "This is the third item's accordion body.",
  },
]

const SECTIONS: AccordionSectionConfig[] = [
  {
    title: 'React Accordion',
    href: 'components/accordion',
    description: 'Click the accordions below to expand/collapse the accordion content.',
    props: { activeItemKey: 2 },
  },
  {
    title: 'React Accordion',
    subtitle: 'Flush',
    href: 'components/accordion#flush',
    description:
      'Add flush to remove the default background-color, borders, and rounded corners to render accordions edge-to-edge with their parent container.',
    props: { flush: true },
  },
  {
    title: 'React Accordion',
    subtitle: 'Always open',
    href: 'components/accordion#always-open',
    description:
      'Add alwaysOpen property to make accordion items stay open when another item is opened.',
    props: { alwaysOpen: true },
  },
]

/* -----------------------------
 * Reusable components
 * ----------------------------- */
const AccordionItems: React.FC<AccordionItemsProps> = ({ items }) => (
  <>
    {items.map(({ key, title, content }) => (
      <CAccordionItem itemKey={key} key={key}>
        <CAccordionHeader>{title}</CAccordionHeader>
        <CAccordionBody>
          <strong>{content}</strong> It is hidden by default, until the collapse plugin adds the
          appropriate classes that we use to style each element. These classes control the overall
          appearance, as well as the showing and hiding via CSS transitions. You can modify any of
          this with custom CSS or overriding our default variables. It&#39;s also worth noting that
          just about any HTML can go within the <code>.accordion-body</code>, though the transition
          does limit overflow.
        </CAccordionBody>
      </CAccordionItem>
    ))}
  </>
)

type AccordionSectionProps = AccordionSectionConfig & {
  items: AccordionItemData[]
}

const AccordionSection = ({
  title,
  subtitle,
  description,
  href,
  props,
  items,
}: AccordionSectionProps) => (
  <CCard className="mb-4">
    <CCardHeader>
      <strong>{title}</strong> {subtitle && <small>{subtitle}</small>}
    </CCardHeader>

    <CCardBody>
      {description && <p className="text-body-secondary small">{description}</p>}

      <DocsExample href={href}>
        <CAccordion {...props}>
          <AccordionItems items={items} />
        </CAccordion>
      </DocsExample>
    </CCardBody>
  </CCard>
)

/* -----------------------------
 * Main component
 * ----------------------------- */
const Accordion: React.FC = () => {
  return (
    <CRow>
      <CCol xs={12}>
        <DocsComponents href="components/accordion/" />

        {SECTIONS.map((section, index) => (
          <AccordionSection
            key={`${section.title}-${index}`}
            {...section}
            items={ACCORDION_ITEMS}
          />
        ))}
      </CCol>
    </CRow>
  )
}

export default Accordion
