/*
 * Copyright (c) 2026. Gryphus Lab
 */
import React from 'react'
import { useLocation } from 'react-router-dom'

import { routes } from '../routes'

import { CBreadcrumb, CBreadcrumbItem } from '@coreui/react'

type Breadcrumb = { pathname: string; name: string; active: boolean }

const AppBreadcrumb = () => {
  const currentLocation = useLocation().pathname

  const getRouteName = (pathname: string, routes: any[]) => {
    const currentRoute = routes.find((route: { path: any }) => route.path === pathname)
    return currentRoute ? currentRoute.name : false
  }

  const getBreadcrumbs = (location: string) => {
    const filteredSegments = location.split('/').filter(Boolean)
    const breadcrumbs = filteredSegments.reduce(
      (acc, curr, index) => {
        const currentPathname = `${acc.currentPath}/${curr}`

        const routeName = getRouteName(currentPathname, routes)
        if (routeName) {
          acc.list.push({
            pathname: currentPathname,
            name: routeName,
            active: index === filteredSegments.length - 1,
          })
        }

        return {
          list: acc.list,
          currentPath: currentPathname,
        }
      },
      { list: [] as Breadcrumb[], currentPath: '' },
    )

    return breadcrumbs.list
  }

  const breadcrumbs = getBreadcrumbs(currentLocation)

  return (
    <CBreadcrumb className="my-0">
      {/* Explicitly unique key for the Home item */}
      <CBreadcrumbItem
        key="breadcrumb-home"
        {...(currentLocation === '/' ? { active: true } : { href: '/' })}
      >
        Home
      </CBreadcrumbItem>

      {breadcrumbs.map((breadcrumb) => {
        return (
          <CBreadcrumbItem
            {...(breadcrumb.active ? { active: true } : { href: breadcrumb.pathname })}
            key={breadcrumb.pathname}
          >
            {breadcrumb.name}
          </CBreadcrumbItem>
        )
      })}
    </CBreadcrumb>
  )
}

export default React.memo(AppBreadcrumb)