import { beforeEach, describe, expect, it } from 'vitest'
import { waitFor } from '@testing-library/react'
import ChecksRadios from './ChecksRadios'
import { renderWithProviders, resetStore } from '../../../test/test-utils'

describe('ChecksRadios', () => {
  beforeEach(() => {
    resetStore()
  })

  it('renders without throwing', async () => {
    const { container } = renderWithProviders(<ChecksRadios />)
    await waitFor(() => {
      expect(container.firstChild).toBeTruthy()
    })
  })
})
