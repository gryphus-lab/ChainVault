import { beforeEach, describe, expect, it } from 'vitest'
import { waitFor } from '@testing-library/react'
import DefaultLayout from './DefaultLayout'
import { renderWithProviders, resetStore } from '../test/test-utils'

describe('DefaultLayout', () => {
  beforeEach(() => {
    resetStore()
  })

  it('renders without throwing', async () => {
    const { container } = renderWithProviders(<DefaultLayout />)
    await waitFor(() => {
      expect(container.firstChild).toBeTruthy()
    })
  })
})
