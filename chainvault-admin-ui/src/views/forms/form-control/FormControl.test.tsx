import { beforeEach, describe, expect, it } from 'vitest'
import { waitFor } from '@testing-library/react'
import FormControl from './FormControl'
import { renderWithProviders, resetStore } from '../../../test/test-utils'

describe('FormControl', () => {
  beforeEach(() => {
    resetStore()
  })

  it('renders without throwing', async () => {
    const { container } = renderWithProviders(<FormControl />)
    await waitFor(() => {
      expect(container.firstChild).toBeTruthy()
    })
  })
})
