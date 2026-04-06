import { beforeEach, describe, expect, it } from 'vitest'
import { waitFor } from '@testing-library/react'
import Tables from './Tables'
import { renderWithProviders, resetStore } from '../../../test/test-utils'

describe('Tables', () => {
  beforeEach(() => {
    resetStore()
  })

  it('renders without throwing', async () => {
    const { container } = renderWithProviders(<Tables />)
    await waitFor(() => {
      expect(container.firstChild).toBeTruthy()
    })
  })
})
