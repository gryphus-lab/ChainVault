import { beforeEach, describe, expect, it } from 'vitest'
import { waitFor } from '@testing-library/react'
import Badges from './Badges'
import { renderWithProviders, resetStore } from '../../../test/test-utils'

describe('Badges', () => {
  beforeEach(() => {
    resetStore()
  })

  it('renders without throwing', async () => {
    const { container } = renderWithProviders(<Badges />)
    await waitFor(() => {
      expect(container.firstChild).toBeTruthy()
    })
  })
})
