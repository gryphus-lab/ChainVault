import { beforeEach, describe, expect, it } from 'vitest'
import { waitFor } from '@testing-library/react'
import Brands from './Brands'
import { renderWithProviders, resetStore } from '../../../test/test-utils'

describe('Brands', () => {
  beforeEach(() => {
    resetStore()
  })

  it('renders without throwing', async () => {
    const { container } = renderWithProviders(<Brands />)
    await waitFor(
      () => {
        expect(container.firstChild).toBeTruthy()
      },
      { timeout: 60000 },
    )
  })
})
