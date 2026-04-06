import { beforeEach, describe, expect, it } from 'vitest'
import { waitFor } from '@testing-library/react'
import AppFooter from './AppFooter'
import { renderWithProviders, resetStore } from '../test/test-utils'

describe('AppFooter', () => {
  beforeEach(() => {
    resetStore()
  })

  it('renders without throwing', async () => {
    const { container } = renderWithProviders(<AppFooter />)
    await waitFor(() => {
      expect(container.firstChild).toBeTruthy()
    })
  })
})
