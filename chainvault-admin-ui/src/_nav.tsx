/*
 * Copyright (c) 2026. Gryphus Lab
 */
import { ReactNode } from 'react'
import CIcon from '@coreui/icons-react'
import { cilSpeedometer } from '@coreui/icons'
import { CNavGroup, CNavItem, CNavTitle } from '@coreui/react'

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

const navIcon = (name: string | string[]) => <CIcon icon={name} customClassName="nav-icon" />

const BADGES = {
  PRO: { color: 'danger', text: 'PRO' },
  NEW: { color: 'info', text: 'NEW' },
}

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

const _nav: NavNode[] = [createItem('Dashboard', '/dashboard', cilSpeedometer)]

export default _nav
