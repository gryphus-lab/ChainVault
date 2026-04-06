import { beforeEach, describe, expect, it } from 'vitest'
import { waitFor } from '@testing-library/react'
import Navs from './Navs'
import { renderWithProviders, resetStore } from '../../../test/test-utils'

describe('Navs', () => {
  beforeEach(() => {
    resetStore()
  })

  it('renders without throwing', async () => {
    const { container } = renderWithProviders(<Navs />)
    await waitFor(() => {
      expect(container.firstChild).toBeTruthy()
    })
  })
})
