import { beforeEach, describe, expect, it } from 'vitest'
import { waitFor } from '@testing-library/react'
import AppSidebar from './AppSidebar'
import { renderWithProviders, resetStore } from '../test/test-utils'

describe('AppSidebar', () => {
  beforeEach(() => {
    resetStore()
  })

  it('renders without throwing', async () => {
    const { container } = renderWithProviders(<AppSidebar />)
    await waitFor(() => {
      expect(container.firstChild).toBeTruthy()
    })
  })
})
