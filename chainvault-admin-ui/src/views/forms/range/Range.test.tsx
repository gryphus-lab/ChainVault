import { beforeEach, describe, expect, it } from 'vitest'
import { waitFor } from '@testing-library/react'
import Range from './Range'
import { renderWithProviders, resetStore } from '../../../test/test-utils'

describe('Range', () => {
  beforeEach(() => {
    resetStore()
  })

  it('renders without throwing', async () => {
    const { container } = renderWithProviders(<Range />)
    await waitFor(() => {
      expect(container.firstChild).toBeTruthy()
    })
  })
})
