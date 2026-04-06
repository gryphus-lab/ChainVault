import { beforeEach, describe, expect, it } from 'vitest'
import { waitFor } from '@testing-library/react'
import Modals from './Modals'
import { renderWithProviders, resetStore } from '../../../test/test-utils'

describe('Modals', () => {
  beforeEach(() => {
    resetStore()
  })

  it('renders without throwing', async () => {
    const { container } = renderWithProviders(<Modals />)
    await waitFor(() => {
      expect(container.firstChild).toBeTruthy()
    })
  })
})
