import { beforeEach, describe, expect, it } from 'vitest'
import { waitFor } from '@testing-library/react'
import Cards from './Cards'
import { renderWithProviders, resetStore } from '../../../test/test-utils'

describe('Cards', () => {
  beforeEach(() => {
    resetStore()
  })

  it('renders without throwing', async () => {
    const { container } = renderWithProviders(<Cards />)
    await waitFor(() => {
      expect(container.firstChild).toBeTruthy()
    })
  })
})
