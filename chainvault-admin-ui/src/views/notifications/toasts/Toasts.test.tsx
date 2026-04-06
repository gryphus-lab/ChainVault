import { beforeEach, describe, expect, it } from 'vitest'
import { waitFor } from '@testing-library/react'
import Toasts from './Toasts'
import { renderWithProviders, resetStore } from '../../../test/test-utils'

describe('Toasts', () => {
  beforeEach(() => {
    resetStore()
  })

  it('renders without throwing', async () => {
    const { container } = renderWithProviders(<Toasts />)
    await waitFor(() => {
      expect(container.firstChild).toBeTruthy()
    })
  })
})
