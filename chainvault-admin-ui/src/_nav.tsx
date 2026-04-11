/*
 * Copyright (c) 2026. Gryphus Lab
 */
import React, { ReactNode } from 'react'
import CIcon from '@coreui/icons-react'
import {
  cilBell,
  cilCalculator,
  cilChartPie,
  cilCursor,
  cilDescription,
  cilDrop,
  cilExternalLink,
  cilNotes,
  cilPencil,
  cilPuzzle,
  cilSpeedometer,
  cilStar,
} from '@coreui/icons'
import { CNavGroup, CNavItem, CNavTitle } from '@coreui/react'

/* -------------------------------------------------------------------------- */
/*                                   TYPES                                    */
/* -------------------------------------------------------------------------- */

type Badge = { color: string; text: string }

interface BaseNavItem {
  name: ReactNode
  icon?: ReactNode
  badge?: Badge
}

type NavItem = BaseNavItem & {
  component: typeof CNavItem
} & ({ to: string; href?: never } | { href: string; to?: never })

type NavGroup = BaseNavItem & {
  component: typeof CNavGroup
  items: NavNode[]
  to?: string
}

type NavTitle = {
  component: typeof CNavTitle
  name: string
}

export type NavNode = NavItem | NavGroup | NavTitle

/* -------------------------------------------------------------------------- */
/*                                  HELPERS                                   */
/* -------------------------------------------------------------------------- */

const navIcon = (name: string | string[]) => <CIcon icon={name} customClassName="nav-icon" />

/**
 * Appends an external link icon to labels
 */
const extLabel = (label: string): ReactNode => (
  <>
    {label}
    <CIcon icon={cilExternalLink} size="sm" className="ms-2" />
  </>
)

const BADGES = {
  PRO: { color: 'danger', text: 'PRO' },
  NEW: { color: 'info', text: 'NEW' },
}

/* -------------------------------------------------------------------------- */
/* FACTORY FUNCTIONS                            */
/* -------------------------------------------------------------------------- */

/**
 * Creates a standard Nav Item
 */
const createItem = (
  name: string,
  to: string,
  iconName?: string | string[],
  badgeKey?: keyof typeof BADGES,
): NavItem => ({
  component: CNavItem,
  name,
  to,
  // iconName is the raw icon object (e.g. cilDrop)
  // navIcon(iconName) returns a JSX Element (<CIcon ... />)
  icon: iconName ? navIcon(iconName) : undefined,
  ...(badgeKey && { badge: BADGES[badgeKey] }),
})

/**
 * Creates an External Link Nav Item (PRO)
 */
const createExternal = (label: string, href: string): NavItem => ({
  component: CNavItem,
  name: extLabel(label),
  href,
  badge: BADGES.PRO,
})

/**
 * Creates a Title section
 */
const createTitle = (name: string): NavTitle => ({
  component: CNavTitle,
  name,
})

/* -------------------------------------------------------------------------- */
/*                                   NAV DATA                                 */
/* -------------------------------------------------------------------------- */

