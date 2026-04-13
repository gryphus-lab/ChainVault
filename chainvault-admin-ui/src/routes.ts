/*
 * Copyright (c) 2026. Gryphus Lab
 */
import React, { type ComponentType, type LazyExoticComponent } from 'react'

export type AppRoute = {
  path?: string
  exact?: boolean
  name?: string
  element?: LazyExoticComponent<ComponentType>
}

// Dashboard
const Dashboard = React.lazy(() => import('./views/dashboard/Dashboard'))

/**
 * Array of route configuration objects
 *
 * @type {Array<Object>}
 * @property {string} path - URL path pattern
 * @property {string} name - Display name for breadcrumbs and navigation
 * @property {React.LazyExoticComponent} element - Lazy-loaded component
 * @property {boolean} [exact] - Whether to match path exactly
 *
 * @example
 * // Route renders when URL matches '/dashboard'
 * { path: '/dashboard', name: 'Dashboard', element: Dashboard }
 *
 * @example
 * // Route with exact match required
 * { path: '/base', name: 'Base', element: Cards, exact: true }
 */
export const routes: AppRoute[] = [
  { path: '/dashboard', name: 'Dashboard', element: Dashboard },
  { path: '*', name: 'Not Found', element: Dashboard }, // Fallback for unmatched routes
]

export default routes
