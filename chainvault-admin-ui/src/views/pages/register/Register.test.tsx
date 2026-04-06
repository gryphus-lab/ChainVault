import { beforeEach, describe, expect, it } from 'vitest'
import { waitFor } from '@testing-library/react'
import Register from './Register'
import { renderWithProviders, resetStore } from '../../../test/test-utils'

describe('Register', () => {
  beforeEach(() => {
    resetStore()
  })

  it('renders without throwing', async () => {
    const { container } = renderWithProviders(<Register />)
    await waitFor(() => {
      expect(container.firstChild).toBeTruthy()
    })
  })
})
