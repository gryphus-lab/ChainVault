import { beforeEach, describe, expect, it } from 'vitest'
import { waitFor } from '@testing-library/react'
import Charts from './Charts'
import { renderWithProviders, resetStore } from '../../test/test-utils'

describe('Charts', () => {
  beforeEach(() => {
    resetStore()
  })

  it('renders without throwing', async () => {
    const { container } = renderWithProviders(<Charts />)
    await waitFor(() => {
      expect(container.firstChild).toBeTruthy()
    })
  })
})
