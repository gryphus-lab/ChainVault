import { beforeEach, describe, expect, it } from 'vitest'
import { waitFor } from '@testing-library/react'
import Popovers from './Popovers'
import { renderWithProviders, resetStore } from '../../../test/test-utils'

describe('Popovers', () => {
  beforeEach(() => {
    resetStore()
  })

  it('renders without throwing', async () => {
    const { container } = renderWithProviders(<Popovers />)
    await waitFor(() => {
      expect(container.firstChild).toBeTruthy()
    })
  })
})
