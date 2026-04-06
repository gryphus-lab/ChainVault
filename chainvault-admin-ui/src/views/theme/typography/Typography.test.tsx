import { beforeEach, describe, expect, it } from 'vitest'
import { waitFor } from '@testing-library/react'
import Typography from './Typography'
import { renderWithProviders, resetStore } from '../../../test/test-utils'

describe('Typography', () => {
  beforeEach(() => {
    resetStore()
  })

  it('renders without throwing', async () => {
    const { container } = renderWithProviders(<Typography />)
    await waitFor(() => {
      expect(container.firstChild).toBeTruthy()
    })
  })
})
