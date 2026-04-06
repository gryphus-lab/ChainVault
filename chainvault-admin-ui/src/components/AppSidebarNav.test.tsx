import { beforeEach, describe, expect, it } from 'vitest'
import { waitFor } from '@testing-library/react'
import { AppSidebarNav, type NavConfigItem } from './AppSidebarNav'
import navigation from '../_nav'
import { renderWithProviders, resetStore } from '../test/test-utils'

describe('AppSidebarNav', () => {
  beforeEach(() => {
    resetStore()
  })

  it('renders without throwing', async () => {
    const { container } = renderWithProviders(
      <AppSidebarNav items={navigation as NavConfigItem[]} />,
    )
    await waitFor(() => {
      expect(container.firstChild).toBeTruthy()
    })
  })
})
