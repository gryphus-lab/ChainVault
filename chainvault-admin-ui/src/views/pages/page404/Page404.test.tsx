import { beforeEach, describe, expect, it } from 'vitest'
import { waitFor } from '@testing-library/react'
import Page404 from './Page404'
import { renderWithProviders, resetStore } from '../../../test/test-utils'

describe('Page404', () => {
  beforeEach(() => {
    resetStore()
  })

  it('renders without throwing', async () => {
    const { container } = renderWithProviders(<Page404 />)
    await waitFor(() => {
      expect(container.firstChild).toBeTruthy()
    })
  })
})
