import { beforeEach, describe, expect, it } from 'vitest'
import { waitFor } from '@testing-library/react'
import AppHeader from './AppHeader'
import { renderWithProviders, resetStore } from '../test/test-utils'

describe('AppHeader', () => {
  beforeEach(() => {
    resetStore()
  })

  it('renders without throwing', async () => {
    const { container } = renderWithProviders(<AppHeader />)
    await waitFor(() => {
      expect(container.firstChild).toBeTruthy()
    })
  })
})
