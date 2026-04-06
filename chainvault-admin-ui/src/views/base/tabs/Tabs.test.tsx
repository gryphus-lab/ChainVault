import { beforeEach, describe, expect, it } from 'vitest'
import { waitFor } from '@testing-library/react'
import Tabs from './Tabs'
import { renderWithProviders, resetStore } from '../../../test/test-utils'

describe('Tabs', () => {
  beforeEach(() => {
    resetStore()
  })

  it('renders without throwing', async () => {
    const { container } = renderWithProviders(<Tabs />)
    await waitFor(() => {
      expect(container.firstChild).toBeTruthy()
    })
  })
})
