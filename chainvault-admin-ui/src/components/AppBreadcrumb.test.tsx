import { beforeEach, describe, expect, it } from 'vitest'
import { waitFor } from '@testing-library/react'
import AppBreadcrumb from './AppBreadcrumb'
import { renderWithProviders, resetStore } from '../test/test-utils'

describe('AppBreadcrumb', () => {
  beforeEach(() => {
    resetStore()
  })

  it('renders without throwing', async () => {
    const { container } = renderWithProviders(<AppBreadcrumb />)
    await waitFor(() => {
      expect(container.firstChild).toBeTruthy()
    })
  })
})