const _nav: NavNode[] = [
  createItem('Dashboard', '/dashboard', cilSpeedometer, 'NEW'),

  createTitle('Theme'),
  createItem('Colors', '/theme/colors', cilDrop),
  createItem('Typography', '/theme/typography', cilPencil),

  createTitle('Components'),
  {
    component: CNavGroup,
    name: 'Base',
    to: '/base',
    icon: navIcon(cilPuzzle),
    items: [
      createItem('Accordion', '/base/accordion'),
      createItem('Breadcrumb', '/base/breadcrumbs'),
      createExternal('Calendar', 'https://coreui.io/react/docs/components/calendar/'),
      createItem('Cards', '/base/cards'),
      createItem('Carousel', '/base/carousels'),
      createItem('Chip', '/base/chip'),
      createItem('Collapse', '/base/collapses'),
      createItem('List group', '/base/list-groups'),
      createItem('Navs & Tabs', '/base/navs'),
      createItem('Pagination', '/base/paginations'),
      createItem('Placeholders', '/base/placeholders'),
      createItem('Popovers', '/base/popovers'),
      createItem('Progress', '/base/progress'),
      createExternal(
        'Smart Pagination',
        'https://coreui.io/react/docs/components/smart-pagination/',
      ),
      createExternal('Smart Table', 'https://coreui.io/react/docs/components/smart-table/'),
      createItem('Spinners', '/base/spinners'),
      createItem('Tables', '/base/tables'),
      createItem('Tabs', '/base/tabs'),
      createItem('Tooltips', '/base/tooltips'),
      createExternal(
        'Virtual Scroller',
        'https://coreui.io/react/docs/components/virtual-scroller/',
      ),
    ],
  },

  {
    component: CNavGroup,
    name: 'Buttons',
    to: '/buttons',
    icon: navIcon(cilCursor),
    items: [
      createItem('Buttons', '/buttons/buttons'),
      createItem('Buttons groups', '/buttons/button-groups'),
      createItem('Dropdowns', '/buttons/dropdowns'),
      createExternal('Loading Button', 'https://coreui.io/react/docs/components/loading-button/'),
    ],
  },

  {
    component: CNavGroup,
    name: 'Forms',
    icon: navIcon(cilNotes),
    items: [
      createExternal('Autocomplete', 'https://coreui.io/react/docs/forms/autocomplete/'),
      createItem('Checks & Radios', '/forms/checks-radios'),
      createItem('Chip Input', '/forms/chip-input'),
      createExternal('Date Picker', 'https://coreui.io/react/docs/forms/date-picker/'),
      createExternal('Date Range Picker', 'https://coreui.io/react/docs/forms/date-range-picker/'),
      createItem('Floating Labels', '/forms/floating-labels'),
      createItem('Form Control', '/forms/form-control'),
      createItem('Input Group', '/forms/input-group'),
      createExternal('Multi Select', 'https://coreui.io/react/docs/forms/multi-select/'),
      createExternal('OTP Input', 'https://coreui.io/react/docs/forms/one-time-password-input/'),
      createExternal('Password Input', 'https://coreui.io/react/docs/forms/password-input/'),
      createItem('Range', '/forms/range'),
      createExternal('Range Slider', 'https://coreui.io/react/docs/forms/range-slider/'),
      createExternal('Rating', 'https://coreui.io/react/docs/forms/rating/'),
      createItem('Select', '/forms/select'),
      createExternal('Stepper', 'https://coreui.io/react/docs/forms/stepper/'),
      createExternal('Time Picker', 'https://coreui.io/react/docs/forms/time-picker/'),
      createItem('Layout', '/forms/layout'),
      createItem('Validation', '/forms/validation'),
    ],
  },

  createItem('Charts', '/charts', cilChartPie),

  {
    component: CNavGroup,
    name: 'Icons',
    icon: navIcon(cilStar),
    items: [
      createItem('CoreUI Free', '/icons/coreui-icons'),
      createItem('CoreUI Flags', '/icons/flags'),
      createItem('CoreUI Brands', '/icons/brands'),
    ],
  },

  {
    component: CNavGroup,
    name: 'Notifications',
    icon: navIcon(cilBell),
    items: [
      createItem('Alerts', '/notifications/alerts'),
      createItem('Badges', '/notifications/badges'),
      createItem('Modal', '/notifications/modals'),
      createItem('Toasts', '/notifications/toasts'),
    ],
  },

  createItem('Widgets', '/widgets', cilCalculator, 'NEW'),

  createTitle('Extras'),

  {
    component: CNavGroup,
    name: 'Pages',
    icon: navIcon(cilStar),
    items: [
      createItem('Login', '/login'),
      createItem('Register', '/register'),
      createItem('Error 404', '/404'),
      createItem('Error 500', '/500'),
    ],
  },

  {
    component: CNavItem,
    name: extLabel('Docs'),
    href: 'https://coreui.io/react/docs/templates/installation/',
    icon: navIcon(cilDescription),
  },
]

export default _nav