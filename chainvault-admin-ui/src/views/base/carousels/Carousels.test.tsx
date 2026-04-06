import { beforeEach, describe, expect, it } from 'vitest'
import { waitFor } from '@testing-library/react'
import Carousels from './Carousels'
import { renderWithProviders, resetStore } from '../../../test/test-utils'

describe('Carousels', () => {
  beforeEach(() => {
    resetStore()
  })

  it('renders without throwing', async () => {
    const { container } = renderWithProviders(<Carousels />)
    await waitFor(() => {
      expect(container.firstChild).toBeTruthy()
    })
  })
})
