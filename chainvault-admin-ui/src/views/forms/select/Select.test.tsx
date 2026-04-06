import { beforeEach, describe, expect, it } from 'vitest'
import { waitFor } from '@testing-library/react'
import Select from './Select'
import { renderWithProviders, resetStore } from '../../../test/test-utils'

describe('Select', () => {
  beforeEach(() => {
    resetStore()
  })

  it('renders without throwing', async () => {
    const { container } = renderWithProviders(<Select />)
    await waitFor(() => {
      expect(container.firstChild).toBeTruthy()
    })
  })
})
