import { beforeEach, describe, expect, it } from 'vitest'
import { waitFor } from '@testing-library/react'
import Page500 from './Page500'
import { renderWithProviders, resetStore } from '../../../test/test-utils'

describe('Page500', () => {
  beforeEach(() => {
    resetStore()
  })

  it('renders without throwing', async () => {
    const { container } = renderWithProviders(<Page500 />)
    await waitFor(() => {
      expect(container.firstChild).toBeTruthy()
    })
  })
})
