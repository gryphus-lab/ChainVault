import { beforeEach, describe, expect, it } from 'vitest'
import { waitFor } from '@testing-library/react'
import Chip from './Chip'
import { renderWithProviders, resetStore } from '../../../test/test-utils'

describe('Chip', () => {
  beforeEach(() => {
    resetStore()
  })

  it('renders without throwing', async () => {
    const { container } = renderWithProviders(<Chip />)
    await waitFor(() => {
      expect(container.firstChild).toBeTruthy()
    })
  })
})
