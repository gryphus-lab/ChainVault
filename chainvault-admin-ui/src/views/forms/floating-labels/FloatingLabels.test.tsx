import { beforeEach, describe, expect, it } from 'vitest'
import { waitFor } from '@testing-library/react'
import FloatingLabels from './FloatingLabels'
import { renderWithProviders, resetStore } from '../../../test/test-utils'

describe('FloatingLabels', () => {
  beforeEach(() => {
    resetStore()
  })

  it('renders without throwing', async () => {
    const { container } = renderWithProviders(<FloatingLabels />)
    await waitFor(() => {
      expect(container.firstChild).toBeTruthy()
    })
  })
})
