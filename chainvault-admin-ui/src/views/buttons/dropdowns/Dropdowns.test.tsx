import { beforeEach, describe, expect, it } from 'vitest'
import { waitFor } from '@testing-library/react'
import Dropdowns from './Dropdowns'
import { renderWithProviders, resetStore } from '../../../test/test-utils'

describe('Dropdowns', () => {
  beforeEach(() => {
    resetStore()
  })

  it('renders without throwing', async () => {
    const { container } = renderWithProviders(<Dropdowns />)
    await waitFor(() => {
      expect(container.firstChild).toBeTruthy()
    })
  })
})
