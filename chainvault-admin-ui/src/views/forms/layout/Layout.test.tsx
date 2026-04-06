import { beforeEach, describe, expect, it } from 'vitest'
import { waitFor } from '@testing-library/react'
import Layout from './Layout'
import { renderWithProviders, resetStore } from '../../../test/test-utils'

describe('Layout', () => {
  beforeEach(() => {
    resetStore()
  })

  it('renders without throwing', async () => {
    const { container } = renderWithProviders(<Layout />)
    await waitFor(() => {
      expect(container.firstChild).toBeTruthy()
    })
  })
})
