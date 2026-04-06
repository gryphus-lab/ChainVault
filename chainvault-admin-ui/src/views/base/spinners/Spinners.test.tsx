import { beforeEach, describe, expect, it } from 'vitest'
import { waitFor } from '@testing-library/react'
import Spinners from './Spinners'
import { renderWithProviders, resetStore } from '../../../test/test-utils'

describe('Spinners', () => {
  beforeEach(() => {
    resetStore()
  })

  it('renders without throwing', async () => {
    const { container } = renderWithProviders(<Spinners />)
    await waitFor(() => {
      expect(container.firstChild).toBeTruthy()
    })
  })
})
