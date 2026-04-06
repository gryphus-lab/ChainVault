import { beforeEach, describe, expect, it } from 'vitest'
import { waitFor } from '@testing-library/react'
import MainChart from './MainChart'
import { renderWithProviders, resetStore } from '../../test/test-utils'

describe('MainChart', () => {
  beforeEach(() => {
    resetStore()
  })

  it('renders without throwing', async () => {
    const { container } = renderWithProviders(<MainChart />)
    await waitFor(() => {
      expect(container.firstChild).toBeTruthy()
    })
  })
})
