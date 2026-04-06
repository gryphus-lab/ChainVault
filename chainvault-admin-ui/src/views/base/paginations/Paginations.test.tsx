import { beforeEach, describe, expect, it } from 'vitest'
import { waitFor } from '@testing-library/react'
import Paginations from './Paginations'
import { renderWithProviders, resetStore } from '../../../test/test-utils'

describe('Paginations', () => {
  beforeEach(() => {
    resetStore()
  })

  it('renders without throwing', async () => {
    const { container } = renderWithProviders(<Paginations />)
    await waitFor(() => {
      expect(container.firstChild).toBeTruthy()
    })
  })
})
