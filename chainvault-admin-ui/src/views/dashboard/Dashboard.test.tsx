import { beforeEach, describe, expect, it } from 'vitest'
import { waitFor } from '@testing-library/react'
import Dashboard from './Dashboard'
import { renderWithProviders, resetStore } from '../../test/test-utils'

describe('Dashboard', () => {
  beforeEach(() => {
    resetStore()
  })

  it('renders without throwing', async () => {
    const { container } = renderWithProviders(<Dashboard />)
    await waitFor(() => {
      expect(container.firstChild).toBeTruthy()
    })
  })
})
